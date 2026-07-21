package it.cnr.contab.pdg00.bp;

import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.ejb.EsercizioComponentSession;
import it.cnr.contab.config00.esercizio.bulk.EsercizioBulk;
import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.contab.pdg00.bulk.AllegatoAccrualBulk;
import it.cnr.contab.pdg00.bulk.Stampa_vpg_stato_patrim_riclassVBulk;
import it.cnr.contab.pdg00.bulk.VpgBilRiclassificatoBulk;
import it.cnr.contab.pdg00.ejb.BilancioAccrualComponentSession;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.contab.reports.bulk.Report;
import it.cnr.contab.reports.service.PrintService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcess;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
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
import it.iss.accrual.xbrl.dto.*;
import jakarta.activation.MimetypesFileTypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private boolean checkFileAccrual(AllegatoGenericoBulk allegato){
       return Optional.ofNullable(allegato)
                .filter(AllegatoAccrualBulk.class::isInstance)
                .map(AllegatoAccrualBulk.class::cast)
                .map(AllegatoAccrualBulk::isAllegatoCancModidicabile).orElse(Boolean.FALSE);

    }

    @Override
    protected Boolean isPossibileCancellazione(AllegatoGenericoBulk allegato) {
        return checkFileAccrual( allegato);

    }

    public boolean isCreaFileAccrualButtonEnabled() {
        return isEnabledButton() && ( !(AccrualBulk.STATO_INVIATO.equalsIgnoreCase((( AccrualBulk)this.getModel()).getStato())));
    }

    @Override
    public boolean isNewButtonEnabled() {
        return isEnabledButton() && super.isNewButtonEnabled() && ( !super.isEditing());
    }

    @Override
    public boolean isSaveButtonEnabled() {
        return isEnabledButton() && super.isSaveButtonEnabled();
    }

    @Override
    public boolean isDeleteButtonEnabled() {
        return isEnabledButton() && super.isDeleteButtonEnabled()
                && ( !(AccrualBulk.STATO_INVIATO.equalsIgnoreCase((( AccrualBulk)this.getModel()).getStato())));
    }

    public boolean isEnabledButton() {
        return enabledButton;
    }

    public enum StampaBilancioRiclassificato
    {
        STATO_PATRIMONIALIE( "/cnrpreventivo/pdg/statopatrimonialericlassificato.jasper"),
        CONTO_ECONOMICO( "/cnrpreventivo/pdg/contoeconomicoriclassificato.jasper");
        private String nomeReport;
        StampaBilancioRiclassificato( String nomeReport){
            this.nomeReport=nomeReport;
        }

        public String getNomeReport() {
            return nomeReport;
        }

        public void setNomeReport(String nomeReport) {
            this.nomeReport = nomeReport;
        }

        public static StampaBilancioRiclassificato fromNomeReport(String nomeReport) {
            for (StampaBilancioRiclassificato nome : StampaBilancioRiclassificato.values()) {
                if (nome.nomeReport.equals(nomeReport)) {
                    return nome;
                }
            }
            throw new IllegalArgumentException(
                    "StampaBilancioRiclassificato non trovato: " + nomeReport
            );
        }
    }

    public enum SchemaBilancio{
        ACCRUAL( "ACCRUAL"),
        ACCRUAL_AGG( "ACCRUAL-AGG");
        String  tipoSchema;
        SchemaBilancio( String tipoSchema){
            this.tipoSchema=tipoSchema;
        }

        public String getTipoSchema() {
            return tipoSchema;
        }

        public void setTipoSchema(String tipoSchema) {
            this.tipoSchema = tipoSchema;
        }
        public static SchemaBilancio fromTipoSchema(String tipoSchema) {
            for (SchemaBilancio tipo : SchemaBilancio.values()) {
                if (tipo.tipoSchema.equals(tipoSchema)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException(
                    "Tipo Schema non trovato: " + tipoSchema
            );
        }
    }
    public enum TipoBilancio{
        ATTIVO( "SPATT"),
        PASSIVO( "SPPAS");
        String  tipoBilancio;
        TipoBilancio( String tipoBilancio){
            this.tipoBilancio=tipoBilancio;
        }

        public String getTipoBilancio() {
            return tipoBilancio;
        }

        public void setTipoBilancio(String tipoBilancio) {
            this.tipoBilancio = tipoBilancio;
        }
        public static TipoBilancio fromTipoBilancio(String tipoBilancio) {
            for (TipoBilancio tipo : TipoBilancio.values()) {
                if (tipo.tipoBilancio.equals(tipoBilancio)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException(
                    "Tipo bilancio non trovato: " + tipoBilancio
            );
        }

    }

    private String getNomeReportPatrimoniale( StampaBilancioRiclassificato stampaBilancioRiclassificato,
                                  SchemaBilancio schemaBilancio,
                                  TipoBilancio tipoBilancio){
        String nomeReport=null;
        if ( SchemaBilancio.ACCRUAL.equals(schemaBilancio)) {
            nomeReport = "StatoPatrimoniale";
            if ( TipoBilancio.ATTIVO.equals(tipoBilancio))
                nomeReport= nomeReport.concat("Attività.pdf");
            else
                nomeReport= nomeReport.concat("Passività.pdf");
        }
        else {
            nomeReport = "SchemaAggiuntivo";
            if ( TipoBilancio.ATTIVO.equals(tipoBilancio))
                nomeReport= nomeReport.concat("SPAtt.pdf");
            else
                nomeReport= nomeReport.concat("SPPass.pdf");
        }

        return nomeReport;
    }
    private String getNomeReportContoEconomico( StampaBilancioRiclassificato stampaBilancioRiclassificato,
                                              SchemaBilancio tipoBilancio){
        return "ContoEconimico.pdf";
    }
    private  Print_spoolerBulk stampaBilancioRiclStatoPatrimoniale(
            UserContext userContext,
            SchemaBilancio schemaBilancio,
            TipoBilancio tipoBilancio)  {

                Print_spoolerBulk print = new Print_spoolerBulk();
                print.setFlEmail(false);
                print.setReport(StampaBilancioRiclassificato.STATO_PATRIMONIALIE.getNomeReport());
                print.setNomeFile(getNomeReportPatrimoniale(StampaBilancioRiclassificato.STATO_PATRIMONIALIE, schemaBilancio, tipoBilancio));
                print.setUtcr(userContext.getUser());
                print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());

                print.addParam("CD_TIPO_BILANCIO", schemaBilancio.getTipoSchema(), String.class);
                print.addParam("ATTPAS", tipoBilancio.getTipoBilancio(), String.class);
                print.addParam("dettaglioConti", "N", String.class);
                print.addParam("uo", "*", String.class);
                print.addParam("CDS", it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(userContext), String.class);
                print.addParam("inEs", it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext), Integer.class);
                print.addParam("IST_COMM", "*", String.class);

        return print;
    }
    private  Print_spoolerBulk stampaBilancioRiclContoEconomico(
            UserContext userContext,
            SchemaBilancio schemaBilancio) {

            Print_spoolerBulk print = new Print_spoolerBulk();
            print.setFlEmail(false);
            print.setReport(StampaBilancioRiclassificato.CONTO_ECONOMICO.getNomeReport());
            print.setNomeFile(getNomeReportContoEconomico(StampaBilancioRiclassificato.CONTO_ECONOMICO, schemaBilancio));
            print.setUtcr(userContext.getUser());
            print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());

            print.addParam("dettaglioConti", "N", String.class);
            print.addParam("uo", "*", String.class);
            print.addParam("CDS", it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(userContext), String.class);
            print.addParam("inEs", it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext), Integer.class);
            print.addParam("IST_COMM", "*", String.class);
            print.addParam("CD_TIPO_BILANCIO", schemaBilancio.getTipoSchema(), String.class);
        return print;
    }


    private  class AccrualReport extends Report {

        private final Print_spoolerBulk spooler;

        public AccrualReport(Report report, Print_spoolerBulk spooler) {
            super(
                    report.getName(),
                    report.getBytes(),
                    report.getContentType(),
                    report.getContentLength()
            );
            this.spooler = spooler;
        }

        public Print_spoolerBulk getSpooler() {
            return spooler;
        }
    }
    private List<AccrualReport> eseguiReportToStorage(UserContext userContext){
        List<Print_spoolerBulk> reports = new ArrayList<Print_spoolerBulk>();
        reports.add( stampaBilancioRiclStatoPatrimoniale( userContext,SchemaBilancio.ACCRUAL,TipoBilancio.ATTIVO));
        reports.add( stampaBilancioRiclStatoPatrimoniale( userContext,SchemaBilancio.ACCRUAL,TipoBilancio.PASSIVO));
        reports.add( stampaBilancioRiclStatoPatrimoniale( userContext,SchemaBilancio.ACCRUAL_AGG,TipoBilancio.ATTIVO));
        reports.add( stampaBilancioRiclStatoPatrimoniale( userContext,SchemaBilancio.ACCRUAL_AGG,TipoBilancio.PASSIVO));
        reports.add( stampaBilancioRiclContoEconomico( userContext,SchemaBilancio.ACCRUAL));
        ExecutorService executor = Executors.newFixedThreadPool(
                reports.size());
        try {
            PrintService printService=SpringUtil.getBean("printService", PrintService.class);
            List<CompletableFuture<AccrualReport>> futures = reports.stream()
                    .map(spooler -> CompletableFuture.supplyAsync(
                            () -> {
                                try {
                                    Report report = printService.executeReport(userContext,spooler);
                                    return new AccrualReport(report, spooler);
                                } catch (IOException|ComponentException e) {
                                    throw new CompletionException(e);
                                }
                            },
                            executor))
                    .toList();

            CompletableFuture<Void> all =
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            all.join();

            List<AccrualReport> files = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            return files;

        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
/*
            if (cause instanceof IOException io) {
                throw io;
            }

            if (cause instanceof ComponentException ce) {
                throw ce;
            }

            throw e;
*/
            // Rilancia l'eccezione originale oppure una tua eccezione applicativa
            if (cause instanceof RuntimeException re) {
                throw re;
            }

            throw new RuntimeException("Errore durante la generazione dei report", cause);

        } finally {
            executor.shutdown();
        }
    }

    public BilancioAccrualComponentSession createComponentSession() throws BusinessProcessException {
        return (BilancioAccrualComponentSession)createComponentSession("CNRPDG00_EJB_BilancioAccrualComponentSession", BilancioAccrualComponentSession.class);
    }
    @Override
    public String getAllegatiFormName() {
        if (this.getCrudArchivioAllegati().getModel() != null && !this.getCrudArchivioAllegati().getModel().isNew())
            if (!((((AllegatoAccrualBulk) this.getCrudArchivioAllegati().getModel()).isAllegatoCancModidicabile())))
                return "readonly";
        return "default";
    }
    @Override
    public BusinessProcess initBusinessProcess(ActionContext actioncontext) throws BusinessProcessException {
        BusinessProcess businessProcess = super.initBusinessProcess(actioncontext);
        BilancioAccrualComponentSession session = createComponentSession();
        AccrualBulk accrualBulk = null;
        try {
            accrualBulk = (AccrualBulk) session.findByPrimaryKey(actioncontext.getUserContext(), new AccrualBulk(CNRUserContext.getEsercizio(actioncontext.getUserContext()),null,null));
        } catch (ComponentException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        if ( Optional.ofNullable(accrualBulk).isPresent())
                edit(actioncontext,accrualBulk);
        else{

        }

        return this;
    }

    private String getDocumentIdAccrrual(AccrualBulk accrualBulk){
        return "DOC_SKA_REND";
    }
    private String getCodiceBdapEnte(){
        return codice_bdap;
    }

    private String getIdContext(Integer esericizio,boolean isInstantContext){
        if (isInstantContext)
            return  "CTX_IST_".concat(esericizio.toString());
        return  "CTX_INT_2025".concat( esericizio.toString());

    }

    private Map<String ,ContextXbrl> getContextXbrl(AccrualBulk accrualBulk){
        Map<String ,ContextXbrl> contexts = new HashMap<String ,ContextXbrl>();

            contexts.put(getIdContext( accrualBulk.getEsercizio(),true), new InstantContextXbrl(
                    getIdContext( accrualBulk.getEsercizio(),true),getCodiceBdapEnte(),
                    LocalDate.of(accrualBulk.getEsercizio(), 12, 31)));

            contexts.put(getIdContext( accrualBulk.getEsercizio(),false), new DurationContextXbrl(
                    getIdContext( accrualBulk.getEsercizio(),false), getCodiceBdapEnte(),
                    LocalDate.of(accrualBulk.getEsercizio(), 01, 01),
                    LocalDate.of(accrualBulk.getEsercizio(), 12, 31)));

        return contexts;
    }

    private AccrualXbrl getAccrualXbrL(ActionContext actionContext, AccrualBulk accrualBulk){
        AccrualXbrl accrual = new AccrualXbrl();
            accrual.setEnte(getCodiceBdapEnte( ));
            accrual.setDocumentId(getDocumentIdAccrrual(accrualBulk));
            accrual.setContexts( getContextXbrl(accrualBulk));

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
            record TotaleImporti(String chiaveTassonomia,BigDecimal parzialeAnno, BigDecimal totaleAnno) {
                public TotaleImporti {
                    if (parzialeAnno.compareTo(BigDecimal.ZERO) != 0 && totaleAnno.compareTo(BigDecimal.ZERO) != 0) {
                        throw new IllegalArgumentException("Errore: Per la Tassonomia ".concat( chiaveTassonomia).concat(" Entrambi gli importi, parzialeAnno e totaleAnno, sono diversi da zero!"));
                    }
                }
                // Metodo per estrarre l'unico valore valorizzato (maggiore di zero)
                public BigDecimal getValoreValido() {
                    if (parzialeAnno.compareTo(BigDecimal.ZERO) != 0) {
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

                            (parziale, totale) ->
                                    new TotaleImporti(chiaveTassonomia, parziale, totale)
                    ));

            if ( totali.getValoreValido().compareTo(BigDecimal.ZERO)!=0)
                accrualXbrl.getFacts().add(new FactXbrl(chiaveTassonomia, totali.getValoreValido(),"2",null,null,accrualXbrl.getContexts().get(getIdContext(accrualBulk.getEsercizio(),
                        ( AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE==tipofilexbrl
                        || AccrualBulk.TIPOFILEXBRL.SCHEMA_AGGIUNTIVO==tipofilexbrl )))));

        });
    }

    private byte[] createFileXbr( ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException, ApplicationException {
        try {
            AccrualXbrl accrualXbrl  =getAccrualXbrL(context,accrualBulk);
            List resultAttivo = createComponentSession().bilancioRiclasPatr(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_ATTIVITA);
            addFactc(accrualXbrl,resultAttivo, accrualBulk,AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE);
            List resultPassivo = createComponentSession().bilancioRiclasPatr(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_PASSIVITA);
            addFactc(accrualXbrl,resultPassivo, accrualBulk,AccrualBulk.TIPOFILEXBRL.STATO_PATRIMONIALE);
            List resultSchedaAttivo = createComponentSession().bilancioRiclasPatrSchedaAgg(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_ATTIVITA);
            addFactc(accrualXbrl,resultSchedaAttivo, accrualBulk,AccrualBulk.TIPOFILEXBRL.SCHEMA_AGGIUNTIVO);
            List resultSchedaPassivo = createComponentSession().bilancioRiclasPatrSchedaAgg(context.getUserContext(), accrualBulk, Stampa_vpg_stato_patrim_riclassVBulk.TIPO_PASSIVITA);
            addFactc(accrualXbrl,resultSchedaPassivo, accrualBulk,AccrualBulk.TIPOFILEXBRL.SCHEMA_AGGIUNTIVO);
            List resultCE = createComponentSession().bilancioRiclasCE(context.getUserContext(), accrualBulk);
            addFactc(accrualXbrl,resultCE, accrualBulk,AccrualBulk.TIPOFILEXBRL.CONTO_ECONOMICO);

            return  creaFileAccrual(accrualXbrl);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (ComponentException  e) {
            throw handleException(e);
        } catch (NoDataNotFoundException|AccrualXbrException|IllegalArgumentException e) {
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
    private String getDescrizioneFileXbrlAccrual(ActionContext context,AccrualBulk accrualBulk){
        return "File Accrual XBRL anno bilancio "+String.valueOf(accrualBulk.getEsercizio());
    }

    private void addFileXbrlToArchivioAllegati(ActionContext context,AccrualBulk accrualBulk, File f){

        AllegatoAccrualBulk allegatoAccrualBulk  = accrualBulk.getAllegatoAccrualXbrl();
        if ( !Optional.ofNullable(allegatoAccrualBulk).isPresent()) {
            allegatoAccrualBulk = new AllegatoAccrualBulk();
            allegatoAccrualBulk.initializeForInsert(this, context);
            allegatoAccrualBulk.setAspectName(allegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_XBRL_ZIP);
            allegatoAccrualBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);
            accrualBulk.getArchivioAllegati().add(allegatoAccrualBulk);
        }else
            allegatoAccrualBulk.setCrudStatus(OggettoBulk.TO_BE_UPDATED);
        allegatoAccrualBulk.setNome(f.getName());
        allegatoAccrualBulk.setDescrizione(getDescrizioneFileXbrlAccrual( context,accrualBulk));
        allegatoAccrualBulk.setFile(f);
        allegatoAccrualBulk.setContentType( new MimetypesFileTypeMap().getContentType(f.getName()));
    }

    private String  getOutputFileNameZip(AccrualBulk accrualBulk){
        return getPrefixFile( accrualBulk).concat(".zip");
    }
    private String  getPrefixFile(AccrualBulk accrualBulk){
        return (accrualBulk.getEsercizio().toString()).concat(getCodiceBdapEnte()).concat("RENDSKA");
    }

    private SchemaBilancio getSchemaBilancio( AccrualReport report) {
        Print_spooler_paramBulk bilancio = null;
        for (Object obj : report.getSpooler().getParams()) {
            Print_spooler_paramBulk p = (Print_spooler_paramBulk) obj;
            if ("CD_TIPO_BILANCIO".equals(p.getNomeParam())) {
                bilancio=p;
                break;
            }
        }
        if ( Optional.ofNullable(bilancio).isPresent())
            return SchemaBilancio.fromTipoSchema(bilancio.getValoreParam());
        return null;
    }
    private TipoBilancio getTipoBilancio( AccrualReport report) {
        Print_spooler_paramBulk tipoBilancio = null;
        for (Object obj : report.getSpooler().getParams()) {
            Print_spooler_paramBulk p = (Print_spooler_paramBulk) obj;
            if ("ATTPAS".equals(p.getNomeParam())) {
                tipoBilancio=p;
                break;
            }
        }
        if ( Optional.ofNullable(tipoBilancio).isPresent())
            return TipoBilancio.fromTipoBilancio(tipoBilancio.getValoreParam());
        return null;
    }
    private String getAspetNameReport( AccrualReport report){
        if ( StampaBilancioRiclassificato.CONTO_ECONOMICO.equals ( StampaBilancioRiclassificato.fromNomeReport(report.spooler.getReport())))
            return AllegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_ACCRUAL_CONTO_ECONOMICO;
        if ( StampaBilancioRiclassificato.STATO_PATRIMONIALIE.equals ( StampaBilancioRiclassificato.fromNomeReport(report.spooler.getReport()))){
           // recuper il tipo i bilancio
            SchemaBilancio bilancio = getSchemaBilancio( report);
            TipoBilancio tipoBilancio=getTipoBilancio( report );
            if ( SchemaBilancio.ACCRUAL.equals(bilancio)){
                if ( TipoBilancio.ATTIVO.equals(tipoBilancio))
                    return AllegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_ACCRUAL_STATO_PATR_ATTIVO;
                if ( TipoBilancio.PASSIVO.equals(tipoBilancio))
                    return AllegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_ACCRUAL_STATO_PATR_PASSIVO;
            }
            if ( SchemaBilancio.ACCRUAL_AGG.equals(bilancio)){
                if ( TipoBilancio.ATTIVO.equals(tipoBilancio))
                    return AllegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_ACCRUAL_AGG_STATO_PATR_ATTIVO;
                if ( TipoBilancio.PASSIVO.equals(tipoBilancio))
                    return AllegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_ACCRUAL_AGG_STATO_PATR_PASSIVO;
            }

        }
        return null;
    }
    private void addFileReportToArchivioAllegati(ActionContext context,AccrualBulk accrualBulk, AccrualReport report) throws ComponentException {
        try {
            String aspectName = getAspetNameReport(report);
            AllegatoAccrualBulk allegatoAccrualBulk = accrualBulk.getAllegatoAccrualFromAspectName(aspectName);
            if (!Optional.ofNullable(allegatoAccrualBulk).isPresent()) {
                allegatoAccrualBulk = new AllegatoAccrualBulk();
                allegatoAccrualBulk.initializeForInsert(this, context);
                allegatoAccrualBulk.setAspectName(aspectName);
                allegatoAccrualBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);
                accrualBulk.getArchivioAllegati().add(allegatoAccrualBulk);
            } else
                allegatoAccrualBulk.setCrudStatus(OggettoBulk.TO_BE_UPDATED);
            allegatoAccrualBulk.setNome(report.getName());
            allegatoAccrualBulk.setDescrizione(report.getName());
            File output = new File(System.getProperty("tmp.dir.SIGLAWeb") + "/tmp/", File.separator + report.getName());

            FileOutputStream f = new FileOutputStream(output);
            f.write(report.getBytes());
            allegatoAccrualBulk.setFile(output);
            allegatoAccrualBulk.setContentType(new MimetypesFileTypeMap().getContentType(report.getName()));
        }catch (IOException e){
            throw new ComponentException(e);
        }

    }

    private void addFilesReportToArichivioAllegati(ActionContext context,AccrualBulk accrualBulk) throws ComponentException {
        List<AccrualReport> reports = eseguiReportToStorage(context.getUserContext());
        for ( AccrualReport report:reports){
            addFileReportToArchivioAllegati( context,accrualBulk,report);
        }
        System.out.println("Ecco");
    }
    public void creaFileAccrual(ActionContext context,AccrualBulk accrualBulk) throws BusinessProcessException, ApplicationException {

        try {
            File output = new File(System.getProperty("tmp.dir.SIGLAWeb") + "/tmp/", File.separator + getOutputFileNameZip( accrualBulk));
            FileOutputStream fos = new FileOutputStream(output);
                 ZipOutputStream zos = new ZipOutputStream(fos);
                    aggiungiFileAZip(zos, getPrefixFile(accrualBulk).concat(".xbrl"), createFileXbr(context,accrualBulk));
                    zos.finish();
                    addFileXbrlToArchivioAllegati(context,accrualBulk,output);
                    //crea e aggiunge i report agli allegati
                    addFilesReportToArichivioAllegati(context,accrualBulk);
                    setModel(context,accrualBulk);
                    update(context);
                    this.edit(context,accrualBulk);


        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw handleException(e);
        }catch (ComponentException e) {
            throw handleException(e);
        }
    }
}