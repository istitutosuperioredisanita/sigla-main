package it.cnr.contab.doccont00.dto;

import it.cnr.contab.progettiric00.core.bulk.ProgettoKey;
import it.cnr.contab.progettiric00.core.bulk.Progetto_piano_economicoBulk;

import java.util.ArrayList;
import java.util.HashMap;

public class ProgettoObbliPluriennaleDto extends ProgettoKey {
    //key = voce del piano
    private HashMap<String,VocePianoObbliPluriennaleDto> vociPianoRimodulaMap;
    // viene settato dal progetto e indica se attiva o meno la rimodulazione automatica a fronte di un'obbligazione pluriennale
    private Boolean flAutoRimodulazione = Boolean.TRUE;

    public ProgettoObbliPluriennaleDto(java.lang.Integer esercizio,java.lang.Integer pg_progetto,java.lang.String tipo_fase) {
        super(esercizio,pg_progetto,tipo_fase);
        vociPianoRimodulaMap = new HashMap<String,VocePianoObbliPluriennaleDto>();


    }

    public ProgettoObbliPluriennaleDto() {
        super();
        vociPianoRimodulaMap = new HashMap<String,VocePianoObbliPluriennaleDto>();

    }

    public HashMap<String, VocePianoObbliPluriennaleDto> getVociPianoRimodulaMap() {
        return vociPianoRimodulaMap;
    }

    public void setVociPianoRimodulaMap(HashMap<String, VocePianoObbliPluriennaleDto> vociPianoRimodulaMap) {
        this.vociPianoRimodulaMap = vociPianoRimodulaMap;
    }

    public Boolean getFlAutoRimodulazione() {
        return flAutoRimodulazione;
    }

    public void setFlAutoRimodulazione(Boolean flAutoRimodulazione) {
        this.flAutoRimodulazione = flAutoRimodulazione;
    }
}
