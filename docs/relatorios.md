# Módulo de Relatórios

Mapa de navegação para não precisar ler os ~90 arquivos do módulo. Para o *porquê* de cada decisão, ver `.claude/continue-glistening-taco.md`.

## Backend (`backend/api/src/main/java/com/locuspark/api`)

- `controller/ReportController.java` — `GET /reports` (JSON) e `GET /reports/export?format=pdf|csv|xlsx`. Ambos ADMIN/SUPER_ADMIN, `companyId` vem do JWT (query é ignorada).
- `service/report/` — agregação: `TicketRecord`/`TicketWindow`/`TicketWindowLoader` (projeção), 9 calculadores `@Component` de método único (Revenue/Stay/Occupancy/Daily/Hourly/PaymentMethod/VehicleType/Partnership/Client), `ReportService` orquestra tudo em `getCompanyReport(companyId, ReportFilter, ReportDetailLimit)`.
- `service/report/export/` — camada de exportação, agnóstica de formato:
  - `document/` monta um `ReportDocument` a partir do `ReportResponse` (seções, KPIs, tabelas).
  - `pdf/`, `xlsx/`, `csv/` — um `ReportExportWriter` por formato, todos consumindo o mesmo `ReportDocument`.
  - `format/` — formatadores pt-BR (moeda, data, percentual, rótulos de enum).
- `service/payment/` — calculadoras de estadia/desconto (`StayCharge`, `TolerancePolicy`, `HourlyRateCalculator`, `GrossStayChargeCalculator`, `PartnershipDiscountCalculator`), usadas pelo checkout, não só pelo relatório.
- `dto/response/report/` — 14 records que formam o `ReportResponse`.

## Frontend (`frontend/src/app`)

- `core/domains/report/` — tipos (`report.types.ts` + 3 arquivos por bloco), `report.service.ts` (GET + export como blob), `report.hooks.ts` (query com `staleTime`/`keepPreviousData`), `report-export.hooks.ts` (mutation única, formato como variável).
- `core/utils/date-range.*` — presets, validação e construção de período, funções puras sem `Date.now()` implícito.
- `pages/reports/` — `reports.component.*` orquestra toolbar + KPIs + gráficos + 7 tabelas (`tables/*.table.ts`, cada uma com `COLUMNS` + `toRows()`).

## Os dois eixos de data

**Faturamento, desconto e formas de pagamento são apurados pela data de SAÍDA** (quando o dinheiro entrou). **Carros, ocupação e picos de horário são apurados pela data de ENTRADA.** Cada bloco da UI e cada consulta (`findPaidRecordsByExitWindow` vs `findRecordsByEntryWindow`) rotula qual eixo está usando — nunca sobrepor os dois num mesmo gráfico.

## Fuso horário

`Ticket.enteredAt`/`exitedAt` são `Instant` (UTC, tipo inequívoco — correção trazida pelo merge com `origin/master`, que resolveu uma ambiguidade real entre front e back). O relatório continua precisando de hora de parede em `America/Sao_Paulo` para bucket diário/horário, pico de ocupação e exibição. Ponto único de conversão: `TicketRecord.enteredAtLocal()`/`.exitedAtLocal()` (mesmo padrão dos acessores derivados `net()/gross()/discount()`). Todo calculador, formatter e exportador consome os acessores `*Local()`, nunca `enteredAt()`/`exitedAt()` cru — isso mantém a garantia original do design (§1.7 do plano): o resto do módulo opera sobre `LocalDateTime` já convertido, sem `ZoneId` espalhado pelo código. `ReportFilter` espelha o mesmo par: `fromInclusive()/toExclusive()` (`LocalDateTime`, para calculadoras) e `fromInstant()/toInstant()` (`Instant`, para os limites de consulta no banco).

## Fallback de linhas legadas

Tickets pagos antes da migration V4 têm `gross_amount`/`discount_amount` `NULL`. `TicketRecord.gross()`/`.discount()` são os **únicos** pontos que leem esses campos — todo agregador consome os acessores derivados, nunca a coluna crua. Não replicar o `null`-check em nenhum outro lugar.

## Armadilhas de Lambda (não reintroduzir)

1. **Binary content types** — `StreamLambdaHandler` precisa de `addBinaryContentTypes("application/pdf", ".xlsx", "text/csv")` no `static` block, senão o PDF/XLSX chega corrompido.
2. **AWT/fontconfig** — zero `java.awt.Font` em qualquer caminho de exportação (sem `autoSizeColumn`, sem `Picture.resize()`, sem `javax.imageio`). O runtime da Lambda não tem fontconfig completo e falha em runtime, não em build.
3. **`SXSSFWorkbook.dispose()`** — obrigatório em `finally`. Sem isso, arquivos temporários em `/tmp` (que persiste entre invocações quentes) acumulam até `No space left on device`.
4. **Ordem de deploy da migration V4** — rodar `flyway:migrate` **antes** de subir o JAR novo (schema aditivo/nullable, seguro nessa direção; o contrário quebra a validação de schema em todo cold start).
