package it.cnr.contab.pdg00.bp;

import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.ejb.EsercizioComponentSession;
import it.cnr.contab.config00.esercizio.bulk.EsercizioBulk;
import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.contab.pdg00.bulk.AllegatoAccrualBulk;
import it.cnr.contab.pdg00.bulk.Stampa_vpg_stato_patrim_riclassVBulk;
import it.cnr.contab.pdg00.bulk.VpgBilRiclassificatoBulk;
import it.cnr.contab.pdg00.ejb.BilancioAccrualComponentSession;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.jada.util.jsp.Button;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import it.iss.accrual.xbrl.AccrualService;
import it.iss.accrual.xbrl.AccrualXbrException;
import it.iss.accrual.xbrl.NoDataNotFoundException;
import it.iss.accrual.xbrl.dto.AccrualXbrl;
import it.iss.accrual.xbrl.dto.ContextXbrl;
import it.iss.accrual.xbrl.dto.DurationContextXbrl;
import it.iss.accrual.xbrl.dto.FactXbrl;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CRUDAccrualMefBP extends AllegatiCRUDBP<AllegatoAccrualBulk, AccrualBulk> {

    private static final Logger log = LoggerFactory.getLogger(CRUDAccrualMefBP.class);

    private boolean enabledButton = false;

    private String codice_bdap=null;


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
           EsercizioBulk ultimoEsercizioAperto = (((EsercizioComponentSession) EJBCommonServices
                    .createEJB(
                            "CNRCONFIG00_EJB_EsercizioComponentSession",
                            EsercizioComponentSession.class
                    ))
                    .getLastEsercizioOpen(context.getUserContext()));
            Integer esercizioScrivania = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(context.getUserContext());

            BiPredicate<Integer, Integer> validaEsercizio = (ultimo, scrivania) -> {
                if (ultimo == null) return true;
                int diff = ultimo - scrivania;
                return diff == 0 || diff == 1;
            };
            enabledButton=validaEsercizio.test(Optional.ofNullable(ultimoEsercizioAperto).orElse(new EsercizioBulk()).getEsercizio(), esercizioScrivania);
            codice_bdap =((it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCONFIG00_EJB_Configurazione_cnrComponentSession")).getVal01(context.getUserContext(), Integer.valueOf(0), "*", Configurazione_cnrBulk.PK_BILANCIO_ACCRUAL, Configurazione_cnrBulk.SK_CODICE_BDAP_ENTE);
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
                {"tabModulo","Modulo","/pdg00/tab_accrual_mef_modulo.jsp"},
                {"tabAllegati","Allegati","/util00/tab_allegati.jsp"}
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

    public boolean isPredisponiButtonEnabled() {
        return isEnabledButton() && this.getModel().getDacr()!=null;
    }

    @Override
    public boolean isNewButtonEnabled() {
        return isEnabledButton() && super.isNewButtonEnabled();
    }

    @Override
    public boolean isSaveButtonEnabled() {
        return isEnabledButton() && super.isSaveButtonEnabled();
    }

    @Override
    public boolean isDeleteButtonEnabled() {
        return isEnabledButton() && super.isDeleteButtonEnabled();
    }

    public boolean isEnabledButton() {
        return enabledButton;
    }

    public BilancioAccrualComponentSession createComponentSession() throws BusinessProcessException {
        return (BilancioAccrualComponentSession)createComponentSession("CNRPDG00_EJB_BilancioAccrualComponentSession", BilancioAccrualComponentSession.class);
    }
    private void generaFileAccrual(ActionContext actionContext, AccrualXbrl dati) throws ApplicationException {
        final AccrualService accrualService = SpringUtil.getBean("accrualService", AccrualService.class);
        try {
            byte[] bytes = accrualService.generaFileXbrl(dati);

        } catch (AccrualXbrException e) {
            throw new ApplicationException(e);
        }catch (NoDataNotFoundException e) {
            throw new ApplicationException(e.getMessage());
        }
    }

    private String getDocumentIdAccrrual(AccrualBulk accrualBulk,AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        return "DOC_SKA_REND";
    }
    private String getCodiceBpaoEnte(AccrualBulk accrualBulk,AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        return codice_bdap;
    }

    private String getIdContext(Integer esericizio,AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        if ( AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE==tipofilexbrl
        || AccrualBulk.TIPOFILEXBRL.SCHEMA_AGGIUNTIVO==tipofilexbrl)
            return  "CTX_INT_".concat(esericizio.toString());
        return  "CTX_IST_2025".concat( esericizio.toString());

    }

    private Map<String ,ContextXbrl> getContextXbrl(AccrualBulk accrualBulk, AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        Map<String ,ContextXbrl> contexts = new HashMap<String ,ContextXbrl>();
        if ( AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE==tipofilexbrl
        ||AccrualBulk.TIPOFILEXBRL.SCHEMA_AGGIUNTIVO==tipofilexbrl) {

            contexts.put(getIdContext( accrualBulk.getEsercizio(),tipofilexbrl), new DurationContextXbrl(
                    getIdContext( accrualBulk.getEsercizio(),tipofilexbrl),getCodiceBpaoEnte(accrualBulk,tipofilexbrl),
                    LocalDate.of(accrualBulk.getEsercizio(), 01, 01),
                    LocalDate.of(accrualBulk.getEsercizio(), 12, 31)));
        }
        if ( AccrualBulk.TIPOFILEXBRL.CONTO_ECONOMICO==tipofilexbrl){
            contexts.put(getIdContext( accrualBulk.getEsercizio(),tipofilexbrl), new DurationContextXbrl(
                    getIdContext( accrualBulk.getEsercizio(),tipofilexbrl), getCodiceBpaoEnte(accrualBulk,tipofilexbrl),
                    LocalDate.of(accrualBulk.getEsercizio(), 01, 01),
                    LocalDate.of(accrualBulk.getEsercizio(), 12, 31)));
        }
        return contexts;
    }

    private AccrualXbrl getAccrualXbrL(ActionContext actionContext, AccrualBulk accrualBulk, AccrualBulk.TIPOFILEXBRL tipofilexbrl){
        AccrualXbrl accrual = new AccrualXbrl();
            accrual.setEnte(getCodiceBpaoEnte( accrualBulk,tipofilexbrl));
            accrual.setDocumentId(getDocumentIdAccrrual(accrualBulk,tipofilexbrl));
            accrual.setContexts( getContextXbrl(accrualBulk,tipofilexbrl));

        return  accrual;
    }
    private byte[] creaFileAccrual(AccrualXbrl accrualXbrl) throws NoDataNotFoundException, AccrualXbrException {
        final AccrualService accrualService = SpringUtil.getBean("accrualService", AccrualService.class);
        return accrualService.generaFileXbrl(accrualXbrl);
    }

    private void addFactc(AccrualXbrl accrualXbrl,List<VpgBilRiclassificatoBulk> datiBilancioRiclassificato,AccrualBulk accrualBulk, AccrualBulk.TIPOFILEXBRL tipofilexbrl){
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
                accrualXbrl.getFacts().add(new FactXbrl(chiaveTassonomia, totali.getValoreValido(),"2",null,null,accrualXbrl.getContexts().get(getIdContext(accrualBulk.getEsercizio(),tipofilexbrl))));

        });
    }
    private byte[] creaFileStatoPatrimoniale(ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException, ApplicationException {
        try {
            AccrualXbrl accrualXbrl  =getAccrualXbrL(context,accrualBulk,AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE);
            List resultAttivo = createComponentSession().bilancioRiclasPatr(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_ATTIVITA);
            addFactc(accrualXbrl,resultAttivo, accrualBulk,AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE);
            List resultPassivo = createComponentSession().bilancioRiclasPatr(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_PASSIVITA);
            addFactc(accrualXbrl,resultPassivo, accrualBulk,AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE);

            return  creaFileAccrual(accrualXbrl) ;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (ComponentException e) {
            throw handleException(e);
        }catch (NoDataNotFoundException|AccrualXbrException e) {
            throw new ApplicationException(e);
        }
    }
    private byte[] creaFileContoEconomico(ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException, ApplicationException {
        try {
            AccrualXbrl accrualXbrl  =getAccrualXbrL(context,accrualBulk,AccrualBulk.TIPOFILEXBRL.CONTO_ECONOMICO);
            List resultCE = createComponentSession().bilancioRiclasCE(context.getUserContext(), accrualBulk);
            addFactc(accrualXbrl,resultCE, accrualBulk,AccrualBulk.TIPOFILEXBRL.CONTO_ECONOMICO);

            return  creaFileAccrual(accrualXbrl);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (ComponentException e) {
            throw handleException(e);
        } catch (NoDataNotFoundException|AccrualXbrException e) {
         throw new ApplicationException(e);
        }
    }
    private byte[] creaFileSchemaAggiuntivo(ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException, ApplicationException {
        try {
            AccrualXbrl accrualXbrl  =getAccrualXbrL(context,accrualBulk,AccrualBulk.TIPOFILEXBRL.SCHEMA_AGGIUNTIVO);
            List resultAttivo = createComponentSession().bilancioRiclasPatrSchedaAgg(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_ATTIVITA);
            addFactc(accrualXbrl,resultAttivo, accrualBulk,AccrualBulk.TIPOFILEXBRL.SCHEMA_AGGIUNTIVO);
            List resultPassivo = createComponentSession().bilancioRiclasPatrSchedaAgg(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_PASSIVITA);
            addFactc(accrualXbrl,resultPassivo, accrualBulk,AccrualBulk.TIPOFILEXBRL.SCHEMA_AGGIUNTIVO);
            return  creaFileAccrual(accrualXbrl) ;

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (ComponentException  e) {
            throw handleException(e);
        } catch (NoDataNotFoundException|AccrualXbrException e) {
            throw new ApplicationException(e);
        }
    }

    private void aggiungiFileAZip(ZipOutputStream zos, String nomeFile, byte[] contenuto) throws IOException {
        byte[] datiSicuri = (contenuto == null) ? new byte[0] : contenuto;
        ZipEntry entry = new ZipEntry(nomeFile);
        zos.putNextEntry(entry);
        zos.write(datiSicuri, 0, datiSicuri.length);
        zos.closeEntry();
    }

    private void addFileToArchivioAllegati(ActionContext context,AccrualBulk accrualBulk, File f){

        AllegatoAccrualBulk allegatoAccrualBulk  = Optional.ofNullable(accrualBulk.getArchivioAllegati()).orElse(new BulkList<>()).
                stream().filter(AllegatoAccrualBulk.class::isInstance)
                .map(AllegatoAccrualBulk.class::cast).
                filter(e->e.getAspectName().equalsIgnoreCase(AllegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_XBRL_ZIP)).findAny().orElse(null);
        if ( !Optional.ofNullable(allegatoAccrualBulk).isPresent()) {
            allegatoAccrualBulk = new AllegatoAccrualBulk();
            allegatoAccrualBulk.initializeForInsert(this, context);
            allegatoAccrualBulk.setAspectName(allegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_XBRL_ZIP);
            allegatoAccrualBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);
            accrualBulk.getArchivioAllegati().add(allegatoAccrualBulk);
        }else
            allegatoAccrualBulk.setCrudStatus(OggettoBulk.TO_BE_UPDATED);
        allegatoAccrualBulk.setNome(f.getName());
        allegatoAccrualBulk.setFile(f);
        allegatoAccrualBulk.setContentType( new MimetypesFileTypeMap().getContentType(f.getName()));



    }
    private String  getOutputFileNameOrdine(AccrualBulk accrualBulk){
        return "File Accrual Zip Esercizio2".concat(accrualBulk.getEsercizio().toString()).concat(".zip");
    }
    public void creaFileAccrual(ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException, ApplicationException {

        try {
            File output = new File(System.getProperty("tmp.dir.SIGLAWeb") + "/tmp/", File.separator + getOutputFileNameOrdine( accrualBulk));
            try (FileOutputStream fos = new FileOutputStream(output);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                aggiungiFileAZip(zos, "ISS_STATO_PATRIMONIALE.xbrl", creaFileStatoPatrimoniale(context,accrualBulk));
                aggiungiFileAZip(zos, "ISS_CONTO_ECONOMICO.xbrl", creaFileContoEconomico(context,accrualBulk));
                aggiungiFileAZip(zos, "ISS_SCHEMA_AGGIUNTIVO.xbrl", creaFileSchemaAggiuntivo(context,accrualBulk));
                zos.finish();

                addFileToArchivioAllegati(context,accrualBulk,output);

                setModel(context,accrualBulk);
                update(context);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw handleException(e);
        }
    }
}