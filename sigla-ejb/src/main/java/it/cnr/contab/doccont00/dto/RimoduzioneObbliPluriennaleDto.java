package it.cnr.contab.doccont00.dto;

import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;

import java.util.ArrayList;

public class RimoduzioneObbliPluriennaleDto {


   private ArrayList<ProgettoObbliPluriennaleDto> progettoObbliPluriennaleList;

    public RimoduzioneObbliPluriennaleDto() {
        this.progettoObbliPluriennaleList = new ArrayList<ProgettoObbliPluriennaleDto>();

    }

    public ArrayList<ProgettoObbliPluriennaleDto> getProgettoObbliPluriennaleList() {
        return progettoObbliPluriennaleList;
    }

    public void setProgettoObbliPluriennaleList(ArrayList<ProgettoObbliPluriennaleDto> progettoObbliPluriennaleList) {
        this.progettoObbliPluriennaleList = progettoObbliPluriennaleList;
    }


}
