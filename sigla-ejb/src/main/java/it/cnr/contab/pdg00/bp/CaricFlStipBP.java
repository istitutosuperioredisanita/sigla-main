package it.cnr.contab.pdg00.bp;

import it.cnr.contab.pdg00.cdip.bulk.*;
import it.cnr.contab.pdg00.ejb.FlussoStipendiComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.OrderConstants;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.jsp.Button;
import it.cnr.jada.util.upload.UploadedFile;
import it.cnr.si.spring.storage.StorageObject;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.ejb.EJBException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Stream;

/**
 * BP per caricamento + elaborazione flusso stipendi.
 * Usa ESCLUSIVAMENTE il file XLSX caricato.
 * Flusso: salvataggio nel documentale (nome UUID.xlsx, TITLE "FLUSSO STIPENDI ISS MM/YYYY") → elaborazione.
 */
public class CaricFlStipBP extends AllegatiCRUDBP<AllegatoGenericoBulk, CaricFlStipBulk> {

    public static final String FLUSSO_STIPENDI = "Flusso Stipendi";
    private Integer esercizio;
    private UserContext uc;

    public CaricFlStipBP() {}
    public CaricFlStipBP(String s) { super(s); }

    @Override
    public void initialize(ActionContext ctx) throws BusinessProcessException {

        super.initialize(ctx);
        this.uc = ctx.getUserContext();
        this.esercizio = CNRUserContext.getEsercizio(ctx.getUserContext());
        setStatus(CRUDBP.INSERT);


        CaricFlStipBulk parent = new CaricFlStipBulk();
        parent.setCrudStatus(OggettoBulk.NORMAL);
        setModel(ctx, initializeModelInsAllegatoStip(ctx, parent));
        assicuraCampiAbilitati(ctx);



    }

    public void create(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
        try {
            try {
                CaricFlStipBulk caricFlStipBulk = (CaricFlStipBulk) this.getModel();
                // Recupera il file caricato e verifica che non sia nullo o vuoto
                it.cnr.jada.util.upload.UploadedFile uploadedFile =
                        ((it.cnr.jada.action.HttpActionContext) context).getMultipartParameter("main.ArchivioAllegati.file");

                if (uploadedFile == null || uploadedFile.getFile() == null || uploadedFile.getFile().length() == 0L) {
                    throw new ApplicationException("File non caricato correttamente o vuoto. Riprovare.");
                }

                File file = uploadedFile.getFile();

                // Elabora il file usando un blocco try-with-resources per garantire la chiusura dello stream
                GestioneStipBulk stipBulkParsed;
                try (InputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                    stipBulkParsed = processFlussoStipendi(context.getUserContext(), caricFlStipBulk,is, file.getName());
                }

                CaricFlStipBulk allegatoStipBulk = setAllegatoStipBulk(stipBulkParsed, file, uploadedFile);

                // Imposta lo stato 'to be created' sull'oggetto allegato nidificato (ci sarà sempre un solo allegato nell'archivio)
                allegatoStipBulk.getArchivioAllegati().get(0).setCrudStatus(OggettoBulk.TO_BE_CREATED);

                // Finalizza la creazione e il salvataggio
                super.create(context);

                setMessage(FormBP.INFO_MESSAGE, "Flusso stipendi elaborato correttamente.");

                // Resetta il tipo rapporto del model dopo l'elaborazione
                ((CaricFlStipBulk) getModel()).setTipo_rapporto("");

            } catch (Exception e) {
                throw handleException(e);
            }
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    @Override
    protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
        super.basicEdit(actioncontext, oggettobulk, flag);
       getCrudArchivioAllegati().setOrderBy(actioncontext, "onlyinsert", OrderConstants.ORDER_DESC);
       getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());
        getCrudArchivioAllegati().setModelIndex(actioncontext, -1);
    }

    public OggettoBulk initializeModelInsAllegatoStip(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
        CaricFlStipBulk parent = (CaricFlStipBulk) oggettobulk;
        String path = getStorePath(parent,true);
        return super.initializeModelForEditAllegati(actioncontext, parent, path, false);
    }



    /** Percorso su CMIS: cartella per FLUSSO_STIPENDI / esercizio / progressivo */
    @Override
    protected String getStorePath(CaricFlStipBulk allegatoParentBulk, boolean create)
            throws BusinessProcessException {
        return CaricFlStipBulk.getStorePathStipendi(FLUSSO_STIPENDI,
                allegatoParentBulk.getEsercizio(), allegatoParentBulk.getProgressivo());
    }

    @Override
    protected Class<AllegatoGenericoBulk> getAllegatoClass() {
        return AllegatoGenericoBulk.class;
    }

    @Override
    public String getAllegatiFormName() {
        return "default";
    }

    @Override
    protected Boolean isPossibileModifica(AllegatoGenericoBulk allegato) { return false; }

    @Override
    protected void completeUpdateAllegato(UserContext userContext, AllegatoGenericoBulk allegato) throws ApplicationException {
        throw new ApplicationException("La modifica dell'allegato non è consentita. Inserire un nuovo file.");
    }

    /** Dopo lo store dell'XLSX, elabora dal documentale */
    @Override
    protected void completeCreateAllegato(AllegatoGenericoBulk allegato, StorageObject so) throws ApplicationException {

    }

    /** Salva (parent) + archivia allegato con nome/titolo richiesti → triggera elaborazione. */
    public void saveFileStipendi(ActionContext context) throws BusinessProcessException {
        try {
            CaricFlStipBulk caricFlStipBulk = (CaricFlStipBulk) this.getModel();
            // Recupera il file caricato e verifica che non sia nullo o vuoto
            it.cnr.jada.util.upload.UploadedFile uploadedFile =
                    ((it.cnr.jada.action.HttpActionContext) context).getMultipartParameter("main.ArchivioAllegati.file");

            if (uploadedFile == null || uploadedFile.getFile() == null || uploadedFile.getFile().length() == 0L) {
                throw new ApplicationException("File non caricato correttamente o vuoto. Riprovare.");
            }

            File file = uploadedFile.getFile();

            // Elabora il file usando un blocco try-with-resources per garantire la chiusura dello stream
            GestioneStipBulk stipBulkParsed;
            try (InputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                stipBulkParsed = processFlussoStipendi(context.getUserContext(), caricFlStipBulk,is, file.getName());
            }

            CaricFlStipBulk allegatoStipBulk = setAllegatoStipBulk(stipBulkParsed, file, uploadedFile);

            // Imposta lo stato 'to be created' sull'oggetto allegato nidificato (ci sarà sempre un solo allegato nell'archivio)
            allegatoStipBulk.getArchivioAllegati().get(0).setCrudStatus(OggettoBulk.TO_BE_CREATED);

            // Finalizza la creazione e il salvataggio
            super.create(context);

            setMessage(FormBP.INFO_MESSAGE, "Flusso stipendi elaborato correttamente.");

            // Resetta il tipo rapporto del model dopo l'elaborazione
            ((CaricFlStipBulk) getModel()).setTipo_rapporto("");

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private CaricFlStipBulk setAllegatoStipBulk(GestioneStipBulk stipBulkParsed, File file, UploadedFile uploadedFile) {
        CaricFlStipBulk allegatoStipBulk = (CaricFlStipBulk) getModel();

        // Aggiorna il modello con i dati elaborati utilizzando un controllo null-safe
        if (stipBulkParsed != null && stipBulkParsed.getStipendiCofiBulk() != null) {
            Stipendi_cofiBulk sc = stipBulkParsed.getStipendiCofiBulk();
            allegatoStipBulk.setEsercizio(sc.getEsercizio());
            allegatoStipBulk.setProgressivo(sc.getMese() != null ? sc.getMese() : sc.getMese_reale());
        }

        // Configura il bulk con il suo allegato
        allegatoStipBulk.setFile(file);
        allegatoStipBulk.setNome(file.getName());
        allegatoStipBulk.setContentType(uploadedFile.getContentType());

        allegatoStipBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);

        // Crea la lista bulk e configura il singolo allegato
        BulkList<AllegatoGenericoBulk> allegati = new BulkList<>();
        allegati.add(allegatoStipBulk);
        allegatoStipBulk.setArchivioAllegati(allegati);
        return allegatoStipBulk;
    }



    // ---------- ELABORAZIONE XLSX ----------

    private GestioneStipBulk processFlussoStipendi(UserContext userContext,CaricFlStipBulk caricFlStipBulk,InputStream in, String filename) throws ApplicationException {
        try {
            String nome = Optional.ofNullable(filename).orElse("").toLowerCase(Locale.ITALY);
            if (!nome.endsWith(".xlsx")) {
                throw new ApplicationException("Formato non supportato: " + filename + ". Caricare un file .xlsx.");
            }

            try (XSSFWorkbook wb = new XSSFWorkbook(in)) {
                return elaboraWorkbookXlsx(userContext,caricFlStipBulk, wb);
            }
        } catch (IOException ex) {
            throw new ApplicationException("Impossibile aprire il file " + filename, ex);
        } catch (BusinessProcessException bpe) {
            throw new ApplicationException("Errore di processo durante l'elaborazione.", bpe);
        }
    }

    private GestioneStipBulk elaboraWorkbookXlsx(UserContext uc, CaricFlStipBulk caricFlStipBulk,XSSFWorkbook wb) throws ApplicationException, BusinessProcessException {
        XSSFSheet sheetLordi = null, sheetRitenute = null;
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            String name = wb.getSheetName(i);
            if (name == null) continue;
            String up = name.toUpperCase(Locale.ITALY);
            if (up.contains("LORD")) sheetLordi = wb.getSheetAt(i);
            else if (up.contains("RITENUTE")) sheetRitenute = wb.getSheetAt(i);
        }
        if (sheetLordi == null)  throw new ApplicationException("Sheet contenente 'LORDI' non trovato.");
        if (sheetRitenute == null) throw new ApplicationException("Sheet contenente 'RITENUTE' non trovato.");

        GestioneStipBulk bulk = new GestioneStipBulk();
        elaboraSheetLordiInterno(sheetLordi, caricFlStipBulk,bulk);
        elaboraSheetRitenuteInterno(sheetRitenute,caricFlStipBulk, bulk);

        try {
            return createComponentSession().gestioneFlussoStipendi(uc, bulk);
        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void elaboraSheetLordiInterno(XSSFSheet sheet, CaricFlStipBulk caricFlStipBulk,GestioneStipBulk bulk) throws ApplicationException {
        DataFormatter fmt = new DataFormatter();
        int headerRowIndex = Utility.findHeaderRow(sheet, fmt, new String[]{"esercizio", "mese", "tipo"});
        if (headerRowIndex == -1) throw new ApplicationException("Header 'esercizio, mese, tipo' non trovato.");

        boolean stipendiCofiSet = false;
        Stipendi_cofiBulk stipendiCofi = null;

        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow r = sheet.getRow(i);
            if (r == null) continue;

            String esercizio = fmt.formatCellValue(r.getCell(0));
            String mese      = fmt.formatCellValue(r.getCell(1));
            String cds       = fmt.formatCellValue(r.getCell(3));
            String annoObblOrigine  = fmt.formatCellValue(r.getCell(4));;
            String numeroObbl= fmt.formatCellValue(r.getCell(5));
            String importo   = fmt.formatCellValue(r.getCell(6));
            if (Utility.isAnyEmpty(esercizio, mese)) continue;

            if (!stipendiCofiSet) {
                stipendiCofi = new Stipendi_cofiBulk();
                stipendiCofi.setEsercizio(Utility.parseInteger(esercizio, "esercizio", i));
                stipendiCofi.setMese(null); // gestito da UI; mese reale proviene dal file
                stipendiCofi.setMese_reale(Utility.parseInteger(mese, "mese reale (da file)", i));

                stipendiCofi.setTipo_flusso(caricFlStipBulk.getTipo_rapporto());

                bulk.setStipendiCofiBulk(stipendiCofi);
                stipendiCofiSet = true;
            }

            if (!Utility.isAnyEmpty(cds, annoObblOrigine, numeroObbl, importo, esercizio)) {
                if (bulk.getStipendiCofiObbScadBulks() == null) {
                    bulk.setStipendiCofiObbScadBulks(new ArrayList<>());
                }
                Stipendi_cofi_obb_scadBulk obbScad = new Stipendi_cofi_obb_scadBulk();


                obbScad.setStipendi_cofi(stipendiCofi);
                //obbScad.setCd_cds_obbligazione(fmt.formatCellValue(r.getCell(3)).trim());
                obbScad.setStipendi_cofi(stipendiCofi);
                obbScad.setKey( new Stipendi_cofi_obb_scadKey(obbScad.getEsercizio(),obbScad.getMese(),
                        fmt.formatCellValue(r.getCell(3)).trim(),
                        Utility.parseInteger(esercizio, "esercizio_obbligazione", i),
                        Utility.parseInteger(annoObblOrigine, "annoObblOrigine", i),
                        Utility.parseLong(numeroObbl, "numeroObbl", i)));

                obbScad.setStipendi_cofi_obb( new Stipendi_cofi_obbBulk(Utility.parseInteger(esercizio, "esercizio_obbligazione", i),
                        fmt.formatCellValue(r.getCell(3)).trim(),
                        Utility.parseInteger(esercizio, "esercizio_obbligazione", i),
                        Utility.parseInteger(annoObblOrigine, "annoObblOrigine", i),
                        Utility.parseLong(numeroObbl, "numeroObbl", i)));
                obbScad.setEsercizio_obbligazione(Utility.parseInteger(esercizio, "esercizio_obbligazione", i));
                //obbScad.setEsercizio_ori_obbligazione(Utility.parseInteger(annoObblOrigine, "annoObblOrigine", i));
                //obbScad.setPg_obbligazione(Utility.parseLong(numeroObbl, "numeroObbl", i));
                obbScad.setIm_totale(Utility.parseBigDecimal(importo, "importo", i));
                bulk.getStipendiCofiObbScadBulks().add(obbScad);
            }
        }
    }

    private void elaboraSheetRitenuteInterno(XSSFSheet sheet,  CaricFlStipBulk caricFlStipBulk,GestioneStipBulk bulk) throws ApplicationException {
        DataFormatter fmt = new DataFormatter();
        int headerRowIndex = Utility.findHeaderRow(sheet, fmt, new String[]{"esercizio", "mese", "codice contributo"});
        if (headerRowIndex == -1) throw new ApplicationException("Header 'esercizio, mese, codice contributo' non trovato.");

        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow r = sheet.getRow(i);
            if (r == null) continue;

            String esercizio       = fmt.formatCellValue(r.getCell(0));
            String mese            = fmt.formatCellValue(r.getCell(1));
            String codiceContributo= fmt.formatCellValue(r.getCell(2));
            String tipo            = fmt.formatCellValue(r.getCell(3));
            String enteDip         = fmt.formatCellValue(r.getCell(4));
            String ammontare       = fmt.formatCellValue(r.getCell(5));

            XSSFCell cellDataInizio = r.getCell(6);
            XSSFCell cellDataFine   = r.getCell(7);

            if (Utility.isAnyEmpty(esercizio, mese, codiceContributo, tipo, enteDip, ammontare)) continue;

            if (bulk.getStipendiCofiCoriBulks() == null) {
                bulk.setStipendiCofiCoriBulks(new ArrayList<>());
            }

            Stipendi_cofi_coriBulk cori = new Stipendi_cofi_coriBulk(bulk.getStipendiCofiBulk().getEsercizio(),
                    bulk.getStipendiCofiBulk().getMese(),
                    codiceContributo.trim(),
                    enteDip.trim());
            cori.setStipendi_cofi(bulk.getStipendiCofiBulk());
            cori.setCd_contributo_ritenuta(codiceContributo.trim());
            cori.setTi_ente_percipiente(enteDip.trim());
            cori.setAmmontare(Utility.parseBigDecimal(ammontare, "ammontare", i));
            cori.setDt_da_competenza_coge(Utility.parseTimestamp(cellDataInizio, "data inizio", i));
            cori.setDt_a_competenza_coge(Utility.parseTimestamp(cellDataFine, "data fine", i));

            bulk.getStipendiCofiCoriBulks().add(cori);
        }
    }

    private void assicuraCampiAbilitati(ActionContext ctx) {
        CaricFlStipBulk m = (CaricFlStipBulk) getModel();
        if (m.getArchivioAllegati().isEmpty()) {
            AllegatoGenericoBulk a = new AllegatoGenericoBulk();
            a.setCrudStatus(OggettoBulk.TO_BE_CREATED); // abilita i campi di upload
            m.addToArchivioAllegati(a);
        }
        getCrudArchivioAllegati().setModelIndex(ctx, 0);
        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());
    }

    public FlussoStipendiComponentSession createComponentSession() throws EJBException, BusinessProcessException {
        return (FlussoStipendiComponentSession) createComponentSession(
                "CNRPDG00_EJB_FlussoStipendiComponentSession",
                FlussoStipendiComponentSession.class
        );
    }

    @Override
    protected Button[] createToolbar() {
        final Properties props = it.cnr.jada.util.Config.getHandler().getProperties(CRUDBP.class);
        return Stream.of(new Button(props, "CRUDToolbar.save")).toArray(Button[]::new);
    }

    @Override public boolean isNewButtonHidden()        { return true; }
    @Override public boolean isDeleteButtonHidden()     { return true; }
    @Override public boolean isSearchButtonHidden()     { return true; }
    @Override public boolean isFreeSearchButtonHidden() { return true; }
    @Override public boolean isSaveButtonEnabled()      { return true; }
}