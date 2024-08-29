package it.cnr.contab.web.rest.model;

import it.cnr.contab.anagraf00.core.bulk.BancaKey;
import it.cnr.contab.anagraf00.core.bulk.TerzoKey;

public class DocumentoGenericoRigaDto {
    private Long  progressivo_riga;
    private java.lang.String ds_riga;
    private java.sql.Timestamp dt_a_competenza_coge;
    private java.sql.Timestamp dt_cancellazione;
    private java.sql.Timestamp dt_da_competenza_coge;
    private java.math.BigDecimal im_riga;
    private TerzoKey terzoKey;
    private BancaKey bancaKey;

    public Long getProgressivo_riga() {
        return progressivo_riga;
    }

    public void setProgressivo_riga(Long progressivo_riga) {
        this.progressivo_riga = progressivo_riga;
    }
}
