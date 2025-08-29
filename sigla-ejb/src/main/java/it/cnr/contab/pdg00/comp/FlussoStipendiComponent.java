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
import it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk;
import it.cnr.contab.compensi00.tabrif.bulk.Gruppo_crBulk;
import it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detBulk;
import it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk;
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
        //inserisciStipendiCofiObbScad(userContext, bulk.getStipendiCofiBulk(), bulk.getStipendiCofiObbScadBulks());
        EsitoCori esito = inserisciStipendiCofiCoriCompleto(userContext, bulk.getStipendiCofiBulk(), bulk.getStipendiCofiCoriBulks());

        // Se vuoi bloccare in presenza errori config:
        if (!esito.isOk()) {
            throw new ComponentException(
                    "Configurazione e check incomplete: " +
                            "codiciNonEsistenti=" + esito.codiciNonEsistenti +
                            ", senzaGruppo=" + esito.codiciSenzaGruppo +
                            ", senzaCapitolo=" + esito.codiciSenzaCapitolo +
                            ", senzaTerzo=" + esito.codiciSenzaTerzo
            );
        }
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
                BigDecimal imTotObbAggr = s.getIm_totale().setScale(2, RoundingMode.HALF_UP);

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
                //il metodo merge aggiunge un valore a una mappa, con la possibilità di aggiornare un valore esistente se la chiave è già presente
                obbAggregate.merge(key, new ObligAgrrDTO(cds, esObb, esOri, pgObb, imTotObbAggr), ObligAgrrDTO::updateImpTot);
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
                scadBulk.setEsercizio(stipendiCofi.getEsercizio());
                scadBulk.setMese( stipendiCofi.getMese());
                scadBulk.setCd_cds_obbligazione(obbBulk.getCd_cds_obbligazione());
                scadBulk.setEsercizio_obbligazione(data.getEsObb());
                scadBulk.setEsercizio_ori_obbligazione(data.getEsOri());
                scadBulk.setPg_obbligazione(obbBulk.getPg_obbligazione());
                scadBulk.setIm_totale(data.getImTotObbAggr());
                scadBulk.setToBeCreated();
                super.creaConBulk(userContext, scadBulk);
            }

        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
    }



    /**
     * Implementa TUTTA la logica delle KTR:
     * - checkConfigurazioneCodiciRitenute.ktr  (validazioni e lookup di configurazione)
     * - Stipendi_cofi_cori.ktr                 (group by, calcolo competenza, table output)
     *
     * @param userContext        Contesto utente per le operazioni di persistenza
     * @param testataStipendi    Testata già persistita (ESERCIZIO/MESE/MESE_REALE/TIPO_FLUSSO…)
     * @param righeContributiRitenute Lista righe "cori" da processare (ammontare, codice ritenuta, tipo ente/percipiente)
     * @return                   Report con le anomalie riscontrate durante l'elaborazione
     */

    //il metodo computeIfAbsent garantisce che ci sia sempre un valore per una data chiave. Se la chiave non ha un valore, ne crea uno e lo aggiunge alla mappa prima di restituirlo.
    //("se non esiste, allora crealo e mettilo nella mappa").
    public EsitoCori inserisciStipendiCofiCoriCompleto(
            UserContext userContext,
            Stipendi_cofiBulk testataStipendi,
            List<Stipendi_cofi_coriBulk> righeContributiRitenute
    ) throws ComponentException, RemoteException {

        final EsitoCori esitoElaborazione = new EsitoCori();

        // === Inizializzazione componenti di accesso ai dati ===
        final Stipendi_cofi_coriHome contributiRitenuteHome =
                (Stipendi_cofi_coriHome) getHome(userContext, Stipendi_cofi_coriBulk.class);

        final it.cnr.contab.compensi00.tabrif.bulk.Tipo_contributo_ritenutaHome tipoContributoRitenutaHome =
                (it.cnr.contab.compensi00.tabrif.bulk.Tipo_contributo_ritenutaHome)
                        getHome(userContext, it.cnr.contab.compensi00.tabrif.bulk.Tipo_contributo_ritenutaBulk.class);

        // === Validazione parametri di input obbligatori ===
        validaParametriInput(testataStipendi, righeContributiRitenute);

        // === Calcolo periodo di competenza contabile (KTR: DATE/MONTHEND) ===
        final Integer meseCompetenza = Optional.ofNullable(testataStipendi.getMese_reale())
                .orElse(testataStipendi.getMese());
        if (meseCompetenza == null) {
            throw new ComponentException("Impossibile determinare il mese di competenza: sia mese_reale che mese sono nulli");
        }

        final Integer annoCompetenza = testataStipendi.getEsercizio();
        final java.sql.Timestamp dataInizioCompetenza = firstDayOfMonthTs(annoCompetenza, meseCompetenza);
        final java.sql.Timestamp dataFineCompetenza = lastDayOfMonthTs(annoCompetenza, meseCompetenza);

        // === Aggregazione dati per chiave (GROUP BY: CODICE + TIPO_ENTE_PERCIPIENTE) ===
        // Mappa: "codice|tipo" -> importo totale aggregato
        final Map<String, BigDecimal> contributiAggregatiPerChiave = new LinkedHashMap<>();
        final Map<String, String> codicePerChiave = new HashMap<>();  // cache per evitare split
        final Map<String, String> tipoEntePerChiave = new HashMap<>(); // cache per evitare split

        for (Stipendi_cofi_coriBulk rigaContributo : righeContributiRitenute) {
            if (rigaContributo == null) continue;

            // Normalizza e valida l'importo (scala 2 decimali)
            BigDecimal importoNormalizzato = Optional.ofNullable(rigaContributo.getAmmontare())
                    .orElse(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
            if (importoNormalizzato.compareTo(BigDecimal.ZERO) == 0) continue;

            // Normalizza il codice contributo/ritenuta (trim + uppercase)
            String codiceContributoNormalizzato = Optional.ofNullable(rigaContributo.getCd_contributo_ritenuta())
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .orElse(null);
            if (codiceContributoNormalizzato == null || codiceContributoNormalizzato.isEmpty()) continue;

            // Valida il tipo ente/percipiente (solo "E" o "P" ammessi)
            String tipoEntePercipienteNormalizzato = Optional.ofNullable(rigaContributo.getTi_ente_percipiente())
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .orElse(null);
            if (!Stipendi_cofi_coriBulk.ENTE.equals(tipoEntePercipienteNormalizzato) &&
                    !Stipendi_cofi_coriBulk.PERCIPIENTE.equals(tipoEntePercipienteNormalizzato)) {
                continue; // Scarta righe con tipo non valido
            }

            // Crea la chiave di aggregazione e accumula l'importo
            final String chiaveAggregazione = codiceContributoNormalizzato + "|" + tipoEntePercipienteNormalizzato;
            contributiAggregatiPerChiave.merge(chiaveAggregazione, importoNormalizzato, BigDecimal::add);
            codicePerChiave.putIfAbsent(chiaveAggregazione, codiceContributoNormalizzato);
            tipoEntePerChiave.putIfAbsent(chiaveAggregazione, tipoEntePercipienteNormalizzato);
        }

        if (contributiAggregatiPerChiave.isEmpty()) {
            return esitoElaborazione; // Nessun dato valido da elaborare
        }

        try {
            // === Cache per ottimizzare le query di validazione (evita query ripetute) ===
            final Map<String, Boolean> cacheCodiciEsistenti = new HashMap<>();
            final Map<String, String> cacheGruppiContributi = new HashMap<>();
            final Map<String, String> cacheElementiVoce = new HashMap<>();

            // === Elaborazione con validazioni integrate (un solo loop) ===
            for (Map.Entry<String, BigDecimal> entryAggregazione : contributiAggregatiPerChiave.entrySet()) {
                final String chiaveAggregazione = entryAggregazione.getKey();
                final String codiceContributo = codicePerChiave.get(chiaveAggregazione);
                final String tipoEntePercipiente = tipoEntePerChiave.get(chiaveAggregazione);
                final BigDecimal importoTotaleAggregato = entryAggregazione.getValue();

                // === VALIDAZIONI DI CONFIGURAZIONE (checkConfigurazioneCodiciRitenute.ktr) ===

                // 1) Verifica esistenza codice in TIPO_CONTRIBUTO_RITENUTA
                boolean esisteCodiceContributo = cacheCodiciEsistenti.computeIfAbsent(codiceContributo, codice -> {
                    try {
                        return tipoContributoRitenutaHome.findTipoCORIValido(codice) != null;
                    } catch (PersistencyException e) {
                        return false;
                    }
                });

                if (!esisteCodiceContributo) {
                    esitoElaborazione.codiciNonEsistenti.add(codiceContributo);
                    continue; // Salta alla prossima chiave
                }

                // 2) Verifica gruppo in TIPO_CR_BASE → CD_GRUPPO_CR
                String codiceGruppoContributo = cacheGruppiContributi.computeIfAbsent(codiceContributo, codice -> {
                    try {
                        return loadCdGruppoCrFor(userContext, testataStipendi.getEsercizio(), codice);
                    } catch (ComponentException e) {
                        return null; // Gestisce come gruppo non trovato
                    }
                });

                if (codiceGruppoContributo == null || codiceGruppoContributo.isEmpty()) {
                    esitoElaborazione.codiciSenzaGruppo.add(codiceContributo);
                    continue; // Salta alla prossima chiave
                }

                // 3) Verifica elemento voce in ASS_TIPO_CORI_EV per (ESERCIZIO+CODICE+TI)
                String codiceElementoVoce = cacheElementiVoce.computeIfAbsent(chiaveAggregazione, chiave -> {
                    try {
                        return loadCdElementoVoceFor(userContext, testataStipendi.getEsercizio(),
                                codiceContributo, tipoEntePercipiente);
                    } catch (ComponentException e) {
                        return null; // Gestisce come elemento voce non trovato
                    }
                });

                if (codiceElementoVoce == null || codiceElementoVoce.isEmpty()) {
                    esitoElaborazione.codiciSenzaCapitolo.add(codiceContributo + " [" + tipoEntePercipiente + "]");
                    continue; // Salta alla prossima chiave
                }

                // 4) Verifica dettaglio gruppo → CD_TERZO_VERSAMENTO, modalità pagamento
                Gruppo_cr_detBulk dettaglioGruppoContributo = loadGruppoCr_CrDetFor(
                        userContext, testataStipendi.getEsercizio(), codiceGruppoContributo);

                if (dettaglioGruppoContributo == null || dettaglioGruppoContributo.getCd_terzo_versamento() == null) {
                    esitoElaborazione.codiciSenzaTerzo.add(
                            codiceContributo + " [GRUPPO=" + codiceGruppoContributo + "]");
                    continue; // Salta alla prossima chiave
                }

                // 5) Verifica esistenza TERZO + BANCA associata
                boolean esisteTerzoVersamento = existsTerzo(userContext, dettaglioGruppoContributo.getCd_terzo_versamento());
                boolean esisteBancaTerzoVersamento = (dettaglioGruppoContributo.getPg_banca() != null) &&
                        existsBanca(userContext, dettaglioGruppoContributo.getCd_terzo_versamento(),
                                dettaglioGruppoContributo.getPg_banca());

                if (!esisteTerzoVersamento || !esisteBancaTerzoVersamento) {
                    esitoElaborazione.codiciSenzaTerzo.add(
                            codiceContributo + " [TERZO=" + dettaglioGruppoContributo.getCd_terzo_versamento() +
                                    ", BANCA=" + dettaglioGruppoContributo.getPg_banca() + "]");
                    continue; // Salta alla prossima chiave
                } else {
                    // Registra configurazione valida per reporting
                    esitoElaborazione.codiciTerziModalitaPagamento.add(
                            codiceContributo + " [TERZO=" + dettaglioGruppoContributo.getCd_terzo_versamento() +
                                    ", MP=" + dettaglioGruppoContributo.getCd_modalita_pagamento() + "]");
                }

                // === TUTTE LE VALIDAZIONI SUPERATE: PROCEDE CON INSERIMENTO/AGGIORNAMENTO ===

                // Crea la chiave primaria per verificare esistenza record
                Stipendi_cofi_coriBulk chiavePrimariaRecord = new Stipendi_cofi_coriBulk(
                        testataStipendi.getEsercizio(),
                        testataStipendi.getMese(),
                        codiceContributo,
                        tipoEntePercipiente
                );

                // Verifica se esiste già un record con questa chiave primaria
                Stipendi_cofi_coriBulk recordEsistente;
                try {
                    recordEsistente = (Stipendi_cofi_coriBulk) contributiRitenuteHome.findByPrimaryKey(
                            userContext, chiavePrimariaRecord);
                } catch (ObjectNotFoundException e) {
                    recordEsistente = null; // Record non esistente, verrà creato
                }

                if (recordEsistente != null) {
                    // Al posto dell'aggiornamento, lancia un'eccezione
                    throw new ApplicationException("Modifica/aggiornamento non consentito di un record già presente");
                } else {
                    // INSERIMENTO
                    Stipendi_cofi_coriBulk nuovoRecordContributo = new Stipendi_cofi_coriBulk();
                    nuovoRecordContributo.setStipendi_cofi(testataStipendi); // Collega alla testata
                    nuovoRecordContributo.setCd_contributo_ritenuta(codiceContributo);
                    nuovoRecordContributo.setTi_ente_percipiente(tipoEntePercipiente);
                    nuovoRecordContributo.setAmmontare(importoTotaleAggregato);
                    nuovoRecordContributo.setDt_da_competenza_coge(dataInizioCompetenza);
                    nuovoRecordContributo.setDt_a_competenza_coge(dataFineCompetenza);
                    nuovoRecordContributo.setToBeCreated();
                    super.creaConBulk(userContext, nuovoRecordContributo);
                }

            }

            return esitoElaborazione;

        } catch (PersistencyException ex) {
            throw new ComponentException("Errore durante l'elaborazione dei contributi e ritenute", ex);
        }
    }

    /**
     * Valida i parametri di input obbligatori per l'elaborazione
     */
    private void validaParametriInput(Stipendi_cofiBulk testataStipendi,
                                      List<Stipendi_cofi_coriBulk> righeContributiRitenute)
            throws ComponentException {

        if (testataStipendi == null) {
            throw new ComponentException("Testata stipendi non può essere nulla");
        }
        if (testataStipendi.getEsercizio() == null || testataStipendi.getMese() == null) {
            throw new ComponentException("Esercizio e mese devono essere valorizzati nella testata stipendi");
        }
        if (righeContributiRitenute == null || righeContributiRitenute.isEmpty()) {
            throw new ComponentException("Lista contributi e ritenute non può essere vuota");
        }
    }

    /*
     * METODI DI LOOKUP PER LE VALIDAZIONI DI CONFIGURAZIONE
     * Implementano la catena di validazioni delle KTR:
     * - tipo_contributo_ritenuta (esistenza codice)
     * - tipo_cr_base (→ CD_GRUPPO_CR)
     * - ASS_TIPO_CORI_EV (→ CD_ELEMENTO_VOCE) per (ESERCIZIO,CODICE,TI)
     * - gruppo_cr_det (→ CD_TERZO_VERSAMENTO, PG_BANCA, CD_MODALITA_PAGAMENTO)
     * - terzo (verifica esistenza CD_TERZO)
     * - banca (verifica esistenza conto per CD_TERZO + PG_BANCA)
     */

    /**
     * Carica il codice gruppo dal TIPO_CR_BASE per un dato contributo/ritenuta
     */
    private String loadCdGruppoCrFor(UserContext userContext, Integer esercizio, String codiceContributo)
            throws ComponentException {
        try {
            it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseHome tipoCrBaseHome =
                    (it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseHome)
                            getHome(userContext, it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk.class);

            it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk chiaveTipoCrBase =
                    new it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk();
            chiaveTipoCrBase.setEsercizio(esercizio);
            chiaveTipoCrBase.setCd_contributo_ritenuta(codiceContributo);

            try {
                it.cnr.contab.compensi00.tabrif.bulk.Tipo_cr_baseBulk recordTrovato =
                        (Tipo_cr_baseBulk) tipoCrBaseHome.findByPrimaryKey(userContext, chiaveTipoCrBase);
                return (recordTrovato != null) ? recordTrovato.getCd_gruppo_cr() : null;
            } catch (ObjectNotFoundException e) {
                return null; // Record non trovato
            }
        } catch (PersistencyException e) {
            return null; // Errore di accesso ai dati
        }
    }

    /**
     * Carica il codice elemento voce dall'ASS_TIPO_CORI_EV per un dato contributo/ritenuta e tipo ente
     */
    private String loadCdElementoVoceFor(UserContext userContext, Integer esercizio,
                                         String codiceContributo, String tipoEntePercipiente)
            throws ComponentException {
        try {
            it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evHome assocTipoCoriEvHome =
                    (it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evHome)
                            getHome(userContext, it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk.class);

            it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk chiaveAssociazione =
                    new it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk();
            chiaveAssociazione.setEsercizio(esercizio);
            chiaveAssociazione.setCd_contributo_ritenuta(codiceContributo);
            chiaveAssociazione.setTi_ente_percepiente(tipoEntePercipiente); // NB: tabella usa "percepiente"

            try {
                it.cnr.contab.compensi00.tabrif.bulk.Ass_tipo_cori_evBulk recordTrovato =
                        (Ass_tipo_cori_evBulk) assocTipoCoriEvHome.findByPrimaryKey(userContext, chiaveAssociazione);
                return (recordTrovato != null) ? recordTrovato.getCd_elemento_voce() : null;
            } catch (ObjectNotFoundException e) {
                return null; // Record non trovato
            }
        } catch (PersistencyException e) {
            return null; // Errore di accesso ai dati
        }
    }

    /**
     * Carica i dettagli del gruppo contributi/ritenute (terzo versamento, banca, modalità pagamento)
     */
    private Gruppo_cr_detBulk loadGruppoCr_CrDetFor(UserContext userContext, Integer esercizio, String codiceGruppo) {
        try {
            it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detHome gruppoDettaglioHome =
                    (it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detHome)
                            getHome(userContext, it.cnr.contab.compensi00.tabrif.bulk.Gruppo_cr_detBulk.class);

            // Crea la chiave per il gruppo principale
            Gruppo_crBulk chiaveGruppoPrincipale = new Gruppo_crBulk();
            chiaveGruppoPrincipale.setEsercizio(esercizio);
            chiaveGruppoPrincipale.setCd_gruppo_cr(codiceGruppo);

            try {
                List dettagliGruppo = gruppoDettaglioHome.getDetailsFor(chiaveGruppoPrincipale);
                return (dettagliGruppo != null && !dettagliGruppo.isEmpty()) ?
                        (Gruppo_cr_detBulk) dettagliGruppo.get(0) : null;
            } catch (ObjectNotFoundException e) {
                return null; // Dettaglio non trovato
            }
        } catch (PersistencyException | ComponentException e) {
            return null; // Errore di accesso ai dati
        }
    }

    /**
     * Verifica l'esistenza di un terzo
     */
    private boolean existsTerzo(UserContext userContext, Integer codiceTerzo) {
        if (codiceTerzo == null) return false;

        try {
            TerzoHome terzoHome = (TerzoHome) getHome(userContext, TerzoBulk.class);

            TerzoBulk chiaveTerzo = new TerzoBulk();
            chiaveTerzo.setCd_terzo(codiceTerzo);

            try {
                return terzoHome.findByPrimaryKey(userContext, chiaveTerzo) != null;
            } catch (ObjectNotFoundException e) {
                return false; // Terzo non trovato
            }
        } catch (PersistencyException | ComponentException e) {
            return false; // Errore di accesso ai dati
        }
    }

    /**
     * Verifica l'esistenza di una banca per un dato terzo
     */
    private boolean existsBanca(UserContext userContext, Integer codiceTerzo, Long progressivoBanca) {
        if (codiceTerzo == null || progressivoBanca == null) return false;

        try {
            BancaHome bancaHome = (BancaHome) getHome(userContext, BancaBulk.class);

            BancaBulk chiaveBanca = new BancaBulk();
            chiaveBanca.setCd_terzo(codiceTerzo);
            chiaveBanca.setPg_banca(progressivoBanca);

            try {
                return bancaHome.findByPrimaryKey(chiaveBanca) != null;
            } catch (ObjectNotFoundException e) {
                return false; // Banca non trovata
            }
        } catch (PersistencyException | ComponentException e) {
            return false; // Errore di accesso ai dati
        }
    }


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