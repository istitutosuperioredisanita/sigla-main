package it.cnr.contab.web.rest.model;

import it.cnr.contab.util.enumeration.TipoIVA;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

abstract  public class DocumentoGenericoDto<T extends DocumentoGenericoRigaDto> extends DocumentoGenericoKeyDto implements Serializable {
    private java.sql.Timestamp data_registrazione;
    private java.lang.String ds_documento_generico;
    private java.sql.Timestamp dt_a_competenza_coge;
    private java.sql.Timestamp dt_da_competenza_coge;
    private java.sql.Timestamp dt_scadenza;
    private java.sql.Timestamp dt_cancellazione;

    private EnumAssMandRevDocGen assMandRev;
    private TipoIVA tipo;
    EnumStatoDocumentoGenerico stato;
    EnumStatoLiqDocumentoGen statoLiquidazione;
    private List<T> righe= new ArrayList<T>();

    public List<T> getRighe() {
        return righe;
    }



    public void setRighe(List<T> righe) {
        this.righe = righe;
    }
    public Class reflectClassType() {
        return ((Class) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public Timestamp getData_registrazione() {
        return data_registrazione;
    }

    public void setData_registrazione(Timestamp data_registrazione) {
        this.data_registrazione = data_registrazione;
    }

    public String getDs_documento_generico() {
        return ds_documento_generico;
    }

    public void setDs_documento_generico(String ds_documento_generico) {
        this.ds_documento_generico = ds_documento_generico;
    }

    public Timestamp getDt_a_competenza_coge() {
        return dt_a_competenza_coge;
    }

    public void setDt_a_competenza_coge(Timestamp dt_a_competenza_coge) {
        this.dt_a_competenza_coge = dt_a_competenza_coge;
    }

    public Timestamp getDt_da_competenza_coge() {
        return dt_da_competenza_coge;
    }

    public void setDt_da_competenza_coge(Timestamp dt_da_competenza_coge) {
        this.dt_da_competenza_coge = dt_da_competenza_coge;
    }

    public Timestamp getDt_scadenza() {
        return dt_scadenza;
    }

    public void setDt_scadenza(Timestamp dt_scadenza) {
        this.dt_scadenza = dt_scadenza;
    }

    public Timestamp getDt_cancellazione() {
        return dt_cancellazione;
    }

    public void setDt_cancellazione(Timestamp dt_cancellazione) {
        this.dt_cancellazione = dt_cancellazione;
    }

    public EnumStatoDocumentoGenerico getStato() {
        return stato;
    }

    public void setStato(EnumStatoDocumentoGenerico stato) {
        this.stato = stato;
    }

    public EnumStatoLiqDocumentoGen getStatoLiquidazione() {
        return statoLiquidazione;
    }

    public void setStatoLiquidazione(EnumStatoLiqDocumentoGen statoLiquidazione) {
        this.statoLiquidazione = statoLiquidazione;
    }

    public TipoIVA getTipo() {
        return tipo;
    }

    public void setTipo(TipoIVA tipo) {
        this.tipo = tipo;
    }

    public EnumAssMandRevDocGen getAssMandRev() {
        return assMandRev;
    }

    public void setAssMandRev(EnumAssMandRevDocGen assMandRev) {
        this.assMandRev = assMandRev;
    }
}
