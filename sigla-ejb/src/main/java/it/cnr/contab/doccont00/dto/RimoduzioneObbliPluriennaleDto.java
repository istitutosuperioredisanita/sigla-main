package it.cnr.contab.doccont00.dto;

import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;

import java.util.ArrayList;
import java.util.HashMap;

public class RimoduzioneObbliPluriennaleDto {


   private HashMap<String,ProgettoObbliPluriennaleDto> progettoObbliPluriennaleMap;
   // viene settato dal progetto e indica se attiva o meno la rimodulazione automatica a fronte di un'obbligazione pluriennale
   private Boolean flAutoRimodulazione = Boolean.TRUE;

    public RimoduzioneObbliPluriennaleDto() {
        this.progettoObbliPluriennaleMap = new HashMap<String,ProgettoObbliPluriennaleDto>();

    }

    public HashMap<String, ProgettoObbliPluriennaleDto> getProgettoObbliPluriennaleMap() {
        return progettoObbliPluriennaleMap;
    }

    public void setProgettoObbliPluriennaleMap(HashMap<String, ProgettoObbliPluriennaleDto> progettoObbliPluriennaleMap) {
        this.progettoObbliPluriennaleMap = progettoObbliPluriennaleMap;
    }

    public boolean isPresentePluriennalePerRimodulazione(){
        for(ProgettoObbliPluriennaleDto progetto : this.getProgettoObbliPluriennaleMap().values()){
            for(VocePianoObbliPluriennaleDto voce : progetto.getVociPianoRimodulaMap().values()){
                if(voce.getAllObbligazionePluerinnali()!=null && !voce.getAllObbligazionePluerinnali().isEmpty() ){
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean getFlAutoRimodulazione() {
        return flAutoRimodulazione;
    }

    public void setFlAutoRimodulazione(Boolean flAutoRimodulazione) {
        this.flAutoRimodulazione = flAutoRimodulazione;
    }
}
