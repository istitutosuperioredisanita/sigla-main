package it.cnr.contab.inventario01.dto;

public class StatoHappySignDto {

    public static final String STATO_INVIATO = "INV";
    public static final String STATO_FIRMATO = "FIR";
    public static final String STATO_RIFIUTATO = "RIF";

    private String stato;
    private String motivoRifiuto;

    public StatoHappySignDto() {
    }

    public StatoHappySignDto(String stato, String motivoRifiuto) {
        this.stato = stato;
        this.motivoRifiuto = motivoRifiuto;
    }

    public boolean isInviato() {
        return STATO_INVIATO.equals(stato);
    }

    public boolean isFirmato() {
        return STATO_FIRMATO.equals(stato);
    }

    public boolean isRifiutato() {
        return STATO_RIFIUTATO.equals(stato);
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getCodiceStato() {
        return stato;
    }

    public void setCodiceStato(String codiceStato) {
        this.stato = codiceStato;
    }

    public String getMotivoRifiuto() {
        return motivoRifiuto;
    }

    public void setMotivoRifiuto(String motivoRifiuto) {
        this.motivoRifiuto = motivoRifiuto;
    }
}