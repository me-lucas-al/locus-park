const fs = require('fs');
const path = require('path');

const inputPath = path.join(__dirname, '..', 'catalogo-fipe-caminhoes.json');
const outputPath = path.join(__dirname, '..', 'catalogo-fipe-caminhoes-limpo.json');

const TRUCK_STOP_WORDS = new Set([
  'DIESEL', 'GASOLINA', 'ELETRICO', 'ELÉTRICO', 'GAS',
  '4X2', '4X4', '6X2', '6X4', '8X2', '8X4', '10X4',
  '2P', '3P', '4P', 'E5', 'E6', 'EURO3', 'EURO5', 'EURO6',
  'AUT', 'AUT.', 'MEC', 'MEC.', 'MANUAL', 'AUTOMATICO', 'AUTOMÁTICO',
  'LEITO', 'TETO', 'ALTO', 'BAIXO', 'ESTENDIDA', 'SIMPLES', 'DUPLA', 'CURTA',
  'ELETRONICO', 'ELETRÔNICO', 'ELETRONICA', 'ELETRÔNICA'
]);

function toTitleCase(str) {
  return str.toLowerCase().replace(/(?:^|\s|-)\S/g, (match) => match.toUpperCase());
}

function cleanBrandName(brandName) {
  let name = brandName;
  if (name.includes(' - ')) {
    name = name.split(' - ')[1];
  }
  if (name.includes('/')) {
    name = name.split('/')[0];
  }
  return toTitleCase(name.trim());
}

function cleanTruckModelName(brandName, rawName) {
  let clean = rawName.replace(/\//g, ' ').replace(/\s+/g, ' ').trim();
  const brandClean = cleanBrandName(brandName);

  const brandWords = brandClean.toUpperCase().split(' ');
  while (true) {
    const firstWord = clean.trim().split(' ')[0].toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (firstWord && brandWords.includes(firstWord)) {
      clean = clean.trim().substring(clean.trim().indexOf(' ') + 1).trim();
    } else {
      break;
    }
  }

  const words = clean.split(' ');
  if (words.length === 0) return { finalName: '' };

  const baseWords = [];
  for (let i = 0; i < words.length; i++) {
    const word = words[i];
    const upper = word.toUpperCase().replace(/[^A-Z0-9.-]/g, '');

    if (i > 0 && TRUCK_STOP_WORDS.has(upper)) {
      if (baseWords.length >= 2 || (baseWords.length >= 1 && /\d/.test(baseWords[0]))) {
        break;
      }
    }

    baseWords.push(word);

    if (baseWords.length >= 3) {
      break;
    }
  }

  const finalName = toTitleCase(baseWords.join(' '));
  return { finalName, original: rawName };
}

function run() {
  if (!fs.existsSync(inputPath)) {
    console.error(`Arquivo não encontrado em: ${inputPath}`);
    process.exit(1);
  }

  const rawData = JSON.parse(fs.readFileSync(inputPath, 'utf8'));
  const outputData = [];

  for (const brandData of rawData) {
    const brandName = cleanBrandName(brandData.nomeMarca);
    const modelsMap = new Map();

    for (const model of (brandData.modelos || [])) {
      const { finalName } = cleanTruckModelName(brandName, model.nomeModelo);
      if (finalName && !modelsMap.has(finalName)) {
        modelsMap.set(finalName, {
          nome: finalName,
          codigoReferencia: model.codigoModelo
        });
      }
    }

    if (modelsMap.size > 0) {
      outputData.push({
        marca: brandName,
        modelos: Array.from(modelsMap.values())
      });
    }
  }

  fs.writeFileSync(outputPath, JSON.stringify(outputData, null, 2), 'utf8');
  console.log(`Catálogo limpo de caminhões salvo em: ${outputPath}`);
  console.log(`Total de marcas: ${outputData.length}`);
  console.log(`Total de modelos: ${outputData.reduce((acc, m) => acc + m.modelos.length, 0)}`);
}

if (require.main === module) {
  run();
}

module.exports = { cleanTruckModelName, cleanBrandName };
