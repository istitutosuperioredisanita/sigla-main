package it.cnr.contab.pdg00.bp;

import it.cnr.contab.pdg00.cdip.bulk.*;
import it.cnr.contab.pdg00.ejb.FlussoStipendiComponentSession;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.jsp.Button;
import it.cnr.jada.util.upload.UploadedFile;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.ejb.EJBException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

/**
 * BP per caricamento + elaborazione flusso stipendi.
 * Usa ESCLUSIVAMENTE il file XLSX caricato.
 * Flusso: salvataggio nel documentale (nome UUID.xlsx, TITLE "FLUSSO STIPENDI ISS MM/YYYY") → elaborazione.
 */
public class CaricFlStipBP extends AllegatiCRUDBP<AllegatoGenericoBulk, CaricFlStipBulk> {

    public static final String FLUSSO_STIPENDI = "Flusso Stipendi";

    public CaricFlStipBP() {}
    public CaricFlStipBP(String s) { super(s); }


    @Override
    public void initialize(ActionContext ctx) throws BusinessProcessException {

        super.initialize(ctx);
        CaricFlStipBulk caricFlStipBulk = (CaricFlStipBulk) this.getModel();
        if (caricFlStipBulk.getArchivioAllegati().isEmpty()) {
            AllegatoGenericoBulk a = new AllegatoGenericoBulk();
            a.setCrudStatus(OggettoBulk.TO_BE_CREATED); // abilita i campi di upload
            caricFlStipBulk.addToArchivioAllegati(a);
        }
        getCrudArchivioAllegati().setModelIndex(ctx, 0);
        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());

    }

    public boolean fillModel(ActionContext actioncontext)
            throws FillException {
        Boolean b = super.fillModel(actioncontext);

        UploadedFile uploadedFile =
                ((it.cnr.jada.action.HttpActionContext) actioncontext).getMultipartParameter("main.ArchivioAllegati.file");
        CaricFlStipBulk caricFlStipBulk = ( CaricFlStipBulk) this.getModel();

        caricFlStipBulk.setArchivioAllegati( new BulkList<>());
            AllegatoGenericoBulk a = new AllegatoGenericoBulk();
            a.setCrudStatus(OggettoBulk.TO_BE_CREATED); // abilita i campi di upload
            caricFlStipBulk.addToArchivioAllegati(a);
        if (!(uploadedFile == null || uploadedFile.getFile() == null || uploadedFile.getFile().length() == 0L)) {
            AllegatoGenericoBulk allegatoGenericoBulk =null;
            if ( (( CaricFlStipBulk) caricFlStipBulk).getArchivioAllegati().isEmpty()){
                allegatoGenericoBulk = new AllegatoGenericoBulk();
                (( CaricFlStipBulk) caricFlStipBulk).addToArchivioAllegati(allegatoGenericoBulk);
            }
                allegatoGenericoBulk=(( CaricFlStipBulk) caricFlStipBulk).getArchivioAllegati().get(0);
                allegatoGenericoBulk.setFile(uploadedFile.getFile());
                allegatoGenericoBulk.setNome(uploadedFile.getName());

            }
        return b;
    }

    @Override
    public void validate(ActionContext actioncontext)
            throws ValidationException {
        super.validate(actioncontext);;
        CaricFlStipBulk caricFlStipBulk = (CaricFlStipBulk) this.getModel();
        if ( Optional.ofNullable(caricFlStipBulk.getArchivioAllegati()).isPresent() &&
                caricFlStipBulk.getArchivioAllegati().size()>0){
            AllegatoGenericoBulk allegatoGenericoBulk= caricFlStipBulk.getArchivioAllegati().get(0);
            allegatoGenericoBulk.validate();
        }
    }



    @Override
    public void create(ActionContext context) throws BusinessProcessException {
            try {
                CaricFlStipBulk caricFlStipBulk = (CaricFlStipBulk) this.getModel();

                File file = caricFlStipBulk.getArchivioAllegati().get(0).getFile();

                GestioneStipBulk stipBulkParsed;
                try (InputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                    stipBulkParsed = processFlussoStipendi(context.getUserContext(), caricFlStipBulk, is, file.getName());
                    caricFlStipBulk.setEsercizio(stipBulkParsed.getStipendiCofiBulk().getEsercizio());
                    caricFlStipBulk.setProgressivo(stipBulkParsed.getStipendiCofiBulk().getMese());
                }
                archiviaAllegati(context);
            } catch (Exception e) {
               throw handleException(e);
            }
    }

    @Override
    protected Boolean isPossibileModifica(AllegatoGenericoBulk allegato) {
        return Boolean.FALSE;
    }
    @Override
    protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
        // Svuota la lista e lascia SOLO un allegato "vuoto" TO_BE_CREATED per l'upload successivo
        CaricFlStipBulk m = (CaricFlStipBulk) getModel();
        m.setTipo_rapporto("");
        BulkList<AllegatoGenericoBulk> nuovo = new BulkList<>();
        AllegatoGenericoBulk a = new AllegatoGenericoBulk();
        a.setCrudStatus(OggettoBulk.TO_BE_CREATED);
        nuovo.add(a);
        m.setArchivioAllegati(nuovo);

        getCrudArchivioAllegati().setModelIndex(actioncontext, 0);
        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());


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
    protected void completeUpdateAllegato(UserContext userContext, AllegatoGenericoBulk allegato) throws ApplicationException {
        throw new ApplicationException("La modifica dell'allegato non è consentita. Inserire un nuovo file.");
    }

    // ---------- ELABORAZIONE XLSX ----------

    private GestioneStipBulk processFlussoStipendi(UserContext userContext,CaricFlStipBulk caricFlStipBulk,InputStream in, String filename) throws ComponentException {
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

    private GestioneStipBulk elaboraWorkbookXlsx(UserContext uc, CaricFlStipBulk caricFlStipBulk,XSSFWorkbook wb) throws ComponentException, BusinessProcessException, RemoteException {
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
        elaboraSheetRitenuteInterno(sheetRitenute, bulk);
        return createComponentSession().gestioneFlussoStipendi(uc, bulk);
    }

    private void elaboraSheetLordiInterno(XSSFSheet sheet, CaricFlStipBulk caricFlStipBulk,GestioneStipBulk bulk) throws ApplicationException {
        DataFormatter fmt = new DataFormatter();
        int headerRowIndex = findHeaderRow(sheet, fmt, new String[]{"esercizio", "mese", "tipo"});
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
            if (isAnyEmpty(esercizio, mese)) continue;

            if (!stipendiCofiSet) {
                stipendiCofi = new Stipendi_cofiBulk();
                stipendiCofi.setEsercizio(parseInteger(esercizio, "esercizio", i));
                stipendiCofi.setMese(null); // gestito da UI; mese reale proviene dal file
                stipendiCofi.setMese_reale(parseInteger(mese, "mese reale (da file)", i));

                stipendiCofi.setTipo_flusso(caricFlStipBulk.getTipo_rapporto());
                bulk.setStipendiCofiBulk(stipendiCofi);
                stipendiCofiSet = true;
            }

            if (!isAnyEmpty(cds, annoObblOrigine, numeroObbl, importo, esercizio)) {
                if (bulk.getStipendiCofiObbScadBulks() == null) {
                    bulk.setStipendiCofiObbScadBulks(new ArrayList<>());
                }
                Stipendi_cofi_obb_scadBulk obbScad = new Stipendi_cofi_obb_scadBulk();
                obbScad.setStipendi_cofi(stipendiCofi);
                obbScad.setStipendi_cofi_obb( new Stipendi_cofi_obbBulk(parseInteger(esercizio, "esercizio_obbligazione", i),
                        fmt.formatCellValue(r.getCell(3)).trim(),
                        parseInteger(esercizio, "esercizio_obbligazione", i),
                        parseInteger(annoObblOrigine, "annoObblOrigine", i),
                        parseLong(numeroObbl, "numeroObbl", i)));
                obbScad.setEsercizio_obbligazione(parseInteger(esercizio, "esercizio_obbligazione", i));
                obbScad.setIm_totale(parseBigDecimal(importo, "importo", i));
                bulk.getStipendiCofiObbScadBulks().add(obbScad);
            }
        }
    }

    private void elaboraSheetRitenuteInterno(XSSFSheet sheet, GestioneStipBulk bulk) throws ApplicationException {
        DataFormatter fmt = new DataFormatter();
        int headerRowIndex = findHeaderRow(sheet, fmt, new String[]{"esercizio", "mese", "codice contributo"});
        if (headerRowIndex == -1) throw new ApplicationException("Header 'esercizio, mese, codice contributo' non trovato.");

        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow r = sheet.getRow(i);
            if (r == null) continue;

            String esercizio       = fmt.formatCellValue(r.getCell(0));
            String mese            = fmt.formatCellValue(r.getCell(1));
            String codiceContributo= fmt.formatCellValue(r.getCell(2));
            String enteDip         = fmt.formatCellValue(r.getCell(4));
            String ammontare       = fmt.formatCellValue(r.getCell(5));

            XSSFCell cellDataInizio = r.getCell(6);
            XSSFCell cellDataFine   = r.getCell(7);

            if (isAnyEmpty(esercizio, mese, codiceContributo, enteDip, ammontare)) continue;

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
            cori.setAmmontare(parseBigDecimal(ammontare, "ammontare", i));
            cori.setDt_da_competenza_coge(parseTimestamp(cellDataInizio, "data inizio", i));
            cori.setDt_a_competenza_coge(parseTimestamp(cellDataFine, "data fine", i));

            bulk.getStipendiCofiCoriBulks().add(cori);
        }
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


    /*Questo metodo cerca in un foglio Excel una riga di intestazione che
	corrisponda a un array di nomi specificati. È fondamentale per trovare il punto di inizio dei dati,
	 indipendentemente dal fatto che ci siano righe vuote o di testo sopra.*/
    public static int findHeaderRow(XSSFSheet sheet, DataFormatter fmt, String[] headerNames) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            XSSFRow r = sheet.getRow(i);
            if (r == null) continue;
            boolean found = true;
            for (int j = 0; j < headerNames.length; j++) {
                XSSFCell cell = r.getCell(j);
                if (cell == null || !headerNames[j].equalsIgnoreCase(fmt.formatCellValue(cell).trim())) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    /*Verifica se una delle stringhe passate in input è null o vuota dopo aver rimosso gli spazi bianchi.
     */
    public static boolean isAnyEmpty(String... strings) {
        for (String s : strings) {
            if (s == null || s.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /*I metodi di parsing convertono i valori delle celle, letti come stringhe,
     nei rispettivi tipi numerici. Sono robusti e gestiscono formati comuni come numeri con o senza decimali e diversi separatori
      (punto o virgola), lanciando un'eccezione personalizzata ApplicationException in caso di errore.
     */
    public static Integer parseInteger(String value, String fieldName, int row) throws ApplicationException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim().replaceAll("\\.0$", ""));
        } catch (NumberFormatException e) {
            throw new ApplicationException("Invalid '" + fieldName + "' format at row " + row, e);
        }
    }

    public static Long parseLong(String value, String fieldName, int row) throws ApplicationException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim().replaceAll("\\.0$", ""));
        } catch (NumberFormatException e) {
            throw new ApplicationException("Invalid '" + fieldName + "' format at row " + row, e);
        }
    }

    public static BigDecimal parseBigDecimal(String value, String fieldName, int row) throws ApplicationException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            String norm = value.trim().replace(".", "").replace(",", ".");
            return new BigDecimal(norm);
        } catch (NumberFormatException e) {
            try {
                String norm = value.trim().replace(",", ".");
                return new BigDecimal(norm);
            } catch (NumberFormatException e2) {
                throw new ApplicationException("Invalid '" + fieldName + "' format at row " + row, e2);
            }
        }
    }

    /*Cerca di leggere le celle della data prima come formato data nativo di Excel,
    usando DateUtil.isCellDateFormatted. Se questo non è possibile, prova a interpretare il contenuto della cella come una stringa.
     */
    public static Timestamp parseTimestamp(XSSFCell cell, String fieldName, int row) throws ApplicationException {
        if (cell == null) {
            return null;
        }

        if (DateUtil.isCellDateFormatted(cell)) {
            // Usa il metodo di utilità della libreria per ottenere la data
            Date d = cell.getDateCellValue();
            return new Timestamp(d.getTime());
        } else {
            // Se non è un formato data numerico, prova a parsarla come stringa
            String value = new DataFormatter().formatCellValue(cell);
            if (value.trim().isEmpty()) {
                return null;
            }
            try {
                // Qui si può aggiungere un parser più robusto se necessario, es. SimpleDateFormat
                return Timestamp.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new ApplicationException("Invalid '" + fieldName + "' format at row " + row, e);
            }
        }
    }



}