package it.cnr.contab.web.rest.model;

import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.DetailedRuntimeException;

public enum EnumTipoAttivitaDocumentoGen {
    COMMERCIALE( "C"),
    ISTITUZIONALE( "I");

    public String attivita;

    public String getAttivita() {
        return attivita;
    }

    private EnumTipoAttivitaDocumentoGen(String attivita) {
        this.attivita = attivita;
    }

    public static EnumTipoAttivitaDocumentoGen getValueFrom(String attivita) {
        for (EnumTipoAttivitaDocumentoGen enumStatoDocumentoGenericoDto : EnumTipoAttivitaDocumentoGen.values()) {
            if (enumStatoDocumentoGenericoDto.attivita.equals(attivita))
                return enumStatoDocumentoGenericoDto;
        }
        throw new DetailedRuntimeException(
                new ApplicationMessageFormatException("Tipo Attivita Documento Generico non trovato {0}!", attivita)
        );
    }
}
