package it.cnr.contab.web.rest.model;

import java.math.BigDecimal;

public class ScadenzarioVoceLineaAttivitaDto {

    private LineaAttivitaKeyDto lineaAttivitaKeyDto;

    BigDecimal prcImputazioneFin;

    public LineaAttivitaKeyDto getLineaAttivitaKeyDto() {
        return lineaAttivitaKeyDto;
    }

    public void setLineaAttivitaKeyDto(LineaAttivitaKeyDto lineaAttivitaKeyDto) {
        this.lineaAttivitaKeyDto = lineaAttivitaKeyDto;
    }

    public BigDecimal getPrcImputazioneFin() {
        return prcImputazioneFin;
    }

    public void setPrcImputazioneFin(BigDecimal prcImputazioneFin) {
        this.prcImputazioneFin = prcImputazioneFin;
    }
}
