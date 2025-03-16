package it.cnr.contab.pdg00.action;

import it.cnr.contab.pdg00.bp.ParametricPrintAndExcelBP;
import it.cnr.contab.pdg00.bulk.Stampa_vpg_bilancio_riclassVBulk;
import it.cnr.contab.pdg00.bulk.Stampa_vpg_conto_econom_riclassVBulk;
import it.cnr.contab.pdg00.bulk.Stampa_vpg_stato_patrim_riclassVBulk;
import it.cnr.contab.pdg00.bulk.VpgBilRiclassificatoBulk;
import it.cnr.contab.reports.action.ParametricPrintAction;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.bulk.ColumnFieldProperty;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.ejb.BulkLoaderIterator;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.excel.bp.OfflineExcelSpoolerBP;
import it.cnr.jada.excel.bulk.Excel_spoolerBulk;
import it.cnr.jada.excel.ejb.BframeExcelComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.Query;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.util.Enumeration;
import java.util.Optional;

public class ParametricPrintAndExcelAction extends ParametricPrintAction {

    public Forward doExcel(ActionContext actioncontext) {
        try {
            final ParametricPrintAndExcelBP parametricPrintBP = (ParametricPrintAndExcelBP) actioncontext.getBusinessProcess();
            Stampa_vpg_bilancio_riclassVBulk stampaVpgBilancioRiclassVBulk = (Stampa_vpg_bilancio_riclassVBulk) parametricPrintBP.getModel();
            BframeExcelComponentSession bframeExcelComponentSession = (BframeExcelComponentSession)
                    EJBCommonServices.createEJB("BFRAMEEXCEL_EJB_BframeExcelComponentSession");
            CRUDComponentSession crudComponentSession = EJBCommonServices.createEJB("JADAEJB_CRUDComponentSession", CRUDComponentSession.class);

            String longDescription = parametricPrintBP.getBulkInfo().getLongDescription();
            final VpgBilRiclassificatoBulk vpgBilRiclassificatoBulk = new VpgBilRiclassificatoBulk();
            final RemoteIterator remoteiterator = crudComponentSession.cerca(actioncontext.getUserContext(), new CompoundFindClause(), vpgBilRiclassificatoBulk);
            Query query = ((BulkLoaderIterator) remoteiterator).getQuery();
            BulkInfo bulkInfo = BulkInfo.getBulkInfo(VpgBilRiclassificatoBulk.class);
            final Optional<Stampa_vpg_conto_econom_riclassVBulk> stampaVpgContoEconomRiclassVBulk = Optional.ofNullable(stampaVpgBilancioRiclassVBulk)
                    .filter(Stampa_vpg_conto_econom_riclassVBulk.class::isInstance)
                    .map(Stampa_vpg_conto_econom_riclassVBulk.class::cast);
            final Optional<Stampa_vpg_stato_patrim_riclassVBulk> stampaVpgStatoPatrimRiclassVBulk = Optional.ofNullable(stampaVpgBilancioRiclassVBulk)
                    .filter(Stampa_vpg_stato_patrim_riclassVBulk.class::isInstance)
                    .map(Stampa_vpg_stato_patrim_riclassVBulk.class::cast);

            StringBuffer beforeStatement = new StringBuffer();
            final String APICE = "'", VIROLA=",", APICE_VIROLA = APICE + VIROLA;
            beforeStatement.append("BEGIN ");
            beforeStatement.append(stampaVpgContoEconomRiclassVBulk.isPresent() ? "PRT_S_CE_RICLASSIFICATO_J":"PRT_S_SP_RICLASSIFICATO");
            beforeStatement.append("(");
            beforeStatement.append(stampaVpgStatoPatrimRiclassVBulk.isPresent() ? APICE + stampaVpgStatoPatrimRiclassVBulk.get().getTi_att_pass() + APICE_VIROLA:"");
            beforeStatement.append(APICE + stampaVpgBilancioRiclassVBulk.getTi_ist_com() + APICE_VIROLA);
            beforeStatement.append(stampaVpgBilancioRiclassVBulk.getEsercizio() + VIROLA);
            beforeStatement.append(APICE + stampaVpgBilancioRiclassVBulk.getCdCDSCRForPrint() + APICE_VIROLA);
            beforeStatement.append(APICE + stampaVpgBilancioRiclassVBulk.getCdUOCRForPrint() + APICE_VIROLA);
            beforeStatement.append(APICE + stampaVpgBilancioRiclassVBulk.getDettaglioContiYN() + APICE_VIROLA);
            beforeStatement.append(APICE + stampaVpgBilancioRiclassVBulk.getTipoBilancio().getCdTipoBilancio() + APICE);
            beforeStatement.append("); END;");

            OrderedHashtable columnLabel = new OrderedHashtable();
            OrderedHashtable columnHeaderLabel = new OrderedHashtable();
            OrderedHashtable colonnedaEstrarre = new OrderedHashtable();
            for (Enumeration enumeration = bulkInfo.getColumnFieldPropertyDictionary().keys(); enumeration.hasMoreElements(); ) {
                String columnName = (String) enumeration.nextElement();
                colonnedaEstrarre.put(columnName, bulkInfo.getColumnFieldPropertyDictionary().get(columnName));
            }
            for (Enumeration enumeration = bulkInfo.getColumnFieldPropertyDictionary().elements(); enumeration.hasMoreElements(); ) {
                ColumnFieldProperty columnfieldproperty = (ColumnFieldProperty) enumeration.nextElement();
                columnLabel.put(columnfieldproperty, columnfieldproperty.getLabel());
                Optional.ofNullable(columnfieldproperty.getHeaderLabel())
                        .ifPresent(s -> columnHeaderLabel.put(columnfieldproperty, s));
            }
            Excel_spoolerBulk bulk = bframeExcelComponentSession.addQueue(
                    actioncontext.getUserContext(),
                    columnLabel,
                    columnHeaderLabel, longDescription, colonnedaEstrarre, query.toString(), beforeStatement.toString(), query.getColumnMap(), new VpgBilRiclassificatoBulk());
            OfflineExcelSpoolerBP excelSpoolerBP = (OfflineExcelSpoolerBP) actioncontext.createBusinessProcess("OfflineExcelSpoolerBP");
            excelSpoolerBP.setModel(actioncontext, bulk);
            return actioncontext.addBusinessProcess(excelSpoolerBP);

        } catch (BusinessProcessException businessprocessexception) {
            return handleException(actioncontext, businessprocessexception);
        } catch (Throwable throwable) {
            return handleException(actioncontext, throwable);
        }
    }
}
