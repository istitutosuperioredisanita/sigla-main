package it.cnr.contab.coepcoan00.core.bulk;

import java.io.Serializable;

public class ResultScrittureContabili implements Serializable {
    public static final boolean LOAD_ANALITICA = true;

    public ResultScrittureContabili(Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk, Scrittura_analiticaBulk scritturaAnaliticaBulk) {
        this.scritturaPartitaDoppiaBulk = scritturaPartitaDoppiaBulk;
        this.scritturaAnaliticaBulk = scritturaAnaliticaBulk;
    }

    Scrittura_analiticaBulk scritturaAnaliticaBulk;
    Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk;

    public Scrittura_partita_doppiaBulk getScritturaPartitaDoppiaBulk() {
        return scritturaPartitaDoppiaBulk;
    }

    public Scrittura_analiticaBulk getScritturaAnaliticaBulk() {
        return scritturaAnaliticaBulk;
    }
}
