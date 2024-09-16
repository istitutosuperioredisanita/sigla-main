package it.cnr.contab.web.rest.model;

import java.io.Serializable;
import java.util.List;

public class AccertamentoScadenzarioDto extends ScadenzarioBaseDto implements Serializable {


    private Long pg_accertamento_scadenzario;

    private List<AccertamentoScadVoceDto> accertamentoScadVoce;

    public Long getPg_accertamento_scadenzario() {
        return pg_accertamento_scadenzario;
    }

    public void setPg_accertamento_scadenzario(Long pg_accertamento_scadenzario) {
        this.pg_accertamento_scadenzario = pg_accertamento_scadenzario;
    }

    public List<AccertamentoScadVoceDto> getAccertamentoScadVoce() {
        return accertamentoScadVoce;
    }

    public void setAccertamentoScadVoce(List<AccertamentoScadVoceDto> accertamentoScadVoce) {
        this.accertamentoScadVoce = accertamentoScadVoce;
    }
}
