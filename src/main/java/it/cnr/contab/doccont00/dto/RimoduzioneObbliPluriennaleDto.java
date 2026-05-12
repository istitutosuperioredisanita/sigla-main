package it.cnr.contab.doccont00.dto;

import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;

import java.util.ArrayList;
import java.util.HashMap;

public class RimoduzioneObbliPluriennaleDto {


   private HashMap<it.cnr.contab.config00.latt.bulk.WorkpackageKey,ProgettoObbliPluriennaleDto> progettoObbliPluriennaleMap;

    public RimoduzioneObbliPluriennaleDto() {
        this.progettoObbliPluriennaleMap = new HashMap<it.cnr.contab.config00.latt.bulk.WorkpackageKey,ProgettoObbliPluriennaleDto>();

    }

    public HashMap<it.cnr.contab.config00.latt.bulk.WorkpackageKey, ProgettoObbliPluriennaleDto> getProgettoObbliPluriennaleMap() {
        return progettoObbliPluriennaleMap;
    }

    public void setProgettoObbliPluriennaleMap(HashMap<it.cnr.contab.config00.latt.bulk.WorkpackageKey, ProgettoObbliPluriennaleDto> progettoObbliPluriennaleMap) {
        this.progettoObbliPluriennaleMap = progettoObbliPluriennaleMap;
    }

    public boolean isPresentePluriennalePerRimodulazione(){
        for(ProgettoObbliPluriennaleDto progetto : this.getProgettoObbliPluriennaleMap().values()){
            if(progetto.getFlAutoRimodulazione()) {
                for (VocePianoObbliPluriennaleDto voce : progetto.getVociPianoRimodulaMap().values()) {
                    if (voce.getAllObbligazionePluerinnali() != null && !voce.getAllObbligazionePluerinnali().isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
