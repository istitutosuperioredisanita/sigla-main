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
import it.cnr.contab.compensi00.tabrif.bulk.*;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceHome;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneHome;
import it.cnr.contab.doccont00.dto.EsitoCori;
import it.cnr.contab.pdg00.cdip.bulk.*;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.ObjectNotFoundException;
import it.cnr.jada.persistency.PersistencyException;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

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
    public GestioneStipBulk gestioneFlussoStipendi(UserContext userContext, GestioneStipBulk bulk)
            throws ComponentException, RemoteException {

        inserisciStipendiCofi(userContext, bulk.getStipendiCofiBulk());
        inserisciStipendiCofiObbScad(userContext, bulk.getStipendiCofiBulk(), bulk.getStipendiCofiObbScadBulks());
        EsitoCori esito = inserisciStipendiCofiCoriCompleto(userContext, bulk.getStipendiCofiBulk(), bulk.getStipendiCofiCoriBulks());

        esito.gestioneNoOk();
        return bulk;
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

        Stipendi_cofiHome home =
                (Stipendi_cofiHome) getHome(userContext, Stipendi_cofiBulk.class);

        try {
            // Validazione input minimi
            if (bulk == null) throw new ComponentException("Stipendi_cofiBulk nullo.");
            if (bulk.getEsercizio() == null) throw new ComponentException("Esercizio non valorizzato (NOT NULL).");
            if (bulk.getMese_reale() == null) throw new ComponentException("Mese_reale non valorizzato (NOT NULL).");
            bulk.setStato(Stipendi_cofiBulk.STATO_NON_LIQUIDATO);
           // Check TIPO_FLUSSO
            String tipoFlusso = bulk.getTipo_flusso();
            if (tipoFlusso == null || (tipoFlusso = tipoFlusso.trim()).isEmpty())
                throw new ComponentException("Tipo_flusso obbligatorio (ammessi C/D).");
            if (!"C".equalsIgnoreCase(tipoFlusso) && !"D".equalsIgnoreCase(tipoFlusso))
                throw new ComponentException("Tipo_flusso non valido: ammessi  C( Collaboratori) o D (Dipendenti).");
            bulk.setTipo_flusso(tipoFlusso.toUpperCase());

            // Se esiste già (ESERCIZIO+MESE), vietiamo la modifica
            if (bulk.getMese() != null) {
                try {
                    //Stipendi_cofiBulk existing = home.findStipendiCofi(bulk); // by PK (ESERCIZIO,MESE)
                    Stipendi_cofiBulk existing = (Stipendi_cofiBulk) home.findByPrimaryKey(userContext, bulk);
                    if (existing != null) {
                        throw new ApplicationException(
                                "Operazione non consentita: la modifica di Stipendi_cofi è vietata"
                        );
                    }
                } catch (ObjectNotFoundException ignore) {
                    // Non esiste: si potrà procedere ad INSERT
                }
            }
            bulk.setProg_flusso(bulk.getMese_reale());
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
    ) throws ComponentException, RemoteException {

        // Validazione iniziale
        if (stipendiCofi == null || stipendiCofi.getEsercizio() == null || stipendiCofi.getMese() == null) {
            throw new ComponentException("Dati della testata stipendi non validi (null, 'esercizio' o 'mese' mancanti).");
        }
        if (!Optional.ofNullable(stipendiCofiObbScadBulks).isPresent()) {
            return;
        }
        Stipendi_cofi_obb_scadHome scadHome = (Stipendi_cofi_obb_scadHome) getHome(userContext, Stipendi_cofi_obb_scadBulk.class);
        Stipendi_cofi_obbHome stipObbHome = (Stipendi_cofi_obbHome) getHome(userContext, Stipendi_cofi_obbBulk.class);
        ObbligazioneHome obbHome = (ObbligazioneHome) getHome(userContext, ObbligazioneBulk.class);
        Map<Stipendi_cofi_obb_scadBulk,Double> stipendiCofiObbTotali= stipendiCofiObbScadBulks.stream().collect(Collectors.groupingBy(o->o,Collectors.summingDouble(o->o.getIm_totale().doubleValue())));

        if (Optional.ofNullable(stipendiCofiObbTotali).isPresent()){
                    for (Stipendi_cofi_obb_scadBulk stipendiCofiObbScadBulk : stipendiCofiObbTotali.keySet()) {
                        ObbligazioneBulk obbligazioneBulk=null;
                        BigDecimal importoTotale = BigDecimal.valueOf(stipendiCofiObbTotali.get(stipendiCofiObbScadBulk).doubleValue());
                        stipendiCofiObbScadBulk.getStipendi_cofi_obb().setCd_cds_obbligazione(normalizeCds(stipendiCofiObbScadBulk.getStipendi_cofi_obb().getCd_cds_obbligazione()));

                        try {
                            obbligazioneBulk = obbHome.findObbligazioneOrd(stipendiCofiObbScadBulk.getStipendi_cofi_obb().getObbligazioni());
                        } catch (  PersistencyException e) {
                        }
                        if ( !Optional.ofNullable(obbligazioneBulk).isPresent()) {
                            throw new ApplicationException(
                                    String.format("Obbligazione con CDS '%s', esercizio '%d' e progressivo '%d' non trovata.", stipendiCofiObbScadBulk.getStipendi_cofi_obb().getCd_cds_obbligazione(),
                                            stipendiCofiObbScadBulk.getStipendi_cofi_obb().getEsercizio_ori_obbligazione(),
                                            stipendiCofiObbScadBulk.getStipendi_cofi_obb().getPg_obbligazione())
                            );
                        }
                        Stipendi_cofi_obbBulk obbBulk =null;
                        try {
                            obbBulk = ( Stipendi_cofi_obbBulk) stipObbHome.findByPrimaryKey(userContext,  stipendiCofiObbScadBulk.getStipendi_cofi_obb());
                        } catch (PersistencyException e) {

                        }
                        if ( !Optional.ofNullable(obbBulk).isPresent()) {
                            stipendiCofiObbScadBulk.getStipendi_cofi_obb().setToBeCreated();
                            super.creaConBulk(userContext, stipendiCofiObbScadBulk.getStipendi_cofi_obb());
                        }

                        stipendiCofiObbScadBulk.setIm_totale(importoTotale);
                        stipendiCofiObbScadBulk.setToBeCreated();
                        super.creaConBulk(userContext, stipendiCofiObbScadBulk);
                    }
        }


    }


    /**
     * Implementa TUTTA la logica delle trasformazioni Pentaho:
     * - checkConfigurazioneCodiciRitenute  (validazioni e lookup di configurazione)
     * - Stipendi_cofi_cori               (group by, calcolo competenza, table output)
     *
     * @param userContext             Contesto utente per le operazioni di persistenza
     * @param testataStipendi         Testata già persistente, inserita precedentemente (ESERCIZIO/MESE/MESE_REALE/TIPO_FLUSSO…)
     * @param righeContributiRitenute Lista righe "cori" da processare (ammontare, codice ritenuta, tipo ente/percipiente)
     * @return Report con le anomalie riscontrate durante l'elaborazione
     */

    //il metodo computeIfAbsent garantisce che ci sia sempre un valore per una data chiave. Se la chiave non ha un valore, ne crea uno e lo aggiunge alla mappa prima di restituirlo.
    //("se non esiste, allora crealo e mettilo nella mappa").
    public EsitoCori inserisciStipendiCofiCoriCompleto(
            UserContext userContext,
            Stipendi_cofiBulk testataStipendi,
            List<Stipendi_cofi_coriBulk> righeContributiRitenute
    ) throws ComponentException, RemoteException {

        EsitoCori esitoElaborazione = new EsitoCori();

        // === Inizializzazione componenti di accesso ai dati ===
        Stipendi_cofi_coriHome contributiRitenuteHome = (Stipendi_cofi_coriHome) getHome(userContext, Stipendi_cofi_coriBulk.class);

        Tipo_contributo_ritenutaHome tipoContributoRitenutaHome = (Tipo_contributo_ritenutaHome) getHome(userContext, Tipo_contributo_ritenutaBulk.class);

        // === Validazione parametri di input obbligatori ===
        validaParametriInput(testataStipendi, righeContributiRitenute);

        // === Calcolo periodo di competenza contabile (KTR: DATE/MONTHEND) ===
        Integer meseCompetenza = Optional.ofNullable(testataStipendi.getMese_reale()).orElse(testataStipendi.getMese());
        if (meseCompetenza == null) {
            throw new ComponentException("Impossibile determinare il mese di competenza: sia mese_reale che mese sono nulli");
        }

        Integer annoCompetenza = testataStipendi.getEsercizio();
        java.sql.Timestamp dataInizioCompetenza = firstDayOfMonthTs(annoCompetenza, meseCompetenza);
        java.sql.Timestamp dataFineCompetenza = lastDayOfMonthTs(annoCompetenza, meseCompetenza);


        Map<Stipendi_cofi_coriBulk,Double> stipendiCofiCoriTotali=  righeContributiRitenute.stream().collect(Collectors.groupingBy(o-> o ,Collectors.summingDouble(o->o.getAmmontare().doubleValue())));
        if (Optional.ofNullable(stipendiCofiCoriTotali).isPresent()) {
            // Mappe che gestiscono le query di validazione

            for (Stipendi_cofi_coriBulk stipendiCofiCoriBulk : stipendiCofiCoriTotali.keySet()) {
                BigDecimal importoTotale = BigDecimal.valueOf(stipendiCofiCoriTotali.get(stipendiCofiCoriBulk).doubleValue());

                try {
                    Tipo_contributo_ritenutaBulk tipoCORIValido = tipoContributoRitenutaHome.findTipoCORIValido(stipendiCofiCoriBulk.getCd_contributo_ritenuta());
                    if ( !Optional.ofNullable(tipoCORIValido).isPresent()) {
                        esitoElaborazione.codiciNonEsistenti.add(stipendiCofiCoriBulk.getCd_contributo_ritenuta());
                        continue; // Salta alla prossima chiave
                    }
                } catch (PersistencyException e) {
                }

                // 2) Verifica gruppo in TIPO_CR_BASE → CD_GRUPPO_CR
                String cdGruppoCodCORI = null;
                try {
                    cdGruppoCodCORI = loadCdGruppoCrFor(userContext, testataStipendi.getEsercizio(), stipendiCofiCoriBulk.getCd_contributo_ritenuta());
                    if ( !Optional.ofNullable(cdGruppoCodCORI).isPresent()) {
                        esitoElaborazione.codiciSenzaGruppo.add(stipendiCofiCoriBulk.getCd_contributo_ritenuta());
                        continue; // Salta alla prossima chiave
                    }
                } catch (PersistencyException e) {
                }

                try {
                    Gruppo_cr_detBulk gruppoCrDetBulk = loadGruppoCr_CrDetFor(userContext, testataStipendi.getEsercizio(), cdGruppoCodCORI);
                    if ( !Optional.ofNullable(gruppoCrDetBulk.getCd_terzo_versamento()).isPresent() || !Optional.ofNullable(gruppoCrDetBulk.getPg_banca()).isPresent() ) {
                        esitoElaborazione.codiciSenzaTerzo.add(
                                stipendiCofiCoriBulk.getCd_contributo_ritenuta() + " [GRUPPO=" + gruppoCrDetBulk.getCd_gruppo_cr() + "]");
                        continue; // Salta alla prossima chiave
                    }
                } catch (PersistencyException e) {
                }

                try {
                    String cdElementoVoce = loadCdElementoVoceFor(userContext, testataStipendi.getEsercizio(), stipendiCofiCoriBulk.getCd_contributo_ritenuta(), stipendiCofiCoriBulk.getTi_ente_percipiente());
                    if(!Optional.ofNullable(cdElementoVoce).isPresent() ) {
                        esitoElaborazione.codiciSenzaCapitolo.add(stipendiCofiCoriBulk.getCd_contributo_ritenuta() + " [" + stipendiCofiCoriBulk.getTi_ente_percipiente() + "]");
                        continue;
                    }
                } catch (PersistencyException e) {
                }

                stipendiCofiCoriBulk.setDt_da_competenza_coge(dataInizioCompetenza);
                stipendiCofiCoriBulk.setDt_a_competenza_coge(dataFineCompetenza);
                stipendiCofiCoriBulk.setToBeCreated();
                super.creaConBulk(userContext, stipendiCofiCoriBulk);

            }
        }

        return esitoElaborazione;


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


    /**
     * Carica il codice gruppo dal TIPO_CR_BASE per un dato contributo/ritenuta
     */
    private String loadCdGruppoCrFor(UserContext userContext, Integer esercizio, String codiceContributo)
            throws PersistencyException {
        try {
            Tipo_cr_baseHome tipoCrBaseHome =
                    (Tipo_cr_baseHome)
                            getHome(userContext, Tipo_cr_baseBulk.class);

            Tipo_cr_baseBulk chiaveTipoCrBase =
                    new Tipo_cr_baseBulk();
            chiaveTipoCrBase.setEsercizio(esercizio);
            chiaveTipoCrBase.setCd_contributo_ritenuta(codiceContributo);

            try {
                Tipo_cr_baseBulk recordTrovato =
                        (Tipo_cr_baseBulk) tipoCrBaseHome.findByPrimaryKey(userContext, chiaveTipoCrBase);
                return (recordTrovato != null) ? recordTrovato.getCd_gruppo_cr() : null;
            } catch (ObjectNotFoundException e) {
                return null; // Record non trovato
            }
        } catch (PersistencyException | ComponentException e) {
            return null; // Errore di accesso ai dati
        }
    }

    /**
     * Carica il codice elemento voce dall'ASS_TIPO_CORI_EV per un dato contributo/ritenuta e tipo ente
     */
    private String loadCdElementoVoceFor(UserContext userContext, Integer esercizio,
                                         String codiceContributo, String tipoEntePercipiente)
            throws PersistencyException {
        try {
            Ass_tipo_cori_evHome assocTipoCoriEvHome =
                    (Ass_tipo_cori_evHome)
                            getHome(userContext, Ass_tipo_cori_evBulk.class);

            try {
                Ass_tipo_cori_evBulk recordTrovato = assocTipoCoriEvHome.getAssCoriEv(esercizio, codiceContributo,
                        Elemento_voceHome.APPARTENENZA_CNR, Elemento_voceHome.GESTIONE_ENTRATE, tipoEntePercipiente);
                return (recordTrovato != null) ? recordTrovato.getCd_elemento_voce() : null;
            } catch (ObjectNotFoundException e) {
                return null; // Record non trovato
            }
        } catch (PersistencyException | ComponentException e) {
            return null; // Errore di accesso ai dati
        }
    }

    /**
     * Carica i dettagli del gruppo contributi/ritenute (terzo versamento, banca, modalità pagamento)
     */
    private Gruppo_cr_detBulk loadGruppoCr_CrDetFor(UserContext userContext, Integer esercizio, String codiceGruppo) throws PersistencyException{
        try {
            Gruppo_cr_detHome gruppoDettaglioHome =
                    (Gruppo_cr_detHome)
                            getHome(userContext, Gruppo_cr_detBulk.class);

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


