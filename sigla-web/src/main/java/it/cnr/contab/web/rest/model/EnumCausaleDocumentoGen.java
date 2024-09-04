package it.cnr.contab.web.rest.model;

import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.DetailedRuntimeException;

public enum EnumCausaleDocumentoGen {

    IN_ATTESA_LIQUIDAZIONE(IDocumentoAmministrativoBulk.ATTLIQ ),
    CONTENZIOSO( IDocumentoAmministrativoBulk.CONT);

    public String causale;

    public String getCausale() {
        return causale;
    }

    private EnumCausaleDocumentoGen(String causale) {
        this.causale = causale;
    }

    public static EnumCausaleDocumentoGen getValueFrom(String causale) {
        for (EnumCausaleDocumentoGen enumCausaleDocumentoGen : EnumCausaleDocumentoGen.values()) {
            if (enumCausaleDocumentoGen.causale.equals(causale))
                return enumCausaleDocumentoGen;
        }
        throw new DetailedRuntimeException(
                new ApplicationMessageFormatException("Causale non trovata {0}!", causale)
        );
    }
}
