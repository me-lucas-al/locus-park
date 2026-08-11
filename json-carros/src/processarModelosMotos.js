const fs = require('fs');
const path = require('path');

const inputPath = path.join(__dirname, '..', 'catalogo-fipe-motos.json');
const outputPath = path.join(__dirname, '..', 'catalogo-fipe-motos-limpo.json');

const KNOWN_MOTO_FAMILIES = [
  // Honda
  { match: /\bBIZ\b/i, name: 'Biz' },
  { match: /\bBROS\b/i, name: 'Bros' },
  { match: /\bTWISTER\b/i, name: 'Twister' },
  { match: /\bCG\b/i, name: 'CG' },
  { match: /\bPOP\b/i, name: 'Pop' },
  { match: /\bPCX\b/i, name: 'PCX' },
  { match: /\bELITE\b/i, name: 'Elite' },
  { match: /\bLEAD\b/i, name: 'Lead' },
  { match: /\bADV\b/i, name: 'ADV' },
  { match: /\bHORNET\b/i, name: 'Hornet' },
  { match: /\bFALCON\b/i, name: 'Falcon' },
  { match: /\bSAHARA\b/i, name: 'Sahara' },
  { match: /\bTORNADO\b/i, name: 'Tornado' },
  { match: /\bTRANSALP\b/i, name: 'Transalp' },
  { match: /\bAFRICA\s*TWIN\b/i, name: 'Africa Twin' },
  { match: /\bSHADOW\b/i, name: 'Shadow' },
  { match: /\bXRE\b/i, name: 'XRE' },
  { match: /\bNC\s*750\b/i, name: 'NC 750X' },
  { match: /\bCB\s*300\b/i, name: 'CB 300' },
  { match: /\bCB\s*500\b/i, name: 'CB 500' },
  { match: /\bCB\s*650\b/i, name: 'CB 650' },
  { match: /\bCB\s*1000\b/i, name: 'CB 1000' },
  { match: /\bCBR\s*500\b/i, name: 'CBR 500R' },
  { match: /\bCBR\s*600\b/i, name: 'CBR 600RR' },
  { match: /\bCBR\s*650\b/i, name: 'CBR 650R' },
  { match: /\bCBR\s*1000\b/i, name: 'CBR 1000RR' },
  { match: /\bCRF\b/i, name: 'CRF' },
  { match: /\bSH\s*150\b/i, name: 'SH 150i' },
  { match: /\bSH\s*300\b/i, name: 'SH 300i' },
  
  // Yamaha
  { match: /\bFAZER\b|\bFZ15\b|\bFZ25\b/i, name: 'Fazer' },
  { match: /\bFACTOR\b/i, name: 'Factor' },
  { match: /\bCROSSER\b/i, name: 'Crosser' },
  { match: /\bLANDER\b/i, name: 'Lander' },
  { match: /\bTENERE\b|\bTÉNÉRÉ\b/i, name: 'Ténéré' },
  { match: /\bNMAX\b/i, name: 'NMax' },
  { match: /\bXMAX\b/i, name: 'XMax' },
  { match: /\bFLUO\b/i, name: 'Fluo' },
  { match: /\bNEO\b/i, name: 'Neo' },
  { match: /\bCRYPTON\b/i, name: 'Crypton' },
  { match: /\bMT-?03\b/i, name: 'MT-03' },
  { match: /\bMT-?07\b/i, name: 'MT-07' },
  { match: /\bMT-?09\b/i, name: 'MT-09' },
  { match: /\b(YZF-?)?R15\b/i, name: 'YZF-R15' },
  { match: /\b(YZF-?)?R3\b/i, name: 'YZF-R3' },
  { match: /\b(YZF-?)?R6\b/i, name: 'YZF-R6' },
  { match: /\b(YZF-?)?R1\b/i, name: 'YZF-R1' },
  { match: /\bXT\s*660\b/i, name: 'XT 660' },
  { match: /\bMIDNIGHT\b/i, name: 'Midnight Star' },
  { match: /\bVIRAGO\b/i, name: 'Virago' },
  { match: /\bDRAG\s*STAR\b/i, name: 'Drag Star' },

  // Suzuki / Haojue
  { match: /\bBURGMA[MN]\b/i, name: 'Burgman' },
  { match: /\bINTRUDER\b/i, name: 'Intruder' },
  { match: /\bYES\b/i, name: 'Yes' },
  { match: /\bCHOPPER\s*ROAD\b/i, name: 'Chopper Road' },
  { match: /\bLINDY\b/i, name: 'Lindy' },
  { match: /\bMASTER\s*RIDE\b/i, name: 'Master Ride' },
  { match: /\bDK\s*150\b/i, name: 'DK 150' },
  { match: /\bDR\s*160\b/i, name: 'DR 160' },
  { match: /\bNK\s*150\b/i, name: 'NK 150' },
  { match: /\bBANDIT\b/i, name: 'Bandit' },
  { match: /\bHAYABUSA\b/i, name: 'Hayabusa' },
  { match: /\bV-?STROM\b/i, name: 'V-Strom' },
  { match: /\bBOULEVARD\b/i, name: 'Boulevard' },
  { match: /\bGSX-?R\b/i, name: 'GSX-R' },
  { match: /\bGSX-?S\b/i, name: 'GSX-S' },
  { match: /\bINAZUMA\b/i, name: 'Inazuma' },

  // Kawasaki
  { match: /\bNINJA\b/i, name: 'Ninja' },
  { match: /\bVERSYS\b/i, name: 'Versys' },
  { match: /\bVULCAN\b/i, name: 'Vulcan' },
  { match: /\bELIMINATOR\b/i, name: 'Eliminator' },
  { match: /\bZ-?300\b/i, name: 'Z300' },
  { match: /\bZ-?400\b/i, name: 'Z400' },
  { match: /\bZ-?650\b/i, name: 'Z650' },
  { match: /\bZ-?800\b/i, name: 'Z800' },
  { match: /\bZ-?900\b/i, name: 'Z900' },
  { match: /\bZ-?1000\b/i, name: 'Z1000' },

  // BMW
  { match: /\bG\s*310\s*GS\b/i, name: 'G 310 GS' },
  { match: /\bG\s*310\s*R\b/i, name: 'G 310 R' },
  { match: /\bF\s*750\s*GS\b/i, name: 'F 750 GS' },
  { match: /\bF\s*800\s*GS\b/i, name: 'F 800 GS' },
  { match: /\bF\s*850\s*GS\b/i, name: 'F 850 GS' },
  { match: /\bF\s*900\s*R\b/i, name: 'F 900 R' },
  { match: /\bF\s*900\s*XR\b/i, name: 'F 900 XR' },
  { match: /\bR\s*1200\s*GS\b/i, name: 'R 1200 GS' },
  { match: /\bR\s*1250\s*GS\b/i, name: 'R 1250 GS' },
  { match: /\bR\s*1300\s*GS\b/i, name: 'R 1300 GS' },
  { match: /\bR\s*18\b/i, name: 'R 18' },
  { match: /\bNINET\b|\bNINE\s*T\b/i, name: 'R nineT' },
  { match: /\bS\s*1000\s*RR\b/i, name: 'S 1000 RR' },
  { match: /\bS\s*1000\s*R\b/i, name: 'S 1000 R' },
  { match: /\bS\s*1000\s*XR\b/i, name: 'S 1000 XR' },

  // Royal Enfield
  { match: /\bHUNTER\b/i, name: 'Hunter 350' },
  { match: /\bSUPER\s*METEOR\b/i, name: 'Super Meteor 650' },
  { match: /\bMETEOR\b/i, name: 'Meteor 350' },
  { match: /\bCLASSIC\s*350\b/i, name: 'Classic 350' },
  { match: /\bHIMALAYAN\b/i, name: 'Himalayan' },
  { match: /\bINTERCEPTOR\b/i, name: 'Interceptor 650' },
  { match: /\bCONTINENTAL\b/i, name: 'Continental GT' },
  { match: /\bSCRAM\b/i, name: 'Scram 411' },
  { match: /\bSHOTGUN\b/i, name: 'Shotgun 650' },

  // Triumph
  { match: /\bSTREET\s*TRIPLE\b/i, name: 'Street Triple' },
  { match: /\bSPEED\s*TRIPLE\b/i, name: 'Speed Triple' },
  { match: /\bSPEED\s*400\b/i, name: 'Speed 400' },
  { match: /\bSCRAMBLER\s*400\b/i, name: 'Scrambler 400X' },
  { match: /\bTIGER\s*900\b/i, name: 'Tiger 900' },
  { match: /\bTIGER\s*1200\b/i, name: 'Tiger 1200' },
  { match: /\bTIGER\s*800\b/i, name: 'Tiger 800' },
  { match: /\bTIGER\s*660\b/i, name: 'Tiger Sport 660' },
  { match: /\bTIGER\b/i, name: 'Tiger' },
  { match: /\bBONNEVILLE\b/i, name: 'Bonneville' },
  { match: /\bROCKET\b/i, name: 'Rocket 3' },

  // Harley-Davidson
  { match: /\bFAT\s*BOY\b/i, name: 'Fat Boy' },
  { match: /\bFAT\s*BOB\b/i, name: 'Fat Bob' },
  { match: /\bIRON\s*883\b|\bIRON\b/i, name: 'Iron 883' },
  { match: /\bSPORTSTER\b/i, name: 'Sportster' },
  { match: /\bHERITAGE\b/i, name: 'Heritage Classic' },
  { match: /\bROAD\s*KING\b/i, name: 'Road King' },
  { match: /\bSTREET\s*GLIDE\b/i, name: 'Street Glide' },
  { match: /\bROAD\s*GLIDE\b/i, name: 'Road Glide' },
  { match: /\bBREAKOUT\b/i, name: 'Breakout' },
  { match: /\bLOW\s*RIDER\b/i, name: 'Low Rider' },
  { match: /\bNIGHTSTER\b/i, name: 'Nightster' },
  { match: /\bPAN\s*AMERICA\b/i, name: 'Pan America' },

  // Ducati
  { match: /\bMONSTER\b/i, name: 'Monster' },
  { match: /\bPANIGALE\b/i, name: 'Panigale' },
  { match: /\bMULTISTRADA\b/i, name: 'Multistrada' },
  { match: /\bSCRAMBLER\b/i, name: 'Scrambler' },
  { match: /\bDIAVEL\b/i, name: 'Diavel' },
  { match: /\bHYPERMOTARD\b/i, name: 'Hypermotard' },
  { match: /\bSTREETFIGHTER\b/i, name: 'Streetfighter' },
  { match: /\bDESERTX\b/i, name: 'DesertX' },

  // Shineray
  { match: /\bPHOENIX\b/i, name: 'Phoenix' },
  { match: /\bJET\b/i, name: 'Jet' },
  { match: /\bWORKER\b/i, name: 'Worker' },
  { match: /\bRIO\b/i, name: 'Rio' },
  { match: /\bSH\s*50\b|\bXY\s*50\b/i, name: '50cc' }
];

const MOTO_STOP_WORDS = new Set([
  'FLEX', 'GASOLINA', 'GASOL', 'DIESEL', 'ELETRICA', 'ELÉTRICA', 'ELECTRIC', 'EV',
  'AUT', 'MEC', 'MANUAL', 'AUTOMATICO', 'AUTOMÁTICO',
  'ABS', 'CBS', 'UBS', 'ESD', 'ESDI', 'ES', 'KS', 'ED', 'K', 'E', 'EX',
  'START', 'FAN', 'TITAN', 'CARGO', 'SPECIAL', 'EDITION', 'LIMITED', 'STD', 'RALLY', 'ADVENTURE',
  'SPORT', 'R', 'RR', 'SP', 'GT', 'SE', 'DX', 'SX', 'S', 'CUSTOM', 'DELUXE', 'CLASSIC',
  'CARBON', 'TOUR', 'TOURING', 'TRIPPER', 'DARK', 'STEALTH', 'BLACK', 'WHITE', 'RED',
  '100', '110', '110I', '125', '125I', '150', '160', '190', '200', '250', '300', '400', '500', '600', '650', '750', '800', '850', '900', '1000', '1100', '1200', '1250', '1300'
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

  // Check known moto families first
  for (const family of KNOWN_MOTO_FAMILIES) {
    if (family.match.test(clean)) {
      return { finalName: family.name, original: rawName };
    }
  }

  // Remove brand name from start if present
  const brandWords = brandClean.toUpperCase().split(' ');
  while (true) {
    const parts = clean.trim().split(' ');
    const firstWord = parts[0].toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (firstWord && brandWords.includes(firstWord)) {
      if (parts.length === 1) break;
      clean = parts.slice(1).join(' ').trim();
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
