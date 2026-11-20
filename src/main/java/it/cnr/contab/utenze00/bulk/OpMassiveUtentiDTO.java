package it.cnr.contab.utenze00.bulk;

import it.cnr.jada.bulk.OggettoBulk;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO di output della gestione massiva delle utenze: codici letti dal file, da inserire o cancellare.
 * In base alla gestione vengono valorizzate:
 * ACCESSI -> utenti, unitaOrganizzative, accessi;
 * RUOLI -> utenti, unitaOrganizzative, ruoli;
 * ORDINI_MAGAZZINO -> utenti, funzioni, unitaOperative.
 */
public class OpMassiveUtentiDTO extends OggettoBulk {
    private static final long serialVersionUID = 1L;

    private Set<String> utenti;
    private Set<String> accessi;
    private Set<String> unitaOrganizzative;
    private Set<String> ruoli;
    private Set<String> funzioni;
    private Set<String> unitaOperative;

    public OpMassiveUtentiDTO() {
        super();
        this.utenti= new LinkedHashSet<>();
        this.accessi = new LinkedHashSet<>();
        this.unitaOrganizzative = new LinkedHashSet<>();
        this.ruoli = new LinkedHashSet<>();
        this.funzioni = new LinkedHashSet<>();
        this.unitaOperative = new LinkedHashSet<>();
    }



    public Set<String> getUtenti() {
        return utenti;
    }

    public void setUtenti(Set<String> utenti) {
        this.utenti = utenti;
    }

    public Set<String> getAccessi() {
        return accessi;
    }

    public void setAccessi(Set<String> accessi) {
        this.accessi = accessi;
    }

    public Set<String> getUnitaOrganizzative() {
        return unitaOrganizzative;
    }

    public void setUnitaOrganizzative(Set<String> unitaOrganizzative) {
        this.unitaOrganizzative = unitaOrganizzative;
    }

    public Set<String> getRuoli() {
        return ruoli;
    }

    public void setRuoli(Set<String> ruoli) {
        this.ruoli = ruoli;
    }

    public Set<String> getFunzioni() {
        return funzioni;
    }

    public void setFunzioni(Set<String> funzioni) {
        this.funzioni = funzioni;
    }

    public Set<String> getUnitaOperative() {
        return unitaOperative;
    }

    public void setUnitaOperative(Set<String> unitaOperative) {
        this.unitaOperative = unitaOperative;
    }
}