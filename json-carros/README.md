# Catálogo FIPE (Carros e Motos)

Scripts Node.js para buscar e processar todas as marcas e modelos de carros e motos na API oficial da FIPE.

## Arquivos Gerados

- `catalogo-fipe.json`: Catálogo bruto de carros
- `catalogo-fipe-limpo.json`: Catálogo processado e simplificado de carros
- `catalogo-fipe-motos.json`: Catálogo bruto de motos
- `catalogo-fipe-motos-limpo.json`: Catálogo processado e simplificado de motos

## Como rodar

Requer Node.js 18 ou superior.

### Carros
```bash
npm run montar       # Baixa marcas e modelos de carros da FIPE
npm run processar    # Limpa e simplifica os nomes de modelos de carros
```

### Motos
```bash
npm run montar:motos    # Baixa marcas e modelos de motos da FIPE
npm run processar:motos # Limpa e simplifica os nomes de modelos de motos
```
