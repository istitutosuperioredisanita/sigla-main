package it.cnr.contab.client.docamm;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.latt.bulk.CofogBulk;
import it.cnr.contab.config00.latt.bulk.Gruppo_linea_attivitaBulk;
import it.cnr.contab.config00.latt.bulk.Insieme_laBulk;
import it.cnr.contab.config00.latt.bulk.Tipo_linea_attivitaBulk;
import it.cnr.contab.config00.pdcfin.bulk.FunzioneBulk;
import it.cnr.contab.config00.pdcfin.bulk.NaturaBulk;
import it.cnr.contab.prevent01.bulk.Pdg_missioneBulk;
import it.cnr.contab.prevent01.bulk.Pdg_programmaBulk;

import java.io.Serializable;

public class LineaAttivita implements Serializable {
    public Gruppo_linea_attivitaBulk gruppo_linea_attivita;

    public Tipo_linea_attivitaBulk tipo_linea_attivita;

    public FunzioneBulk funzione;

    public NaturaBulk natura;
    public it.cnr.contab.config00.sto.bulk.CdrBulk centro_responsabilita;
    public it.cnr.contab.progettiric00.core.bulk.ProgettoBulk progetto;

    public TerzoBulk responsabile;

    public Insieme_laBulk insieme_la;

    public CofogBulk cofog;

    public Pdg_programmaBulk pdgProgramma;

    public Pdg_missioneBulk pdgMissione;

    public Gruppo_linea_attivitaBulk getGruppo_linea_attivita() {
        return gruppo_linea_attivita;
    }

    public void setGruppo_linea_attivita(Gruppo_linea_attivitaBulk gruppo_linea_attivita) {
        this.gruppo_linea_attivita = gruppo_linea_attivita;
    }

    public Tipo_linea_attivitaBulk getTipo_linea_attivita() {
        return tipo_linea_attivita;
    }

    public void setTipo_linea_attivita(Tipo_linea_attivitaBulk tipo_linea_attivita) {
        this.tipo_linea_attivita = tipo_linea_attivita;
    }
}
