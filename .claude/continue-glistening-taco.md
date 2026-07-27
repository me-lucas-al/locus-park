# Relatórios Locus Park — período configurável + exportação PDF/CSV/XLSX

## Contexto

A tela de relatórios hoje é um protótipo, e está quebrada:

- **Contrato divergente entre back e front.** `ReportResponse` (Java) devolve `Map<PaymentMethod, BigDecimal> revenueByPaymentMethod`; `report.types.ts` declara `paymentMethodSummaries: PaymentMethodSummary[]`. O template faz `report.paymentMethodSummaries.length` — `.length` de `undefined`. A tabela de formas de pagamento **nunca renderiza** contra a API real; `report.spec.ts` passa só porque mocka o formato errado.
- **Sem filtro de data.** `ReportService.getCompanyReport` agrega *todos* os tickets `PAID` da empresa, desde sempre. O front manda `?companyId=` e o back ignora (deriva do JWT) — honrar esse parâmetro seria um IDOR.
- **Pouca informação.** 3 KPIs e uma tabela. Nada de carros, clientes, convênios, ocupação, picos de horário.
- **Exportação inexistente na prática.** Um CSV via `data:text/csv` + `encodeURI`, que estoura em payloads grandes e faz mojibake no Excel pt-BR. Sem PDF, sem XLSX, sem logo.
- **Zero índices no banco.** Nenhuma migration cria `CREATE INDEX`. Consulta por faixa de data varre a tabela inteira dentro do tenant.
- **Bruto e desconto são perdidos.** O checkout grava só o `total_amount` líquido; quanto uma parceria descontou é irrecuperável.
- **`/reports` aberto a qualquer autenticado**, inclusive `EMPLOYEE`, com dados financeiros completos.

**Resultado esperado:** tela com seletor de período (Hoje / Últimos 7 dias / Este mês / personalizado), o máximo de informação possível sobre carros, faturamento, permanência, formas de pagamento por cliente, convênios, ocupação e picos, e exportação em **PDF, CSV e XLSX gerados no backend, com o logo do Locus Park**.

### Decisões do usuário (já tomadas)

| Tema | Decisão |
|---|---|
| Geração dos arquivos | **Backend (Java)** — OpenPDF + Apache POI + Commons CSV, endpoint `GET /reports/export?format=` |
| Eixo da data | **Híbrido** — faturamento/pagamentos pela **saída**; carros/ocupação/picos pela **entrada**. Cada bloco rotula seu eixo |
| Bruto/desconto | **Persistir** — V4 adiciona `gross_amount` e `discount_amount`; linhas antigas caem em `bruto = total, desconto = 0` |
| Permissão | **Só ADMIN e SUPER_ADMIN** |
| Fuso | **`TZ=America/Sao_Paulo` na Lambda**, tudo naive, sem conversão no código |
| Detalhe no PDF | **PDF completo** — todos os tickets do período |

### Padrão de código (skill `code-quality-engineer`)

Zero comentários · máximo 100 linhas por arquivo · SRP · DIP + injeção por construtor · DRY · early returns · Conventional Commits.

Isso é o que produz a quantidade de arquivos abaixo. É intencional, e também é o que atende `plan-token-optimization`: cada arquivo é carregável isoladamente, sem arrastar o módulo inteiro para o contexto. Ao final, um `docs/relatorios.md` de ~40 linhas mapeia o módulo para que sessões futuras não precisem ler os ~90 arquivos.

---

## Parte 1 — Backend: dados

### 1.1 Migration V4

`src/main/resources/db/migration/V4__add_ticket_charge_breakdown_and_report_indexes.sql`

```sql
ALTER TABLE tickets ADD gross_amount DECIMAL(10, 2) NULL;
ALTER TABLE tickets ADD discount_amount DECIMAL(10, 2) NULL;

CREATE INDEX idx_tickets_company_entered_at ON tickets (company_id, entered_at);
CREATE INDEX idx_tickets_company_exited_at  ON tickets (company_id, exited_at);
CREATE INDEX idx_tickets_company_status_entered_at ON tickets (company_id, status, entered_at);
```

Colunas **NULL, sem backfill**: preencher `gross_amount = total_amount` mentiria para linhas que tiveram desconto. NULL é o sinal que o fallback lê.

> ⚠️ **Ordem de deploy é obrigatória.** `ddl-auto=validate` e Flyway desabilitado em prod (`application-prod.properties`). Rodar `mvn -Pmigrate flyway:migrate` **antes** de subir o JAR com os campos novos, senão a Lambda falha na validação de schema em todo cold start. V4 é aditiva/nullable, então o JAR *antigo* continua funcionando contra o schema *novo* — a janela é segura nessa direção.

### 1.2 `PaymentService` → calculadoras (`service/payment/`)

`Ticket` ganha `grossAmount` e `discountAmount` (`@Column(name="gross_amount", precision=10, scale=2)`).

| Arquivo | Tipo | Assinatura |
|---|---|---|
`StayCharge.java` | record | `StayCharge(BigDecimal gross, discount, net)` + `static free()` + `static of(gross, net)` |
`TolerancePolicy.java` | @Component | `boolean isWithinTolerance(long stayMinutes, TariffConfiguration)` |
`HourlyRateCalculator.java` | @Component | `BigDecimal amountFor(long billableHours, TariffConfiguration)` |
`GrossStayChargeCalculator.java` | @Component | `BigDecimal grossAmount(long stayMinutes, boolean crossedDate, TariffConfiguration, PricingConfiguration)` |
`PartnershipDiscountCalculator.java` | @Component | `BigDecimal netAmount(BigDecimal gross, Partnership, long stayMinutes, TariffConfiguration)` |
`PaymentService.java` | modificar | `StayCharge calculateStayCharge(Ticket, LocalDateTime exitTime, TariffConfiguration, PricingConfiguration)` |

**Semânticas que precisam sobreviver bit a bit** (hoje sem nenhum teste):

1. **Tolerância curto-circuita ANTES do desconto.** Hoje `totalMinutes <= tolerance` faz `return ZERO` sem chegar em `applyDiscount`. Se `gross = 0` fluísse para o calculador de desconto, uma parceria `FREE_HOURS` **recalcularia um valor positivo a partir da tarifa** e uma saída gratuita passaria a cobrar. O bouncer fica em `PaymentService`, acima dos calculadores.
2. **`FREE_HOURS` é recálculo, não subtração** — descarta `gross` e reconstrói de `firstHourValue + additionalFractionValue * (h-1)`. Logo `discount` é **derivado**: `StayCharge.of` calcula `discount = gross - net`, com clamp — um recálculo `FREE_HOURS` sobre base de diária pode exceder `gross`, gerando desconto negativo. Clamp de `net` em `gross` e de `discount` em `ZERO`, uma única vez, dentro de `of`.
3. `setScale(2, HALF_UP)` em `gross` e `net` **antes** de derivar `discount`, para que `gross == net + discount` valha exatamente (o XLSX vai somar essas colunas).
4. `HourlyRateCalculator` é a extração DRY da fórmula que hoje aparece duplicada no ramo horário e no ramo `FREE_HOURS`.
5. `crossedDate` é calculado em `PaymentService` e passado adiante, para que `GrossStayChargeCalculator` seja função pura de `(minutos, boolean, tarifa, pricing)`.

`TicketService.checkOut` passa a gravar os três valores. `calculateStayAmount` é **deletado** (um único chamador, zero referências em teste). Adicionar `grossAmount`/`discountAmount` a `TicketResponse` — MapStruct pega por nome, sem `@Mapping`.

### 1.3 Projeção e consultas

**Três consultas, não uma com `OR`.** Uma condição `(entered_at BETWEEN … OR exited_at BETWEEN …)` depende de *index merge*, que o otimizador do TiDB recusa com frequência e degrada para full scan. A forma sargável equivalente (`entered_at < :to AND (exited_at IS NULL OR exited_at >= :from)`) degenera em `entered_at < :to`, ou seja, varre todo o histórico — e piora todo mês.

| Consulta | Predicado | Alimenta |
|---|---|---|
`A` financeira | `company_id = ? AND status = PAID AND exited_at >= :from AND exited_at < :to` | faturamento, desconto, formas de pagamento, ticket médio, saídas gratuitas, saídas por dia/hora |
`B` operacional | `company_id = ? AND entered_at >= :from AND entered_at < :to` | entradas por dia, hora de pico, ativos, varredura de ocupação |
`C` linha de base | `count(*)` com `entered_at < :from AND (exited_at IS NULL OR exited_at >= :from)` | offset inicial do pico de ocupação — fecha o buraco das estadias que atravessam a janela inteira e não estão nem em `A` nem em `B` |

Custo: 2 round trips extras (~10–60 ms). Aceito.

**Projeção por construtor, não `join fetch`.** 50k tickets como entidades gerenciadas = 4 objetos/linha na L1 cache com dirty-check ≈ 60–90 MB de heap; um record plano de 19 campos é ~1 objeto/linha, ~4× menos. Em Lambda de 1 GB isso é a diferença entre funcionar e OOM. Também elimina N+1 por construção (não existe proxy lazy para tocar por acidente) e torna os agregadores funções puras sobre um value type imutável — testáveis com `new TicketRecord(...)`, sem grafo de entidades nem Mockito.

`service/report/TicketRecord.java` — record de 19 campos (`ticketId, status, enteredAt, exitedAt, totalAmount, grossAmount, discountAmount, paymentMethod, plate, model, color, vehicleType, clientId, clientName, clientCpf, partnershipId, partnershipName, partnershipDiscountType, partnershipValue`) + 4 acessores derivados:

```java
BigDecimal net()      // totalAmount != null ? totalAmount : ZERO
BigDecimal gross()    // grossAmount  != null ? grossAmount  : net()
BigDecimal discount() // discountAmount != null ? discountAmount : ZERO
OptionalLong stayMinutes() // empty quando exitedAt == null
```

**Essa é a única garantia DRY do fallback de linhas legadas**: `grossAmount`/`discountAmount` nunca são desreferenciados fora de `TicketRecord`, e nenhum agregador escreve um null check de `exitedAt`.

`repository/TicketRecordQuery.java` — `SELECT`, `EXIT_WINDOW`, `ENTRY_WINDOW` como `public static final String` (text blocks), concatenados nas anotações. `TicketRepository` ganha `findPaidRecordsByExitWindow`, `findRecordsByEntryWindow`, `countPresentAt`.

**Corrigir no mesmo PR:** `TicketService.listAllTicketsByCompany` faz `findAll().stream().filter(...)` — carrega os tickets de **todos os tenants** a cada chamada. Adicionar `findAllByCompanyId(UUID)`. Isso vai dominar o custo do relatório se ficar.

### 1.4 DTOs (`dto/response/report/`) — 14 records

`ReportResponse` (raiz, compõe os demais), `ReportPeriodResponse`, `ReportCompanyResponse`, `ReportSummaryResponse`, `RevenueSummaryResponse`, `StaySummaryResponse`, `OccupancySummaryResponse`, `PaymentMethodSummaryResponse`, `VehicleTypeSummaryResponse`, `DailySummaryResponse`, `HourlySummaryResponse`, `PartnershipSummaryResponse`, `ClientSummaryResponse`, `TicketRowResponse`.

```java
record ReportResponse(
    ReportPeriodResponse period, ReportCompanyResponse company, ReportSummaryResponse summary,
    List<PaymentMethodSummaryResponse> paymentMethodSummaries,
    List<VehicleTypeSummaryResponse> vehicleTypeSummaries,
    List<DailySummaryResponse> dailySummaries,
    List<HourlySummaryResponse> hourlySummaries,
    List<PartnershipSummaryResponse> partnershipSummaries,
    List<ClientSummaryResponse> clientSummaries,
    List<TicketRowResponse> tickets,
    long ticketCount, boolean ticketsTruncated,
    BigDecimal totalRevenue, long totalServices, double averageStayMinutes) {}
```

Conteúdo por bloco:
- `RevenueSummaryResponse(grossRevenue, discountGranted, netRevenue, averageTicketValue, highestTicketValue, lowestTicketValue, paidTicketCount, freeExitCount)`
- `StaySummaryResponse(averageMinutes, minimumMinutes, maximumMinutes, totalMinutes, openStayCount)`
- `OccupancySummaryResponse(totalSpots, entryCount, exitCount, activeCount, peakConcurrentVehicles, peakAt, peakOccupancyRate, averageOccupancyRate, turnoverPerSpot)`
- `ClientSummaryResponse(clientId, name, cpf, ticketCount, totalSpent, averageStayMinutes, List<PaymentMethod> paymentMethodsUsed)` ← "as formas de pagamento de cada cliente"
- `TicketRowResponse` — 16 campos (`ticketId, status, plate, model, color, vehicleType, clientName, clientCpf, enteredAt, exitedAt, stayMinutes, partnershipName, paymentMethod, grossAmount, discountAmount, totalAmount`)

**`totalRevenue` / `totalServices` / `averageStayMinutes` / `paymentMethodSummaries` ficam na raiz** de propósito: casam exatamente com o `report.types.ts` atual, então o rename map→array corrige o contrato quebrado **e** o front pode subir a UI rica independentemente, sem lockstep.

Cardinalidade: formas de pagamento e tipos de veículo emitem **todos os valores do enum, inclusive zerados** (preserva o comportamento do `EnumMap` atual e mantém a legenda estável entre períodos); `dailySummaries` emite **todo dia do período, inclusive zerados** (eixo sem lacunas para o gráfico); `hourlySummaries` emite as 24 horas.

Valores crus no JSON (`Plate.getValue()`, `Cpf.getValue()`), consistente com os DTOs irmãos. **Máscara vive só na camada de exportação.**

### 1.5 Agregação (`service/report/`) — 15 arquivos

`ReportService` (orquestrador) + `TicketRecord` + `TicketWindow` + `TicketWindowLoader` + `ReportDetailLimit` + 9 calculadores `@Component` de método único + `TicketRowMapper` + `SharePercentCalculator`. Mais `mapper/ReportCompanyMapper` (MapStruct, com o `default String map(Cnpj)` espelhando `CompanyMapper`).

`TicketWindow(List<TicketRecord> paid, entered, all, long presentAtStart)` + `static of(...)` que deduplica por `ticketId` e ordena `all` por `enteredAt` desc.

Qual conjunto cada calculador lê — **é a parte sutil, precisa estar no PR**:

| Calculador | Lê | Nota |
|---|---|---|
`RevenueSummaryCalculator` | `paid()` | `freeExitCount` = pagos com `net() == 0`; `lowestTicketValue` inclui os zeros; `averageTicketValue` = ZERO quando count 0 |
`StaySummaryCalculator` | `all()` | ignora `stayMinutes()` vazio na média; `openStayCount` = os vazios; min/max = 0 em entrada vazia |
`OccupancySummaryCalculator` | `all()` + `presentAtStart` | sweep-line: `(enteredAt, +1)`, `(exitedAt, −1)`, ordenado com **−1 antes de +1 em empate** (um carro saindo e outro entrando no mesmo minuto não pode inflar o pico); clamp do contador em 0 (há risco de `exitedAt < enteredAt` no legado); `totalSpots` nulo/zero → taxas 0.0. Maior arquivo (~65 linhas); se passar de 100, extrair `OccupancyTimeline` |
`DailySummaryCalculator` | ambos | entradas por data de entrada; receita/descontos por data de saída; itera o período preenchendo lacunas |
`HourlySummaryCalculator` | ambos | 24 linhas sempre |
`PartnershipSummaryCalculator` | `all()` + subconjunto `paid()` | `usageCount` inclui ativos, `discountGranted` não (uma parceria em ticket ativo ainda não concedeu nada) |
`ClientSummaryCalculator` | `all()` + subconjunto `paid()` | agrupa por `clientId`; exclui `clientId == null` (avulsos aparecem no detalhe); `paymentMethodsUsed` distinto e ordenado por ordinal |

`ReportDetailLimit` — record com `JSON = 20_000` e `EXPORT = 20_000`:

```java
ReportResponse getCompanyReport(UUID companyId, ReportFilter filter, ReportDetailLimit detailLimit)
```

Sem overload (é assim que o default silenciosamente diverge). O limite só chega em `TicketRowMapper` — a agregação é uma só, com duas profundidades de detalhe. `ticketsTruncated = detailLimit.exceededBy(window.all().size())`.

> **Por que 20 000 e não 50 000:** o usuário pediu PDF completo, e o PDF é o formato mais gordo (~180 B/linha). O teto real de resposta é 6 MB no API Gateway, e corpo binário é base64 → **teto efetivo ≈ 4,5 MB**. 20 000 linhas ≈ 3,8 MB de PDF ≈ 5,1 MB em base64. Acima disso o endpoint devolve **400 com a contagem real e instrução para reduzir o intervalo** — truncar silenciosamente um artefato financeiro é o pior desfecho possível. Saída de longo prazo (fora de escopo): gravar em S3 e devolver URL pré-assinada; a interface de writer não muda.

### 1.6 Filtro, controller, segurança

`dto/request/ReportFilter.java` — record `(LocalDate from, LocalDate to)` com validação em construtor compacto:

```java
to   = to != null ? to : LocalDate.now();
from = from != null ? from : to.minusDays(29);
if (from.isAfter(to)) throw new BusinessException("A data inicial não pode ser posterior à data final.");
if (ChronoUnit.DAYS.between(from, to) >= 366) throw new BusinessException("O período do relatório não pode exceder 366 dias.");
```

+ `fromInclusive()` = `from.atStartOfDay()`, `toExclusive()` = `to.plusDays(1).atStartOfDay()`, `days()`.

O par inclusivo-no-usuário / semi-aberto-no-SQL precisa existir **em um só lugar**: quem digita `01/07 – 31/07` espera o dia 31 inteiro, e `[from, to+1dia)` é a única forma que é ao mesmo tempo amigável a índice e livre do bug do `23:59:59.999`. Bean Validation não resolve — `@RequestParam` não é validado como grupo sem `@Validated`, que o projeto não usa em nenhum lugar.

`ReportController`:

```java
@GetMapping
public ResponseEntity<ReportResponse> getReport(
        @RequestAttribute(name = "companyId", required = false) UUID companyId,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
    requireCompany(companyId);
    return ResponseEntity.ok(reportService.getCompanyReport(
            companyId, new ReportFilter(from, to), ReportDetailLimit.JSON));
}
```

`requireCompany` é o bouncer privado compartilhado com `/export`. Hoje um `SUPER_ADMIN` sem empresa faz `@RequestAttribute` falhar → catch-all → **500**; passa a ser **400** com mensagem acionável. 400 e não 403: o token é válido e o usuário está autorizado na rota; 403 diria ao front para deslogar, o que é errado.

`?companyId=` do front continua ignorado — honrar seria IDOR. O front vai parar de enviar.

**`GlobalExceptionHandler`** ganha handler 400 para `MethodArgumentTypeMismatchException` + `MissingServletRequestParameterException` — hoje `?from=lixo` dá 500.

**`SecurityConfig`** — antes de `.anyRequest().authenticated()`:

```java
.requestMatchers(HttpMethod.GET, "/reports", "/reports/export").hasAnyRole("ADMIN", "SUPER_ADMIN")
```

Idioma declarativo, que é o que o projeto já usa em `/companies` e `/users/company/*` (o `if (user.getRole() != ADMIN)` do `ClientController.delete` é o precedente mais fraco: por método, esquecido em endpoints novos, corpo vazio). Nega no filter chain, então o controller nem roda e um chamador não autorizado não gera nenhum hit no banco. Paths explícitos em vez de `/reports/**` para que um futuro `POST` não herde a regra por acidente.

Os dois guards coexistem e ambos são alcançáveis:

| Chamador | Filter chain | Bouncer | Resultado |
|---|---|---|---|
`EMPLOYEE` com empresa | nega | — | **403**, serviço nunca chamado |
`ADMIN` com empresa | passa | ok | **200** |
`SUPER_ADMIN` sem empresa | passa (tem `ROLE_ADMIN`) | `companyId == null` | **400** |
sem token | nega | — | **401** |

**CORS — correção obrigatória** em `SecurityConfig.corsConfigurationSource()`: hoje não há `setExposedHeaders`, então o navegador **não consegue ler `Content-Disposition`** cross-origin (e é sempre cross-origin: `:8080` vs `:4200` em dev, Vercel vs API GW em prod). Sem isso todo download cai no nome de arquivo de fallback.

```java
configuration.setExposedHeaders(List.of("Content-Disposition"));
```

### 1.7 Fuso horário

Decisão do usuário: **`TZ=America/Sao_Paulo` como variável de ambiente da Lambda**, mantendo `LocalDateTime` naive e sem nenhuma conversão no código. `LocalDateTime.now()` passa a significar hora de parede do pátio em todo lugar, o que é a intuição do operador, e os agregadores diário/horário ficam aritmética pura.

Rodapé do relatório e legenda dos gráficos: `Horários em America/Sao_Paulo (UTC−3)`.

**Risco conhecido e aceito:** registros já gravados enquanto a Lambda rodava em UTC ficam deslocados 3 h. Isso afeta **só** o bucket diário/horário e `peakAt` — durações são invariantes a fuso, e o total do período só sofre no efeito de borda (~3 h ≈ 0,05 % de um total mensal). Auditoria de uma linha para medir o estrago (um pátio tem tráfego quase nulo de 03:00–05:00 **local**, e 00:00–02:00 local vira 03:00–05:00 UTC):

```sql
SELECT SUM(HOUR(entered_at) BETWEEN 3 AND 5)  AS madrugada_3_5,
       SUM(HOUR(entered_at) BETWEEN 0 AND 2)  AS madrugada_0_2,
       SUM(HOUR(entered_at) BETWEEN 12 AND 14) AS pico_almoco,
       COUNT(*) AS total
FROM tickets;
```

`madrugada_3_5 ≈ 0` → dados já são locais, nada a fazer. `madrugada_3_5` significativamente maior que `madrugada_0_2` → dados são UTC e o histórico fica 3 h deslocado. Se o resultado for misto, não há correção honesta (`tickets` não tem `created_at`, e UUIDv4 não é ordenável no tempo) — registrar como dívida de qualidade de dado e considerar um V5 com `created_at` + `@CreationTimestamp`.

---

## Parte 2 — Backend: exportação

### 2.1 Dependências (versões verificadas no Maven Central hoje)

| Uso | Coordenada | Versão | Licença |
|---|---|---|---|
PDF | `com.github.librepdf:openpdf` | **3.0.5** | MPL-2.0 OR LGPL-2.1+ |
XLSX | `org.apache.poi:poi-ooxml` | **5.5.1** | Apache-2.0 |
CSV | `org.apache.commons:commons-csv` | **1.14.1** | Apache-2.0 |
Extrair texto de PDF — **`<scope>test</scope>`** | `org.apache.pdfbox:pdfbox` | **3.0.8** | Apache-2.0 |

> ⚠️ **OpenPDF 3.x renomeou o pacote Java.** A migração `com.lowagie.text.*` → **`org.openpdf.text.*`** aconteceu na 2.4.0 e a 3.0.0 removeu o namespace antigo. **Todo** tutorial e resposta de StackOverflow anterior a 2025 mostra `com.lowagie` e **não compila**. Imports corretos: `org.openpdf.text.{Document, Font, Image}`, `org.openpdf.text.pdf.{PdfWriter, PdfPTable, BaseFont, PdfPageEventHelper}`. Confirmar na primeira build com `mvn dependency:tree -Dincludes=com.github.librepdf`. (Verificado: `openpdf-3.0.5.jar` existe e é `jar`, não pom aggregator. O índice de busca do Maven Central mostra 2.2.2 por estar desatualizado — o `maven-metadata.xml` diz 3.0.5.)

**Por que OpenPDF e não PDFBox para escrever:** PDFBox não tem engine de layout — sem primitiva de tabela, sem quebra de página automática, sem cabeçalho repetido. Para uma tabela de 11 colunas atravessando dezenas de páginas isso é reimplementar paginação à mão, o que viola o limite de 100 linhas de saída. `PdfPTable.setHeaderRows(n)`, que OpenPDF herdou do iText 2.x, repete cabeçalhos entre páginas de graça — é a razão inteira da escolha. PDFBox entra só em escopo de teste, para *ler* o PDF de volta. **iText 7 está descartado: AGPL-3.0 alcançaria um backend SaaS hospedado e exigiria abrir o código.**

**Custo:** ≈ **19–20 MB** no JAR shaded (`poi-ooxml-lite` 6 MB + `poi` 3 MB + `poi-ooxml` 2,1 MB + `xmlbeans` 2,8 MB + openpdf ~2 MB + transitivos). Init dominado pelo bootstrap do `SchemaTypeSystem` do XMLBeans no primeiro `SXSSFWorkbook`: **300–900 ms, às vezes >1,5 s**.

> ⚠️ **Isto contradiz a otimização deliberada de tamanho do JAR** que o `pom.xml` já faz (excluir o Tomcat, shade). Consequências a aceitar conscientemente: **medir `target/api-aws.jar` antes de commitar** — se o artefato atual já estiver perto de 50 MB, +20 MB cruza o limite de upload direto do `UpdateFunctionCode` e o deploy passa a exigir S3 ou imagem de container, **no mesmo PR**, não como surpresa no release. Se o tamanho pesar: POI+XMLBeans são **100 %** do peso; a variante PDF+CSV custa 2,1 MB em vez de 20 MB. Também dá para excluir `springdoc-openapi-starter-webmvc-ui` (webjars do Swagger UI) do artefato da Lambda.

### 2.2 Transporte binário na Lambda — a linha que faz funcionar

`ResponseEntity<byte[]>`, buffered. **`StreamingResponseBody` não é utilizável neste deploy** e seria enganoso: `StreamLambdaHandler` bufferiza a resposta inteira em `AwsProxyResponse` antes de escrever, então nada streama. Streaming real exige `InvokeWithResponseStream`/Function URLs, que `aws-serverless-java-container` não suporta.

**`StreamLambdaHandler.java` — dentro do `static` block, antes de `getHttpApiV2ProxyHandler`:**

```java
LambdaContainerHandler.getContainerConfig().addBinaryContentTypes(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/csv");
```

Sem isso o container devolve os bytes como string UTF-8 e **o PDF/XLSX chega corrompido**. `text/csv` entra na lista para que o BOM passe byte-exato.

Boa notícia ao ler o handler real: ele usa `getHttpApiV2ProxyHandler`, ou seja **payload format 2.0**. HTTP APIs honram `isBase64Encoded` automaticamente — **não há `binaryMediaTypes` para configurar no API Gateway**, que é o passo que morde quem usa REST API v1. Só a linha acima é necessária.

`application.properties`: `server.compression.enabled=true` e `server.compression.mime-types=text/csv,application/json`. **Obrigatório para CSV**, não opcional: 20 000 linhas de CSV cru ≈ 3,2 MB, e texto comprime ~6:1.

### 2.3 AWT / fontconfig — mitigar não precisando

O runtime Java da Lambda (AL2023) **não** traz fontconfig completo. Qualquer caminho que meça ou rasterize com `java.awt.Font` falha em **runtime**, não em build (`InternalError` saindo de `sun.awt.FontConfiguration`). `headless=true` **não** resolve — headless ainda precisa de fontconfig para *métricas*. A mitigação é arquitetural: **escolher os caminhos sem AWT.**

| Caminho | Regra | Por quê |
|---|---|---|
Fontes do PDF | **só base-14** (`BaseFont.HELVETICA`, `HELVETICA_BOLD`), `NOT_EMBEDDED` | OpenPDF traz as métricas AFM das base-14. Zero AWT |
Logo no PDF | `Image.getInstance(byte[])` | OpenPDF decodifica PNG com `org.openpdf.text.pdf.codec.PngImage`, não `javax.imageio` |
Largura de coluna no XLSX | `setColumnWidth(i, chars * 256)` — **nunca `autoSizeColumn`** | `autoSizeColumn` constrói `java.awt.Font` e chama `getStringBounds` → exatamente o crash de fontconfig. E é O(linhas × colunas) de medição de fonte — segundos com 20 k linhas mesmo onde funciona |
Logo no XLSX | `XSSFClientAnchor` com span de col/row — **nunca `Picture.resize()`** | `resize()` vai a `ImageUtils` → `ImageIO` → AWT |

Reforço: variável de ambiente `JAVA_TOOL_OPTIONS=-Djava.awt.headless=true` na configuração da função (único lugar garantido antes de qualquer classe carregar e durante o checkpoint do SnapStart). `-Djava.awt.headless=true` também no `argLine` do surefire — máquinas Windows têm fontes e passam de qualquer jeito, **containers Linux de CI frequentemente não**, e é isso que evita o "funciona na minha máquina".

**Acentos são falha silenciosa** — nada lança quando falta glifo, o texto simplesmente desaparece. `service/report/export/pdf/PdfFonts.java` fixa a codificação explicitamente:

```java
BaseFont HELVETICA = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
```

`WINANSI` é Cp1252, que contém **tudo** que o português precisa (`ç Ç ã Ã õ Õ á é í ó ú â ê ô à ü ñ º ª`); `R$` é ASCII. **Nunca usar `FontFactory.getFont(name, size)`** — a codificação default dele é detalhe de versão da biblioteca, e é exatamente assim que acentos desaparecem entre releases. Teste que trava isso: extrair o texto de volta com PDFBox e assertar `Convênio`, `Caminhão`, `São`, `Permanência`.

### 2.4 Abstração — modelo de documento agnóstico de formato

Duas escolhas deliberadas, ambas a serviço do DRY:

1. `contentType()`/`fileExtension()` moram no **enum**, não no writer — são dado sobre um formato, não comportamento de um renderizador. O writer faz uma coisa: bytes.
2. Writers consomem **`ReportDocument`, não `ReportResponse`**. `ReportResponse` é o contrato HTTP; `ReportDocument` é a declaração agnóstica do *que* a exportação contém — títulos de seção, cabeçalhos, ordem, tipos de célula, extratores. Montado **uma vez** por `ReportDocumentAssembler` e consumido identicamente pelos três writers. Adicionar uma seção = escrever um `ReportSectionFactory`, não editar três writers. Com `write(ReportResponse)` cada writer redecidiria as colunas e elas divergiriam.

**O ponto de coerência de todo o desenho:** `ReportColumn.extractor` devolve o valor **cru e tipado** (`BigDecimal`/`LocalDateTime`/`Long`/`String`/enum) e `ReportCellType` dirige a renderização. PDF e CSV passam por `ReportCellFormatter` para obter string pt-BR; o XLSX escreve **célula numérica/data com `numFmt`**, para que o usuário consiga somar a coluna no Excel. Uma declaração, três renderizações, e o XLSX não fica aleijado em strings.

```
enums/ReportExportFormat.java          PDF/CSV/XLSX + contentType() + fileExtension()
config/ReportExportFormatConverter.java Converter<String,ReportExportFormat> — o binding default de enum
                                        do Spring é case-SENSITIVE, então ?format=pdf falharia
service/report/export/
  ReportExportWriter.java              interface: format(); byte[] write(ReportDocument)
  ReportExportService.java             Map<Format,Writer> montado no construtor via
                                       Collectors.toMap (chave duplicada = falha no startup)
  ReportExportFile.java                record(fileName, format, content)
  ReportFileNameFactory.java           relatorio-locus-park-2026-07-01-a-2026-07-31.pdf
                                       ASCII puro, sem acento — evita RFC 5987 no header
  ReportLogo.java                      ClassPathResource("reports/…png") lido UMA vez em
                                       @PostConstruct; ausente → array vazio + warn (logo
                                       faltando degrada, não vira 500)
  document/  ReportDocument{,Header}, ReportKpi, ReportTable<T>, ReportColumn<T>,
             ReportCellType, ReportColumnScope, ReportSectionFactory, ReportKpiFactory,
             ReportDocumentAssembler
  document/section/  PaymentMethodSection @Order(10) … TicketDetailSection @Order(70)
  format/    ReportLocale, CurrencyFormatter, NumberFormatter, PercentFormatter,
             ReportDateFormatter, DurationFormatter, PlateFormatter, DocumentFormatter,
             PaymentMethodLabel, VehicleTypeLabel, TicketStatusLabel, DiscountTypeLabel,
             ReportCellFormatter
  pdf/       PdfReportExportWriter, PdfDocumentFactory, PdfFonts, PdfHeaderRenderer,
             PdfKpiGridRenderer, PdfTableRenderer, PdfDailyRevenueChartRenderer,
             PdfPageFooterEvent
  xlsx/      XlsxReportExportWriter, XlsxSummarySheetWriter, XlsxSheetWriter,
             XlsxCellWriter, XlsxColumnWidthPolicy, XlsxStyleRegistry
  csv/       CsvReportExportWriter, CsvFormatProvider, CsvSectionWriter, Utf8BomWriter
```

Seções ordenadas por `@Order` + injeção de `List<ReportSectionFactory>` (mecanismo idiomático do Spring; reordenar é mudar um token).

**Formatação:** `Locale.of("pt","BR")` explícito em `ReportLocale` (Java 21; `new Locale(...)` está deprecado) — **nunca o default da JVM**, que na Lambda é `en_US` ou `C`. `CurrencyFormatter` usa `new DecimalFormat("'R$' #,##0.00", DecimalFormatSymbols.getInstance(PT_BR))` e **não** `NumberFormat.getCurrencyInstance` — este último emite espaço não-quebrável (U+00A0) entre `R$` e os dígitos, que é válido em Cp1252 no PDF mas aparece como caractere estranho no CSV/Excel e quebra asserção de string exata. Máscaras de placa/CPF/CNPJ **delegam aos value objects do domínio** (`new Plate(raw).toString()`), então a regra (incluindo o ramo Mercosul sem hífen) existe uma única vez; `try/catch` devolvendo o valor cru, para que uma linha legada malformada não derrube a exportação inteira.

Cada classe de label é um **`switch` exaustivo sem `default`** — adicionar uma constante em `PaymentMethod` passa a ser **erro de compilação** em vez de `null` silencioso num PDF.

### 2.5 Layout do PDF

**A4 paisagem** (`PageSize.A4.rotate()`, 842 × 595 pt), margens 28 pt → **786 pt úteis**. Retrato dá ~535 pt, ou seja 33 pt por coluna para 16 colunas — ilegível (só `dd/MM/yyyy HH:mm` precisa de ~52 pt em corpo 7).

Detalhe de tickets no PDF: **11 colunas** (`Placa, Tipo, Cliente, Entrada, Saída, Permanência, Convênio, Pagamento, Bruto, Desconto, Total`) ≈ 71 pt cada em corpo 8. As 5 restantes (`ticketId, color, clientCpf, status, model`) ficam marcadas `ReportColumnScope.DATA_ONLY` e aparecem só em XLSX/CSV — declarativo, sem `if` em writer.

Estrutura: cabeçalho (logo `scaleToFit(96,32)` + nome + CNPJ mascarado + vagas + período + gerado em) → grid de ~14 KPIs em 4 colunas → gráfico de barras de faturamento diário → uma `PdfPTable` por seção na ordem do `@Order` → **todos os tickets do período** (decisão do usuário).

**Paginação:** `setHeaderRows(2)` com duas linhas de cabeçalho — linha 1 = título da seção com `setColspan(n)` (fundo escuro, branco negrito), linha 2 = cabeçalhos de coluna. OpenPDF repete **as duas** em toda quebra: cabeçalho repetido *e* título de continuação de graça, e um título nunca fica órfão no pé da página porque é estruturalmente parte da tabela, não um `Paragraph` anterior. Isso elimina toda a classe de ajuste com `setKeepWithNext`. Zebra nas linhas ímpares: 3 linhas de código, grande ganho de leitura.

**Rodapé "página X de Y":** `PdfPageEventHelper.onEndPage` escreve `Página X de` + um `PdfTemplate` reservado por `createTemplate(30,12)`; `onCloseDocument` estampa o total nele. Idioma padrão do iText, evita render em duas passadas. Mesmo rodapé leva o timestamp de geração e o fuso.

**Gráfico de barras diário** — incluir, mas só quando `period.days() <= 62`: `PdfContentByte.rectangle` + `fill()` numa caixa 700 × 140 pt, uma barra por dia, eixo Y escalado por `max(revenue)`, 3 linhas de grade rotuladas, rótulos de X a cada 5 dias. Com 366 dias cada barra tem 1,9 pt e o gráfico é ruído — acima de 62 dias, pular silenciosamente. ~70 linhas, primitivas vetoriais puras, zero AWT.

**Crash de período vazio, mecanismo exato:** `new Document(...)` + `close()` sem nada adicionado lança `ExceptionConverter: The document has no pages.` — não ocorre porque o cabeçalho é sempre adicionado. O risco restante é `PdfPTable` com header rows e zero linhas de corpo, que renderiza só os cabeçalhos: bouncer em `PdfTableRenderer` emitindo uma linha `"Nenhum registro no período."` com colspan. Gráfico pulado quando `dailySeries` vazio.

### 2.6 Layout do XLSX

**Uma aba por seção.** Uma tabela de 16 colunas e um resumo de 4 na mesma aba força largura de coluna no mínimo comum denominador e torna painel congelado e autofiltro inúteis (ambos são por aba, faixa única). Abas: `Resumo`, `Formas de Pagamento`, `Tipos de Veículo`, `Diário`, `Por Hora`, `Convênios`, `Clientes`, `Tickets`.

**`SXSSFWorkbook`, não `XSSFWorkbook`:** `new SXSSFWorkbook(new XSSFWorkbook(), 100)` + `setCompressTempFiles(true)`. Com 20 000 × 16 o XSSF mantém ~320 000 `XSSFCell` vivos ≈ 100–160 MB de heap; SXSSF guarda janela de 100 linhas e descarrega o resto em arquivo temporário.

Ressalvas do SXSSF, todas acionáveis:
1. **`workbook.dispose()` em `finally` é obrigatório.** SXSSF escreve em `java.io.tmpdir` = `/tmp` na Lambda, que tem **512 MB e persiste entre invocações quentes**. Sem `dispose()` os temporários acumulam até o container morrer com `No space left on device` — falha que só aparece depois de N exportações bem-sucedidas, isto é, exatamente o tipo que chega em produção.
2. `setAutoFilter` precisa ser chamado antes das linhas serem descarregadas — sabemos `rows().size()` de antemão, então definir a faixa logo após o cabeçalho.
3. `createFreezePane` e imagens funcionam em aba SXSSF.

**`XlsxStyleRegistry` é classe simples, NÃO `@Component`.** `workbook.createCellStyle()` dentro de loop de linha é o bug canônico do POI: XLSX tem teto de ~64 000 estilos e você chega lá com memória quadrática muito antes. E `CellStyle` está ligado ao workbook que o criou — um registry singleton entregaria estilos de um workbook morto (arquivo corrompido) ou acumularia estilos para sempre. Instanciado **por chamada de `write()`**, recebendo o `Workbook` no construtor. *Escrever isso no PR: "por que isso não é um `@Component`" é a primeira pergunta que vai receber na review.*

Valores **tipados, não strings**, para o Excel somar e ordenar:

| `ReportCellType` | Chamada POI | `numFmt` | Largura (chars) |
|---|---|---|---|
`CURRENCY` | `setCellValue(bd.doubleValue())` | `R$ #,##0.00` | 14 |
`DECIMAL` | `setCellValue(double)` | `#,##0.00` | 12 |
`INTEGER` | `setCellValue(long)` | `#,##0` | 10 |
`PERCENT` | `setCellValue(v / 100d)` | `0.0%` | 10 |
`DATE_TIME` | `setCellValue(LocalDateTime)` | `dd/mm/yyyy hh:mm` | 18 |
`DURATION_MINUTES` | `setCellValue(String)` | — | 12 |
`TEXT` | `setCellValue(String)` | — | 28 |

`PERCENT` **precisa** ser dividido por 100 — o formato `%` do Excel multiplica por 100, e errar isso produz `1250,0%`. `DATE_TIME` precisa de `setCellValue(LocalDateTime)` **mais** o estilo de data, senão o Excel mostra o serial `46204,6`.

Logo só na aba `Resumo`, via `addPicture(bytes, PICTURE_TYPE_PNG)` + `XSSFClientAnchor` com `setCol1/Row1/Col2/Row2`.

**Crash de período vazio:** `setAutoFilter` com `lastRow < firstRow` lança `IllegalArgumentException`. Bouncer em `XlsxSheetWriter`: linhas vazias → escrever cabeçalho + uma linha `"Nenhum registro no período."` e **pular o autofiltro**.

### 2.7 Layout do CSV

**Um CSV seccionado** (preâmbulo de resumo + blocos separados por linha em branco), não detalhe puro. O usuário pediu um artefato por formato; um CSV só de detalhe descarta silenciosamente todos os agregados, a empresa e o período — o mesmo erro do contrato quebrado, em outro meio. Excel e Sheets abrem sem reclamar.

```
Relatório Locus Park
Empresa;Estacionamento Central
CNPJ;12.345.678/0001-90
Vagas;120
Período;01/07/2026 a 31/07/2026
Gerado em;25/07/2026 14:32 (America/Sao_Paulo)

RESUMO
Faturamento líquido;12.345,67
…

FORMAS DE PAGAMENTO
Forma de Pagamento;Quantidade;Receita (R$);Participação
Dinheiro;120;3.456,78;28,0%
```

Trade-off declarado: um parser RFC 4180 estrito rejeita a contagem variável de colunas. O consumidor aqui é Excel/Sheets/humano. Se leitura por máquina virar requisito, manter `format=csv` seccionado e **adicionar** `format=csv-zip` (um arquivo RFC 4180 por seção num zip) — adição pura de um `ReportExportWriter`, que é o retorno da abstração.

- **Delimitador `;`** — separador de lista do Excel pt-BR; com `,` a linha inteira cai na coluna A.
- **Decimal `,`** via `CurrencyFormatter.formatBare` → `1.234,56`. **`R$` é omitido no CSV**: o prefixo faz o Excel tratar a célula como texto e mata as somas. A unidade vai no cabeçalho (`Receita (R$)`).
- Conflito delimitador/decimal: **não existe** — `1.234,56` não contém `;`. Só texto livre com `;` (nome de cliente) precisa de aspas, e `QuoteMode.MINIMAL` cuida. Testar com um cliente chamado `Silva; Souza & Cia`.
- **BOM UTF-8 obrigatório.** Sem ele o Excel pt-BR no Windows abre como Cp1252 e `Convênio` vira `ConvÃªnio`. `commons-csv` **não** escreve BOM: gravar `0xEF 0xBB 0xBF` no `ByteArrayOutputStream` **antes** de envolvê-lo no `OutputStreamWriter`.
- Terminador `\r\n` (default do `CSVFormat.EXCEL`).
- **API verificada na 1.14.1:** `build()` está **deprecado**, o método terminal é **`get()`** — `CSVFormat.EXCEL.builder().setDelimiter(';').setRecordSeparator("\r\n").setQuoteMode(MINIMAL).get()`. Os `withDelimiter(...)` também estão deprecados; não copiar exemplos antigos.

### 2.8 Endpoint

```java
@GetMapping("/export")
public ResponseEntity<byte[]> export(
        @RequestAttribute(name = "companyId", required = false) UUID companyId,
        @RequestParam ReportExportFormat format,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
    requireCompany(companyId);
    ReportFilter filter = new ReportFilter(from, to);
    ReportResponse report = reportService.getCompanyReport(companyId, filter, ReportDetailLimit.EXPORT);
    ReportExportFile file = reportExportService.export(report, filter, format);
    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(format.contentType()))
            .header(CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
            .contentLength(file.content().length)
            .body(file.content());
}
```

Bouncer em `ReportExportService`: `report.ticketsTruncated()` → `BusinessException` com a contagem real e instrução para reduzir o intervalo (§1.5).

### 2.9 Aquecimento do SnapStart

`config/ReportExportWarmup.java` — `@Component implements org.crac.Resource`, recebe `List<ReportExportWriter>`, e em `beforeCheckpoint` renderiza `ReportDocument.EMPTY` por cada writer descartando os bytes.

Eager, não lazy: o valor inteiro do SnapStart é realocar inicialização para o snapshot. Lazy joga o bootstrap do XMLBeans na **primeira exportação de cada ambiente de execução novo** — o usuário clica "Exportar XLSX" e espera 2 s extras, imprevisivelmente, para sempre. E se o SnapStart for desligado algum dia, eager apenas move o custo para a fase de init (cobrada, limite de 10 s) em vez de para uma requisição — **eager ganha nas duas configurações**, o que é o que faz disso um default e não uma aposta.

Três perigos, tratados: (1) exceção em `beforeCheckpoint` **falha a publicação da versão**, ou seja quebra o deploy — `try/catch` + `log.warn` por writer, nunca propagar; aquecimento é otimização e não pode derrubar release. (2) `dispose()` do SXSSF em `finally`. (3) Tempo de publicação cresce ~1–2 s, uma vez por deploy.

`ReportDocument.EMPTY` é também o fixture de período vazio — então **o caminho de aquecimento *é* o caminho do relatório vazio**, e qualquer crash ali aparece no deploy em vez de no usuário. É a razão principal de aquecer com documento vazio em vez de um sintético populado.

---

## Parte 3 — Frontend

### 3.1 Tipos e locale

`core/types/date-range.types.ts` (o alias `@core/types/*` já existe no `tsconfig.json`, sem uso) — `DateRangePreset = 'TODAY'|'LAST_7_DAYS'|'THIS_MONTH'`, `DateRangeSelection`, `DateRange{from,to}`, `DateRangeErrorCode`, `DateRangeValidation`, `DateRangePresetOption`.

Tipos do relatório divididos para respeitar 100 linhas: `report-summary.types.ts`, `report-breakdown.types.ts`, `report-ticket.types.ts`, e `report.types.ts` (reescrito) reexportando os três para manter um único ponto de entrada. Todos os campos `readonly`.

Uniões de enum — **valores confirmados lendo os enums Java**, não adivinhados:

```ts
export type TicketStatus  = 'ACTIVE' | 'PAID';
export type VehicleType   = 'CAR' | 'MOTORCYCLE' | 'VAN' | 'TRUCK';
export type DiscountType  = 'PERCENTAGE' | 'FIXED_VALUE' | 'FREE_HOURS';
export type ClientType    = 'AVULSO' | 'MENSALISTA';
```

> **Bug pré-existente a sinalizar, não corrigir aqui:** `modal-exit.component.ts` e `settings-price` comparam `discountType === 'percentage'` em minúsculas, o que nunca casa com `PERCENTAGE`. Não narrar os tipos existentes (`TicketResponse.status: string`) neste PR para não arrastar dashboard/history/exit; os mappers de label do relatório toleram as duas grafias.

`app.config.ts` — registro em nível de módulo, antes do `bootstrapApplication`:

```ts
registerLocaleData(localePt, 'pt-BR');
// providers:
{ provide: LOCALE_ID, useValue: 'pt-BR' },
{ provide: DEFAULT_CURRENCY_CODE, useValue: 'BRL' },
```

Registrar explicitamente como `'pt-BR'` evita depender do fallback de locale pai. **Blast radius levantado exaustivamente: 11 bindings.** Os 3 de `modal-exit.html` (`R$ {{x | number:'1.2-2'}}` → hoje `R$ 1,500.50`) passam a `R$ 1.500,50` — **é correção de bug**; trocar por `| currency` ao encostar no arquivo. Os padrões de data explícitos (`dd/MM/yyyy, HH:mm`) são invariantes a locale e não mudam. Não existe nenhum `| currency`/`| percent` hoje, então nada mais pode regredir. Custo ~12 kB.

Decisão: **moeda, data e percentual passam por pipes do Angular**, não por utils próprias — com `LOCALE_ID` pt-BR e `DEFAULT_CURRENCY_CODE` BRL, `{{ x | currency }}` *é* a implementação compartilhada, e uma `formatBrl()` seria uma segunda que divergiria. Utils em TS só onde se precisa de **string**: `format-duration.ts`, `format-percent.ts`, `format-document.ts`, `payment-method-label.ts`, `vehicle-type-label.ts`, `hour-label.ts`. `paymentMethodLabel` substitui o switch de `reports.component.ts:61` **e** o array de `modal-exit.component.ts` — esse é o ganho DRY.

### 3.2 Serviço, hooks e download

`report.service.ts` — **`companyId` sai** (IDOR):

```ts
getReport(range: DateRange): Observable<ReportResponse> {
  return this.http.get<ReportResponse>(this.baseUrl, { params: { from: range.from, to: range.to } });
}
downloadExport(format: ReportExportFormat, range: DateRange): Observable<HttpResponse<Blob>> {
  return this.http.get(`${this.baseUrl}/export`,
    { params: { format, from: range.from, to: range.to }, responseType: 'blob', observe: 'response' });
}
```

Arquivos novos, pequenos e puros:

| Arquivo | Export |
|---|---|
`shared/utils/content-disposition.ts` | `filenameFromContentDisposition(header, fallback)` — tenta RFC 5987 `filename*=UTF-8''` primeiro, depois `filename="…"`, depois bare; sanitiza (segmento após o último `/` ou `\`, remove controle e `"`); vazio → fallback |
`shared/utils/download-blob.ts` | `downloadBlob(blob, filename)` — `createObjectURL` → anchor oculto → `click()` → `remove()` → `revokeObjectURL` |
`shared/utils/error-message.ts` | `extractErrorMessage(body, fallback)` — `JSON.parse` em try/catch, primeiro não-vazio de `message`/`error`/`detail`; nunca renderiza HTML de página de erro |
`shared/utils/blob-error.ts` | `readBlobErrorMessage(error, fallback)` — **o fix do `[object Blob]`** |
`core/domains/report/report-export.filename.ts` | fallback igual à convenção do backend, para ser indistinguível |
`core/domains/report/report-export.mapper.ts` | `HttpResponse<Blob>` → `{blob, filename}`; body null → throw |
`core/domains/report/report-export.hooks.ts` | `useReportExportMutation()` |

`readBlobErrorMessage` é necessário porque com `responseType:'blob'` um 400/500 chega com o corpo JSON **dentro de um `Blob`**:

```
if (!(error instanceof HttpErrorResponse)) return fallback;
if (error.status === 0)   return 'Não foi possível conectar ao servidor. Verifique sua conexão.';
if (error.status === 403) return 'Você não tem permissão para exportar relatórios.';
if (error.error instanceof Blob) return extractErrorMessage(await error.error.text(), fallback);
```

401 não precisa de ramo — o interceptor já desloga e redireciona.

**Uma mutation, formato como variável** (o pipeline é idêntico para os três; três hooks triplicariam boilerplate e ainda precisariam coordenar para impedir exportações concorrentes). Qual formato está baixando vem de `mutation.variables()`, sem signal extra:

```ts
protected readonly exportingFormat = computed<ReportExportFormat | null>(() =>
  this.exportMutation.isPending() ? this.exportMutation.variables()?.format ?? null : null);
```

Sem toast de sucesso — o indicador de download do navegador já é a confirmação. Toast só em falha.

`report.hooks.ts` (reescrito):

```ts
export function isReportRangeEnabled(range: DateRange): boolean {
  if (!range.from || !range.to) return false;
  return range.from <= range.to;
}

export function useReportQuery(range: Signal<DateRange>) {
  const service = inject(ReportService);
  return injectQuery(() => ({
    queryKey: ['reports', 'summary', range().from, range().to] as const,
    queryFn: () => lastValueFrom(service.getReport(range())),
    enabled: isReportRangeEnabled(range()),
    staleTime: 60_000,
    placeholderData: keepPreviousData,
  }));
}
```

`staleTime`/`placeholderData` são os primeiros do codebase, e justificados: o payload é pesado (até 20 k linhas) e analytics não é realtime — com o default `staleTime: 0` toda remontagem e todo foco de janela refazem a chamada. `keepPreviousData` evita que cada troca de preset apague a página inteira: `isPending()` → skeleton; `isFetching() && !isPending()` → barra de progresso de 2 px + `opacity:.6`, deixando visível que os números estão velhos. Aplicar **por query**, sem retrofit no `QueryClient` global.

Adicionar `invalidateQueries({queryKey:['reports']})` ao `onSuccess` de `useCheckOutMutation` para que uma saída atualize um relatório aberto.

> ⚠️ **Bloqueador de build:** `pages/dashboard/dashboard.ts` chama `useReportQuery(this.companyId)` e `reportQuery.refetch()`. Mudar a assinatura quebra a compilação. E como `/reports` passa a ser ADMIN-only, o dashboard de um `EMPLOYEE` tomaria 403. Correção mínima no mesmo passo: `useReportQuery(signal(buildRange('TODAY', new Date())))` com `enabled: isAdmin()`, envolver o terceiro stat card em `@if (isAdmin())` e trocar o `refetch()` condicional por `invalidateQueries`. Consequência: operadores perdem aquele card — aceitável; um contador próprio para não-admin fica como follow-up (precisaria de endpoint novo ou aceitar semântica de "tickets ativos").

### 3.3 Seletor de período

| Arquivo | Conteúdo |
|---|---|
`core/utils/date-iso.ts` | `toIsoDate(Date)`, `isIsoDate(string)`, `addDays(Date, n)`, `daysBetween(from, to)` |
`core/utils/date-range.factory.ts` | `buildRange(preset, now)`, `DEFAULT_RANGE_PRESET`, `PRESET_OPTIONS` |
`core/utils/date-range.validation.ts` | `validateRange(range, now)`, `MAX_RANGE_DAYS = 366` |
`shared/components/date-range-picker/*` | UI |

`toIsoDate` usa `getFullYear/getMonth/getDate` com padding — **nunca `toISOString()`**, que em UTC−3 transforma `2026-07-25T22:00` em `2026-07-26`. (Esse bug exato existe hoje em `history.component.ts`; anotar como follow-up, fora de escopo.)

Presets: `TODAY` → `from == to`; `LAST_7_DAYS` → **7 dias corridos incluindo hoje** (`addDays(now,-6)`); `THIS_MONTH` → dia 1 do mês até hoje, rotulado **"Este mês (até hoje)"**, porque data futura é inválida e não tem dado.

`validateRange` é cadeia de bouncers em **comparação de string** (`YYYY-MM-DD` é lexicograficamente ordenado, sem parse de `Date`): `INVALID_DATE` → `FROM_AFTER_TO` → `FUTURE_DATE` → `RANGE_TOO_LONG` (366, igual ao backend). `new Date()` nunca é chamado dentro das funções puras — o componente fornece no limite da chamada, então os specs passam data congelada sem `vi.setSystemTime`.

UI: linha de chips (`Hoje` / `Últimos 7 dias` / `Este mês`) **mais** dois `<input type="date">` sempre visíveis com `[max]="today"`. Chip → emite `buildRange(preset, new Date())`. Editar input → `selection.set('CUSTOM')` + valida; inválido → mostra erro e **não emite** (a query nunca dispara com range ruim). O `max` é dica de UX; digitação contorna em vários navegadores, daí a validação em TS.

### 3.4 Composição da página

```
pages/reports/
  reports.component.ts|html|css              orquestrador (~70 linhas TS)
  components/report-toolbar/                 date-range-picker + grupo de exportação + refresh
  components/report-kpis-revenue/            8 KPIs   (summary.revenue)
  components/report-kpis-stay/               5 KPIs   (summary.stay)
  components/report-kpis-occupancy/          8 KPIs   (summary.occupancy)
  components/report-bar-chart/               gráfico de barras CSS, reusado 2×
  components/report-table/                   tabela genérica paginada, reusada 7×
  components/report-table/report-cell.component.ts
  tables/{payment-method,vehicle-type,daily,hourly,partnership,client,ticket}.table.ts
```

21 KPIs com SVG inline não cabem num template de ≤100 linhas, e o corte em três mapeia 1:1 em `summary.revenue`/`stay`/`occupancy`.

Compartilhados novos: **preencher o stub `shared/components/summary-card/`** (hoje `export class SummaryCard {}` com template `<p>summary-card works!</p>`, importado em lugar nenhum) com `input.required<string>()` para `label`/`value`, `input('')` para `hint`, `input<SummaryCardTone>('slate')` para o tom, e ícone via `<ng-content select="[cardIcon]">` (projeção de conteúdo, **não** input de string SVG, que exigiria `DomSanitizer`/`innerHTML`). Passa a ser o card de KPI único do app; migrar o `.stat-card` do dashboard fica como follow-up. Mais `inline-notice` (`info|warning|error`) e `empty-state` — o relatório tem ~8 seções que esvaziam independentemente.

**Tabela genérica em vez de 7 tabelas dedicadas.** `report-table` recebe `columns: readonly ReportColumn[]`, `rows: readonly Record<string, unknown>[]`, `pageSize = 50`, `totals`. `report-cell` faz `@switch` no `format` (`text|integer|decimal|currency|percent|duration|date|datetime`) e aplica o pipe, renderizando `'—'` para null. Trade-off: perde checagem de chave em tempo de compilação; mitigado por cada `tables/*.table.ts` exportar sua interface de linha tipada + um `toRows()` puro cujo spec assere que as chaves casam com `COLUMNS`. Ganho: **um** arquivo de CSS de tabela em vez de sete, e o orçamento de 8 kB deixa de ser preocupação.

**Paginação é obrigatória, não opcional** — `tickets` pode ter 20 000 linhas; renderizar tudo congela a página. `pageSize` 50 com anterior/próximo e "x–y de z". Sem biblioteca de virtual scroll.

**Rotular os dois eixos de bucketing.** Não sobrepor faturamento e entradas num gráfico só — significam coisas diferentes no mesmo eixo. Dentro de uma seção "Movimentação diária", dois gráficos empilhados compartilhando os rótulos de data: *"Faturamento por dia — data de pagamento (saída)"* e *"Entradas por dia — data de entrada"*. Subtítulo da seção enuncia a regra uma vez: *"Valores financeiros são apurados pela data de saída/pagamento. Contagens de entrada, ocupação e horários de pico usam a data de entrada. Horários em America/Sao_Paulo."* Cabeçalhos da tabela diária repetem compacto: `Entradas (entrada)`, `Saídas (saída)`, `Faturamento (saída)`.

`ticketsTruncated` → `inline-notice variant="warning"` acima da tabela: *"Exibindo apenas os primeiros 20.000 registros. Os indicadores acima consideram todos os N registros. Use a exportação para a lista completa."*

**Exportação: três botões num grupo segmentado, não dropdown.** O app não tem primitiva de menu/popover, então dropdown significaria clique-fora, focus trap, `aria-expanded`, `role="menu"` e Escape para zero ganho funcional sobre três affordances visíveis; e cada botão carrega seu próprio spinner inline, o que um gatilho colapsado não faz. Todos desabilitam enquanto qualquer um está pendente (o endpoint é caro; exportação paralela não vale suportar). **`appLoading` NÃO serve aqui** — ele reescreve `button.innerHTML`, destruindo os anchors de binding do bloco `@if`; usar spinner com `@if`, reaproveitando o `@keyframes spin` global.

**Tokens CSS em `styles.css`** (~25 linhas aditivas: `--lp-bg`, `--lp-surface`, `--lp-border`, `--lp-text`, `--lp-muted`, `--lp-radius`, `--lp-shadow` e pares de cor). Vale a pena **porque** este PR senão copiaria a paleta slate 12 vezes. Risco zero: puramente aditivo, as 10 páginas existentes ficam com seus hexes. Não refatorar página existente aqui. Nota: `dashboard.css` já tem 12,9 kB e **já** estoura o *warning* de 8 kB hoje (o erro é 16 kB) — manter cada CSS novo ≤3 kB.

### 3.5 Acesso e rota

`core/guards/admin.guard.ts` — `isAdmin()` de `@core/utils/jwt`; senão toast + `router.navigate(['/dashboard'])`. Necessário porque o interceptor **não** desloga em 403, então um deep link renderizaria página de erro.

`app.routes.ts` — **tornar reports lazy**, apesar de ser a única rota lazy hoje:

```ts
{ path: 'reports',
  loadComponent: () => import('./pages/reports/reports.component').then((m) => m.Reports),
  canActivate: [authGuard, adminGuard] }
```

É agora a maior página **e** é ADMIN-only: operadores baixariam código que nunca alcançam. `loadComponent` não exige config extra e é aditivo. `manage-team`/`settings-price` têm o mesmo argumento — follow-up.

`layout/sidebar/sidebar.html` — mover o link "Relatórios" para dentro do `@if (isAdmin())` existente, como manage-team e settings-price.

---

## Parte 4 — Testes

Convenções do repositório, mantidas: backend `@ExtendWith(MockitoExtension.class)` para serviços e `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters=false)` + `.requestAttr("companyId", id)` para controllers com `@RequestAttribute`; frontend `*.spec.ts` ao lado da unidade, `it('deve …')` em português, `TestBed` + `provideHttpClientTesting()` + `httpMock.verify()`. Novo neste PR: specs de função pura sem TestBed, e o primeiro uso de `vi.fn`.

**Backend, ~30 classes.** Fixtures compartilhados `TicketRecordFixture` (builders `paid/active/legacy/withPartnership`) e `ReportResponseFixture` (`full()` com acentos, um cliente `Silva; Souza & Cia`, uma placa Mercosul, uma linha legada de bruto nulo, um ticket ativo de `exitedAt` nulo; e `empty()`) — os três writers são assertados contra **entrada idêntica**, que é o que torna a consistência entre formatos testável.

Prioridades, com os casos que realmente importam:

- **Motor de preço, hoje 0 % coberto** — `HourlyRateCalculatorTest`, `TolerancePolicyTest`, `GrossStayChargeCalculatorTest`, `PartnershipDiscountCalculatorTest`, `StayChargeTest`, `PaymentServiceTest`, `TicketServiceTest`. Casos-regressão indispensáveis: **dentro da tolerância com parceria `FREE_HOURS` → `StayCharge.free()`** (prova que a tolerância curto-circuita antes do desconto); `FREE_HOURS` sobre base de diária excedendo o bruto → clamp; invariante `gross == net + discount` em todos os casos; limite `horas == dailyTriggerHours` (`>=`); `firstHourValue`/`dailyValue`/`toleranceMinutes` nulos.
- **Agregação** — um test class por calculador. Casos que pegam bug de verdade: janela vazia sem divisão por zero; `exitedAt` nulo ignorado na média mas contado em `openStayCount`; **saída e entrada no mesmo instante não inflam o pico** (ordenação −1 antes de +1); `totalSpots` nulo → taxas 0.0; dias sem movimento aparecem zerados; `Σ dailySummaries.revenue == summary.revenue.netRevenue`; ticket que entra dia 1 e sai dia 2 conta entrada no dia 1 e receita no dia 2; linha legada de `grossAmount` nulo somando coerente.
- **Repositório** — `@DataJpaTest`: a projeção materializa os 19 campos **incluindo `Plate` e `Cpf` via converter** (é o risco real desta escolha; se falhar, plano B é `join fetch` + `TicketRecordFactory`, sem mudar nenhum dos 9 agregadores); isolamento de tenant; limite inferior inclusivo e superior exclusivo; `left join` não elimina linha de cliente/convênio nulo; `countPresentAt` conta estadia que atravessa a janela inteira.
- **Filtro** — defaults (30 dias), `from > to` → 400, exatamente 366 → 400, 365 → ok, `toExclusive()` = meia-noite de `to+1` (inclusividade do último dia).
- **Exportação** — PDF: começa com `%PDF-`, e **PDFBox extrai `Convênio`/`Caminhão`/`São` idênticos** (a asserção anti-Cp1252, manter para sempre); rodapé `Página 1 de`; detalhe com 11 cabeçalhos, não 16; período >62 dias não renderiza o gráfico. XLSX relido com POI: 8 abas nomeadas; célula de moeda é **numérica** com `numFmt`, não string; percentual = valor/100; painel congelado e autofiltro; **contagem de `CellStyle` do workbook < 30** (trava o bug de estilo por célula); larguras explícitas > 0; **nenhum temporário remanescente em `java.io.tmpdir` após `write()`** (prova o `dispose()`). CSV: 3 primeiros bytes `EF BB BF`; `\r\n`; `;`; decimal `,` **sem prefixo `R$`**; nome com `;` entre aspas; comparação literal do bloco `FORMAS DE PAGAMENTO`. **`empty()` gera arquivo válido e não vazio nos três formatos** — é o crash mais provável no mundo real.
- **Controller e acesso** — `ReportControllerTest` (`@WebMvcTest`): sem datas → filtro default; `from=lixo` → 400; **sem `companyId` → 400** (cobre o 500 de hoje); `?companyId=<outra>` ignorado; os três content types e o `Content-Disposition`; `verify` de que o export recebeu `ReportDetailLimit.EXPORT`. Mais **`ReportAccessControlTest`** (`@SpringBootTest` + filtros LIGADOS, token real): `EMPLOYEE` → **403 com `verify(reportService, never())`**, `ADMIN` → 200, `SUPER_ADMIN` sem empresa → 400, sem token → 401. **Essa classe separada é necessária porque `@WebMvcTest` com `addFilters=false` não cobre o 403.**

`src/test/resources/application-test.properties` ganha `spring.main.headless=true`; o `argLine` do surefire ganha `-Djava.awt.headless=true`.

**Frontend, ~18 specs.** Casos que pegam bug de verdade: `toIsoDate(new Date(2026,6,25,23,30))` → `'2026-07-25'` (**guarda de regressão do shift UTC**); `LAST_7_DAYS` atravessando mês e ano; `THIS_MONTH` no dia 1 → `from == to`; ano bissexto; 366 válido / 367 → `RANGE_TOO_LONG`; header exato do backend → nome de arquivo exato, `filename*=UTF-8''relat%C3%B3rio.pdf` → `'relatório.pdf'`, ambos presentes → RFC 5987 ganha, `"../../etc/passwd"` → `'passwd'`; **`report.spec.ts` assere `expect(req.request.params.has('companyId')).toBe(false)`** (guarda anti-IDOR) e `req.request.responseType === 'blob'`; `blob-error` com `new HttpErrorResponse({status:400, error:new Blob([json])})` → mensagem decodificada; cada `toRows()` com o conjunto exato de chaves casando com suas `COLUMNS`; e um `core/locale/locale.spec.ts` assertando `formatCurrency(1500.5,'pt-BR','R$','BRL','1.2-2') === 'R$ 1.500,50'`, que tranca a razão inteira da mudança de locale sem precisar de spec de componente.

**Stubs de jsdom necessários** (verificado contra o jsdom 28 instalado): `Blob.prototype.text()` **funciona**; `URL.createObjectURL`/`revokeObjectURL` são **undefined**, então precisam ser **atribuídos** (`URL.createObjectURL = vi.fn(() => 'blob:x')`) — `vi.spyOn` lança em propriedade inexistente; e `vi.spyOn(HTMLAnchorElement.prototype,'click')` para o jsdom não logar "Not implemented: navigation".

`tsconfig.spec.json` precisa incluir `"src/**/*.fixture.ts"` — hoje só lista `*.d.ts` e `*.spec.ts`, então o fixture ficaria fora do programa de type-check.

### Não testável em unidade — verificar à mão

1. **O arquivo em si.** `ng serve` + backend local, logar como ADMIN, escolher "Este mês", baixar os três: o PDF abre com o logo, cabeçalho e período? O XLSX soma a coluna de faturamento? O CSV abre no Excel pt-BR com acentos e colunas separadas?
2. **`Content-Disposition` cross-origin.** `HttpTestingController` nunca aplica CORS, então o spec passa mesmo se o header for invisível em produção. DevTools → Network → confirmar `Access-Control-Expose-Headers: Content-Disposition` (§1.6).
3. **Shade.** Nenhum teste pega falha de shading — testes rodam no classpath **não** shaded, então um `module-info` mesclado incorretamente ou um `.xsb` de XMLBeans perdido passa no CI e falha só na Lambda. Adições necessárias além do `ServicesResourceTransformer` já presente: filtro excluindo `module-info.class` **e** `META-INF/versions/*/module-info.class` (o `log4j-api` traz o segundo, e dois descritores de módulo num jar é inválido), mais `ApacheLicenseResourceTransformer`/`ApacheNoticeResourceTransformer` (dever de notice do LGPL/MPL do OpenPDF). Garantir que `poi-ooxml-lite` e `poi-ooxml-full` nunca coexistam (tipos de schema duplicados → `SchemaTypeLoaderException`); excluir `poi-ooxml-full` defensivamente. **Atenuante:** instanciamos `SXSSFWorkbook`/`XSSFWorkbook` **direto**, não via `WorkbookFactory`, então o caminho de `ServiceLoader` do POI sai do caminho crítico. **Portão de release: três downloads reais contra a função publicada, um por formato.**
4. **Memória da Lambda.** SXSSF com 20 k linhas + 20 k `TicketRecord` + o `byte[]` bufferizado querem **≥1024 MB**, idealmente 1536 (que também compra vCPU proporcional e reduz o tempo de exportação). Configuração atual desconhecida — confirmar antes de habilitar XLSX em produção.
5. **Tamanho do JAR e caminho de deploy** (§2.1).
6. **`<input type="date">`** nativo, `max`, e formato de exibição (depende do locale do SO; jsdom não renderiza widget).
7. **Orçamento de bundle** — `budgets` só valem na configuração `production`; `ng test` usa `development`, então um estouro **não** falha os testes. Ler a tabela de budget em `ng build`.
8. **Sidebar + `adminGuard`** — `isAdmin()` lê `localStorage` direto (não injetável). Logar como EMPLOYEE: sem link "Relatórios", e `/reports` redireciona para `/dashboard`.

---

## Parte 5 — Ordem de execução

Backend e frontend são independentes até o passo 12, porque os aliases legados na raiz de `ReportResponse` mantêm o `reports.component.html` atual compilando.

| # | Commit | Escopo |
|---|---|---|
1 | `fix(api): retornar 400 para parâmetros de requisição inválidos` | `GlobalExceptionHandler` |
2 | `perf(tickets): filtrar tickets por empresa na consulta` | `TicketRepository`, `TicketService`, `TicketServiceTest` |
3 | `feat(db): registrar bruto e desconto no ticket e indexar consultas de relatório` | V4, `Ticket` — **rodar `mvn -Pmigrate flyway:migrate` antes de qualquer deploy** |
4 | `refactor(payment): extrair calculadoras de estadia e desconto` | `service/payment/` (5 arquivos), `PaymentService`, `TicketService`, 6 test classes |
5 | `feat(reports): projetar tickets do período com bruto, desconto e cliente` | `TicketRecord`, `TicketWindow`, `TicketWindowLoader`, `TicketRecordQuery`, `TicketRepository`, fixture, 3 test classes |
6 | `feat(reports): calcular resumos, quebras e linhas detalhadas` | 14 records, 11 componentes, `ReportCompanyMapper`, `ReportDetailLimit`, 11 test classes |
7 | `feat(reports): filtrar relatório por período e exigir empresa vinculada` | `ReportFilter`, `ReportController`, 2 test classes |
8 | `feat(reports): restringir relatórios a administradores` | `SecurityConfig` (regra de role + `setExposedHeaders`), `ReportAccessControlTest` |
9 | `build(api): adicionar bibliotecas de exportação de relatórios` | `pom.xml`, logo em `src/main/resources/reports/`, `StreamLambdaHandler`, `application.properties` — **medir o JAR e rodar `mvn dependency:tree` aqui** |
10 | `feat(reports): modelar documento de exportação agnóstico de formato` | enum, converter, `document/` + `section/` + `format/`, `ReportExportWriter`/`File`/`FileNameFactory`/`Logo`, fixture, 8 test classes |
11 | `feat(reports): exportar relatório em PDF, XLSX e CSV` | `pdf/` (8), `xlsx/` (6), `csv/` (4), `ReportExportService`, endpoint `/export`, 4 test classes. Dividir em 11a/11b/11c por formato se o tamanho da review incomodar |
12 | `perf(reports): pré-carregar geradores de arquivo no SnapStart` | `ReportExportWarmup` |
13 | `feat(web): tipar contrato de relatórios e aplicar locale pt-BR` | tipos, uniões de enum, `app.config.ts` |
14 | `feat(web): adicionar utilitários de período, formatação e download` | utils puros + specs |
15 | `feat(web): consumir relatório por período e exportação` | `report.service.ts`, `report.hooks.ts`, hooks de export, `report.spec.ts`, `tsconfig.spec.json` — **corrigir `dashboard.ts`/`dashboard.html` aqui, senão a build quebra** |
16 | `feat(web): restringir relatórios a administradores` | `admin.guard.ts`, `app.routes.ts`, `sidebar.html` |
17 | `feat(web): extrair componentes compartilhados de relatório` | tokens em `styles.css`, `summary-card`, `inline-notice`, `empty-state`, `date-range-picker` |
18 | `feat(web): reconstruir a página de relatórios` | `report-table`/`report-cell`, 7 `tables/*` + specs, `report-bar-chart`, 3 componentes de KPI, `report-toolbar`, `reports.component.*` |
19 | `refactor(web): reaproveitar rótulos e moeda no modal de saída` | `modal-exit` usa `PAYMENT_METHOD_LABELS` e `| currency` |
20 | `docs(reports): mapear o módulo de relatórios` | `docs/relatorios.md` (~40 linhas): o que cada pacote faz, os dois eixos de data, a regra do fallback legado, e as 4 armadilhas de Lambda (binary types, AWT, `dispose()`, ordem de migration) |

## Verificação

```bash
# Backend
cd backend/api && ./mvnw test                     # toda a matriz acima
./mvnw -DskipTests package && ls -lh target/api-aws.jar   # tamanho do artefato (§2.1)
./mvnw dependency:tree -Dincludes=com.github.librepdf,org.apache.poi
./mvnw spring-boot:run                            # local, perfil dev

# Frontend
cd frontend && npm test                           # vitest
npm run build                                     # ler a tabela de budget
npm start                                         # localhost:4200
```

Ponta a ponta, logado como ADMIN em `/reports`:
1. Trocar entre Hoje / Últimos 7 dias / Este mês e uma faixa personalizada — os números mudam, a página não pisca (`keepPreviousData`), range inválido mostra erro e não dispara requisição.
2. Conferir moeda `R$ 1.500,50` e data `dd/MM/yyyy HH:mm`.
3. Baixar PDF, CSV e XLSX. **Abrir cada um:** logo presente, cabeçalho com empresa/CNPJ/período, acentos corretos (`Convênio`, `Caminhão`), rodapé com página X de Y no PDF, coluna de faturamento somável no XLSX, CSV abrindo em colunas no Excel pt-BR.
4. Pedir um período que exceda 20 000 tickets → 400 com mensagem legível (não `[object Blob]`).
5. Logar como EMPLOYEE → sem link "Relatórios", `/reports` redireciona, e `GET /reports` direto devolve 403.
6. Rodar a auditoria de fuso (§1.7) e confirmar se o histórico está local ou UTC.
7. Após deploy: os três downloads contra a função publicada (portão de release do §Parte 4, item 3).
