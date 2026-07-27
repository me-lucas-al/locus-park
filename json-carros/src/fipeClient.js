const FIPE_BASE_URL = 'https://parallelum.com.br/fipe/api/v1';

/**
 * Busca todas as marcas de um tipo de veículo (carros, motos ou caminhoes).
 */
async function buscarMarcas(tipoVeiculo = 'carros') {
  const resposta = await fetch(`${FIPE_BASE_URL}/${tipoVeiculo}/marcas`);

  if (!resposta.ok) {
    throw new Error(`Falha ao buscar marcas (status ${resposta.status})`);
  }

  return resposta.json();
}

/**
 * Busca todos os modelos de uma marca específica.
 */
async function buscarModelosPorMarca(codigoMarca, tipoVeiculo = 'carros') {
  const url = `${FIPE_BASE_URL}/${tipoVeiculo}/marcas/${codigoMarca}/modelos`;
  const resposta = await fetch(url);

  if (!resposta.ok) {
    throw new Error(`Falha ao buscar modelos da marca ${codigoMarca} (status ${resposta.status})`);
  }

  const dados = await resposta.json();
  return dados.modelos;
}

module.exports = { buscarMarcas, buscarModelosPorMarca };
