/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.pdg00.comp;

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.BancaHome;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoHome;
import it.cnr.contab.compensi00.tabrif.bulk.Gruppo_crBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneHome;
import it.cnr.contab.doccont00.dto.EsitoCori;
import it.cnr.contab.doccont00.dto.ObligAgrrDTO;
import it.cnr.contab.pdg00.cdip.bulk.*;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.ObjectNotFoundException;
import it.cnr.jada.persistency.PersistencyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.*;

import static it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofi_coriBulk.firstDayOfMonthTs;
import static it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofi_coriBulk.lastDayOfMonthTs;

/**
 * Insert the type's description here.
 * Creation date: (08/10/2001 15:39:09)
 *
 * @author: CNRADM
 */
public class FlussoStipendiComponent extends CRUDComponent {
    public FlussoStipendiComponent() {
        super();
    }

    /**
     * Orchestratore: salva testata e poi righe OBB/SCAD.
     */
    public void gestioneFlussoStipendi(UserContext userContext, GestioneStipBulk bulk)
            throws ComponentException, RemoteException {

        inserisciStipendiCofi(userContext, bulk.getStipendiCofiBulk());
        inserisciStipendiCofiObbScad(userContext, bulk.getStipendiCofiBulk(), bulk.getStipendiCofiObbScadBulks());
//        EsitoCori esito = inserisciStipendiCofiCoriCompleto(userContext, bulk.getStipendiCofiBulk(), bulk.getStipendiCofiCoriBulks());
//
//        // Se vuoi bloccare in presenza errori config:
//        if (!esito.isOk()) {
//            throw new ComponentException(
//                    "Configurazione ritenute incompleta: " +
//                            "codiciNonEsistenti=" + esito.codiciNonEsistenti +
//                            ", senzaGruppo=" + esito.codiciSenzaGruppo +
//                            ", senzaCapitolo=" + esito.codiciSenzaCapitolo +
//                            ", senzaTerzo=" + esito.codiciSenzaTerzo
//            );
//        }
    }


    /**
     * Upsert su STIPENDI_COFI.
     * - Default STATO='I'
     * - TIPO_FLUSSO obbligatorio ∈ {C,D}
     * - PROG_FLUSSO = MESE
     * - Se MESE è null → PK assegnata da Home prima dell'insert
     */
    public void inserisciStipendiCofi(UserContext userContext, Stipendi_cofiBulk bulk)
            throws ApplicationException, ComponentException, RemoteException {

        final Stipendi_cofiHome home =
                (Stipendi_cofiHome) getHome(userContext, Stipendi_cofiBulk.class);

        try {
            // Validazione input minimi
            if (bulk == null) throw new ComponentException("Stipendi_cofiBulk nullo.");
            if (bulk.getEsercizio() == null) throw new ComponentException("Esercizio non valorizzato (NOT NULL).");
            if (bulk.getMese_reale() == null) throw new ComponentException("Mese_reale non valorizzato (NOT NULL).");

            // Default STATO
            if (bulk.getStato() == null || bulk.getStato().trim().isEmpty()) {
                bulk.setStato(Stipendi_cofiBulk.STATO_NON_LIQUIDATO);
            }

            // Check TIPO_FLUSSO
            String tipoFlusso = bulk.getTipo_flusso();
            if (tipoFlusso == null || (tipoFlusso = tipoFlusso.trim()).isEmpty())
                throw new ComponentException("Tipo_flusso obbligatorio (ammessi C/D).");
            if (!"C".equalsIgnoreCase(tipoFlusso) && !"D".equalsIgnoreCase(tipoFlusso))
                throw new ComponentException("Tipo_flusso non valido: usare 'C' o 'D'.");
            bulk.setTipo_flusso(tipoFlusso.toUpperCase());

            // Se esiste già (ESERCIZIO+MESE), vietiamo la modifica
            if (bulk.getMese() != null) {
                try {
                    //Stipendi_cofiBulk existing = home.findStipendiCofi(bulk); // by PK (ESERCIZIO,MESE)
                    Stipendi_cofiBulk existing = (Stipendi_cofiBulk) home.findByPrimaryKey(userContext,bulk);
                    if (existing != null) {
                        throw new ApplicationException(
                                "Operazione non consentita: la modifica di Stipendi_cofi è vietata"
                        );
                    }
                } catch (ObjectNotFoundException ignore) {
                    // Non esiste: si potrà procedere ad INSERT
                }
            }

            // INSERT: assicuro PK (MESE), poi PROG_FLUSSO = MESE
            if (bulk.getMese() == null) {
                home.initializePrimaryKeyForInsert(userContext, bulk);
                if (bulk.getMese() == null) {
                    throw new ComponentException("Impossibile assegnare la PK (MESE).");
                }
            }

            bulk.setProg_flusso(bulk.getMese());
            bulk.setToBeCreated();
            super.creaConBulk(userContext, bulk);

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
    }


    /**
     * Inserisce e associa i dati degli stipendi a specifiche obbligazioni.
     * Questo metodo aggrega gli importi, normalizza i codici CDS, verifica l'esistenza
     * delle obbligazioni nel database e garantisce l'inserimento corretto dei dati.
     * La logica è stata ottimizzata per risolvere un problema di persistenza,
     * assicurando che i campi chiave siano impostati direttamente sugli oggetti bulk
     * per un corretto funzionamento dei metodi di ricerca e creazione.
     */
    public void inserisciStipendiCofiObbScad(
            UserContext userContext,
            Stipendi_cofiBulk stipendiCofi,
            List<Stipendi_cofi_obb_scadBulk> stipendiCofiObbScadBulks
    ) throws ApplicationException, ComponentException, RemoteException {

        // Validazione iniziale
        if (stipendiCofi == null || stipendiCofi.getEsercizio() == null || stipendiCofi.getMese() == null) {
            throw new ComponentException("Dati della testata stipendi non validi (null, 'esercizio' o 'mese' mancanti).");
        }
        if (stipendiCofiObbScadBulks == null || stipendiCofiObbScadBulks.isEmpty()) {
            return;
        }

        try {
            final Stipendi_cofi_obb_scadHome scadHome = (Stipendi_cofi_obb_scadHome) getHome(userContext, Stipendi_cofi_obb_scadBulk.class);
            final Stipendi_cofi_obbHome stipObbHome = (Stipendi_cofi_obbHome) getHome(userContext, Stipendi_cofi_obbBulk.class);
            final ObbligazioneHome obbHome = (ObbligazioneHome) getHome(userContext, ObbligazioneBulk.class);

            // Mappa per aggregare gli importi e memorizzare i dati chiave delle obbligazioni
            final Map<String, ObligAgrrDTO> obbAggregate = new LinkedHashMap<>();

            for (Stipendi_cofi_obb_scadBulk s : stipendiCofiObbScadBulks) {
                if (s == null || s.getIm_totale() == null || s.getIm_totale().compareTo(BigDecimal.ZERO) == 0) {
                    continue; // Salta elementi nulli o con importo zero
                }

                // Normalizzazione dei dati
                String cds = normalizeCds(s.getCd_cds_obbligazione());
                Integer esObb = s.getEsercizio_obbligazione();
                Integer esOri = s.getEsercizio_ori_obbligazione();
                Long pgObb = s.getPg_obbligazione();
                BigDecimal totalAmount = s.getIm_totale().setScale(2, RoundingMode.HALF_UP);

                if (cds == null || esObb == null || esOri == null || pgObb == null) {
                    continue;
                }

                // Verifica che l'obbligazione esista prima dell'aggregazione
                ObbligazioneBulk obligationKey = new ObbligazioneBulk(cds, esObb, esOri, pgObb);
                try {
                    obbHome.findObbligazioneOrd(obligationKey);
                } catch (ObjectNotFoundException e) {
                    throw new ApplicationException(
                            String.format("Obbligazione con CDS '%s', esercizio '%d' e progressivo '%d' non trovata.", cds, esObb, pgObb)
                    );
                }

                // Aggrega gli importi per la stessa obbligazione usando il DTO
                final String key = String.join("|", cds, String.valueOf(esObb), String.valueOf(esOri), String.valueOf(pgObb));
                obbAggregate.merge(key, new ObligAgrrDTO(cds, esObb, esOri, pgObb, totalAmount), ObligAgrrDTO::merge);
            }

            if (obbAggregate.isEmpty()) {
                return;
            }

            // Processa i dati aggregati: Pre-check e inserimento/creazione
            for (ObligAgrrDTO data : obbAggregate.values()) {
                // Pre-check: verifica se la riga SCAD da inserire esiste già
                Stipendi_cofi_obb_scadBulk scadCheck = new Stipendi_cofi_obb_scadBulk(stipendiCofi.getEsercizio(), stipendiCofi.getMese(), data.getCds(), data.getEsObb(), data.getEsOri(), data.getPgObb());
                try {
                    if (scadHome.findStipendiCofiObbSacd(scadCheck) != null) {
                        throw new ApplicationException("Modifica/aggiornamento non consentito: riga SCAD esistente.");
                    }
                } catch (ObjectNotFoundException e) {
                    // Comportamento atteso, l'oggetto non esiste.
                }

                // Assicura l'esistenza della riga OBB (find or create)
                Stipendi_cofi_obbBulk obbBulk = new Stipendi_cofi_obbBulk(stipendiCofi.getEsercizio(), data.getCds(), data.getEsObb(), data.getEsOri(), data.getPgObb());
                try {
                    stipObbHome.findByPrimaryKey(userContext, obbBulk);
                } catch (ObjectNotFoundException e) {
                    obbBulk.setToBeCreated();
                    super.creaConBulk(userContext, obbBulk);
                }

                // Inserimento finale della riga SCAD
                Stipendi_cofi_obb_scadBulk scadBulk = new Stipendi_cofi_obb_scadBulk();
                scadBulk.setCd_cds_obbligazione(obbBulk.getCd_cds_obbligazione());
                scadBulk.setPg_obbligazione(obbBulk.getPg_obbligazione());
                scadBulk.setEsercizio(stipendiCofi.getEsercizio());
                scadBulk.setMese( stipendiCofi.getMese());
                scadBulk.setEsercizio_obbligazione(data.getEsObb());
                scadBulk.setEsercizio_ori_obbligazione(data.getEsOri());
                scadBulk.setIm_totale(data.getTotalAmount());
                scadBulk.setToBeCreated();
                super.creaConBulk(userContext, scadBulk);
            }

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
    }



//    /**
//     * Implementa TUTTA la logica delle KTR:
//     * - checkConfigurazioneCodiciRitenute.ktr  (validazioni e lookup di configurazione)
//     * - Stipendi_cofi_cori.ktr                 (group by, calcolo competenza, table output)
//     *
//     * @param userContext      UC
//     * @param stipendiCofi     Testata già persistita (ESERCIZIO/MESE/MESE_REALE/TIPO_FLUSSO…)
//     * @param righe            Lista righe “cori” da processare (ammontare, codice ritenuta, ti ente/percipiente)
//     * @return                 Report con le anomalie riscontrate
//     */
//    public EsitoCori inserisciStipendiCofiCoriCompleto(
//            UserContext userContext,
//            Stipendi_cofiBulk stipendiCofi,
//            List<Stipendi_cofi_coriBulk> righe
//    ) throws ComponentException, RemoteException {
//
//        final EsitoCori esito = new EsitoCori();
//
//        // === Home principali (come nelle altre funzioni) ===
//        final Stipendi_cofi_coriHome coriHome =
//                (Stipendi_cofi_coriHome) getHome(userContext, Stipendi_cofi_coriBulk.class);
//
//        final it.cnr.contab.compensi00.tabrif.bulk.Tipo_contributo_ritenutaHome tipoCRHome =
//                (it.cnr.contab.compensi00.tabrif.bulk.Tipo_contributo_ritenutaHome)
//                        getHome(userContext, it.cnr.contab.compensi00.tabrif.bulk.Tipo_contributo_ritenutaBulk.class);
//
//        // === Validazione input testata/lista (NOT NULL) ===
//        if (stipendiCofi == null) throw new ComponentException("Stipendi_cofiBulk nullo.");
//        if (stipendiCofi.getEsercizio() == null || stipendiCofi.getMese() == null)
//            throw new ComponentException("'esercizio' o 'mese' non valorizzati sulla testata stipendi.");
//        if (righe == null || righe.isEmpty()) return esito;
//
//        // === Mese/Anno di competenza per DT_* (KTR: DATE(esercizio, mese_reale, 1) / MONTHEND) ===
//        final Integer meseCompetenza = Optional.ofNullable(stipendiCofi.getMese_reale())
//                .orElse(stipendiCofi.getMese());
//        if (meseCompetenza == null)
//            throw new ComponentException("Mese di competenza non determinabile (mese_reale e mese null).");
//        final Integer annoCompetenza = stipendiCofi.getEsercizio();
//        final java.sql.Timestamp dtDaCompetenza = firstDayOfMonthTs(annoCompetenza, meseCompetenza);
//        final java.sql.Timestamp dtACompetenza  = lastDayOfMonthTs(annoCompetenza, meseCompetenza);
//
//        // === Normalizzazione + GROUP BY come in Stipendi_cofi_cori.ktr ===
//        // Chiave di aggregazione: ESERCIZIO(testata)/MESE(testata)/CODICE/TI ; Aggregato: SUM(AMMONTARE)
//        final Map<String, BigDecimal> totByKey = new LinkedHashMap<>();
//        final Map<String, String> codiceByKey            = new HashMap<>();
//        final Map<String, String> tiByKey                = new HashMap<>();
//
//        for (Stipendi_cofi_coriBulk r : righe) {
//            if (r == null) continue;
//
//            // -- Importo: default 0 e scala 2 (KTR: SUM BigNumber)
//            java.math.BigDecimal add = Optional.ofNullable(r.getAmmontare())
//                    .orElse(java.math.BigDecimal.ZERO)
//                    .setScale(2, java.math.RoundingMode.HALF_UP);
//            if (add.compareTo(java.math.BigDecimal.ZERO) == 0) continue;
//
//            // -- Codice ritenuta: trim/upper
//            String codice = Optional.ofNullable(r.getCd_contributo_ritenuta())
//                    .map(String::trim).map(String::toUpperCase).orElse(null);
//            if (codice == null || codice.isEmpty()) continue;
//
//            // -- TI_ENTE_PERCIPIENTE: ammetti “E” / “P” (KTR: campo “ente/dip”)
//            String ti = Optional.ofNullable(r.getTi_ente_percipiente())
//                    .map(String::trim).map(String::toUpperCase).orElse(null);
//            if (!Stipendi_cofi_coriBulk.ENTE.equals(ti) && !Stipendi_cofi_coriBulk.PERCIPIENTE.equals(ti)) {
//                continue; // scarto righe con flag non valido
//            }
//
//            final String key = codice + "|" + ti;
//            totByKey.merge(key, add, java.math.BigDecimal::add);
//            codiceByKey.putIfAbsent(key, codice);
//            tiByKey.putIfAbsent(key, ti);
//        }
//
//        if (totByKey.isEmpty()) return esito; // niente da fare
//
//        try {
//            // === VALIDAZIONI DI CONFIGURAZIONE (checkConfigurazioneCodiciRitenute.ktr) ===
//            // Per ogni chiave (codice|ti) valida nell'ordine: esistenza codice → gruppo → capitolo (EV) → terzo/modalità
//
//            // Cache minima per ridurre query ripetute
//            final Map<String, Boolean> cacheCodiceEsiste   = new HashMap<>();
//            final Map<String, String>  cacheGruppoPerCod   = new HashMap<>();   // codice → CD_GRUPPO_CR
//            final Map<String, String>  cacheCapitoloPerKey = new HashMap<>();   // (codice|ti) → CD_ELEMENTO_VOCE
//
//            for (String k : totByKey.keySet()) {
//                final String codice = codiceByKey.get(k);
//                final String ti     = tiByKey.get(k);
//
//                // 1) Esistenza in TIPO_CONTRIBUTO_RITENUTA (KTR: tipo_contributo_ritenuta, switch/case su DS_CONTRIBUTO_RITENUTA)
//                boolean esisteCodice = cacheCodiceEsiste.computeIfAbsent(codice, c -> {
//                    try {
//                        it.cnr.contab.compensi00.tabrif.bulk.Tipo_contributo_ritenutaBulk key =
//                                new it.cnr.contab.compensi00.tabrif.bulk.Tipo_contributo_ritenutaBulk();
//                        key.setCd_contributo_ritenuta(c);
//                        try {
//                            return tipoCRHome.findTipoCORIValido(key.getCd_contributo_ritenuta()) != null;
//                        } catch (ObjectNotFoundException nf) {
//                            return false;
//                        }
//                    } catch (PersistencyException e) {
//                        return false;
//                    }
//                });
//                if (!esisteCodice) {
//                    esito.codiciNonEsistenti.add(codice);
//                    continue;
//                }
//
//                // 2) Lookup in TIPO_CR_BASE per ottenere il gruppo (KTR: tipo_cr_base → CD_GRUPPO_CR, switch/case su null → “SenzaGruppo”)
//                String cdGruppo = cacheGruppoPerCod.computeIfAbsent(codice, c ->
//                        loadCdGruppoCrFor(userContext, stipendiCofi.getEsercizio(), c));
//                if (cdGruppo == null || cdGruppo.isEmpty()) {
//                    esito.codiciSenzaGruppo.add(codice);
//                    continue;
//                }
//
//                // 3) Lookup capitolo/EV (KTR: ASS_TIPO_CORI_EV per chiave ESERCIZIO+CODICE+TI → CD_ELEMENTO_VOCE; null → “SenzaCapitolo”)
//                String cdElementoVoce = cacheCapitoloPerKey.computeIfAbsent(k, kk ->
//                        loadCdElementoVoceFor(userContext, stipendiCofi.getEsercizio(), codice, ti));
//                if (cdElementoVoce == null || cdElementoVoce.isEmpty()) {
//                    esito.codiciSenzaCapitolo.add(codice + " [" + ti + "]");
//                    continue;
//                }
//
//                // 4) Dettaglio gruppo: deve esistere un terzo versamento e la relativa modalità/IBAN (KTR: gruppo_cr → gruppo_cr_det → switch/case su CD_TERZO_VERSAMENTO)
//                GruppoDet det = loadGruppoCrDetFor(userContext, stipendiCofi.getEsercizio(), cdGruppo);
//                if (det == null || det.cdTerzoVersamento == null) {
//                    esito.codiciSenzaTerzo.add(codice + " [GRUPPO=" + cdGruppo + "]");
//                    continue;
//                }
//
//                // 5) Verifica esistenza TERZO + BANCA (KTR: terzo → banca; writer “TerziModalitaPagamento” anche per reporting)
//                boolean okTerzo = existsTerzo(userContext, det.cdTerzoVersamento);
//                boolean okBanca = (det.pgBanca != null) && existsBanca(userContext, det.cdTerzoVersamento, det.pgBanca);
//                if (!okTerzo || !okBanca) {
//                    esito.codiciSenzaTerzo.add(codice + " [TERZO=" + det.cdTerzoVersamento + ", BANCA=" + det.pgBanca + "]");
//                    continue;
//                } else {
//                    esito.codiciTerziModalitaPagamento.add(codice + " [TERZO=" + det.cdTerzoVersamento + ", MP=" + det.cdModalitaPagamento + "]");
//                }
//            }
//
//            // === SCRITTURA (Table output Stipendi_cofi_cori) solo per chiavi che hanno superato i controlli ===
//            for (Map.Entry<String, java.math.BigDecimal> e : totByKey.entrySet()) {
//                final String k      = e.getKey();
//                final String codice = codiceByKey.get(k);
//                final String ti     = tiByKey.get(k);
//
//                // se il codice è ricaduto in una delle anomalie → non scrivo
//                if (esito.codiciNonEsistenti.contains(codice)) continue;
//                if (esito.codiciSenzaGruppo.contains(codice)) continue;
//                if (esito.codiciSenzaCapitolo.contains(codice + " [" + ti + "]")) continue;
//                if (esito.codiciSenzaTerzo.stream().anyMatch(s -> s.startsWith(codice + " "))) continue;
//
//                final java.math.BigDecimal totale = e.getValue().setScale(2, java.math.RoundingMode.HALF_UP);
//
//                // PK (ESERCIZIO testata, MESE testata, CODICE, TI) – KTR: MESE ← progressivo_mese (qui: testata.getMese())
//                Stipendi_cofi_coriBulk stipendiCofiCoriBulkPK = new Stipendi_cofi_coriBulk(
//                        stipendiCofi.getEsercizio(), stipendiCofi.getMese(), codice, ti
//                );
//
//                Stipendi_cofi_coriBulk existing;
//                try {
//                    existing = (Stipendi_cofi_coriBulk) coriHome.findByPrimaryKey(userContext,stipendiCofiCoriBulkPK);
//                } catch (ObjectNotFoundException nf) {
//                    existing = null;
//                }
//
//                if (existing != null) {
//                    // UPDATE ammontare + periodo competenza
//                    existing.setAmmontare(totale);
//                    existing.setDt_da_competenza_coge(dtDaCompetenza);
//                    existing.setDt_a_competenza_coge(dtACompetenza);
//                    existing.setToBeUpdated();
//                    super.modificaConBulk(userContext, existing);
//                } else {
//                    // INSERT nuova riga
//                    Stipendi_cofi_coriBulk nuovo = new Stipendi_cofi_coriBulk();
//                    nuovo.setStipendi_cofi(stipendiCofi);           // collega la testata (ESERCIZIO/MESE)
//                    nuovo.setCd_contributo_ritenuta(codice);
//                    nuovo.setTi_ente_percipiente(ti);
//                    nuovo.setAmmontare(totale);
//                    nuovo.setDt_da_competenza_coge(dtDaCompetenza);
//                    nuovo.setDt_a_competenza_coge(dtACompetenza);
//                    nuovo.setToBeCreated();
//                    super.creaConBulk(userContext, nuovo);
//                }
//            }
//
//            return esito;
//
//        } catch (PersistencyException ex) {
//            throw new ComponentException(ex);
//        }
//    }
//
//
//    /* ====== SEZIONE LOOKUP ======
//       Qui colleghi le tue Home/DAO reali.
//       La KTR fa:
//       - tipo_contributo_ritenuta (già gestito sopra)
//       - tipo_cr_base (→ CD_GRUPPO_CR)
//       - ASS_TIPO_CORI_EV (→ CD_ELEMENTO_VOCE) per (ESERCIZIO,CODICE,TI)
//       - gruppo_cr_det (→ CD_TERZO_VERSAMENTO, PG_BANCA, CD_MODALITA_PAGAMENTO)
//       - terzo        (verifica esistenza CD_TERZO)
//       - banca        (verifica esistenza conto per CD_TERZO + PG_BANCA)
//    */
//    private String loadCdGruppoCrFor(UserContext uc, Integer esercizio, String cdContributo) {
//        try {
//            // Esempio con Home dedicata; se non l’hai, usa SQLBuilder nella tua infrastruttura JADA
//            it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseHome home =
//                    (it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseHome)
//                            getHome(uc, it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk.class);
//
//            it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk key =
//                    new it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk();
//            key.setEsercizio(esercizio);
//            key.setCd_contributo_ritenuta(cdContributo);
//
//            try {
//                it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk row = home.findTipo_cr_base(key);
//                return (row != null) ? row.getCd_gruppo_cr() : null;
//            } catch (ObjectNotFoundException nf) {
//                return null;
//            }
//        } catch (PersistencyException e) {
//            return null;
//        }
//    }
//
//    private String loadCdElementoVoceFor(UserContext uc, Integer esercizio, String cdContributo, String ti) {
//        try {
//            it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evHome home =
//                    (it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evHome)
//                            getHome(uc, it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk.class);
//
//            it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk key =
//                    new it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk();
//            key.setEsercizio(esercizio);
//            key.setCd_contributo_ritenuta(cdContributo);
//            key.setTi_ente_percepiente(ti); // NB: la tabella ha colonna “TI_ENTE_PERCEPIENTE”
//
//            try {
//                it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk row = home.findAss_tipo_cori_ev(key);
//                return (row != null) ? row.getCd_elemento_voce() : null;
//            } catch (ObjectNotFoundException nf) {
//                return null;
//            }
//        } catch (PersistencyException e) {
//            return null;
//        }
//    }
//
//    /** Bean minimale per il dettaglio gruppo CR (gruppo_cr_det). */
//    private static final class GruppoDet {
//        final String  cdTerzoVersamento;
//        final Integer pgBanca;
//        final String  cdModalitaPagamento;
//
//        public String getCdTerzoVersamento() {
//            return cdTerzoVersamento;
//        }
//
//        public Integer getPgBanca() {
//            return pgBanca;
//        }
//
//        public String getCdModalitaPagamento() {
//            return cdModalitaPagamento;
//        }
//
//        GruppoDet(String t, Integer p, String mp) { this.cdTerzoVersamento = t; this.pgBanca = p; this.cdModalitaPagamento = mp; }
//    }
//
//    private GruppoDet loadGruppoCrDetFor(UserContext uc, Integer esercizio, String cdGruppo) {
//        try {
//            it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detHome home =
//                    (it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detHome)
//                            getHome(uc, it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detBulk.class);
//
//            it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detBulk key =
//                    new it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detBulk();
//            key.setEsercizio(esercizio);
//            key.setCd_gruppo_cr(cdGruppo);
//
//            Gruppo_crBulk gruppoCrBulk = new Gruppo_crBulk();
//            gruppoCrBulk.setEsercizio(esercizio);
//            gruppoCrBulk.setCd_gruppo_cr(cdGruppo);
//
//            try {
//                List row = home.getDetailsFor(gruppoCrBulk);
//                GruppoDet gruppoDet = (GruppoDet) row.get(0);
//                if (row == null) return null;
//                return new GruppoDet(gruppoDet.getCdTerzoVersamento(), gruppoDet.getPgBanca(), gruppoDet.getCdModalitaPagamento());
//            } catch (ObjectNotFoundException nf) {
//                return null;
//            }
//        } catch (PersistencyException | ComponentException e) {
//            return null;
//        }
//    }
//
//    private boolean existsTerzo(UserContext uc, String cdTerzo) {
//        if (cdTerzo == null) return false;
//        try {
//            TerzoHome home = (TerzoHome) getHome(uc, TerzoBulk.class);
//
//            TerzoBulk key = new TerzoBulk();
//            key.setCd_terzo(Integer.valueOf(cdTerzo));
//
//            try {
//                return home.findByPrimaryKey(key) != null;
//            } catch (ObjectNotFoundException nf) {
//                return false;
//            }
//        } catch (PersistencyException e) {
//            return false;
//        } catch (ComponentException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private boolean existsBanca(UserContext uc, String cdTerzo, Integer pgBanca) {
//        if (cdTerzo == null || pgBanca == null) return false;
//        try {
//            BancaHome home = (BancaHome) getHome(uc, BancaBulk.class);
//
//            BancaBulk key = new BancaBulk();
//            key.setCd_terzo(Integer.valueOf(cdTerzo));
//            key.setPg_banca(Long.valueOf(pgBanca));
//
//            try {
//                return home.findByPrimaryKey(key) != null;
//            } catch (ObjectNotFoundException nf) {
//                return false;
//            }
//        } catch (PersistencyException e) {
//            return false;
//        } catch (ComponentException e) {
//            throw new RuntimeException(e);
//        }
//    }
//


    /**
     * Converte CDS a 3 cifre se singola cifra (9→999, altrimenti 00d). Non altera gli altri formati.
     */
    private static String normalizeCds(String cds) {
        if (cds == null) return null;
        String s = cds.trim().toUpperCase();
        if (s.length() == 1 && Character.isDigit(s.charAt(0))) {
            char d = s.charAt(0);
            return (d == '9') ? "999" : ("00" + d);
        }
        return s;
    }


}