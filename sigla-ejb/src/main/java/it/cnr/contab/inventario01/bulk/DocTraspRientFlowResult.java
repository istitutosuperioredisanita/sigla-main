/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.inventario01.bulk;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe che rappresenta il risultato del flusso di firma per i documenti di trasporto/rientro
 * Gestisce gli stati e i tipi di operazione sui documenti
 */
public class DocTraspRientFlowResult implements Serializable {

    private static final long serialVersionUID = 1L;

    // Tipi di flusso
    public final static String TIPO_FLUSSO_TRASPORTO = "trasporto";
    public final static String TIPO_FLUSSO_RIENTRO = "rientro";

    // Esiti flusso
    public final static String ESITO_FLUSSO_FIRMATO = "FIRMATO";
    public final static String ESITO_FLUSSO_RIFIUTATO = "RIFIUTATO";

    // Mappatura stato flusso -> stato documento
    public final static Map<String, String> STATO_FLUSSO_DOCUMENTI;
    public final static Map<String, String> TIPO_FLUSSO_DOCUMENTO;

    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(ESITO_FLUSSO_FIRMATO, Doc_trasporto_rientroBulk.STATO_FIRMATO);
        aMap.put(ESITO_FLUSSO_RIFIUTATO, Doc_trasporto_rientroBulk.STATO_INSERITO);
        STATO_FLUSSO_DOCUMENTI = Collections.unmodifiableMap(aMap);
    }

    static {
        Map<String, String> aMap = new HashMap<String, String>();
        //TODO da decommentare
//        aMap.put(TIPO_FLUSSO_TRASPORTO, Doc_trasporto_rientro_respintoBulk.OPERAZIONE_DOC_TRASPORTO);
//        aMap.put(TIPO_FLUSSO_RIENTRO, Doc_trasporto_rientro_respintoBulk.OPERAZIONE_DOC_RIENTRO);
        TIPO_FLUSSO_DOCUMENTO = Collections.unmodifiableMap(aMap);
    }

    private String processInstanceId;      // UUID HappySign
    private String tipoDocumento;          // T o R
    private Long idDocumento;              // Chiave composta come stringa
    private Integer esercizio;
    private Long pgInventario;
    private Long pgDocTrasportoRientro;
    private String stato;                  // Esito (FIRMATO/RIFIUTATO)
    private String commento;               // Motivo rifiuto
    private String user;                   // Utente che ha eseguito l'azione

    public DocTraspRientFlowResult() {
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Long getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(Long idDocumento) {
        this.idDocumento = idDocumento;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Long getPgInventario() {
        return pgInventario;
    }

    public void setPgInventario(Long pgInventario) {
        this.pgInventario = pgInventario;
    }

    public Long getPgDocTrasportoRientro() {
        return pgDocTrasportoRientro;
    }

    public void setPgDocTrasportoRientro(Long pgDocTrasportoRientro) {
        this.pgDocTrasportoRientro = pgDocTrasportoRientro;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getCommento() {
        return commento;
    }

    public void setCommento(String commento) {
        this.commento = commento;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "DocTrasportoRientroFlowResult{" +
                "processInstanceId='" + processInstanceId + '\'' +
                ", tipoDocumento='" + tipoDocumento + '\'' +
                ", esercizio=" + esercizio +
                ", pgInventario=" + pgInventario +
                ", pgDocTrasportoRientro=" + pgDocTrasportoRientro +
                ", stato='" + stato + '\'' +
                ", commento='" + commento + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}