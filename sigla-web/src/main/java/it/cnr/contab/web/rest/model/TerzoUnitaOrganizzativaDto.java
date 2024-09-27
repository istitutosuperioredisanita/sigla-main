package it.cnr.contab.web.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
@JsonPropertyOrder({"cd_terzo","cd_anag","unita_organizzativa","denominazione_sede","nome_unita_organizzativa","note","modalitaPagamentoDto"})
public class TerzoUnitaOrganizzativaDto implements Serializable {

    private TerzoBulk terzo;

    public TerzoUnitaOrganizzativaDto(TerzoBulk terzo, List<ModalitaPagamentoDto> modalitaPagamentoDto) {
        this.terzo = terzo;
        this.modalitaPagamentoDto = modalitaPagamentoDto;
    }

    public List<ModalitaPagamentoDto> getModalitaPagamentoDto() {
        return modalitaPagamentoDto;
    }
    private List<ModalitaPagamentoDto> modalitaPagamentoDto;

    @JsonIgnore
   public TerzoBulk getTerzo() {
       return terzo;
    }
    public Integer getCd_terzo(){
        return Optional.ofNullable(terzo)
                .map(TerzoBulk::getCd_terzo)
                .orElse(null);
    }
    public String getUnita_organizzativa(){
        return Optional.ofNullable(terzo)
                .map(TerzoBulk::getUnita_organizzativa)
                .map(Unita_organizzativaBulk::getCd_unita_organizzativa)
                .orElse(null);
    }

    public String getDenominazione_sede(){
        return Optional.ofNullable(terzo)
                .map(TerzoBulk::getDenominazione_sede)
                .orElse(null);
    }
    public String getNome_unita_organizzativa(){
        return Optional.ofNullable(terzo)
                .map(TerzoBulk::getNome_unita_organizzativa)
                .orElse(null);
    }
    public String getNote(){
        return Optional.ofNullable(terzo)
                .map(TerzoBulk::getNote)
                .orElse(null);
    }
    public Integer getCd_anag(){
        return Optional.ofNullable(terzo)
                .map(TerzoBulk::getCd_anag)
                .orElse(null);
    }

    public void setTerzo(TerzoBulk terzo) {
        this.terzo = terzo;
    }
}
