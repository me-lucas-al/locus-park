const fs = require('fs');
const path = require('path');

const inputPath = path.join(__dirname, '..', 'catalogo-fipe.json');
const outputPath = path.join(__dirname, '..', 'catalogo-fipe-limpo.json');

// Stop words that indicate versions, engines, body styles, or specs
const STOP_WORDS = new Set([
  // Transmission / Engine Specs
  '16V', '8V', '24V', '12V', '20V', 'V6', 'V8', 'V12', 'L4', 'L6', 'W12',
  'AUT', 'MEC', 'MANUAL', 'AUTOMATICO', 'AUTOMÁTICO', 'MECANICO', 'MECÂNICO',
  'TIPTRONIC', 'DSG', 'CVT', 'S-TRONIC', 'TIPT', 'TIP', 'TRONIC', 'MOTION', 'IMOTION', 'I-MOTION',
  'POWERSHIFT', 'DUALOGIC', 'E-TORQ', 'ETORQ', 'MULTIJET', 'FLEXPOWER', 'MULTIPOWER', 'TRIFUEL', 'TETRAFUEL',
  'TURBO', 'BITURBO', 'BI-TURBO', 'SUPERCHARGER', 'COMPRESSOR', 'TDI', 'TSI', 'TFSI', 'FSI', 'MPI', 'GDI', 'TGDI', 'VTEC', 'MIVEC',
  'MSI', 'SDRIVE', 'XDRIVE', 'E-DRIVE', 'EDRIVE', 'FSIe', 'TGDI',
  
  // Fuel / Traction
  'FLEX', 'GASOLINA', 'DIESEL', 'GASOL', 'DIES', 'D', 'G', 'F', 'HIBRIDO', 'HÍBRIDO', 'HYBRID', 'ELETRICO', 'ELÉTRICO', 'ELECTRIC',
  'EV', 'PHEV', 'MHEV', 'BIOFLEX', '4X4', '4X2', 'AWD', 'FWD', 'RWD', '4WD', 'QUATTRO', 'QUATRO', '2WD', 'SYNCRO',

  // Body Styles / Doors
  'HATCH', 'SEDAN', 'SED', 'WAGON', 'COUPE', 'COUPÉ', 'CONVERSIVEL', 'CONVERSÍVEL', 'CABRIO', 'CABRIOLET', 'SPYDER', 'ROADSTER',
  'AVANT', 'SW', 'SPORTBACK', 'TOURING', 'FURGÃO', 'FURGAO', 'VAN', 'MINIBUS', 'BUS', 'PICK-UP', 'PICKUP', 'CHASSI',
  'CS', 'CD', 'CE', 'CABINE', 'DUPLA', 'SIMPLES', 'ESTENDIDA', 'DOUBLE', 'SINGLE', 'CREW',
  '2P', '4P', '3P', '5P', 'PORTAS', 'DOORS',

  // Common Trim Levels / Versions / FIPE Typos
  'ADVANT', 'ADVANTAGE', 'ADVAN', 'ADV', 'ELEG', 'ELEGANCE', 'ELITE', 'GSI', 'GTS', 'GTI', 'GLD', 'GLI', 'CLD', 'CLI',
  'LS', 'LT', 'LTZ', 'PREMIER', 'RS', 'SS', 'RX', 'RXS', 'RXT', 'RTS', 'RT', 'LX', 'LXS', 'EX', 'EXL', 'EXS', 'DX', 'ELX', 'HLX',
  'SI', 'S', 'SE', 'SEL', 'XLT', 'XLS', 'XL', 'STORM', 'ALLURE', 'GRIFFE', 'ACTIVE', 'FELINE', 'PRESENCE', 'PACK', 'PRESTIGE',
  'STYLE', 'EVOLUTION', 'VISION', 'SENSE', 'LIKE', 'DRIVE', 'WAY', 'TREKKING', 'ADVENTURE', 'SPORTING', 'X-LINE', 'ADVANCE',
  'AMBITION', 'ATTRACTION', 'AMBIENTE', 'L', 'GL', 'GLS', 'GS', 'SL', 'SLE', 'SLX', 'SV', 'SXT', 'R/T', 'SRT', 'SRT8',
  'OVERLAND', 'LATITUDE', 'LONGITUDE', 'TRAILHAWK', 'RUBICON', 'SAHARA', 'SPORT', 'LIMITED', 'EXCLUSIVE', 'EXCELENCE',
  'SIGNATURE', 'LUXURY', 'EXECUTIVE', 'EXEC', 'EXECUTIVA', 'CUSTOM', 'STD', 'SUPER', 'CLUB', 'CITY', 'PLUS', 'TECH',
  'POWER', 'RALLYE', 'OURO', 'BLACK', 'GREY', 'SILVER', 'GOLD', 'PLATINUM', 'WHITE', 'RED', 'BLUE', 'GREEN', 'YELLOW', 'ORANGE',
  'DARK', 'SHADOW', 'NIGHT', 'DYNAMIC', 'DYNAMIQUE', 'DYNAM', 'DYN', 'EXPRESSION', 'AUTHENTIQUE', 'PRIVILÈGE', 'PRIVILEGE',
  'INITIALE', 'BOSE', 'ZEN', 'INTENSE', 'ICONIC', 'LIFE', 'PLAY', 'CONNECT', 'CONNECTED', 'FREEDOM', 'VOLCANO', 'RANCH',
  'ULTRA', 'ENDURANCE', 'WORKING', 'HARD', 'OPENING', 'LAUNCH', 'FOUNDER', 'FOUNDER\'S', 'FIRST', 'HIGH', 'HIGHLINE',
  'TREND', 'TRENDLINE', 'COMFORT', 'COMFORTLINE', 'SPORTLINE', 'TRACK', 'FIELD', 'CROSS', 'OFFROAD', 'TRAIL', 'X-ROAD',
  'LOCKER', 'RUN', 'ATTRACTIVE', 'ESSENCE', 'ABSOLUTE', 'T-JET', 'HGT', 'ABARTH', 'AM', 'ESC', 'ESCOLAR', 'MICROBUS',
  'DE', 'LUXE', 'MIDNIGHT', 'FIFTY', 'GRAPHITE', 'TOURING', 'BLUEMOTION', 'PEPPER', 'PRIME', 'ROCK', 'ROUTE', 'SELEÇÃO', 'SELECAO',
  'CLASS', 'CLASSE', 'PRO', 'PLUS', 'MINI', 'EXPRESS', 'EXPRESS+', 'CARAVELLE', 'INTERMEDIATE', 'GP', 'COMF', 'HIGHLI',
  'ALTIS', 'READY', 'X-WAY', 'XWAY', 'SE-G', 'SEG', 'GR-SPORT', 'GRSPORT',
  'COMPETITION', 'GHIA', 'PURE', 'RECHARGE', 'INSCRIPTION', 'INSCRIPT', 'INSC', 'MOMENTUM', 'MOMENT', 'R-DESIGN', 'RDESIGN',
  'POLESTAR', 'ULTIMATE', 'ULTIM', 'ULTRA', 'KINETIC', 'FIRST',
  'OUTBOUND', 'R-', 'DYN', 'DYNAMIC', 'R-DYNAMIC', 'RDYNAMIC', 'X-DY', 'XDY', 'R-DYN', 'RDYN', 'METROP', 'METROPOLITAN',
  'EDITION', 'EDIT', 'ED', 'AUTO', 'AUTOB', 'AUTOBIO', 'AUTOBIOGRAPHY', 'SUP', 'SUPERC', 'SUPERCHAR', 'SUPERCHARG', 'SUPERCHARGED',
  'L', 'SE', 'HSE', 'S', 'BASE', 'TROPHY', 'SVAUTOBIOGRAPHY', 'PRESTIGETECH', 'ZANZIBAR', 'LONDON',
  'SUNRISE', 'XTREME', 'EXTREME', 'COMFOR', 'HGHI', 'HIGLI', 'SOUND', 'SPIRIT', 'JUNIOR', 'COMODORO', 'DIPLOMATA', 'OPALA',
  'BEV', '4M', '4MATIC', 'AMG', 'LINE', 'EDT', 'PLAT', 'PLATINUM', 'TWIN', 'SKIN', 'PROGRESSIVE',
  'CGI', 'HIG', 'EXT', 'SPORTB', 'SPB', 'SB', 'PERFORMANCE', 'PERF', 'AMBIT', 'PREST', 'PRES'
]);

// Patterns for regex-based checks
const STOP_REGEX_PATTERNS = [
  /\d\.\d/,          // matches any motor designation like 1.0, 2.0, CS2.0, CD2.0, Highline1.6
  /\d+V/i,           // matches valves like 16V, 8V, 12V, 24V
  /\d+CV/i,          // e.g. 115cv, 420cv
  /\d+HP/i,          // e.g. 150hp, 300hp
  /^\d+P$/i,         // e.g. 2p, 4p
  /^\d+L$/i,         // e.g. 17L, 16L
  /^\(E\d\)$/i,      // e.g. (E6), (E5)
  /^[A-Z]-\d+$/i,    // matches engine / version suffixes like G-60, Z-28, SS-10
  /^[A-Z]+\d+[A-Z]*$/i // matches engine / power codes like I6, D300, D350, T8, T5, T6, TD4, TD6, P400e
];

const COMPOUND_KNOWN_MODELS = new Set([
  'GRAND SIENA', 'CROSS FOX', 'PALIO WEEKEND', 'UNO MILLE', 'MILLE FIRE',
  'NOVO UNO', 'NOVO GOL', 'NOVO PALIO', 'NOVA SAVEIRO', 'NOVA STRADA',
  'NOVO FIESTA', 'NOVO VOYAGE', 'GRAND VITARA', 'LAND ROVER',
  'RANGE ROVER', 'PORSCHE 911', 'PEUGEOT 208', 'PEUGEOT 308',
  'PEUGEOT 2008', 'PEUGEOT 3008', 'PEUGEOT 5008', 'TOYOTA HILUX',
  'CHEVROLET S10', 'DEL REY', 'TOWN & COUNTRY', 'DISCOVERY SPORT',
  'RANGE ROVER EVOQUE', 'RANGE ROVER VELAR', 'RANGE ROVER SPORT',
  'HAVAL H6', 'HAVAL H9', 'POER P30',
  'E-CO CARGO', 'E-CO DELIVERY', 'E-CO TRUCK', 'E-CO TECH'
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

function postProcessCapitalization(str) {
  return str
    .replace(/\bRs\b/g, 'RS')
    .replace(/\bGt\b/g, 'GT')
    .replace(/\bGti\b/g, 'GTI')
    .replace(/\bGts\b/g, 'GTS')
    .replace(/\bGli\b/g, 'GLI')
    .replace(/\bC3\b/g, 'C3')
    .replace(/\bC4\b/g, 'C4')
    .replace(/\bC5\b/g, 'C5')
    .replace(/\bM2\b/g, 'M2')
    .replace(/\bM3\b/g, 'M3')
    .replace(/\bM4\b/g, 'M4')
    .replace(/\bM5\b/g, 'M5')
    .replace(/\bM6\b/g, 'M6')
    .replace(/\bBmw\b/g, 'BMW')
    .replace(/\bByd\b/g, 'BYD')
    .replace(/\bVw\b/g, 'VW')
    .replace(/\bGc\b/g, 'GC')
    .replace(/\bGp\b/g, 'GP')
    .replace(/\bXc\b/g, 'XC')
    .replace(/\bFipe\b/g, 'FIPE');
}

function cleanModelName(brandName, rawName) {
  let cleanRaw = rawName;
  
  // Normalize e.co variations (like e.co Cargo, e.coCargo, e.co Delivery)
  cleanRaw = cleanRaw.replace(/\be\.co\s*cargo/gi, 'e-co Cargo');
  cleanRaw = cleanRaw.replace(/\be\.co\s*delivery/gi, 'e-co Delivery');
  cleanRaw = cleanRaw.replace(/\be\.co\s*truck/gi, 'e-co Truck');
  cleanRaw = cleanRaw.replace(/\be\.co\s*tech/gi, 'e-co Tech');
  cleanRaw = cleanRaw.replace(/\be\.co/gi, 'e-co');

  const brandLower = brandName.toLowerCase();
  const brandClean = cleanBrandName(brandName);
  const brandWords = brandClean.toUpperCase().split(' ');
  
  // Remove brand words from the beginning of rawName if present
  while (true) {
    const firstWordOfModel = cleanRaw.trim().split(' ')[0].toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (firstWordOfModel && brandWords.includes(firstWordOfModel)) {
      cleanRaw = cleanRaw.trim().substring(cleanRaw.trim().indexOf(' ') + 1).trim();
    } else {
      break;
    }
  }
  
  if (brandLower.includes('land rover')) {
    // 1. Discovery Sport (first, to prevent matching Disc/Discov alone)
    cleanRaw = cleanRaw.replace(/\bDisc(ov)?(ery)?[ .]*Sp\b\.?/gi, 'Discovery Sport ');
    
    // 2. Discovery (remaining Disc/Discov)
    cleanRaw = cleanRaw.replace(/\bDisc(ov)?\b\.?/gi, 'Discovery ');
    
    // 3. Range Rover
    cleanRaw = cleanRaw.replace(/\bRange[ .]*R\b\.?/gi, 'Range Rover ');
    
    // 4. Evoque
    cleanRaw = cleanRaw.replace(/\bEVOQ\b\.?/gi, 'Evoque ');
    
    // 5. Defender
    cleanRaw = cleanRaw.replace(/\bDef(e)?\b\.?/gi, 'Defender ');
    
    // 6. Velar
    cleanRaw = cleanRaw.replace(/\bVEL\b\.?/gi, 'Velar ');
    
    // 7. Sport
    cleanRaw = cleanRaw.replace(/\bSP\b\.?/gi, 'Sport ');
  }

  // Normalize string: replace slashes and non-decimal dots with spaces
  cleanRaw = cleanRaw.replace(/\//g, ' ');
  cleanRaw = cleanRaw.replace(/(?<!\d)\.|\.(?!\d)/g, ' ');
  cleanRaw = cleanRaw.replace(/\s+/g, ' ').trim();
  
  const words = cleanRaw.split(' ');
  if (words.length === 0) return { finalName: '', isAmbiguous: false, cutReason: '', original: rawName };

  const firstWord = words[0].toUpperCase();
  const baseWords = [words[0]];
  let isAmbiguous = false;
  let cutReason = '';


  
  // Mercedes-Benz/Other Classe A, B, R override
  if (firstWord === 'CLASSE' && words.length > 1) {
    baseWords.push(words[1]);
  }
  // GAC / AION Y, AION V, AION S override
  else if (firstWord === 'AION' && words.length > 1) {
    baseWords.push(words[1]);
  }
  // Chery / Caoa Chery Tiggo/Arrizo rule
  else if ((brandLower.includes('chery') || brandLower.includes('caoa')) && (firstWord === 'TIGGO' || firstWord === 'ARRIZO')) {
    if (words.length > 1) {
      const secondWord = words[1].toUpperCase();
      if (/^\d+[X]?$/.test(secondWord)) {
        baseWords.push(words[1]); // e.g. "Tiggo 5X", "Arrizo 6"
      }
    }
  } 
  // Mercedes-Benz class rule: C 180, A 200, GLA 200
  else if (brandLower.includes('mercedes')) {
    const mercedesClasses = new Set([
      'A', 'B', 'C', 'CL', 'CLA', 'CLK', 'CLS', 'E', 'G', 'GLA', 'GLB', 'GLC', 'GLE', 'GLS',
      'S', 'SL', 'SLC', 'SLK', 'SLS', 'ML', 'V', 'X', 'EQA', 'EQB', 'EQC', 'EQE', 'EQS', 'AMG'
    ]);
    if (mercedesClasses.has(firstWord) && words.length > 1) {
      const secondWord = words[1].toUpperCase();
      if (/^\d+$/.test(secondWord) || secondWord === 'CLASS' || secondWord === 'CLASSE') {
        baseWords.push(words[1]); // e.g. "A 200", "C 180"
      }
    }
  }
  // Citroën C3/C4/C5 models
  else if (brandLower.includes('citro') || brandLower.includes('ds')) {
    if ((firstWord === 'C3' || firstWord === 'C4' || firstWord === 'C5') && words.length > 1) {
      const secondWord = words[1].toUpperCase();
      const citroenCompounds = new Set(['PICASSO', 'LOUNGE', 'CACTUS', 'PALLAS', 'AIRCROSS']);
      if (citroenCompounds.has(secondWord)) {
        baseWords.push(words[1]);
      }
    }
  }
  // Land Rover Range Rover
  else if (brandLower.includes('land') && firstWord === 'RANGE' && words.length > 1 && words[1].toUpperCase() === 'ROVER') {
    baseWords.push(words[1]); // Keep "Range Rover"
    if (words.length > 2) {
      const thirdWord = words[2].toUpperCase();
      const landRoverCompounds = new Set(['EVOQUE', 'VELAR', 'SPORT', 'VOGUE']);
      if (landRoverCompounds.has(thirdWord)) {
        baseWords.push(words[2]); // Keep "Range Rover Evoque", etc.
      }
    }
  }
  // BMW iX/i3/i4/i7 e.g. iX 1, iX 2, iX 3
  else if (brandLower.includes('bmw') && (firstWord === 'IX' || firstWord === 'I3' || firstWord === 'I4' || firstWord === 'I7') && words.length > 1) {
    const secondWord = words[1].toUpperCase();
    if (/^\d+$/.test(secondWord)) {
      baseWords.push(words[1]); // Keep "iX 1", "iX 2"
    }
  }
  // BYD Sealion 7 / Song Pro / Shark GS / King GS
  else if (brandLower.includes('byd')) {
    const bydModels = new Set(['SEALION', 'ATTO', 'SONG', 'SHARK', 'KING', 'DOLPHIN', 'SEAL']);
    if (bydModels.has(firstWord) && words.length > 1) {
      const secondWord = words[1].toUpperCase();
      if (/^\d+$/.test(secondWord) || secondWord === 'PRO' || secondWord === 'PLUS' || secondWord === 'MINI') {
        baseWords.push(words[1]);
      }
    }
  }

  // If we already added words using specific overrides, we can skip processing those indexes
  const startIndex = baseWords.length;

  for (let i = startIndex; i < words.length; i++) {
    const word = words[i];
    
    // Normalize unicode to remove accents (diacritics) e.g. Seleção -> Selecao
    const normalizedWord = word.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    
    // Keep dots for regex checking
    const wordUpperWithDot = normalizedWord.toUpperCase().replace(/[^A-Z0-9-+\.]/g, ''); 
    // Strip dots for direct word checking
    const wordUpperClean = wordUpperWithDot.replace(/\./g, '');

    // Check if the current combination matches a known compound model
    const currentCombination = baseWords.concat(word).join(' ').toUpperCase();
    if (COMPOUND_KNOWN_MODELS.has(currentCombination)) {
      baseWords.push(word);
      continue;
    }

    // Check static stop words
    if (STOP_WORDS.has(wordUpperClean)) {
      break;
    }

    // Check stop patterns
    let matchStop = false;
    for (const pattern of STOP_REGEX_PATTERNS) {
      if (pattern.test(wordUpperWithDot)) {
        matchStop = true;
        cutReason = `matched pattern ${pattern.toString()}`;
        break;
      }
    }

    if (matchStop) {
      break;
    }

    // Heuristics: if it's a number, it's usually engine/version (e.g. Gol 1000, Gol 1.0, Uno 1.6, Haval H6 19)
    if (/^\d+$/.test(wordUpperClean)) {
      if (i === 1) {
        baseWords.push(word);
        continue;
      } else {
        isAmbiguous = true;
        cutReason = `number at index > 1: ${word}`;
        break;
      }
    }

    // BMW "M" override for X5 M / X6 M
    if (wordUpperClean === 'M') {
      const prevWord = baseWords[baseWords.length - 1].toUpperCase();
      if (prevWord === 'X5' || prevWord === 'X6') {
        baseWords.push(word);
        continue;
      }
    }

    // If it's a short alphabetic word (1 to 3 letters) and all uppercase, it might be a version (like SX, EX, S, SL, LT, etc.)
    if (/^[A-Z]{1,3}$/.test(wordUpperClean)) {
      isAmbiguous = true;
      cutReason = `short uppercase: ${word}`;
      break;
    }

    baseWords.push(word);
  }

  const titleCased = toTitleCase(baseWords.join(' '));
  const finalName = postProcessCapitalization(titleCased);
  return {
    finalName,
    isAmbiguous,
    cutReason,
    original: rawName
  };
}

function run() {
  console.log('Iniciando processamento com preservação de pontos para regex...');
  if (!fs.existsSync(inputPath)) {
    console.error(`Arquivo não encontrado em: ${inputPath}`);
    process.exit(1);
  }

  const rawData = JSON.parse(fs.readFileSync(inputPath, 'utf8'));
  const outputData = [];
  const ambiguities = [];

  for (const brandData of rawData) {
    const brandName = cleanBrandName(brandData.nomeMarca);
    const modelsMap = new Map();

    for (const model of brandData.modelos) {
      const { finalName, isAmbiguous, cutReason, original } = cleanModelName(brandName, model.nomeModelo);
      
      if (!modelsMap.has(finalName)) {
        modelsMap.set(finalName, {
          nome: finalName,
          codigoReferencia: model.codigoModelo
        });
      }

      if (isAmbiguous) {
        ambiguities.push({
          marca: brandName,
          original,
          limpo: finalName,
          motivo: cutReason
        });
      }
    }

    outputData.push({
      marca: brandName,
      modelos: Array.from(modelsMap.values())
    });
  }

  fs.writeFileSync(outputPath, JSON.stringify(outputData, null, 2), 'utf8');
  console.log(`Processamento concluído! Catálogo limpo salvo em: ${outputPath}`);
  console.log(`Total de ambiguidades registradas: ${ambiguities.length}`);

  // Save ambiguities to a separate review file
  const reviewPath = path.join(__dirname, '..', 'ambiguidades-revisao.json');
  fs.writeFileSync(reviewPath, JSON.stringify(ambiguities, null, 2), 'utf8');
  console.log(`Ambiguidades salvas em: ${reviewPath}`);
}

if (require.main === module) {
  run();
}

module.exports = {
  cleanModelName,
  cleanBrandName
};

