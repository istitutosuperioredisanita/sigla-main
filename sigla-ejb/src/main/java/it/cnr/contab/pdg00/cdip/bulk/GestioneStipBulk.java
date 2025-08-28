package it.cnr.contab.pdg00.cdip.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

import java.util.List;


//controlli su pentaho
public class GestioneStipBulk extends OggettoBulk implements Persistent{

    Stipendi_cofiBulk stipendiCofiBulk;
    List<Stipendi_cofi_obb_scadBulk> stipendiCofiObbScadBulks;
    List<Stipendi_cofi_coriBulk> stipendiCofiCoriBulks;

    public GestioneStipBulk() {
    }

    public GestioneStipBulk(Stipendi_cofiBulk stipendiCofiBulk, List<Stipendi_cofi_obb_scadBulk> stipendiCofiObbScadBulks, List<Stipendi_cofi_coriBulk> stipendiCofiCoriBulks) {
        this.stipendiCofiBulk = stipendiCofiBulk;
        this.stipendiCofiObbScadBulks = stipendiCofiObbScadBulks;
        this.stipendiCofiCoriBulks = stipendiCofiCoriBulks;
    }

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