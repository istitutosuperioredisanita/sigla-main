package it.cnr.contab.coepcoan00.core.bulk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultScrittureContabili implements Serializable {
    public ResultScrittureContabili() {
    }

    public ResultScrittureContabili(IDocumentoCogeBulk documentoCoge, Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk) {
        this(documentoCoge, scritturaPartitaDoppiaBulk, null, null, null);
    }

    public ResultScrittureContabili(IDocumentoCogeBulk documentoCoge, Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk, Scrittura_analiticaBulk scritturaAnaliticaBulk) {
        this(documentoCoge, scritturaPartitaDoppiaBulk, scritturaAnaliticaBulk, null, null);
    }

    public ResultScrittureContabili(IDocumentoCogeBulk documentoCoge, Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk, Scrittura_analiticaBulk scritturaAnaliticaBulk, List<Scrittura_partita_doppiaBulk> otherScritturaPartitaDoppiaBulk, List<Scrittura_analiticaBulk> otherScritturaAnaliticaBulk) {
        this.documentoCoge = documentoCoge;
        this.scritturaPartitaDoppiaBulk = scritturaPartitaDoppiaBulk;
        this.scritturaAnaliticaBulk = scritturaAnaliticaBulk;
        this.otherScritturaPartitaDoppiaBulk = Optional.ofNullable(otherScritturaPartitaDoppiaBulk).orElse(new ArrayList<>());
        this.otherScritturaAnaliticaBulk = Optional.ofNullable(otherScritturaAnaliticaBulk).orElse(new ArrayList<>());
    }

    IDocumentoCogeBulk documentoCoge;
    Scrittura_analiticaBulk scritturaAnaliticaBulk;
    Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk;

    //Rappresentano scritture costruite su altri oggetti, quale ad esempio nel caso di riscontro a valore con rettifica prezzi, contengono la scrittura generata sulla consegna
    List<Scrittura_analiticaBulk> otherScritturaAnaliticaBulk;
    List<Scrittura_partita_doppiaBulk> otherScritturaPartitaDoppiaBulk;

    public IDocumentoCogeBulk getDocumentoCoge() {
        return documentoCoge;
    }

    public Scrittura_partita_doppiaBulk getScritturaPartitaDoppiaBulk() {
        return scritturaPartitaDoppiaBulk;
    }

    public Scrittura_analiticaBulk getScritturaAnaliticaBulk() {
        return scritturaAnaliticaBulk;
    }

    public List<Scrittura_partita_doppiaBulk> getOtherScritturaPartitaDoppiaBulk() {
        return otherScritturaPartitaDoppiaBulk;
    }

    public List<Scrittura_analiticaBulk> getOtherScritturaAnaliticaBulk() {
        return otherScritturaAnaliticaBulk;
    }
}
