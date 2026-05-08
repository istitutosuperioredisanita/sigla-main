package it.cnr.contab.inventario01.bulk;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe che rappresenta il risultato del flusso di firma per i documenti di trasporto/rientro.
 */
public class DocTraspRientFlowResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TIPO_FLUSSO_TRASPORTO = "trasporto";
    public static final String TIPO_FLUSSO_RIENTRO = "rientro";

    public static final String ESITO_FLUSSO_FIRMATO = "FIRMATO";
    public static final String ESITO_FLUSSO_RIFIUTATO = "RIFIUTATO";

    public static final Map<String, String> STATO_FLUSSO_DOCUMENTI;
    public static final Map<String, String> TIPO_FLUSSO_DOCUMENTO;

    static {
        Map<String, String> map = new HashMap<>();
        map.put(ESITO_FLUSSO_FIRMATO, Doc_trasporto_rientroBulk.STATO_DEFINITIVO);
        map.put(ESITO_FLUSSO_RIFIUTATO, Doc_trasporto_rientroBulk.STATO_INSERITO);
        STATO_FLUSSO_DOCUMENTI = Collections.unmodifiableMap(map);
    }

    static {
        Map<String, String> map = new HashMap<>();
        map.put(TIPO_FLUSSO_TRASPORTO, Doc_trasporto_rientroBulk.TRASPORTO);
        map.put(TIPO_FLUSSO_RIENTRO, Doc_trasporto_rientroBulk.RIENTRO);
        TIPO_FLUSSO_DOCUMENTO = Collections.unmodifiableMap(map);
    }

    private String processInstanceId;
    private String tipoDocumento;
    private Long idDocumento;
    private Integer esercizio;
    private Long pgInventario;
    private Long pgDocTrasportoRientro;
    private String stato;
    private String commento;
    private String user;

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
        return "DocTraspRientFlowResult{" +
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