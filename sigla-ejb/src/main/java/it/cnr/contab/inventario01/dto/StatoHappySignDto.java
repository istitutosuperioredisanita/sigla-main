package it.cnr.contab.inventario01.dto;

public class StatoHappySignDto {

    public static final String STATO_INVIATO = "INVIATO";
    public static final String STATO_FIRMATO = "FIRMATO";
    public static final String STATO_RIFIUTATO = "RIFIUTATO";

    private String stato;
    private String motivoRifiuto;

    public StatoHappySignDto() {
    }

    public StatoHappySignDto(String stato, String motivoRifiuto) {
        this.stato = stato;
        this.motivoRifiuto = motivoRifiuto;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getMotivoRifiuto() {
        return motivoRifiuto;
    }

    public void setMotivoRifiuto(String motivoRifiuto) {
        this.motivoRifiuto = motivoRifiuto;
    }

    public boolean isFirmato() {
        return STATO_FIRMATO.equals(stato);
    }

    public boolean isRifiutato() {
        return STATO_RIFIUTATO.equals(stato);
    }

    public boolean isInviato() {
        return STATO_INVIATO.equals(stato);
    }
}