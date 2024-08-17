package it.cnr.contab.web.rest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class ObbligazioneScadenzarioDto implements Serializable {

    private Date dt_scadenza;
    private String ds_scadenza;
    private BigDecimal im_scadenza;

    public Date getDt_scadenza() {
        return dt_scadenza;
    }

    public void setDt_scadenza(Date dt_scadenza) {
        this.dt_scadenza = dt_scadenza;
    }

    public String getDs_scadenza() {
        return ds_scadenza;
    }

    public void setDs_scadenza(String ds_scadenza) {
        this.ds_scadenza = ds_scadenza;
    }

    public BigDecimal getIm_scadenza() {
        return im_scadenza;
    }

    public void setIm_scadenza(BigDecimal im_scadenza) {
        this.im_scadenza = im_scadenza;
    }
}
