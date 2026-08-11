const fs = require('fs/promises');
const path = require('path');
const { buscarMarcas, buscarModelosPorMarca } = require('./fipeClient');
const { aguardar } = require('./aguardar');

const TIPO_VEICULO = 'motos';
const ARQUIVO_SAIDA = path.join(__dirname, '..', 'catalogo-fipe-motos.json');
const PAUSA_ENTRE_REQUISICOES_MS = 250;

async function montarCatalogoMotos() {
  console.log('Buscando lista de marcas de motos na FIPE...');
  const marcas = await buscarMarcas(TIPO_VEICULO);
  console.log(`${marcas.length} marcas de motos encontradas. Buscando modelos de cada uma...`);

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

  console.log(`\nCatálogo de motos salvo em: ${ARQUIVO_SAIDA}`);
  if (marcasComErro.length > 0) {
    console.warn(`Marcas com erro: ${marcasComErro.join(', ')}`);
  }
}

if (require.main === module) {
  montarCatalogoMotos().catch((erro) => {
    console.error('Erro ao montar o catálogo de motos:', erro);
    process.exit(1);
  });
}

module.exports = { montarCatalogoMotos };
