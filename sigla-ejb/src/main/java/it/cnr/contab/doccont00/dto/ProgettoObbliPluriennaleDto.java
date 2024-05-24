package it.cnr.contab.doccont00.dto;

import it.cnr.contab.progettiric00.core.bulk.ProgettoKey;
import it.cnr.contab.progettiric00.core.bulk.Progetto_piano_economicoBulk;

import java.util.ArrayList;

public class ProgettoObbliPluriennaleDto extends ProgettoKey {

    private ArrayList<ObbligazionePluriennaleDto> obbligazionePlurAdd;
    private ArrayList<ObbligazionePluriennaleDto> obbligazionePlurDel;
    private Progetto_piano_economicoBulk pianoEconomicoProgetto;

    public ProgettoObbliPluriennaleDto(java.lang.Integer esercizio,java.lang.Integer pg_progetto,java.lang.String tipo_fase) {
        super(esercizio,pg_progetto,tipo_fase);
        obbligazionePlurAdd = new ArrayList<>();
        obbligazionePlurDel = new ArrayList<>();

    }

    public ProgettoObbliPluriennaleDto() {
        super();
        obbligazionePlurAdd = new ArrayList<>();
        obbligazionePlurDel = new ArrayList<>();
    }

    public ArrayList<ObbligazionePluriennaleDto> getObbligazionePlurAdd() {
        return obbligazionePlurAdd;
    }

    public void setObbligazionePlurAdd(ArrayList<ObbligazionePluriennaleDto> obbligazionePlurAdd) {
        this.obbligazionePlurAdd = obbligazionePlurAdd;
    }

    public ArrayList<ObbligazionePluriennaleDto> getObbligazionePlurDel() {
        return obbligazionePlurDel;
    }

    public void setObbligazionePlurDel(ArrayList<ObbligazionePluriennaleDto> obbligazionePlurDel) {
        this.obbligazionePlurDel = obbligazionePlurDel;
    }

    public Progetto_piano_economicoBulk getPianoEconomicoProgetto() {
        return pianoEconomicoProgetto;
    }

    public void setPianoEconomicoProgetto(Progetto_piano_economicoBulk pianoEconomicoProgetto) {
        this.pianoEconomicoProgetto = pianoEconomicoProgetto;
    }

    public ArrayList<ObbligazionePluriennaleDto> getAllObbligazionePluerinnali(){
        ArrayList<ObbligazionePluriennaleDto> tot = new ArrayList<ObbligazionePluriennaleDto>();
        tot.addAll(this.getObbligazionePlurAdd());
        tot.addAll(this.getObbligazionePlurDel());
        return tot;
    }
}
