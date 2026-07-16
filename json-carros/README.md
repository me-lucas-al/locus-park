# Catálogo FIPE (marcas + modelos)

Script Node.js que busca todas as marcas de carro na API da FIPE e, para cada uma,
busca seus modelos, salvando tudo em um único arquivo `catalogo-fipe.json`.

## Como rodar

Requer Node.js 18 ou superior (usa o `fetch` nativo, sem dependências externas).

```bash
npm run montar
```

O script vai:
1. Buscar a lista de marcas (`GET /carros/marcas`).
2. Para cada marca, buscar seus modelos (`GET /carros/marcas/{codigo}/modelos`).
3. Salvar tudo em `catalogo-fipe.json`, na raiz do projeto.

## Formato do JSON gerado

```json
[
  {
    "codigoMarca": "59",
    "nomeMarca": "VW - VolksWagen",
    "modelos": [
      { "codigoModelo": 5940, "nomeModelo": "AMAROK High.CD 2.0 16V TDI 4x4 Dies. Aut" },
      { "codigoModelo": 5941, "nomeModelo": "AMAROK Comfor. 3.0 V6 TDI 4x4 Dies. Aut." }
    ]
  }
]
```

## Sobre o limite de requisições

A API da FIPE permite 500 requisições gratuitas por dia sem token. O script já
inclui uma pequena pausa (300ms) entre cada requisição de marca, então rodar
uma vez por dia é suficiente e seguro dentro do limite gratuito.

Se alguma marca falhar (erro de rede, limite atingido, etc.), o script continua
rodando as demais e mostra no final quais marcas precisam ser buscadas de novo.

## Próximo passo (opcional)

Este JSON ainda tem os nomes de modelo "crus" (com motor/versão/tração juntos,
ex: "AMAROK High.CD 2.0 16V TDI 4x4 Dies. Aut"). Se você quiser um select
simples com só "Amarok", "Gol", "Corolla" etc., use este arquivo como entrada
para um segundo processamento de limpeza/deduplicação dos nomes.
