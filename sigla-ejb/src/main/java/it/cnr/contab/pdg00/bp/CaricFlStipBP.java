package it.cnr.contab.pdg00.bp;

import it.cnr.contab.gestiva00.core.bulk.V_cons_dett_ivaBulk;
import it.cnr.contab.gestiva00.ejb.StampaRegistriIvaComponentSession;
import it.cnr.contab.pdg00.cdip.bulk.*;
import it.cnr.contab.pdg00.ejb.FlussoStipendiComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentIBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.OrderConstants;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.action.SimpleCRUDBP;
import it.cnr.jada.util.jsp.Button;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.ejb.EJBException;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.isEmpty;


public class CaricFlStipBP extends SimpleCRUDBP {
    public static final String FLUSSO_STIPENDI = "Flusso Stipendi";
    private Integer esercizio;
    private static final Logger logger = LoggerFactory.getLogger(CaricFlStipBP.class);

    public CaricFlStipBP() {
    }

    public CaricFlStipBP(String s) {
        super(s);
    }


    protected void init(Config config, ActionContext context) throws BusinessProcessException {
        super.init(config, context);
        setModel(context, new CaricFlStipBulk());
        //setModel(context, new GestioneStipBulk());

//        try {
//            doCaricaGestioneStip(context);
//        } catch (Exception e) {
//            throw new BusinessProcessException(
//                    "Errore durante il caricamento Gestione Stipendi da Excel: " + e.getMessage(), e
//            );
//        }
    }


    public void doCaricaGestioneStip(ActionContext context)
            throws BusinessProcessException, ComponentException, IOException {

        String path = "C:\\Users\\mirra_davide\\Desktop\\JAVA\\ISS\\penthao workflow\\Flusso Stipendi\\FLUSSO SIGLA ORGANI AGOSTO 2025.xlsx";
        File file = new File(path);
        if (!file.exists()) {
            throw new ApplicationException("File non trovato: " + path);
        }

        // Crea il bulk temporaneo per l'elaborazione, MA NON settarlo come model
        GestioneStipBulk bulk = new GestioneStipBulk();
        // RIMUOVI QUESTA RIGA: setModel(context, bulk);

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
             XSSFWorkbook wb = new XSSFWorkbook(in)) {

            XSSFSheet sheetLordi = null;
            XSSFSheet sheetRitenute = null;

            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                String name = wb.getSheetName(i);
                if (name == null) continue;
                String up = name.toUpperCase();
                if (up.contains("LORDI")) sheetLordi = wb.getSheetAt(i);
                else if (up.contains("RITENUTE")) sheetRitenute = wb.getSheetAt(i);
            }

            if (sheetLordi == null)  throw new ApplicationException("Sheet contenente 'LORDI' non trovato nel file!");
            if (sheetRitenute == null) throw new ApplicationException("Sheet contenente 'RITENUTE' non trovato nel file!");

            // Pulisci eventuali liste precedenti se vuoi ricaricare
            bulk.setStipendiCofiObbScadBulks(null);
            bulk.setStipendiCofiCoriBulks(null);

            // Manteniamo lo Stipendi_cofiBulk esistente (che ha già tipo_flusso),
            // ma se è null lo creiamo adesso.
            if (bulk.getStipendiCofiBulk() == null) {
                bulk.setStipendiCofiBulk(new Stipendi_cofiBulk());
            }

            // LORDI → popolazione Stipendi_cofiBulk + ObbScad
            elaboraSheetLordi(context, sheetLordi, bulk);

            // RITENUTE → popolazione Cori
            elaboraSheetRitenute(context, sheetRitenute, bulk);

            //LOG BULK
            System.out.println("BULK: " + bulk.toString());

            // Chiama il component per salvare i dati
            createComponentSession().gestioneFlussoStipendi(context.getUserContext(), bulk);

        } catch (IllegalArgumentException e) {
            throw new ApplicationException("Formato file non valido!", e);
        } catch (RecordFormatException e) {
            throw new ApplicationException("Errore nella lettura del file!", e);
        }
    }



    private void elaboraSheetLordi(ActionContext context, XSSFSheet sheet, GestioneStipBulk bulk) throws ApplicationException {
        DataFormatter fmt = new DataFormatter();
        int headerRowIndex = Utility.findHeaderRow(sheet, fmt, new String[]{"esercizio", "mese", "tipo"});

        if (headerRowIndex == -1) {
            throw new ApplicationException("Header row 'esercizio, mese, tipo' not found in the sheet.");
        }

        boolean stipendiCofiSet = false;

        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow r = sheet.getRow(i);
            if (r == null) continue;

            String esercizio = fmt.formatCellValue(r.getCell(0));
            String mese = fmt.formatCellValue(r.getCell(1));
            String tipo = fmt.formatCellValue(r.getCell(2));
            String cds = fmt.formatCellValue(r.getCell(3));
            String annoObbl = fmt.formatCellValue(r.getCell(4));
            String numeroObbl = fmt.formatCellValue(r.getCell(5));
            String importo = fmt.formatCellValue(r.getCell(6));
            String esercizioObbl = fmt.formatCellValue(r.getCell(12));

            if (Utility.isAnyEmpty(esercizio, mese)) {
                continue;
            }

            Stipendi_cofiBulk stipendiCofi = new Stipendi_cofiBulk();
            if (!stipendiCofiSet) {
                stipendiCofi.setEsercizio(Utility.parseInteger(esercizio, "esercizio", i));

                // NON imposto la PK "mese" (progressivo): lascio null per farlo settare alla home
                stipendiCofi.setMese(null);

                // Imposta il mese "reale" letto da Excel (il campo si chiama mese)
                stipendiCofi.setMese_reale(Utility.parseInteger(mese, "mese reale (da file)", i));

                // Tipo flusso (se presente)
                if (!StringUtils.isEmpty(tipo)) {
                    stipendiCofi.setTipo_flusso(tipo.trim());
                }

                // Non setto lo STATO, lo metto nel component prima dell'insert.
                bulk.setStipendiCofiBulk(stipendiCofi);
                stipendiCofiSet = true;
            }


            if (!Utility.isAnyEmpty(cds, annoObbl, numeroObbl, importo, esercizioObbl)) {
                if (bulk.getStipendiCofiObbScadBulks() == null) {
                    bulk.setStipendiCofiObbScadBulks(new ArrayList<>());
                }

                Stipendi_cofi_obb_scadBulk obbScad = new Stipendi_cofi_obb_scadBulk();
                obbScad.setStipendi_cofi(stipendiCofi);
                obbScad.setEsercizio(obbScad.getEsercizio());
                obbScad.setMese(obbScad.getMese());
                obbScad.setCd_cds_obbligazione(cds.trim());
                obbScad.setEsercizio_obbligazione(Utility.parseInteger(esercizioObbl, "esercizio_obbligazione", i));
                obbScad.setEsercizio_ori_obbligazione(Utility.parseInteger(annoObbl, "annoObbl", i));
                obbScad.setPg_obbligazione(Utility.parseLong(numeroObbl, "numeroObbl", i));
                obbScad.setIm_totale(Utility.parseBigDecimal(importo, "importo", i));

                bulk.getStipendiCofiObbScadBulks().add(obbScad);
            }
        }
    }



    private void elaboraSheetRitenute(ActionContext context, XSSFSheet sheet, GestioneStipBulk bulk)
            throws ApplicationException {

        DataFormatter fmt = new DataFormatter();
        int headerRowIndex = Utility.findHeaderRow(sheet, fmt, new String[]{"esercizio", "mese", "codice contributo"});

        if (headerRowIndex == -1) {
            throw new ApplicationException("Header row 'esercizio, mese, codice contributo' not found in the sheet.");
        }



        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow r = sheet.getRow(i);
            if (r == null) continue;

            String esercizio = fmt.formatCellValue(r.getCell(0));
            String mese = fmt.formatCellValue(r.getCell(1));
            String codiceContributo = fmt.formatCellValue(r.getCell(2));
            String tipo = fmt.formatCellValue(r.getCell(3));
            String enteDip = fmt.formatCellValue(r.getCell(4));
            String ammontare = fmt.formatCellValue(r.getCell(5));

            // I valori delle date non vengono più formattati come stringhe all'inizio
            XSSFCell cellDataInizio = r.getCell(6);
            XSSFCell cellDataFine = r.getCell(7);

            if (Utility.isAnyEmpty(esercizio, mese, codiceContributo, tipo, enteDip, ammontare)) {
                continue;
            }

            if (bulk.getStipendiCofiCoriBulks() == null) {
                bulk.setStipendiCofiCoriBulks(new ArrayList<>());
            }

            Stipendi_cofi_coriBulk cori = new Stipendi_cofi_coriBulk();

            cori.setEsercizio(cori.getEsercizio());
            cori.setMese(cori.getMese());
            cori.setCd_contributo_ritenuta(codiceContributo.trim());
            cori.setTi_ente_percipiente(enteDip.trim());
            cori.setAmmontare(Utility.parseBigDecimal(ammontare, "ammontare", i));

            // Uso del metodo ausiliario per le date
            cori.setDt_da_competenza_coge(Utility.parseTimestamp(cellDataInizio, "data inizio", i));
            cori.setDt_a_competenza_coge(Utility.parseTimestamp(cellDataFine, "data fine", i));

            bulk.getStipendiCofiCoriBulks().add(cori);
        }
    }


    @Override
    protected Button[] createToolbar() {
            final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(CRUDBP.class);
        return Stream.of(
                new Button(properties, "CRUDToolbar.save")
        ).toArray(Button[]::new);
    }


    public FlussoStipendiComponentSession createComponentSession()
            throws javax.ejb.EJBException,
            BusinessProcessException {
        return (FlussoStipendiComponentSession)createComponentSession("CNRPDG00_EJB_FlussoStipendiComponentSession",FlussoStipendiComponentSession.class);
    }


//    @Override
//    protected void initialize(ActionContext actioncontext) throws BusinessProcessException {
//        super.initialize(actioncontext);
//        esercizio = CNRUserContext.getEsercizio(actioncontext.getUserContext());
//        setStatus(INSERT);
//        final AllegatoParentIBulk allegatoParentIBulk = new AllegatoParentIBulk();
//        allegatoParentIBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);
//        allegatoParentIBulk.setToBeCreated();
//        setModel(actioncontext, initializeModelForEditAllegati(actioncontext, allegatoParentIBulk));
//        getCrudArchivioAllegati().setOrderBy(actioncontext, "lastModificationDate", OrderConstants.ORDER_DESC);
//    }
//


//    @Override
//    public OggettoBulk initializeModelForEdit(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
//        Optional.ofNullable(getModel())\
//                .filter(AllegatoParentIBulk.class::isInstance)
//                .map(AllegatoParentIBulk.class::cast)
//                .ifPresent(allegatoParentIBulk -> allegatoParentIBulk.getArchivioAllegati().clear());
//        return initializeModelForEditAllegati(actioncontext, oggettobulk);
//    }

//    @Override
//    public boolean isSaveButtonEnabled() {
//        return super.isSaveButtonEnabled();
//    }
//
//    @Override
//    public boolean isSearchButtonHidden() {
//        return true;
//    }
//
//    @Override
//    public boolean isFreeSearchButtonHidden() {
//        return true;
//    }
//
//    @Override
//    public boolean isNewButtonHidden() {
//        return false;
//    }
//    @Override
//    public boolean isDeleteButtonHidden() {
//        return true;
//    }
//
//    @Override
//    protected String getStorePath(AllegatoParentIBulk allegatoParentIBulk, boolean create) throws BusinessProcessException {
//        return AllegatoParentIBulk.getStorePath(FLUSSO_STIPENDI, esercizio);
//    }
//
//    @Override
//    protected Class<AllegatoGenericoBulk> getAllegatoClass() {
//        return AllegatoGenericoBulk.class;
//    }

//    @Override
//    public String getAllegatiFormName() {
//        return "lastModificationDate";
//    }

//    @Override
//    protected void completeUpdateAllegato(UserContext userContext, AllegatoGenericoBulk allegato) throws ApplicationException {
//        super.completeUpdateAllegato(userContext, allegato);
//        FatturaElettronicaPassivaComponentSession component =
//                (FatturaElettronicaPassivaComponentSession) EJBCommonServices.createEJB("CNRDOCAMM00_EJB_FatturaElettronicaPassivaComponentSession");
//        try {
//            Optional.ofNullable(allegato.getFile())
//                .ifPresent(file -> {
//                    Integer result;
//                    try {
//                        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
//                        XSSFSheet s = wb.getSheet(wb.getSheetName(0));
//                        Map<String, String> results = new HashMap<String, String>();
//                        for(int i=8; i <= s.getLastRowNum(); i++){
//                            final XSSFRow row = s.getRow(i);
//                            final Optional<String> identifictivoSDI = Optional.ofNullable(row).map(cells -> cells.getCell(1)) .map(XSSFCell::getStringCellValue);
//                            if (identifictivoSDI.isPresent()) {
//                                final Optional<String> esito = Optional.ofNullable(row.getCell(20)).map(XSSFCell::getStringCellValue);
//                                if (esito.filter(s1 -> s1.length() > 0).isPresent()) {
//                                    results.put(identifictivoSDI.get(), esito.get());
//                                }
//                            }
//                        }
//                        result = component.aggiornaEsitoPCC(userContext, results);
//                    } catch (IOException | ComponentException e) {
//                        throw new ApplicationRuntimeException(e);
//                    } catch (NotOfficeXmlFileException _ex) {
//                        try {
//                            final CSVReader reader = new CSVReaderBuilder(new FileReader(file))
//                                    .withCSVParser(new CSVParserBuilder()
//                                            .withSeparator(';')
//                                            .build()
//                                    ).build();
//                            reader.skip(13);
//                            Iterator<String[]> iterator = reader.iterator();
//                            Map<String, String> results = new HashMap<String, String>();
//                            while (iterator.hasNext()) {
//                                String[] record = iterator.next();
//                                final Optional<String> identifictivoSDI = Optional.ofNullable(record).map(strings -> strings[1]);
//                                if (identifictivoSDI.isPresent()) {
//                                    final Optional<String> esito = Optional.ofNullable(record).map(strings -> strings[20]);
//                                    if (esito.filter(s1 -> s1.length() > 0).isPresent()) {
//                                        results.put(identifictivoSDI.get(), esito.get());
//                                    }
//                                }
//                            }
//                            reader.close();
//                            results.entrySet().stream().forEach(stringStringEntry -> logger.debug("Aggiornamento IdentificativoSDI: {} con Esito: {}", stringStringEntry.getKey(), stringStringEntry.getValue()));
//                            result = component.aggiornaEsitoPCC(userContext, results);
//                        } catch (IOException|ComponentException e) {
//                            throw new ApplicationRuntimeException(e);
//                        }
//                    }
//                    setMessage(FormBP.INFO_MESSAGE, "L'esito è stato aggiornato su " + result + " fatture elettroniche.");
//                });
//        } catch (Exception _ex) {
//            throw new ApplicationMessageFormatException("Il file non è stato elaborato per il seguente errore: {0}", _ex.getMessage());
//        }
//    }

}
