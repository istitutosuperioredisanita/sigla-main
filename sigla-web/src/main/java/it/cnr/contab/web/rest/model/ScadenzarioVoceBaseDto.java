package it.cnr.contab.web.rest.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ScadenzarioVoceBaseDto implements Serializable {
    private LineaAttivitaKeyDto lineaAttivitaKeyDto;
    private BigDecimal percentuale;
    private BigDecimal im_voce;

    public LineaAttivitaKeyDto getLineaAttivitaKeyDto() {
        return lineaAttivitaKeyDto;
    }

    public void setLineaAttivitaKeyDto(LineaAttivitaKeyDto lineaAttivitaKeyDto) {
        this.lineaAttivitaKeyDto = lineaAttivitaKeyDto;
    }

    public BigDecimal getPercentuale() {
        return percentuale;
    }

    public void setPercentuale(BigDecimal percentuale) {
        this.percentuale = percentuale;
    }

    public BigDecimal getIm_voce() {
        return im_voce;
    }

    public void setIm_voce(BigDecimal im_voce) {
        this.im_voce = im_voce;
    }
}
