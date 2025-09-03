package it.cnr.contab.pdg00.cdip.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import java.util.List;

/**
 * Bulk per la gestione dei dati elaborati dal file Excel.
 * NON implementa AllegatoParentBulk perché non gestisce allegati direttamente.
 */
public class GestioneStipBulk extends OggettoBulk {

    private static final long serialVersionUID = 1L;

    private Stipendi_cofiBulk stipendiCofiBulk;
    private List<Stipendi_cofi_obb_scadBulk> stipendiCofiObbScadBulks;
    private List<Stipendi_cofi_coriBulk> stipendiCofiCoriBulks;

    // Costruttori
    public GestioneStipBulk() {
        super();
    }

    public GestioneStipBulk(Stipendi_cofiBulk stipendiCofiBulk,
                            List<Stipendi_cofi_obb_scadBulk> stipendiCofiObbScadBulks,
                            List<Stipendi_cofi_coriBulk> stipendiCofiCoriBulks) {
        this.stipendiCofiBulk = stipendiCofiBulk;
        this.stipendiCofiObbScadBulks = stipendiCofiObbScadBulks;
        this.stipendiCofiCoriBulks = stipendiCofiCoriBulks;
    }

    // Getters e Setters
    public Stipendi_cofiBulk getStipendiCofiBulk() {
        return stipendiCofiBulk;
    }

    public void setStipendiCofiBulk(Stipendi_cofiBulk stipendiCofiBulk) {
        this.stipendiCofiBulk = stipendiCofiBulk;
    }

    public List<Stipendi_cofi_obb_scadBulk> getStipendiCofiObbScadBulks() {
        return stipendiCofiObbScadBulks;
    }

    public void setStipendiCofiObbScadBulks(List<Stipendi_cofi_obb_scadBulk> stipendiCofiObbScadBulks) {
        this.stipendiCofiObbScadBulks = stipendiCofiObbScadBulks;
    }

    public List<Stipendi_cofi_coriBulk> getStipendiCofiCoriBulks() {
        return stipendiCofiCoriBulks;
    }

    public void setStipendiCofiCoriBulks(List<Stipendi_cofi_coriBulk> stipendiCofiCoriBulks) {
        this.stipendiCofiCoriBulks = stipendiCofiCoriBulks;
    }

    @Override
    public String toString() {
        return "GestioneStipBulk{" +
                "stipendiCofiBulk=" + stipendiCofiBulk +
                ", stipendiCofiObbScadBulks=" + stipendiCofiObbScadBulks +
                ", stipendiCofiCoriBulks=" + stipendiCofiCoriBulks +
                '}';
    }
}