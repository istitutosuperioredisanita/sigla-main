package it.cnr.contab.coepcoan00.core.bulk;

import java.io.Serializable;

public class ResultScrittureContabili implements Serializable {

    public ResultScrittureContabili(IDocumentoCogeBulk documentoCoge, Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk) {
        this.documentoCoge = documentoCoge;
        this.scritturaPartitaDoppiaBulk = scritturaPartitaDoppiaBulk;
        this.scritturaAnaliticaBulk = null;
    }

    public ResultScrittureContabili(IDocumentoCogeBulk documentoCoge, Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk, Scrittura_analiticaBulk scritturaAnaliticaBulk) {
        this.documentoCoge = documentoCoge;
        this.scritturaPartitaDoppiaBulk = scritturaPartitaDoppiaBulk;
        this.scritturaAnaliticaBulk = scritturaAnaliticaBulk;
    }

    IDocumentoCogeBulk documentoCoge;
    Scrittura_analiticaBulk scritturaAnaliticaBulk;
    Scrittura_partita_doppiaBulk scritturaPartitaDoppiaBulk;

    public IDocumentoCogeBulk getDocumentoCoge() {
        return documentoCoge;
    }

    public Scrittura_partita_doppiaBulk getScritturaPartitaDoppiaBulk() {
        return scritturaPartitaDoppiaBulk;
    }

    public Scrittura_analiticaBulk getScritturaAnaliticaBulk() {
        return scritturaAnaliticaBulk;
    }
}
