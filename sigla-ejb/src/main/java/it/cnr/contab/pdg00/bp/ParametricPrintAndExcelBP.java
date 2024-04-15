package it.cnr.contab.pdg00.bp;

import it.cnr.contab.pdg00.bulk.Stampa_vpg_bilancio_riclassVBulk;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.jada.util.jsp.Button;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class ParametricPrintAndExcelBP extends ParametricPrintBP {
    public ParametricPrintAndExcelBP() {
    }

    public ParametricPrintAndExcelBP(String function) {
        super(function);
    }

    @Override
    public Button[] createToolbar() {
        final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
        return Stream.concat(Arrays.asList(super.createToolbar()).stream(),
                Arrays.asList(
                        new Button(properties, "Toolbar.excel")
                ).stream()).toArray(Button[]::new);
    }

    public boolean isExcelButtonHidden() {
        return !Optional.ofNullable(getModel())
                .filter(Stampa_vpg_bilancio_riclassVBulk.class::isInstance)
                .map(Stampa_vpg_bilancio_riclassVBulk.class::cast)
                .map(Stampa_vpg_bilancio_riclassVBulk::getDettaglioConti)
                .orElse(Boolean.FALSE);
    }
}

