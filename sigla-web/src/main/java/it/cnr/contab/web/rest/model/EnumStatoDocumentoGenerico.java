package it.cnr.contab.web.rest.model;

import io.swagger.annotations.ApiModel;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.DetailedRuntimeException;

@ApiModel( "Stato Documento Generico")
public enum EnumStatoDocumentoGenerico {
    INIZIALE(Documento_genericoBulk.STATO_INIZIALE),
    CONTABILIZZATO(Documento_genericoBulk.STATO_CONTABILIZZATO),
    PARZIALE(Documento_genericoBulk.STATO_PARZIALE),
    PAGATO(Documento_genericoBulk.STATO_PAGATO),
    ANNULLATO(Documento_genericoBulk.STATO_ANNULLATO);

    public String stato;

    public String getStato() {
        return this.stato;
    }
    private EnumStatoDocumentoGenerico(String stato) {
        this.stato = stato;
    }

    public static EnumStatoDocumentoGenerico getValueFrom(String stato) {
        for (EnumStatoDocumentoGenerico enumStatoDocumentoGenericoDto : EnumStatoDocumentoGenerico.values()) {
            if (enumStatoDocumentoGenericoDto.stato.equals(stato))
                return enumStatoDocumentoGenericoDto;
        }
        throw new DetailedRuntimeException(
                new ApplicationMessageFormatException("Stato Documento Generico non trovato {0}!", stato)
        );
    }


}
