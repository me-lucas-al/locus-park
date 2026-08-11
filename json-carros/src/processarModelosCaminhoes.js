const fs = require('fs');
const path = require('path');

const inputPath = path.join(__dirname, '..', 'catalogo-fipe-caminhoes.json');
const outputPath = path.join(__dirname, '..', 'catalogo-fipe-caminhoes-limpo.json');

const KNOWN_TRUCK_FAMILIES = [
  // Mercedes-Benz
  { match: /\bACTROS\b/i, name: 'Actros' },
  { match: /\bAXOR\b/i, name: 'Axor' },
  { match: /\bATEGO\b/i, name: 'Atego' },
  { match: /\bACCELO\b/i, name: 'Accelo' },
  { match: /\bATRON\b/i, name: 'Atron' },
  { match: /\b1113\b/i, name: '1113' },
  { match: /\b1313\b/i, name: '1313' },
  { match: /\b1513\b/i, name: '1513' },
  { match: /\b1518\b/i, name: '1518' },
  { match: /\b1620\b/i, name: '1620' },
  { match: /\b1935\b/i, name: '1935' },
  { match: /\b1938\b/i, name: '1938' },
  { match: /\b710\b/i, name: '710' },
  { match: /\b608\b/i, name: '608' },
  { match: /\b709\b/i, name: '709' },
  { match: /\b712\b/i, name: '712' },
  { match: /\b914\b/i, name: '914' },
  { match: /\b915\b/i, name: '915' },
  { match: /\b1214\b/i, name: '1214' },
  { match: /\b1218\b/i, name: '1218' },
  { match: /\b1418\b/i, name: '1418' },
  { match: /\b1618\b/i, name: '1618' },
  { match: /\b1718\b/i, name: '1718' },
  { match: /\b2423\b/i, name: '2423' },
  { match: /\b2425\b/i, name: '2425' },
  { match: /\b2428\b/i, name: '2428' },
  { match: /\b2544\b/i, name: '2544' },
  { match: /\b2638\b/i, name: '2638' },
  { match: /\b2644\b/i, name: '2644' },
  { match: /\b2651\b/i, name: '2651' },
  { match: /\b3340\b/i, name: '3340' },
  { match: /\b3344\b/i, name: '3344' },

  // Scania
  { match: /\bS[ÉE]RIE\s*R\b|\bR-?\s*440\b|\bR-?\s*450\b|\bR-?\s*480\b|\bR-?\s*500\b|\bR-?\s*540\b|\bR-?\s*420\b|\bR-?\s*380\b|\bR-?\s*360\b/i, name: 'Série R' },
  { match: /\bS[ÉE]RIE\s*G\b|\bG-?\s*380\b|\bG-?\s*420\b|\bG-?\s*440\b|\bG-?\s*470\b/i, name: 'Série G' },
  { match: /\bS[ÉE]RIE\s*P\b|\bP-?\s*250\b|\bP-?\s*310\b|\bP-?\s*340\b|\bP-?\s*360\b|\bP-?\s*94\b|\bP-?\s*114\b|\bP-?\s*124\b/i, name: 'Série P' },
  { match: /\bS[ÉE]RIE\s*S\b|\bS-?\s*500\b|\bS-?\s*540\b/i, name: 'Série S' },
  { match: /\b112\b/i, name: '112' },
  { match: /\b113\b/i, name: '113' },
  { match: /\b124\b/i, name: '124' },
  { match: /\b142\b/i, name: '142' },
  { match: /\b143\b/i, name: '143' },

  // Volvo
  { match: /\bFH\b/i, name: 'FH' },
  { match: /\bFMX\b/i, name: 'FMX' },
  { match: /\bFM\b/i, name: 'FM' },
  { match: /\bVM\b/i, name: 'VM' },
  { match: /\bNH\b/i, name: 'NH' },
  { match: /\bNL\b/i, name: 'NL' },
  { match: /\bN-?10\b|\bN10\b/i, name: 'N10' },
  { match: /\bN-?12\b|\bN12\b/i, name: 'N12' },

  // Volkswagen
  { match: /\bCONSTELLATION\b/i, name: 'Constellation' },
  { match: /\bE-?DELIVERY\b/i, name: 'e-Delivery' },
  { match: /\bDELIVERY\b/i, name: 'Delivery' },
  { match: /\bMETEOR\b/i, name: 'Meteor' },
  { match: /\bWORKER\b/i, name: 'Worker' },
  { match: /\bTITAN\b/i, name: 'Titan' },

  // Iveco
  { match: /\bDAILY\b/i, name: 'Daily' },
  { match: /\bTECTOR\b/i, name: 'Tector' },
  { match: /\bSTRALIS\b/i, name: 'Stralis' },
  { match: /\bHI-?WAY\b/i, name: 'Hi-Way' },
  { match: /\bS-?WAY\b/i, name: 'S-Way' },
  { match: /\bVERTIS\b/i, name: 'Vertis' },
  { match: /\bEUROCARGO\b/i, name: 'Eurocargo' },
  { match: /\bCURSOR\b/i, name: 'Cursor' },

  // DAF
  { match: /\bXF\b/i, name: 'XF' },
  { match: /\bCF\b/i, name: 'CF' },
  { match: /\bLF\b/i, name: 'LF' },

  // Ford
  { match: /\bCARGO\b/i, name: 'Cargo' },
  { match: /\bF-?4000\b/i, name: 'F-4000' },
  { match: /\bF-?350\b/i, name: 'F-350' },
  { match: /\bF-?12000\b/i, name: 'F-12000' },
  { match: /\bF-?14000\b/i, name: 'F-14000' },
  { match: /\bF-?250\b/i, name: 'F-250' }
];

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

  // Check known truck families first
  for (const family of KNOWN_TRUCK_FAMILIES) {
    if (family.match.test(clean)) {
      return { finalName: family.name, original: rawName };
    }
  }

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
      break;
    }

    baseWords.push(word);

    if (baseWords.length >= 2) {
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
      if (finalName && finalName.length >= 2 && !modelsMap.has(finalName)) {
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
