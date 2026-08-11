const fs = require('fs');
const path = require('path');

const inputPath = path.join(__dirname, '..', 'catalogo-fipe.json');
const outputPath = path.join(__dirname, '..', 'catalogo-fipe-limpo.json');

const KNOWN_CAR_FAMILIES = [
  // Volkswagen
  { match: /\bCROSS\s*FOX\b|\bCROSSFOX\b/i, name: 'CrossFox' },
  { match: /\bSPACE\s*CROSS\b/i, name: 'Space Cross' },
  { match: /\bSPACE\s*FOX\b|\bSPACEFOX\b/i, name: 'SpaceFox' },
  { match: /\bPOLO\s*SEDAN\b/i, name: 'Polo Sedan' },
  { match: /\bPOLO\s*TRACK\b/i, name: 'Polo Track' },
  { match: /\bPOLO\b/i, name: 'Polo' },
  { match: /\bNEW\s*BEETLE\b/i, name: 'New Beetle' },
  { match: /\bFUSCA\b/i, name: 'Fusca' },
  { match: /\bKOMBI\b/i, name: 'Kombi' },
  { match: /\bPARATI\b/i, name: 'Parati' },
  { match: /\bSANTANA\s*QUANTUM\b|\bQUANTUM\b/i, name: 'Quantum' },
  { match: /\bSANTANA\b/i, name: 'Santana' },
  { match: /\bSAVEIRO\b/i, name: 'Saveiro' },
  { match: /\bVOYAGE\b/i, name: 'Voyage' },
  { match: /\bGOLF\s*VARIANT\b/i, name: 'Golf Variant' },
  { match: /\bGOLF\b/i, name: 'Golf' },
  { match: /\bJETTA\s*VARIANT\b/i, name: 'Jetta Variant' },
  { match: /\bJETTA\b/i, name: 'Jetta' },
  { match: /\bPASSAT\s*VARIANT\b/i, name: 'Passat Variant' },
  { match: /\bPASSAT\b/i, name: 'Passat' },
  { match: /\bT-?CROSS\b/i, name: 'T-Cross' },
  { match: /\bTAOS\b/i, name: 'Taos' },
  { match: /\bTIGUAN\b/i, name: 'Tiguan' },
  { match: /\bTOUAREG\b/i, name: 'Touareg' },
  { match: /\bNIVUS\b/i, name: 'Nivus' },
  { match: /\bVIRTUS\b/i, name: 'Virtus' },
  { match: /\bAMAROK\b/i, name: 'Amarok' },
  { match: /\bUP\b|\bUP!\b/i, name: 'Up!' },
  { match: /\bFOX\b/i, name: 'Fox' },
  { match: /\bGOL\b/i, name: 'Gol' },

  // Chevrolet / GM
  { match: /\bONIX\s*PLUS\b/i, name: 'Onix Plus' },
  { match: /\bONIX\b/i, name: 'Onix' },
  { match: /\bPRISMA\b/i, name: 'Prisma' },
  { match: /\bCORSA\s*SEDAN\b/i, name: 'Corsa Sedan' },
  { match: /\bCORSA\s*WAGON\b/i, name: 'Corsa Wagon' },
  { match: /\bCORSA\s*PICK\s*UP\b|\bCORSA\s*PICKUP\b/i, name: 'Corsa Pick-Up' },
  { match: /\bCORSA\b/i, name: 'Corsa' },
  { match: /\bCLASSIC\b/i, name: 'Classic' },
  { match: /\bCELTA\b/i, name: 'Celta' },
  { match: /\bCOBALT\b/i, name: 'Cobalt' },
  { match: /\bCRUZE\s*SPORT6\b/i, name: 'Cruze Sport6' },
  { match: /\bCRUZE\b/i, name: 'Cruze' },
  { match: /\bSPIN\b/i, name: 'Spin' },
  { match: /\bTRACKER\b/i, name: 'Tracker' },
  { match: /\bEQUINOX\b/i, name: 'Equinox' },
  { match: /\bTRAILBLAZER\b/i, name: 'Trailblazer' },
  { match: /\bBLAZER\b/i, name: 'Blazer' },
  { match: /\bS-?10\b|\bS10\b/i, name: 'S10' },
  { match: /\bMONTANA\b/i, name: 'Montana' },
  { match: /\bASTRA\s*SEDAN\b/i, name: 'Astra Sedan' },
  { match: /\bASTRA\b/i, name: 'Astra' },
  { match: /\bVECTRA\s*GT\b/i, name: 'Vectra GT' },
  { match: /\bVECTRA\s*SEDAN\b|\bVECTRA\b/i, name: 'Vectra' },
  { match: /\bZAFIRA\b/i, name: 'Zafira' },
  { match: /\bMERIVA\b/i, name: 'Meriva' },
  { match: /\bAGILE\b/i, name: 'Agile' },
  { match: /\bSONIC\b/i, name: 'Sonic' },
  { match: /\bCAPTIVA\b/i, name: 'Captiva' },
  { match: /\bCAMARO\b/i, name: 'Camaro' },
  { match: /\bCORVETTE\b/i, name: 'Corvette' },
  { match: /\bOPALA\b/i, name: 'Opala' },
  { match: /\bCARAVAN\b/i, name: 'Caravan' },
  { match: /\bCHEVETTE\b/i, name: 'Chevette' },
  { match: /\bKADETT\b/i, name: 'Kadett' },
  { match: /\bIPANEMA\b/i, name: 'Ipanema' },
  { match: /\bMONZA\b/i, name: 'Monza' },
  { match: /\bOMEGA\b/i, name: 'Omega' },
  { match: /\bSUPREMA\b/i, name: 'Suprema' },

  // Fiat
  { match: /\bPALIO\s*WEEKEND\b|\bWEEKEND\b/i, name: 'Palio Weekend' },
  { match: /\bPALIO\b/i, name: 'Palio' },
  { match: /\bGRAND\s*SIENA\b/i, name: 'Grand Siena' },
  { match: /\bSIENA\b/i, name: 'Siena' },
  { match: /\bUNO\s*MILLE\b|\bMILLE\b/i, name: 'Uno Mille' },
  { match: /\bUNO\b/i, name: 'Uno' },
  { match: /\bSTRADA\b/i, name: 'Strada' },
  { match: /\bTORO\b/i, name: 'Toro' },
  { match: /\bMOBI\b/i, name: 'Mobi' },
  { match: /\bARGO\b/i, name: 'Argo' },
  { match: /\bCRONOS\b/i, name: 'Cronos' },
  { match: /\bPULSE\b/i, name: 'Pulse' },
  { match: /\bFASTBACK\b/i, name: 'Fastback' },
  { match: /\bFIORINO\b/i, name: 'Fiorino' },
  { match: /\bDUCATO\b/i, name: 'Ducato' },
  { match: /\bSCUDO\b/i, name: 'Scudo' },
  { match: /\bDOBLO\b|\bDOBLÒ\b/i, name: 'Doblò' },
  { match: /\bIDEA\b/i, name: 'Idea' },
  { match: /\bLINEA\b/i, name: 'Linea' },
  { match: /\bPUNTO\b/i, name: 'Punto' },
  { match: /\bSTILO\b/i, name: 'Stilo' },
  { match: /\bMAREA\s*WEEKEND\b/i, name: 'Marea Weekend' },
  { match: /\bMAREA\b/i, name: 'Marea' },
  { match: /\bBRAVO\b/i, name: 'Bravo' },
  { match: /\bBRAVA\b/i, name: 'Brava' },
  { match: /\bTEMPRA\b/i, name: 'Tempra' },
  { match: /\bTIPO\b/i, name: 'Tipo' },
  { match: /\bFREEMONT\b/i, name: 'Freemont' },
  { match: /\b500\b/i, name: '500' },
  { match: /\bTITANO\b/i, name: 'Titano' },

  // Ford
  { match: /\bECOSPORT\b/i, name: 'EcoSport' },
  { match: /\bFIESTA\s*SEDAN\b/i, name: 'Fiesta Sedan' },
  { match: /\bFIESTA\b/i, name: 'Fiesta' },
  { match: /\bKA\s*SEDAN\b|\bKA\+\b/i, name: 'Ka Sedan' },
  { match: /\bKA\b/i, name: 'Ka' },
  { match: /\bFOCUS\s*SEDAN\b/i, name: 'Focus Sedan' },
  { match: /\bFOCUS\b/i, name: 'Focus' },
  { match: /\bFUSION\b/i, name: 'Fusion' },
  { match: /\bRANGER\b/i, name: 'Ranger' },
  { match: /\bMAVERICK\b/i, name: 'Maverick' },
  { match: /\bBRONCO\s*SPORT\b|\bBRONCO\b/i, name: 'Bronco' },
  { match: /\bTERRITORY\b/i, name: 'Territory' },
  { match: /\bMUSTANG\b/i, name: 'Mustang' },
  { match: /\bEDGE\b/i, name: 'Edge' },
  { match: /\bTRANSIT\b/i, name: 'Transit' },
  { match: /\bESCORT\b/i, name: 'Escort' },
  { match: /\bVERONA\b/i, name: 'Verona' },
  { match: /\bDEL\s*REY\b/i, name: 'Del Rey' },
  { match: /\bCORCEL\b/i, name: 'Corcel' },
  { match: /\bPAMPA\b/i, name: 'Pampa' },
  { match: /\bF-?1000\b/i, name: 'F-1000' },
  { match: /\bF-?250\b/i, name: 'F-250' },

  // Toyota
  { match: /\bCOROLLA\s*CROSS\b/i, name: 'Corolla Cross' },
  { match: /\bCOROLLA\b/i, name: 'Corolla' },
  { match: /\bYARIS\s*CROSS\b/i, name: 'Yaris Cross' },
  { match: /\bYARIS\s*SEDAN\b/i, name: 'Yaris Sedan' },
  { match: /\bYARIS\b/i, name: 'Yaris' },
  { match: /\bETIOS\s*SEDAN\b/i, name: 'Etios Sedan' },
  { match: /\bETIOS\b/i, name: 'Etios' },
  { match: /\bSW4\b|\bHILUX\s*SW4\b/i, name: 'SW4' },
  { match: /\bHILUX\b/i, name: 'Hilux' },
  { match: /\bRAV4\b|\bRAV\s*4\b/i, name: 'RAV4' },
  { match: /\bCAMRY\b/i, name: 'Camry' },
  { match: /\bPRIUS\b/i, name: 'Prius' },
  { match: /\bCOROLLA\s*GR\b|\bGR\s*COROLLA\b/i, name: 'GR Corolla' },
  { match: /\bBANDEIRANTE\b/i, name: 'Bandeirante' },

  // Honda
  { match: /\bCIVIC\b/i, name: 'Civic' },
  { match: /\bCITY\s*SEDAN\b/i, name: 'City Sedan' },
  { match: /\bCITY\s*HATCH\b/i, name: 'City Hatch' },
  { match: /\bCITY\b/i, name: 'City' },
  { match: /\bFIT\b/i, name: 'Fit' },
  { match: /\bHR-?V\b|\bHRV\b/i, name: 'HR-V' },
  { match: /\bCR-?V\b|\bCRV\b/i, name: 'CR-V' },
  { match: /\bWR-?V\b|\bWRV\b/i, name: 'WR-V' },
  { match: /\bZR-?V\b|\bZRV\b/i, name: 'ZR-V' },
  { match: /\bACCORD\b/i, name: 'Accord' },

  // Hyundai
  { match: /\bHB20S\b/i, name: 'HB20S' },
  { match: /\bHB20X\b/i, name: 'HB20X' },
  { match: /\bHB20\b/i, name: 'HB20' },
  { match: /\bCRETA\b/i, name: 'Creta' },
  { match: /\bTUCSON\b/i, name: 'Tucson' },
  { match: /\bIX35\b|\bIX-?35\b/i, name: 'ix35' },
  { match: /\bSANTA\s*FE\b|\bSANTA\s*FÉ\b/i, name: 'Santa Fe' },
  { match: /\bVERACRUZ\b/i, name: 'Veracruz' },
  { match: /\bELANTRA\b/i, name: 'Elantra' },
  { match: /\bAZERA\b/i, name: 'Azera' },
  { match: /\bSONATA\b/i, name: 'Sonata' },
  { match: /\bI30\b|\bI-?30\b/i, name: 'i30' },
  { match: /\bVELOSTER\b/i, name: 'Veloster' },
  { match: /\bHR\b/i, name: 'HR' },

  // Renault
  { match: /\bKWID\b/i, name: 'Kwid' },
  { match: /\bSTEPWAY\b|\bSANDERO\s*STEPWAY\b/i, name: 'Stepway' },
  { match: /\bSANDERO\b/i, name: 'Sandero' },
  { match: /\bLOGAN\b/i, name: 'Logan' },
  { match: /\bOROCH\b|\bDUSTER\s*OROCH\b/i, name: 'Oroch' },
  { match: /\bDUSTER\b/i, name: 'Duster' },
  { match: /\bCAPTUR\b/i, name: 'Captur' },
  { match: /\bKARDIAN\b/i, name: 'Kardian' },
  { match: /\bMEGANE\s*GRAND\s*TOUR\b|\bGRAND\s*TOUR\b/i, name: 'Megane Grand Tour' },
  { match: /\bMEGANE\b/i, name: 'Megane' },
  { match: /\bFLUENCE\b/i, name: 'Fluence' },
  { match: /\bCLIO\s*SEDAN\b/i, name: 'Clio Sedan' },
  { match: /\bCLIO\b/i, name: 'Clio' },
  { match: /\bSCENIC\b|\bSCÉNIC\b/i, name: 'Scenic' },
  { match: /\bSYMBOL\b/i, name: 'Symbol' },
  { match: /\bMASTER\b/i, name: 'Master' },
  { match: /\bKANGOO\b/i, name: 'Kangoo' },

  // Nissan
  { match: /\bKICKS\b/i, name: 'Kicks' },
  { match: /\bVERSA\b/i, name: 'Versa' },
  { match: /\bSENTRA\b/i, name: 'Sentra' },
  { match: /\bMARCH\b/i, name: 'March' },
  { match: /\bFRONTIER\b/i, name: 'Frontier' },
  { match: /\bTIIDA\s*SEDAN\b/i, name: 'Tiida Sedan' },
  { match: /\bTIIDA\b/i, name: 'Tiida' },
  { match: /\bLIVINA\s*GRAND\b|\bGRAND\s*LIVINA\b/i, name: 'Grand Livina' },
  { match: /\bLIVINA\b/i, name: 'Livina' },

  // Jeep
  { match: /\bRENEGADE\b/i, name: 'Renegade' },
  { match: /\bCOMPASS\b/i, name: 'Compass' },
  { match: /\bCOMMANDER\b/i, name: 'Commander' },
  { match: /\bWRANGLER\b/i, name: 'Wrangler' },
  { match: /\bGRAND\s*CHEROKEE\b/i, name: 'Grand Cherokee' },
  { match: /\bCHEROKEE\b/i, name: 'Cherokee' },
  { match: /\bGLADIATOR\b/i, name: 'Gladiator' },

  // Peugeot
  { match: /\b208\b/i, name: '208' },
  { match: /\b2008\b/i, name: '2008' },
  { match: /\b3008\b/i, name: '3008' },
  { match: /\b5008\b/i, name: '5008' },
  { match: /\b207\s*SEDAN\b|\b207\s*PASSION\b/i, name: '207 Passion' },
  { match: /\b207\s*SW\b/i, name: '207 SW' },
  { match: /\b207\b/i, name: '207' },
  { match: /\b206\s*SW\b/i, name: '206 SW' },
  { match: /\b206\b/i, name: '206' },
  { match: /\b307\s*SEDAN\b/i, name: '307 Sedan' },
  { match: /\b307\b/i, name: '307' },
  { match: /\b308\b/i, name: '308' },
  { match: /\b408\b/i, name: '408' },
  { match: /\bPARTNER\b/i, name: 'Partner' },
  { match: /\bEXPERT\b/i, name: 'Expert' },
  { match: /\bBOXER\b/i, name: 'Boxer' },

  // Citroën
  { match: /\bC3\s*AIRCROSS\b/i, name: 'C3 Aircross' },
  { match: /\bC3\b/i, name: 'C3' },
  { match: /\bC4\s*CACTUS\b/i, name: 'C4 Cactus' },
  { match: /\bC4\s*LOUNGE\b/i, name: 'C4 Lounge' },
  { match: /\bC4\s*PALLAS\b/i, name: 'C4 Pallas' },
  { match: /\bGRAND\s*C4\s*PICASSO\b/i, name: 'Grand C4 Picasso' },
  { match: /\bC4\s*PICASSO\b/i, name: 'C4 Picasso' },
  { match: /\bC4\b/i, name: 'C4' },
  { match: /\bC5\b/i, name: 'C5' },
  { match: /\bAIRCROSS\b/i, name: 'Aircross' },
  { match: /\bXSARA\s*PICASSO\b/i, name: 'Xsara Picasso' },
  { match: /\bXSARA\b/i, name: 'Xsara' },
  { match: /\bBERLINGO\b/i, name: 'Berlingo' },
  { match: /\bJUMPY\b/i, name: 'Jumpy' },
  { match: /\bJUMPER\b/i, name: 'Jumper' },

  // BYD
  { match: /\bDOLPHIN\s*MINI\b/i, name: 'Dolphin Mini' },
  { match: /\bDOLPHIN\b/i, name: 'Dolphin' },
  { match: /\bSEALION\s*7\b|\bSEALION\b/i, name: 'Sealion 7' },
  { match: /\bSEAL\b/i, name: 'Seal' },
  { match: /\bSONG\s*PLUS\b/i, name: 'Song Plus' },
  { match: /\bSONG\s*PRO\b/i, name: 'Song Pro' },
  { match: /\bYUAN\s*PLUS\b/i, name: 'Yuan Plus' },
  { match: /\bYUAN\s*PRO\b/i, name: 'Yuan Pro' },
  { match: /\bKING\b/i, name: 'King' },
  { match: /\bSHARK\b/i, name: 'Shark' },
  { match: /\bTAN\b/i, name: 'Tan' },
  { match: /\bHAN\b/i, name: 'Han' },

  // GWM
  { match: /\bHAVAL\s*H6\b|\bH6\b/i, name: 'Haval H6' },
  { match: /\bORA\s*03\b|\bORA\b/i, name: 'Ora 03' },
  { match: /\bTANK\s*300\b|\bTANK\b/i, name: 'Tank 300' },
  { match: /\bPOER\b/i, name: 'Poer' },

  // Caoa Chery
  { match: /\bTIGGO\s*5X\b|\bTIGGO\s*5\b/i, name: 'Tiggo 5X' },
  { match: /\bTIGGO\s*7\b/i, name: 'Tiggo 7' },
  { match: /\bTIGGO\s*8\b/i, name: 'Tiggo 8' },
  { match: /\bTIGGO\s*2\b/i, name: 'Tiggo 2' },
  { match: /\bTIGGO\s*3X\b/i, name: 'Tiggo 3X' },
  { match: /\bARRIZO\s*6\b/i, name: 'Arrizo 6' },
  { match: /\bARRIZO\s*5\b/i, name: 'Arrizo 5' },
  { match: /\bICAR\b/i, name: 'iCar' },
  { match: /\bQQ\b/i, name: 'QQ' },

  // Land Rover
  { match: /\bDISCOVERY\s*SPORT\b/i, name: 'Discovery Sport' },
  { match: /\bDISCOVERY\b/i, name: 'Discovery' },
  { match: /\bRANGE\s*ROVER\s*EVOQUE\b|\bEVOQUE\b/i, name: 'Range Rover Evoque' },
  { match: /\bRANGE\s*ROVER\s*VELAR\b|\bVELAR\b/i, name: 'Range Rover Velar' },
  { match: /\bRANGE\s*ROVER\s*SPORT\b/i, name: 'Range Rover Sport' },
  { match: /\bRANGE\s*ROVER\b/i, name: 'Range Rover' },
  { match: /\bDEFENDER\b/i, name: 'Defender' },
  { match: /\bFREELANDER\b/i, name: 'Freelander' },

  // Volvo
  { match: /\bEX30\b/i, name: 'EX30' },
  { match: /\bEX90\b/i, name: 'EX90' },
  { match: /\bXC40\b/i, name: 'XC40' },
  { match: /\bXC60\b/i, name: 'XC60' },
  { match: /\bXC90\b/i, name: 'XC90' },
  { match: /\bC40\b/i, name: 'C40' },
  { match: /\bC30\b/i, name: 'C30' },
  { match: /\bV40\b/i, name: 'V40' },
  { match: /\bV60\b/i, name: 'V60' },
  { match: /\bS60\b/i, name: 'S60' },
  { match: /\bS90\b/i, name: 'S90' },

  // Mitsubishi
  { match: /\bL200\s*TRITON\b|\bTRITON\b/i, name: 'L200 Triton' },
  { match: /\bL200\b/i, name: 'L200' },
  { match: /\bPAJERO\s*FULL\b/i, name: 'Pajero Full' },
  { match: /\bPAJERO\s*SPORT\b/i, name: 'Pajero Sport' },
  { match: /\bPAJERO\s*DAKAR\b/i, name: 'Pajero Dakar' },
  { match: /\bPAJERO\s*TR4\b|\bTR4\b/i, name: 'Pajero TR4' },
  { match: /\bPAJERO\s*IO\b/i, name: 'Pajero iO' },
  { match: /\bPAJERO\b/i, name: 'Pajero' },
  { match: /\bECLIPSE\s*CROSS\b/i, name: 'Eclipse Cross' },
  { match: /\bECLIPSE\b/i, name: 'Eclipse' },
  { match: /\bASX\b/i, name: 'ASX' },
  { match: /\bOUTLANDER\b/i, name: 'Outlander' },
  { match: /\bLANCER\b/i, name: 'Lancer' }
];

const CAR_STOP_WORDS = new Set([
  '16V', '8V', '24V', '12V', '20V', 'V6', 'V8', 'V12',
  'AUT', 'AUT.', 'MEC', 'MEC.', 'MANUAL', 'AUTOMATICO', 'AUTOMÁTICO',
  'FLEX', 'GASOLINA', 'DIESEL', 'HIBRIDO', 'HÍBRIDO', 'HYBRID', 'ELETRICO', 'ELÉTRICO',
  'TURBO', 'TSI', 'TDI', 'TFSI', 'FSI', 'MPI', 'MSI', 'THP', 'GDI', 'TGDI',
  '4X4', '4X2', 'AWD', 'FWD', 'RWD', '2P', '4P', '3P', '5P',
  'SEDAN', 'HATCH', 'WAGON', 'SW', 'COUPE', 'COUPÉ', 'CABRIOLET',
  'LT', 'LTZ', 'PREMIER', 'RS', 'GTI', 'GTS', 'GLI', 'PLUS', 'SPECIAL',
  'HIGHLINE', 'COMFORTLINE', 'TRENDLINE', 'TRACK', 'DRIVE', 'TREKKING', 'ADVENTURE',
  'ATTRACTIVE', 'ESSENCE', 'SPORTING', 'WAY', 'VOLCANO', 'FREEDOM', 'RANCH', 'ULTRA',
  'LIMITED', 'SPORT', 'LONGITUDE', 'LATITUDE', 'TRAILHAWK', 'OVERLAND',
  'ALLURE', 'GRIFFE', 'FEEL', 'SHINE', 'LIVE', 'INTENSE', 'ZEN', 'ICONIC',
  'EX', 'EXL', 'LX', 'LXS', 'TOURING', 'ALTIS', 'XEI', 'GLI', 'GR-SPORT'
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

function cleanCarModelName(brandName, rawName) {
  let clean = rawName.replace(/\//g, ' ').replace(/\s+/g, ' ').trim();
  clean = clean.replace(/\(novo\)|\(nova\)|\(new\)|\(todas as versões\)|\(modelo antigo\)|\(antigo\)/gi, '').trim();

  // Check known family rules first
  for (const family of KNOWN_CAR_FAMILIES) {
    if (family.match.test(clean)) {
      return { finalName: family.name, original: rawName };
    }
  }

  const brandClean = cleanBrandName(brandName);
  const brandLower = brandClean.toLowerCase();

  // Handle Mercedes-Benz classes: Classe A, Classe C, CLA, GLA, GLC, GLE, GLS, Sprinter
  if (brandLower.includes('mercedes')) {
    const mbMatch = clean.match(/\b(CLASSE\s+[A-Z]|CLA|GLA|GLB|GLC|GLE|GLS|SLC|SLK|SL|CLS|SPRINTER|VITO|ACTROS|AXOR|ATEGO|ACCELO)\b/i);
    if (mbMatch) {
      return { finalName: toTitleCase(mbMatch[0]), original: rawName };
    }
    const cMatch = clean.match(/\b([A-Z]\s*\d{3})\b/i);
    if (cMatch) {
      return { finalName: cMatch[0].toUpperCase().replace(/\s+/g, ' '), original: rawName };
    }
  }

  // Handle BMW: Série 1, Série 3, Série 5, X1, X3, X5, X6, M3, M5, Z4, iX, i3, i4
  if (brandLower.includes('bmw')) {
    const xMatch = clean.match(/\b(X\d|M\d|Z\d|I\d|IX\d?)\b/i);
    if (xMatch) {
      return { finalName: xMatch[0].toUpperCase(), original: rawName };
    }
    const numMatch = clean.match(/\b(\d{3}[a-z]?)\b/i);
    if (numMatch) {
      return { finalName: numMatch[0], original: rawName };
    }
  }

  // Handle Audi: A1, A3, A4, A5, A6, A7, A8, Q3, Q5, Q7, Q8, TT, R8, e-tron
  if (brandLower.includes('audi')) {
    const audiMatch = clean.match(/\b(RS\s*Q?\d|A\d|Q\d|TT|R8|E-TRON)\b/i);
    if (audiMatch) {
      return { finalName: audiMatch[0].toUpperCase().replace(/\s+/g, ' '), original: rawName };
    }
  }

  // Strip brand name from start if present
  const brandWords = brandClean.toUpperCase().split(' ');
  while (true) {
    const firstWord = clean.trim().split(' ')[0].toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (firstWord && brandWords.includes(firstWord)) {
      clean = clean.trim().substring(clean.trim().indexOf(' ') + 1).trim();
    } else {
      break;
    }
  }

  const words = clean.split(' ').filter(w => w.length > 0);
  if (words.length === 0) return { finalName: '' };

  const firstUpper = words[0].toUpperCase();
  if (firstUpper === 'NOVO' || firstUpper === 'NOVA' || firstUpper === 'NEW') {
    words.shift();
  }

  if (words.length === 0) return { finalName: '' };

  const baseWords = [words[0]];
  for (let i = 1; i < words.length; i++) {
    const word = words[i];
    const upper = word.toUpperCase().replace(/[^A-Z0-9-]/g, '');

    if (CAR_STOP_WORDS.has(upper) || /\d\.\d/.test(word) || /^\d+V$/i.test(word)) {
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
      const { finalName } = cleanCarModelName(brandName, model.nomeModelo);
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
  console.log(`Catálogo limpo de carros salvo em: ${outputPath}`);
  console.log(`Total de marcas: ${outputData.length}`);
  console.log(`Total de modelos: ${outputData.reduce((acc, m) => acc + m.modelos.length, 0)}`);
}

if (require.main === module) {
  run();
}

module.exports = { cleanCarModelName, cleanBrandName };
