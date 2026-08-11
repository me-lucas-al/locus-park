const fs = require('fs/promises');
const path = require('path');
const { buscarMarcas, buscarModelosPorMarca } = require('./fipeClient');
const { aguardar } = require('./aguardar');

const TIPO_VEICULO = 'caminhoes';
const ARQUIVO_SAIDA = path.join(__dirname, '..', 'catalogo-fipe-caminhoes.json');
const PAUSA_ENTRE_REQUISICOES_MS = 250;

async function montarCatalogoCaminhoes() {
  console.log('Buscando lista de marcas de caminhões na FIPE...');
  const marcas = await buscarMarcas(TIPO_VEICULO);
  console.log(`${marcas.length} marcas de caminhões encontradas. Buscando modelos de cada uma...`);

  const catalogo = [];
  const marcasComErro = [];

  for (const [indice, marca] of marcas.entries()) {
    const progresso = `[${indice + 1}/${marcas.length}]`;

    try {
      const modelos = await buscarModelosPorMarca(marca.codigo, TIPO_VEICULO);

      catalogo.push({
        codigoMarca: marca.codigo,
        nomeMarca: marca.nome,
        modelos: (modelos || []).map((modelo) => ({
          codigoModelo: modelo.codigo,
          nomeModelo: modelo.nome,
        })),
      });

      console.log(`${progresso} ${marca.nome}: ${(modelos || []).length} modelos`);
    } catch (erro) {
      console.warn(`${progresso} Falha ao buscar "${marca.nome}": ${erro.message}`);
      marcasComErro.push(marca.nome);
    }

    await aguardar(PAUSA_ENTRE_REQUISICOES_MS);
  }

  await fs.writeFile(ARQUIVO_SAIDA, JSON.stringify(catalogo, null, 2), 'utf-8');

  console.log(`\nCatálogo de caminhões salvo em: ${ARQUIVO_SAIDA}`);
  if (marcasComErro.length > 0) {
    console.warn(`Marcas com erro: ${marcasComErro.join(', ')}`);
  }
}

if (require.main === module) {
  montarCatalogoCaminhoes().catch((erro) => {
    console.error('Erro ao montar o catálogo de caminhões:', erro);
    process.exit(1);
  });
}

module.exports = { montarCatalogoCaminhoes };
