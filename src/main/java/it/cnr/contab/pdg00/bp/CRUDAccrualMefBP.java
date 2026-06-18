package it.cnr.contab.pdg00.bp;

import it.cnr.contab.config00.ejb.EsercizioComponentSession;
import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.contab.pdg00.bulk.AllegatoAccrualBulk;
import it.cnr.contab.pdg00.bulk.Stampa_vpg_stato_patrim_riclassVBulk;
import it.cnr.contab.pdg00.bulk.VpgBilRiclassificatoBulk;
import it.cnr.contab.pdg00.ejb.BilancioAccrualComponentSession;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.jada.util.jsp.Button;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import it.iss.accrual.xbrl.AccrualService;
import it.iss.accrual.xbrl.dto.AccrualXbrl;
import it.iss.accrual.xbrl.dto.ContextXbrl;
import it.iss.accrual.xbrl.dto.DurationContextXbrl;
import it.iss.accrual.xbrl.dto.FactXbrl;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CRUDAccrualMefBP extends AllegatiCRUDBP<AllegatoAccrualBulk, AccrualBulk> {

    private static final Logger log = LoggerFactory.getLogger(CRUDAccrualMefBP.class);

    private boolean esercizioChiuso = false;

    public CRUDAccrualMefBP() {
        super();
    }

    public CRUDAccrualMefBP(String function) {
        super(function );
    }

    @Override
    protected void init(Config config, ActionContext context)
            throws BusinessProcessException {

        try {
            esercizioChiuso = (((EsercizioComponentSession) EJBCommonServices
                    .createEJB(
                            "CNRCONFIG00_EJB_EsercizioComponentSession",
                            EsercizioComponentSession.class
                    ))
                    .isEsercizioChiuso(context.getUserContext()));

        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
        super.init(config, context);

    }

    /**
     * Insert the method's description here.
     * Creation date: (03/04/2002 18.04.30)
     * @return it.cnr.jada.util.action.SimpleDetailCRUDController
     */
    public it.cnr.jada.util.jsp.Button[] createToolbar() {
        Button[] toolbar = super.createToolbar();
        Button[] newToolbar = new Button[ toolbar.length + 1 ];
        for ( int i = 0; i< toolbar.length; i++ )
            newToolbar[ i ] = toolbar[ i ];
        newToolbar[ toolbar.length ] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()),"Toolbar.creafileaccrual");
        return newToolbar;

    }

    public String[][] getTabs() {
        return new String[][]{
                {
                        "tabModulo",
                        "Modulo",
                        "/pdg00/tab_accrual_mef_modulo.jsp"
                },
                {
                        "tabAllegati",
                        "Allegati",
                        "/util00/tab_allegati.jsp"
                }
        };
    }

    @Override
    protected void resetTabs(ActionContext actioncontext) {
        setTab("tab", "tabModulo");
    }
    /**
     * Stessa logica del BP Trasporto:
     * il path viene costruito dal Bulk e il BP restituisce solo il primo elemento.
     */
    @Override
    protected String getStorePath(AccrualBulk accrual, boolean create)
            throws BusinessProcessException {

        if (accrual == null) {
            throw new BusinessProcessException("Modulo Accrual MEF non presente");
        }

        return accrual.getStorePath().get(0);
    }

    @Override
    protected Class getAllegatoClass() {
        return AllegatoAccrualBulk.class;
    }

    @Override
    protected void completeAllegato(AllegatoAccrualBulk allegato, StorageObject storageObject) throws ApplicationException {
        Optional.ofNullable(storageObject.<List<String>>getPropertyValue(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value()))
                .map(strings -> strings.stream())
                .ifPresent(stringStream -> {
                    stringStream
                            .filter(s -> AllegatoAccrualBulk.aspectNamesKeys.get(s) != null)
                            .findFirst()
                            .ifPresent(s -> allegato.setAspectName(s));
                });
        super.completeAllegato(allegato, storageObject);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return !isEsercizioChiuso() && super.isNewButtonEnabled();
    }

    @Override
    public boolean isSaveButtonEnabled() {
        return !isEsercizioChiuso() && super.isSaveButtonEnabled();
    }

    @Override
    public boolean isDeleteButtonEnabled() {
        return !isEsercizioChiuso() && super.isDeleteButtonEnabled();
    }
    public boolean isEsercizioChiuso() {
        return esercizioChiuso;
    }

    public BilancioAccrualComponentSession createComponentSession() throws BusinessProcessException {
        return (BilancioAccrualComponentSession)createComponentSession("CNRPDG00_EJB_BilancioAccrualComponentSession", BilancioAccrualComponentSession.class);
    }
    private void generaFileAccrual(ActionContext actionContext, AccrualXbrl dati) throws ApplicationException {
        final AccrualService accrualService = SpringUtil.getBean("accrualService", AccrualService.class);
        try {
            byte[] bytes = accrualService.generaFileXbrl(dati);
        } catch (JAXBException e) {
            throw new ApplicationException(e);
        }
    }

    private String getDocumentIdAccrrual(AccrualBulk accrualBulk,AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        return "DOC_SKA_REND";
    }
    private String getEnte(AccrualBulk accrualBulk,AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        return "ISS";
    }

    private String getIdContext(Integer esericizio,AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        if ( AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE==tipofilexbrl)
            return  "CTX_INT_".concat(esericizio.toString());
        return  "CTX_IST_2025".concat( esericizio.toString());

    }

    private Map<String ,ContextXbrl> getContextXbrl(AccrualBulk accrualBulk, AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        Map<String ,ContextXbrl> contexts = new HashMap<String ,ContextXbrl>();
        if ( AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE==tipofilexbrl) {

            contexts.put(getIdContext( accrualBulk.getEsercizio(),tipofilexbrl), new DurationContextXbrl(
                    getIdContext( accrualBulk.getEsercizio(),tipofilexbrl),getEnte(accrualBulk,tipofilexbrl),
                    LocalDate.of(accrualBulk.getEsercizio(), 01, 01),
                    LocalDate.of(accrualBulk.getEsercizio(), 12, 31)));
        }
        if ( AccrualBulk.TIPOFILEXBRL.CONTO_ECONOMICO==tipofilexbrl){
            contexts.put(getIdContext( accrualBulk.getEsercizio(),tipofilexbrl), new DurationContextXbrl(
                    getIdContext( accrualBulk.getEsercizio(),tipofilexbrl), getEnte(accrualBulk,tipofilexbrl),
                    LocalDate.of(accrualBulk.getEsercizio(), 01, 01),
                    LocalDate.of(accrualBulk.getEsercizio(), 12, 31)));
        }
        return contexts;
    }

    private AccrualXbrl getAccrualXbrL(ActionContext actionContext, AccrualBulk accrualBulk, AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        AccrualXbrl accrual = new AccrualXbrl();
            accrual.setEnte(getEnte( accrualBulk,tipofilexbrl));
            accrual.setDocumentId(getDocumentIdAccrrual(accrualBulk,tipofilexbrl));
            accrual.setContexts( getContextXbrl(accrualBulk,tipofilexbrl));

        return  accrual;
    }
    private byte[] creaFileAccrual(AccrualXbrl accrualXbrl) throws JAXBException {
        final AccrualService accrualService = SpringUtil.getBean("accrualService", AccrualService.class);
        return accrualService.generaFileXbrl(accrualXbrl);
    }

    private void addFactc(AccrualXbrl accrualXbrl,List<VpgBilRiclassificatoBulk> datiBilancioRiclassificato, AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        Map<String, List<VpgBilRiclassificatoBulk>> raggrTassonomie= datiBilancioRiclassificato.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(
                        VpgBilRiclassificatoBulk::getNomeTassAccrual,
                        LinkedHashMap::new, // Garantisce che l'ordine delle chiavi rispecchi la lista iniziale
                        Collectors.toList()
                ));
        List<FactXbrl> facts = new ArrayList<FactXbrl>();

        raggrTassonomie.forEach((chiaveTassonomia, listaBulk) -> {
            record TotaleImporti(BigDecimal parzialeAnno, BigDecimal totaleAnno) {
                public TotaleImporti {
                    if (parzialeAnno.compareTo(BigDecimal.ZERO) > 0 && totaleAnno.compareTo(BigDecimal.ZERO) > 0) {
                        throw new IllegalArgumentException("Errore: Entrambi gli importi sono maggiori di zero!");
                    }
                }
                // Metodo per estrarre l'unico valore valorizzato (maggiore di zero)
                public BigDecimal getValoreValido() {
                    if (parzialeAnno.compareTo(BigDecimal.ZERO) > 0) {
                        return parzialeAnno;
                    }
                    return totaleAnno;
                }
                // Metodo helper per sommare
                public static BigDecimal safeAdd(BigDecimal accumulator, BigDecimal val) {
                    BigDecimal cleanVal = (val == null) ? BigDecimal.ZERO : val;
                    return accumulator.add(cleanVal);
                }
            }

            TotaleImporti totali = listaBulk.stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.teeing(
                            Collectors.mapping(VpgBilRiclassificatoBulk::getParzialeIAnno,
                                    Collectors.reducing(BigDecimal.ZERO, TotaleImporti::safeAdd)),
                            Collectors.mapping(VpgBilRiclassificatoBulk::getTotaleIAnno,
                                    Collectors.reducing(BigDecimal.ZERO, TotaleImporti::safeAdd)),

                            // Unione nel record
                            TotaleImporti::new
                    ));

            if ( totali.getValoreValido().compareTo(BigDecimal.ZERO)!=0)
                accrualXbrl.getFacts().add(new FactXbrl(chiaveTassonomia, totali.getValoreValido(),"2",null,null,accrualXbrl.getContexts().get(getIdContext(2025,tipofilexbrl))));

        });
    }
    private byte[] creaFileStatoPatrimoniale(ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException {
        try {
            AccrualXbrl accrualXbrl  =getAccrualXbrL(context,accrualBulk,AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE);
            List resultAttivo = createComponentSession().bilancioRiclasPatr(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_ATTIVITA);
            addFactc(accrualXbrl,resultAttivo, AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE);
            List resultPassivo = createComponentSession().bilancioRiclasPatr(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_PASSIVITA);
            addFactc(accrualXbrl,resultPassivo, AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE);

            return  creaFileAccrual(accrualXbrl) ;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (ComponentException |JAXBException e) {
            throw handleException(e);
        }
    }
    private byte[] creaFileContoEconomico(ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException {
        try {
            AccrualXbrl accrualXbrl  =getAccrualXbrL(context,accrualBulk,AccrualBulk.TIPOFILEXBRL.CONTO_ECONOMICO);
            List resultCE = createComponentSession().bilancioRiclasCE(context.getUserContext(), accrualBulk);
            addFactc(accrualXbrl,resultCE, AccrualBulk.TIPOFILEXBRL.CONTO_ECONOMICO);

            return  creaFileAccrual(accrualXbrl);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (ComponentException |JAXBException e) {
            throw handleException(e);
        }
    }
    private void aggiungiFileAZip(ZipOutputStream zos, String nomeFile, byte[] contenuto) throws IOException {
        byte[] datiSicuri = (contenuto == null) ? new byte[0] : contenuto;
        ZipEntry entry = new ZipEntry(nomeFile);
        zos.putNextEntry(entry);
        zos.write(datiSicuri, 0, datiSicuri.length);
        zos.closeEntry();
    }
    public void creaFileAccrual(ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException {

        try {
            String percorsoZip = "/home/csalvio/Esportazione_Bilancio.zip";
            try (FileOutputStream fos = new FileOutputStream(percorsoZip);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                aggiungiFileAZip(zos, "ISS_STATO_PATRIMONIALE.xbrl", creaFileStatoPatrimoniale(context,accrualBulk));
                aggiungiFileAZip(zos, "ISS_CONTO_ECONOMICO.xbrl", creaFileContoEconomico(context,accrualBulk));
                //aggiungiFileAZip(zos, "3_Note_Informative.csv", fileNote);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw handleException(e);
        }
    }
}