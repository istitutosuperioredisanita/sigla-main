package it.cnr.contab.web.rest.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

 abstract public class DocumentoGenericoRigaDto {
    private Long  progressivo_riga;
    private java.lang.String ds_riga;
    private java.sql.Timestamp dt_a_competenza_coge;
    private java.sql.Timestamp dt_cancellazione;
    private java.sql.Timestamp dt_da_competenza_coge;
    private java.math.BigDecimal im_riga;

    private EnumAssMandRevDocGenRiga assMandRev;


     abstract public TerzoPagamentoIncasso getTerzo();

    public Long getProgressivo_riga() {
        return progressivo_riga;
    }

    public void setProgressivo_riga(Long progressivo_riga) {
        this.progressivo_riga = progressivo_riga;
    }

    public String getDs_riga() {
        return ds_riga;
    }

    public void setDs_riga(String ds_riga) {
        this.ds_riga = ds_riga;
    }

    public Timestamp getDt_a_competenza_coge() {
        return dt_a_competenza_coge;
    }

    public void setDt_a_competenza_coge(Timestamp dt_a_competenza_coge) {
        this.dt_a_competenza_coge = dt_a_competenza_coge;
    }

    public Timestamp getDt_cancellazione() {
        return dt_cancellazione;
    }

    public void setDt_cancellazione(Timestamp dt_cancellazione) {
        this.dt_cancellazione = dt_cancellazione;
    }

    public Timestamp getDt_da_competenza_coge() {
        return dt_da_competenza_coge;
    }

    public void setDt_da_competenza_coge(Timestamp dt_da_competenza_coge) {
        this.dt_da_competenza_coge = dt_da_competenza_coge;
    }

    public BigDecimal getIm_riga() {
        return im_riga;
    }

    public void setIm_riga(BigDecimal im_riga) {
        this.im_riga = im_riga;
    }

     public EnumAssMandRevDocGenRiga getAssMandRev() {
         return assMandRev;
     }

     public void setAssMandRev(EnumAssMandRevDocGenRiga assMandRev) {
         this.assMandRev = assMandRev;
     }
 }
