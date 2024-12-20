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

package it.cnr.contab.config00.bulk;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Configurazione_cnrBulk extends Configurazione_cnrBase {

    public final static String SK_SDI = "SDI";
    public final static String PK_EMAIL_PEC = "EMAIL_PEC";
    public final static String PK_TERZO_SPECIALE = "TERZO_SPECIALE";
    public final static String PK_BLOCCO_DETRAZIONI = "BLOCCO_DETRAZIONI";
    /**
     * VAL01 Y -> presente l'economico patrimoniale parallela, N -> presente l'economico patrimoniale parallela
     * VAL02 Y -> blocco scritture proposte, N -> è possibile modificare le scritture
     */
    public final static String PK_ECONOMICO_PATRIMONIALE = "ECONOMICO_PATRIMONIALE";
    public final static String SK_TIPO_ECONOMICO_PATRIMONIALE = "TIPO_ECONOMICO_PATRIMONIALE";
    public final static String SK_ASSOCIAZIONE_CONTI = "ASSOCIAZIONE_CONTI";

    public final static String PK_TRATTAMENTO_SPECIALE = "TIPO_TRATTAMENTO_SPECIALE";
    public final static String SK_TRATTAMENTO_STIPENDI = "TRATTAMENTO_STIPENDI";

    public final static String SK_IMPUTAZIONE_MANUALE = "IMPUTAZIONE_MANUALE";

    public final static String SK_CODICE_DIVERSI_PGIRO = "CODICE_DIVERSI_PGIRO";
    public final static String SK_CODICE_DIVERSI_IMPEGNI = "CODICE_DIVERSI_IMPEGNI";
    public final static String PK_OBBLIGATORIETA_ORDINI = "OBBLIGATORIETA_ORDINI";
    public final static String PK_PARAMETRI_ORDINI = "PARAMETRI_ORDINI";
    public final static String SK_DIVERSI_STIPENDI = "DIVERSI_STIPENDI";

    public final static String PK_RIDUZIONE_CUNEO_DL_3_2020 = "RIDUZIONE_CUNEO_DL_3_2020";
    public final static String SK_DATA_INIZIO = "DATA_INIZIO";
    public final static String PK_LINEA_ATTIVITA_SPECIALE = "LINEA_ATTIVITA_SPECIALE";
    public final static String SK_LINEA_COMUNE_VERSAMENTO_IVA = "LINEA_COMUNE_VERSAMENTO_IVA";

    public final static String PK_BOLLO_VIRTUALE = "BOLLO_VIRTUALE";
    public final static String SK_BOLLO_VIRTUALE_CODICE_FATTURA_ATTIVA = "CODICE_DOCUMENTO_FATTURA_ATTIVA";


    public final static String SK_BOLLO_VIRTUALE_IMPORTO_LIMITE = "IMPORTO_LIMITE";

    public final static String SK_GG_DT_PREV_CONSEGNA = "GIORNI_DATA_PREVISTA_CONSEGNA";

    public final static String SK_PROGETTO_RICHIESTA = "PROGETTO_RICHIESTA";
    public final static String SK_GAE_RICHIESTA = "GAE_RICHIESTA";
    public final static String SK_VOCE_RICHIESTA = "VOCE_RICHIESTA";

    public final static String SK_LINEA_ATTIVITA_ENTRATA_ENTE = "LINEA_ATTIVITA_ENTRATA_ENTE";
    public final static String SK_LINEA_ATTIVITA_SPESA_ENTE = "LINEA_ATTIVITA_SPESA_ENTE";
    public final static String SK_COSTO_DOC_NON_LIQUIDABILE = "COSTO_DOC_NON_LIQUIDABILE";

    public final static String PK_CD_DIVISA = "CD_DIVISA";
    public final static String SK_EURO = "EURO";

    public final static String PK_CDS_SPECIALE = "CDS_SPECIALE";
    public final static String SK_CDS_SAC = "CDS_SAC";
    public final static String PK_ELEMENTO_VOCE_SPECIALE = "ELEMENTO_VOCE_SPECIALE";
    public final static String SK_VOCE_IVA_FATTURA_ESTERA = "VOCE_IVA_FATTURA_ESTERA";
    public final static String SK_NETTO_DA_TRATTENERE = "NETTO_DA_TRATTENERE";
    public final static String SK_TEMPO_IND_SU_PROGETTI_FINANZIATI = "TEMPO_IND_SU_PROGETTI_FINANZIATI";

    public final static String PK_BENE_SERVIZIO_SPECIALE = "BENE_SERVIZIO_SPECIALE";
    public final static String SK_SCONTO_ABBUONO = "SCONTO_ABBUONO";

    public final static String PK_CONTO_CORRENTE_SPECIALE = "CONTO_CORRENTE_SPECIALE";
    public final static String SK_ENTE = "ENTE";
    public final static String SK_BANCA_ITALIA = "BANCA_ITALIA";

    public final static String PK_UO_SPECIALE = "UO_SPECIALE";
    public final static String SK_UO_ACCREDITAMENTO_SAC = "UO_ACCREDITAMENTO_SAC";
    public final static String SK_UO_DISTINTA_TUTTA_SAC = "UO_DISTINTA_TUTTA_SAC";
    public final static String SK_UO_RAGIONERIA = "UO_RAGIONERIA";
    public final static String SK_UO_VERSAMENTO_IVA = "UO_VERSAMENTO_IVA";

    public final static String PK_PDG_VARIAZIONE = "PDG_VARIAZIONE";
    public final static String SK_TIPO_VAR_APPROVA_CDS = "TIPO_VAR_APPROVA_CDS";

    public final static String PK_VAR_STANZ_RES = "VAR_STANZ_RES";
    public final static String SK_TIPO_VAR_APPROVA_CNR = "TIPO_VAR_APPROVA_CNR";

    public final static String PK_CDR_SPECIALE = "CDR_SPECIALE";
    public final static String SK_CDR_PERSONALE = "CDR_PERSONALE";
    public final static String SK_CDR_SERVIZIO_ENTE = "CDR_SERVIZIO_ENTE";

    public final static String PK_FATTURAZIONE_ELETTRONICA = "FATTURAZIONE_ELETTRONICA";
    public final static String SK_DOMINIO_EMAIL = "DOMINIO_EMAIL";

    public final static String SK_MAIL_REFERENTE_TECNICO = "MAIL_REFERENTE_TECNICO";
    public final static String SK_TELEFONO_REFERENTE_TECNICO = "TELEFONO_REFERENTE_TECNICO";
    public final static String SK_ATTIVA = "ATTIVA";
    public final static String SK_PASSIVA = "PASSIVA";
    public final static String SK_PASSIVA_PROF = "PASSIVA_PROF";


    public final static String PK_CODICE_SIOPE_DEFAULT = "CODICE_SIOPE_DEFAULT";
    public final static String SK_MANDATO_ACCREDITAMENTO = "MANDATO_ACCREDITAMENTO";
    public final static String SK_REVERSALE_TRASFERIMENTO = "REVERSALE_TRASFERIMENTO";

    public final static String PK_LIMITE_COLL_MERAMENTE_OCCASIONALI = "LIMITE_COLL_MERAMENTE_OCCASIONALI";

    public final static String PK_COSTANTI = "COSTANTI";
    public final static String SK_TOTALE_GIORNI_LAVORATIVI_COSTI_PERSONALE = "TOTALE_GIORNI_LAVORATIVI_COSTI_PERSONALE";

    public final static String SK_CODICE_ANAG_ENTE = "CODICE_ANAG_ENTE";

    public final static String PK_ANNI_RESIDUI_VAR_ST_RES = "ANNI_RESIDUI_VAR_ST_RES";
    public final static String PK_ANNI_RESIDUI_IM_RES_IMP = "ANNI_RESIDUI_IM_RES_IMP";
    public final static String PK_ANNI_RESIDUI_IM_RES_PRO = "ANNI_RESIDUI_IM_RES_PRO";
    public final static String SK_MODELLO_INTRA_12 = "MODELLO_INTRA_12";
    public final static String SK_MODELLO_INTRASTAT = "MODELLO_INTRASTAT";
    public final static String SK_BLACKLIST = "BLACKLIST";
    public final static String PK_INCARICHI_MODIFICA_ALLEGATI = "INCARICHI_MODIFICA_ALLEGATI";
    public final static String SK_INCARICHI_MOD_CONTRATTO = "INCARICHI_MOD_CONTRATTO";
    public final static String SK_INCARICHI_MOD_CURRICULUM = "INCARICHI_MOD_CURRICULUM";

    public final static String PK_LIMITE_UTILIZZO_CONTANTI = "LIMITE_UTILIZZO_CONTANTI";
    public final static String SK_LIMITE1 = "LIMITE1";
    public final static String PK_SPLIT_PAYMENT = "SPLIT_PAYMENT";
    public final static String PK_CONTO_CORRENTE_BANCA_ITALIA = "CONTO_CORRENTE_BANCA_ITALIA";
    public final static String SK_CODICE = "CODICE";

    public final static String PK_INTEGRAZIONE_SDI = "INTEGRAZIONE_SDI";
    public final static String SK_INTEGRAZIONE_SDI = "MODALITA";

    public final static String SK_GESTIONE_ORDINI = "GESTIONE";
    public final static String PK_ORDINI = "ORDINI";

    public final static String SK_CIG_IMP_SENZA_FLUSSI_FINANZ = "CIG_IMP_SENZA_FLUSSI_FINANZ";

    public final static String SK_ORDINE_AUT_ROTTURA_UO_DESTINAZIONE = "ORDINI_AUT_ROTTURA_UO_DEST";
    public final static String SK_ORDINE_IMPEGNO_UO_DESTINAZIONE = "IMPEGNO_ORDINI_UO_DEST";
    public final static String PK_FATTURA_PASSIVA = "FATTURA_PASSIVA";
    public final static String SK_GEST_IRREGISTABILE = "GEST_IRREGISTABILE";
    public final static String PK_FATTURA_ATTIVA = "FATTURA_ATTIVA";
    public final static String SK_CHECK_IMP_INTRASTAT="CHECK_IMP_INTRASTAT";
    public final static String SK_LIMITE_REG_TARDIVA = "LIMITE_REG_TARDIVA";
    public final static String SK_REGISTAZIONE_ANNO_PREC= "REGISTAZIONE_ANNO_PREC";

    public final static String PK_GESTIONE_PROGETTI = "PROGETTI";
    public final static String SK_PROGETTO_PIANO_ECONOMICO = "PIANO_ECONOMICO";
    public final static String SK_NATURA_REIMPIEGO = "NATURA_REIMPIEGO";
    public final static String SK_ASS_PROGETTI_ANGAGRAFICO = "ASS_PROGETTI_ANGAGRAFICO";

    public final static String PK_FLUSSO_ORDINATIVI = "FLUSSO_ORDINATIVI";
    public final static String SK_CODICE_ABI_BT = "CODICE_ABI_BT";
    public final static String SK_CODICE_A2A = "CODICE_A2A";
    public final static String SK_CODICE_ENTE = "CODICE_ENTE";
    public final static String SK_CODICE_ENTE_BT = "CODICE_ENTE_BT";
    public final static String SK_CODICE_TRAMITE_BT = "CODICE_TRAMITE_BT";
    public final static String SK_CODICE_ISTAT_ENTE = "CODICE_ISTAT_ENTE";
    public final static String SK_ATTIVO_SIOPEPLUS = "ATTIVO_SIOPEPLUS";
    public final static String SK_INVIA_TAG_BILANCIO = "INVIA_TAG_BILANCIO";
    public final static String SK_TIPO_RAPPORTO_TESORERIA = "TIPO_RAPPORTO_TESORERIA";

    public final static String SK_ANNULLA_MANDATI = "ANNULLA_MANDATI";
    public final static String SK_ANNULLA_REVERSALI = "ANNULLA_REVERSALI";
    public final static String SK_RIPORTA_MANDATO_DAFIRMARE = "RIPORTA_MANDATO_DAFIRMARE";

    public final static String PK_AGGIORNAMENTO_IMPEGNO_DA_ORDINE = "AGGIORNAMENTO IMPEGNO DA ORDINE";
    public final static String IMPEGNO_CHIUSURA_FORZATA_A_RESIDUO = "IMPEGNO CHIUSURA FORZATA A RESIDUO";
    public final static String IMPEGNO_CHIUSURA_FORZATA_A_COMPETENZA = "IMPEGNO CHIUSURA FORZATA A COMPETENZA";

    public final static String PK_BLOCCO_RESIDUI = "BLOCCO_RESIDUI";
    public final static String SK_NATURA_FINANZIAMENTO = "NATURA_FINANZIAMENTO";

	public final static String PK_CORI_SPECIALE = "CORI_SPECIALE";
	public final static String SK_IVA = "IVA";
    public final static String SK_CORI_STIPENDI_EXTRA = "CORI_STIPENDI_EXTRA";

    public final static String PK_VOCEEP_SPECIALE = "VOCEEP_SPECIALE";
    public final static String SK_IVA_CREDITO = "IVA_CREDITO";
    public final static String SK_IVA_DEBITO = "IVA_DEBITO";
    public final static String SK_ERARIO_C_IVA = "ERARIO_C_IVA";
    public final static String SK_ACCONTO_ANTICIPO_DOCATT = "ACCONTO_ANTICIPO_DOCATT";
    public final static String SK_CREDITO_DEBITO_ANTICIPO = "CREDITO_DEBITO_ANTICIPO";
    public final static String SK_CREDITO_DEBITO_ECONOMO = "CREDITO_DEBITO_ECONOMO";
    public final static String SK_CREDITO_DEBITO_DEFAULT = "CREDITO_DEBITO_DEFAULT";
    public final static String SK_CREDITO_RIMBORSO_ANTICIPO = "CREDITO_RIMBORSO_ANTICIPO";
    public final static String SK_COSTO_IVA_NON_DETRAIBILE = "COSTO_IVA_NON_DETRAIBILE";
    public final static String SK_UTILE_PERDITE_CAMBI = "UTILE_PERDITE_CAMBI";
    public final static String SK_COMMISSIONI_BANCARIE = "COMMISSIONI_BANCARIE";
    public final static String SK_CONTI_LIQUIDAZIONE_ESTERNA_IVA = "CONTI_LIQUIDAZIONE_ESTERNA_IVA";
    public final static String SK_CREDITO_RITENUTE_SPLIT_PAYMENT = "CREDITO_RITENUTE_SPLIT_PAYMENT";
    public final static String SK_FATTURE_DA_RICEVERE = "FATTURE_DA_RICEVERE";
    public final static String SK_FATTURE_DA_EMETTERE = "FATTURE_DA_EMETTERE";
    public final static String SK_RATEI_PASSIVI = "RATEI_PASSIVI";
    public final static String SK_RATEI_ATTIVI = "RATEI_ATTIVI";
    public final static String SK_BANCA = "BANCA";
    public final static String SK_CONTO_ECONOMICO = "CONTO_ECONOMICO";
    public final static String SK_STATO_PATRIMONIALE = "STATO_PATRIMONIALE";
    public final static String SK_UTILE_PERDITA_ESERCIZIO = "UTILE_PERDITA_ESERCIZIO";
    public final static String SK_SOPRAVVENIENZE_ATTIVE = "SOPRAVVENIENZE_ATTIVE";
    public final static String SK_SOPRAVVENIENZE_PASSIVE = "SOPRAVVENIENZE_PASSIVE";

    public final static String PK_VARIAZIONE_AUTOMATICA = "VARIAZIONE_AUTOMATICA";
    public final static String SK_SPESA = "SPESA";

    public final static String PK_STIPENDI = "STIPENDI";
    public final static String SK_CODICE_BOLLO = "CODICE_BOLLO";

	public final static String PK_IMPEGNI = "IMPEGNI";
	public final static String SK_IMPEGNI_PLURIENNALI = "IMPEGNI_PLURIENNALI";

	public final static String PK_ACCERTAMENTI = "ACCERTAMENTI";
	public final static String SK_ACCERTAMENTI_PLURIENNALI = "ACCERTAMENTI_PLURIENNALI";

    public final static String PK_INVENTARIO = "INVENTARIO";
    public final static String SK_GESTIONE_ETICHETTA_BENE = "GESTIONE_ETICHETTA_BENE";
    public final static String SK_GESTIONE_BENE_DISMESSO_INVENTARIO = "GESTIONE_BENE_DISMESSO";

    public final static String SK_GESTIONE_INVENTARIA_DA_DOCUMENTI = "GESTIONE_INVENTARIA_DA_DOCUMENTI";

    public final static String PK_PAGAMENTO_ESTERO = "PAGAMENTO_ESTERO";
    public final static String SK_PAGAMENTO_ESTERO_ISS = "MODULO_PAGAMENTO_ISS";

    public final static String PK_ESERCIZIO_SPECIALE = "ESERCIZIO_SPECIALE";
    public final static String SK_ESERCIZIO_PARTENZA = "ESERCIZIO_PARTENZA";

    public final static String PK_PCC = "PCC";
    public final static String SK_MODIFICA = "MODIFICA";

    public final static String PK_GESTIONE_CONTRATTI = "CONTRATTI";
    public final static String SK_ATT_REST_STORED_FROM_SIGLA = "ATT_REST_CONTR_STORED_FROM_SIGLA";

    public final static String PK_SOSPESI = "SOSPESI";
    public final static String SK_GESTIONE_STATO_INIZIALE = "GESTIONE_STATO_INIZIALE";


    public final static String PK_STEP_FINE_ANNO = "STEP_FINE_ANNO";

    public final static String PK_DIPENDENTI = "DIPENDENTI";

    public final static String SK_MODALITA_PAGAMENTO = "MODALITA_PAGAMENTO";

    public final static String SK_LIQ_IVA_ANTICIPATA = "LIQ_IVA_ANTICIPATA";

    public final static Map<String,String> TI_STEP_FINE_ANNO = new HashMap<String, String>() {{
        put("N", "No");
        put("Y", "Si");
        put("P", "Parziale");
        put("T", "Totale");
    }};

    public enum StepFineAnno {
        APERTURA_PREVISIONE("010_APERTURA_PREVISIONE"),
        ESERCIZIO_APERTO("020_ESERCIZIO_APERTO"),
        RIBALTAMENTO_ANAGRAFICHE("030_RIBALTAMENTO_ANAGRAFICHE"),
        RIBALTAMENTO_RESIDUI("040_RIBALTAMENTO_RESIDUI"),
        FINE_INVENTARIAZIONE("050_FINE_INVENTARIAZIONE"),
        FINE_EVASIONE("060_FINE_EVASIONE"),
        FATTURE_DA_RICEVERE("070_FATTURE_DA_RICEVERE"),
        RATEI_RISCONTI("080_RATEI_RISCONTI"),
        CHIUSURA_PROVVISORIA("090_CHIUSURA_PROVVISORIA"),
        CHIUSURA_DEFINITIVA("100_CHIUSURA_DEFINITIVA"),
        CARICHI_SCARICHI_MAG("110_FINE_MOV_MAG"),
        RIAPERTURA_CONTI("120_RIAPERTURA_CONTI");

        private final String value;

        private StepFineAnno(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static StepFineAnno getValueFrom(String value) {
            for (StepFineAnno stepFineAnno : StepFineAnno.values()) {
                if (stepFineAnno.value.equals(value))
                    return stepFineAnno;
            }
            throw new IllegalArgumentException("StepFineAnno no found for value: " + value);
        }
    }

    public Configurazione_cnrBulk() {
        super();
    }

    public Configurazione_cnrBulk(java.lang.String cd_chiave_primaria, java.lang.String cd_chiave_secondaria, java.lang.String cd_unita_funzionale, java.lang.Integer esercizio) {
        super(cd_chiave_primaria, cd_chiave_secondaria, cd_unita_funzionale, esercizio);
    }

    public String getVal(int fieldNumber) {
        if (fieldNumber==1)
            return this.getVal01();
        else if (fieldNumber==2)
            return this.getVal02();
        else if (fieldNumber==3)
            return this.getVal03();
        else if (fieldNumber==4)
            return this.getVal04();
        return null;
    }

    public static void stepFineAnno(UserContext context, StepFineAnno stepFineAnno) throws BusinessProcessException {
        try {
            final Configurazione_cnrBulk configurazione = Utility
                    .createConfigurazioneCnrComponentSession()
                    .getConfigurazione(
                            context,
                            CNRUserContext.getEsercizio(context),
                            "*",
                            Configurazione_cnrBulk.PK_STEP_FINE_ANNO,
                            stepFineAnno.value()
                    );
            if (Optional.ofNullable(configurazione).isPresent()) {
                final Optional<LocalDateTime> dataFineEvasione = Optional.ofNullable(configurazione.getDt01())
                        .map(timestamp -> timestamp.toLocalDateTime());
                if (dataFineEvasione.isPresent() && dataFineEvasione
                        .filter(localDateTime ->
                                Optional.ofNullable(configurazione.getVal02())
                                    .filter(s -> s.equalsIgnoreCase("Y") || s.equalsIgnoreCase("T"))
                                        .isPresent()
                        )
                        .map(d -> d.isBefore(EJBCommonServices.getServerTimestamp().toLocalDateTime())).get()) {
                    throw new ApplicationMessageFormatException(
                            "La funzione è bloccata per l''anno {0} dal {1}",
                            String.valueOf(CNRUserContext.getEsercizio(context)),
                            dataFineEvasione.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    );
                }
            }
        } catch (ComponentException | RemoteException e) {
            throw new BusinessProcessException(e);
        }
    }
}
