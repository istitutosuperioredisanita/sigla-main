package it.cnr.contab.web.rest.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.cnr.contab.anagraf00.core.bulk.Modalita_pagamentoBulk;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
@JsonPropertyOrder({"cdModalitaPagamento","dsModalitaPagemento","dettagliPagamento"})
public class ModalitaPagamentoDto implements Serializable {
    Modalita_pagamentoBulk modalitaPagamentoBulk;

    List<DettaglioModalitaPagDto> dettagliPagamento;

    public ModalitaPagamentoDto(Modalita_pagamentoBulk modalitaPagamentoBulk, List<DettaglioModalitaPagDto> dettagliPagamento) {
        this.modalitaPagamentoBulk = modalitaPagamentoBulk;
        this.dettagliPagamento = dettagliPagamento;
    }
    public String getCdModalitaPagamento(){
        return Optional.ofNullable(modalitaPagamentoBulk).map(s-> s.getCd_modalita_pag()).orElse("");
    }
    public String getDsModalitaPagemento(){
        return Optional.ofNullable(modalitaPagamentoBulk).
                map(s->modalitaPagamentoBulk.getRif_modalita_pagamento())
                .filter(s->s!=null)
                .map(Rif_modalita_pagamentoBulk::getDs_modalita_pag)
                .orElse("");

    }
    public List<DettaglioModalitaPagDto> getDettagliPagamento() {
        return dettagliPagamento;
    }
}
