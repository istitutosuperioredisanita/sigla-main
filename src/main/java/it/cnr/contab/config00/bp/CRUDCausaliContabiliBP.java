package it.cnr.contab.config00.bp;

import it.cnr.contab.config00.bulk.AssCausaleVoceEPBulk;
import it.cnr.contab.config00.bulk.CausaleContabileBulk;
import it.cnr.contab.util.ICancellatoLogicamente;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.SimpleCRUDBP;
import it.cnr.jada.util.action.SimpleDetailCRUDController;

import java.rmi.RemoteException;
import java.util.Optional;

public class CRUDCausaliContabiliBP extends SimpleCRUDBP {

    private final SimpleDetailCRUDController assCausaleVoceEPController = new SimpleDetailCRUDController(
            "AssCausaleVoceEP",
            AssCausaleVoceEPBulk.class,
            "assCausaleVoceEPBulks",
            this
    );

    public CRUDCausaliContabiliBP() {
    }

    public CRUDCausaliContabiliBP(String s) {
        super(s);
    }

    public SimpleDetailCRUDController getAssCausaleVoceEPController() {
        return assCausaleVoceEPController;
    }

    @Override
    protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
        super.basicEdit(actioncontext, oggettobulk, flag);
        CausaleContabileBulk causaleContabileBulk = Optional.ofNullable(getModel())
                .filter(CausaleContabileBulk.class::isInstance)
                .map(CausaleContabileBulk.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Model not found!"));
        try {
            causaleContabileBulk.setAssCausaleVoceEPBulks(
                new BulkList<>(Utility.createCRUDComponentSession().find(
                    actioncontext.getUserContext(),
                    CausaleContabileBulk.class,
                    "findAssCausaleVoceEPBulk",
                    actioncontext.getUserContext(),
                    causaleContabileBulk.getCdCausale()))
            );
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
        if (getStatus()!=VIEW){
            if (Optional.ofNullable(getModel())
                    .filter(ICancellatoLogicamente.class::isInstance)
                    .map(ICancellatoLogicamente.class::cast)
                    .map(ICancellatoLogicamente::isCancellatoLogicamente)
                    .orElse(Boolean.FALSE)) {
                setStatus(VIEW);
            }
        }
    }
}
