package it.cnr.contab.web.rest.model;

import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.DetailedRuntimeException;

public enum EnumAssMandRevDocGenRiga {
    ASSOCIATO_A_MAND_REV(Documento_generico_rigaBulk.ASSOCIATO_A_MANDATO),
    NO_ASSOCIATO_A_MAND_REV(Documento_generico_rigaBulk.NON_ASSOCIATO_A_MANDATO);
    String assMandRev;

    public String getAssMandRev() {
        return assMandRev;
    }

    EnumAssMandRevDocGenRiga(String assMandRev) {
        this.assMandRev = assMandRev;
    }
    public static EnumAssMandRevDocGenRiga getValueFrom(String assMandRev) {
        for (EnumAssMandRevDocGenRiga enumAssMandRevDocGen : EnumAssMandRevDocGenRiga.values()) {
            if (enumAssMandRevDocGen.assMandRev.equals(assMandRev))
                return enumAssMandRevDocGen;
        }
        throw new DetailedRuntimeException(
                new ApplicationMessageFormatException("Assciazione Mandato Reversale non trovata {0}!", assMandRev)
        );
    }

}
