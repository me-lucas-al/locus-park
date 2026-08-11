const fs = require('fs');
const path = require('path');

const inputPath = path.join(__dirname, '..', 'catalogo-fipe-motos.json');
const outputPath = path.join(__dirname, '..', 'catalogo-fipe-motos-limpo.json');

const MOTO_STOP_WORDS = new Set([
  'FLEX', 'GASOLINA', 'GASOL', 'DIESEL', 'ELETRICA', 'ELÉTRICA', 'ELECTRIC', 'EV',
  'AUT', 'MEC', 'MANUAL', 'AUTOMATICO', 'AUTOMÁTICO',
  'ABS', 'CBS', 'UBS', 'ESD', 'ESDI', 'ES', 'KS', 'ED', 'K', 'E',
  'START', 'FAN', 'TITAN', 'CARGO', 'SPECIAL', 'EDITION', 'LIMITED', 'STD', 'RALLY', 'ADVENTURE',
  'SPORT', 'R', 'RR', 'SP', 'GT', 'SE', 'DX', 'SX', 'S', 'CUSTOM', 'DELUXE', 'CLASSIC',
  'CARBON', 'TOUR', 'TOURING', 'TRIPPER', 'DARK', 'STEALTH', 'BLACK', 'WHITE', 'RED'
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

function cleanMotoModelName(brandName, rawName) {
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
    const upper = word.toUpperCase().replace(/[^A-Z0-9-]/g, '');

    if (i > 0 && MOTO_STOP_WORDS.has(upper)) {
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
      const { finalName } = cleanMotoModelName(brandName, model.nomeModelo);
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
  console.log(`Catálogo limpo de motos salvo em: ${outputPath}`);
  console.log(`Total de marcas: ${outputData.length}`);
  console.log(`Total de modelos: ${outputData.reduce((acc, m) => acc + m.modelos.length, 0)}`);
}

if (require.main === module) {
  run();
}

module.exports = { cleanMotoModelName, cleanBrandName };
