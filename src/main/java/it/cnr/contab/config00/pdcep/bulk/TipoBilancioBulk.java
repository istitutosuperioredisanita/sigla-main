package it.cnr.contab.config00.pdcep.bulk;

public class TipoBilancioBulk extends TipoBilancioBase {

    public final static String ACCRUAL = "ACCRUAL";
    public final static String ACCRUAL_AGG = "ACCRUAL-AGG";
    public final static String IRES = "IRES";
    public final static String CIVILISTICO = "CIVILISTICO";

    private final static java.util.Dictionary tipoBilancioKeys;

    static {
        tipoBilancioKeys = new it.cnr.jada.util.OrderedHashtable();
        tipoBilancioKeys.put(ACCRUAL, "Accrual");
        tipoBilancioKeys.put(ACCRUAL_AGG, "Accrual Aggregato");
        tipoBilancioKeys.put(IRES, "IRES");
        tipoBilancioKeys.put(CIVILISTICO, "Civilistico");
    }

    public TipoBilancioBulk() {
        super();
    }

    public TipoBilancioBulk(String cdTipoBilancio) {
        super(cdTipoBilancio);
    }

    public final java.util.Dictionary getTipoBilancioKeys() {
        return tipoBilancioKeys;
    }
}