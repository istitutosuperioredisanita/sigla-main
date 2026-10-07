package it.cnr.contab.progettiric00.dto;

import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;

public class RiportaProgettoDto extends OggettoBulk  {

    private Integer esercizioEstrazione;
    private String cdProgettoOld;
    private Integer pgProgettoOld;
    private String cdProgettoNew;

    public Integer getEsercizioEstrazione() {
        return esercizioEstrazione;
    }

    public void setEsercizioEstrazione(Integer esercizioEstrazione) {
        this.esercizioEstrazione = esercizioEstrazione;
    }

    public Integer getPgProgettoOld() {
        return pgProgettoOld;
    }

    public void setPgProgettoOld(Integer pgProgettoOld) {
        this.pgProgettoOld = pgProgettoOld;
    }

    public String getCdProgettoOld() {
        return cdProgettoOld;
    }

    public void setCdProgettoOld(String cdProgettoOld) {
        this.cdProgettoOld = cdProgettoOld;
    }

    public String getCdProgettoNew() {
        return cdProgettoNew;
    }

    public void setCdProgettoNew(String cdProgettoNew) {
        this.cdProgettoNew = cdProgettoNew;
    }
}
