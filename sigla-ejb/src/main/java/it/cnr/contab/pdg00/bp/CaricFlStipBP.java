package it.cnr.contab.pdg00.bp;

import it.cnr.contab.pdg00.cdip.bulk.CaricFlStipBulk;
import it.cnr.contab.pdg00.cdip.bulk.GestioneStipBulk;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofiBulk;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofi_obb_scadBulk;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofi_coriBulk;
import it.cnr.contab.pdg00.ejb.FlussoStipendiComponentSession;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;

import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentIBulk;

import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.OrderConstants;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.jsp.Button;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.DataFormatter;



import javax.ejb.EJBException;
import java.io.InputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

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
        esercizio = CNRUserContext.getEsercizio(ctx.getUserContext());
        setStatus(CRUDBP.INSERT);

        CaricFlStipBulk bulk = new CaricFlStipBulk();
        bulk.setCrudStatus(OggettoBulk.NORMAL);
        setModel(ctx, initializeModelForEditAllegati(ctx, bulk));
        getCrudArchivioAllegati().setOrderBy(ctx, "onlyinsert", OrderConstants.ORDER_DESC);
//        assicuraCampiAbilitati(ctx); // abilitazione upload subito

    }


    protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
        super.basicEdit(actioncontext, oggettobulk, flag);
        getCrudArchivioAllegati().setOrderBy(actioncontext, "onlyinsert", OrderConstants.ORDER_DESC);
        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());
        getCrudArchivioAllegati().setModelIndex(actioncontext, -1);
    }

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
        Optional.ofNullable(getModel())
                .filter(AllegatoParentIBulk.class::isInstance)
                .map(AllegatoParentIBulk.class::cast)
                .ifPresent(allegatoParentIBulk -> allegatoParentIBulk.getArchivioAllegati().clear());
        return initializeModelForEditAllegati(actioncontext, oggettobulk);
    }



    // target: .../ComunicazioniDAL/Flusso Stipendi/<esercizio>
    @Override
    protected String getStorePath(CaricFlStipBulk parent, boolean create) {
        return AllegatoParentIBulk.getStorePath(FLUSSO_STIPENDI, esercizio);
    }

    @Override
    protected Class<AllegatoGenericoBulk> getAllegatoClass() {
        return AllegatoGenericoBulk.class;
    }

    @Override
    public String getAllegatiFormName() {
        return "onlyinsert"; // form senza pulsante "Apri File"
    }

    // --- SOLO UN FILE ALLA VOLTA ---
    @Override
    protected boolean isChildGrowable(boolean isGrowable) {
        CaricFlStipBulk parent = (CaricFlStipBulk) getModel();
        return isGrowable && parent.getArchivioAllegati().isEmpty();
    }


    // Vietiamo qualsiasi modifica
    @Override
    protected Boolean isPossibileModifica(AllegatoGenericoBulk allegato) {
        return false;
    }

    @Override
    protected void completeUpdateAllegato(UserContext userContext, AllegatoGenericoBulk allegato)
            throws ApplicationException {
        throw new ApplicationException("La modifica dell’allegato non è consentita. Inserire un nuovo file.");
    }

    @Override
    protected void completeCreateAllegato(AllegatoGenericoBulk allegato,
                                          it.cnr.si.spring.storage.StorageObject so)
            throws ApplicationException {
        elaboraDaStorage(allegato); // process dopo store
    }

    private void elaboraDaStorage(AllegatoGenericoBulk allegato) throws ApplicationException {
        try (InputStream in = storeService.getResource(allegato.getStorageKey())) {
            processFlussoStipendi(in, allegato.getNome());
            setMessage(FormBP.INFO_MESSAGE, "Flusso stipendi elaborato correttamente.");
        } catch (IOException e) {
            throw new ApplicationException("Errore lettura file archiviato: " + allegato.getNome(), e);
        }
    }

    // --- SOLO CREATE: blocca qualsiasi update invocato per errore ---
    @Override
    public void update(ActionContext ctx) throws BusinessProcessException {
        throw new BusinessProcessException(new ApplicationException(
                "Operazione non consentita: è prevista solo la creazione di un nuovo allegato."
        ));
    }

    // --- PERCORSO UNICO DI SALVATAGGIO ---
    @Override
    public void create(ActionContext ctx) throws BusinessProcessException {
        CaricFlStipBulk parent = (CaricFlStipBulk) getModel();
        AllegatoGenericoBulk a = parent.getArchivioAllegati().get(0);
        initialize(ctx);

        if (a.getCrudStatus() != OggettoBulk.TO_BE_CREATED) {
            throw new BusinessProcessException(
                    new ApplicationException("Operazione non consentita: è possibile solo inserire un nuovo allegato.")
            );
        }

        if (a.getFile() != null) {
            if (a.getNome() == null) {
                a.setNome(a.parseFilename(a.getFile().getName()));
            }
        }

        // 1) Salva fisicamente il file nello store (via AllegatiCRUDBP.archiviaAllegati)
        super.create(ctx);

        try {
            elaboraDaStorage(a);
            setMessage(FormBP.INFO_MESSAGE, "Flusso stipendi elaborato correttamente.");
        } catch (ApplicationException ex) {
            throw new BusinessProcessException(ex);
        }

        // 3) Reset per nuovo upload
        resetForNextUpload(ctx);
    }



    // RESET dopo salvataggio riuscito
    private void resetForNextUpload(ActionContext ctx) throws BusinessProcessException {
        CaricFlStipBulk model = (CaricFlStipBulk) getModel();
        model.getArchivioAllegati().clear();
        model.setTipo_rapporto(null);
//        assicuraCampiAbilitati(ctx);  // nuovo TO_BE_CREATED
        setStatus(CRUDBP.INSERT);
    }

    // ---------- LOGICA ELABORAZIONE FILE ----------
    private void processFlussoStipendi(InputStream in, String filename)
            throws ApplicationException {
        String f = filename == null ? "" : filename.toLowerCase();
        try {
            if (f.endsWith(".xlsx")) {
                try (XSSFWorkbook wb = new XSSFWorkbook(in)) {
                    elaboraWorkbookXlsx(this.uc, wb);
                }
            } else {
                throw new ApplicationException("Formato non supportato: " + filename);
            }
        } catch (IOException ex) {
            throw new ApplicationException("Impossibile aprire il file " + filename, ex);
        } catch (BusinessProcessException bpe) {
            throw new ApplicationException("Errore di processo durante l’elaborazione.", bpe);
        }
    }

    private void elaboraWorkbookXlsx(UserContext uc, XSSFWorkbook wb)
            throws ApplicationException, BusinessProcessException {

        XSSFSheet sheetLordi = null, sheetRitenute = null;
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            String name = wb.getSheetName(i);
            if (name == null) continue;
            String up = name.toUpperCase();
            if (up.contains("LORDI")) sheetLordi = wb.getSheetAt(i);
            else if (up.contains("RITENUTE")) sheetRitenute = wb.getSheetAt(i);
        }
        if (sheetLordi == null)  throw new ApplicationException("Sheet contenente 'LORDI' non trovato.");
        if (sheetRitenute == null) throw new ApplicationException("Sheet contenente 'RITENUTE' non trovato.");

        GestioneStipBulk bulk = new GestioneStipBulk();
        elaboraSheetLordiInterno(sheetLordi, bulk);
        elaboraSheetRitenuteInterno(sheetRitenute, bulk);

        try {
            createComponentSession().gestioneFlussoStipendi(uc, bulk);
        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void elaboraSheetLordiInterno(XSSFSheet sheet, GestioneStipBulk bulk) throws ApplicationException {
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
            String tipo      = fmt.formatCellValue(r.getCell(2));
            String cds       = fmt.formatCellValue(r.getCell(3));
            String annoObbl  = fmt.formatCellValue(r.getCell(4));
            String numeroObbl= fmt.formatCellValue(r.getCell(5));
            String importo   = fmt.formatCellValue(r.getCell(6));
            String esercizioObbl = fmt.formatCellValue(r.getCell(12));

            if (Utility.isAnyEmpty(esercizio, mese)) continue;

            if (!stipendiCofiSet) {
                stipendiCofi = new Stipendi_cofiBulk();
                stipendiCofi.setEsercizio(Utility.parseInteger(esercizio, "esercizio", i));
                stipendiCofi.setMese(null); // da UI
                stipendiCofi.setMese_reale(Utility.parseInteger(mese, "mese reale (da file)", i));
                if (!org.springframework.util.StringUtils.isEmpty(tipo)) {
                    stipendiCofi.setTipo_flusso(tipo.trim());
                }
                bulk.setStipendiCofiBulk(stipendiCofi);
                stipendiCofiSet = true;
            }

            if (!Utility.isAnyEmpty(cds, annoObbl, numeroObbl, importo, esercizioObbl)) {
                if (bulk.getStipendiCofiObbScadBulks() == null) {
                    bulk.setStipendiCofiObbScadBulks(new ArrayList<>());
                }
                Stipendi_cofi_obb_scadBulk obbScad = new Stipendi_cofi_obb_scadBulk();
                obbScad.setStipendi_cofi(stipendiCofi);
                obbScad.setCd_cds_obbligazione(cds.trim());
                obbScad.setEsercizio_obbligazione(Utility.parseInteger(esercizioObbl, "esercizio_obbligazione", i));
                obbScad.setEsercizio_ori_obbligazione(Utility.parseInteger(annoObbl, "annoObbl", i));
                obbScad.setPg_obbligazione(Utility.parseLong(numeroObbl, "numeroObbl", i));
                obbScad.setIm_totale(Utility.parseBigDecimal(importo, "importo", i));
                bulk.getStipendiCofiObbScadBulks().add(obbScad);
            }
        }
    }

    private void elaboraSheetRitenuteInterno(XSSFSheet sheet, GestioneStipBulk bulk) throws ApplicationException {
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

            Stipendi_cofi_coriBulk cori = new Stipendi_cofi_coriBulk();
            cori.setCd_contributo_ritenuta(codiceContributo.trim());
            cori.setTi_ente_percipiente(enteDip.trim());
            cori.setAmmontare(Utility.parseBigDecimal(ammontare, "ammontare", i));
            cori.setDt_da_competenza_coge(Utility.parseTimestamp(cellDataInizio, "data inizio", i));
            cori.setDt_a_competenza_coge(Utility.parseTimestamp(cellDataFine, "data fine", i));

            bulk.getStipendiCofiCoriBulks().add(cori);
        }
    }
//
//    private void assicuraCampiAbilitati(ActionContext ctx) {
//        CaricFlStipBulk m = (CaricFlStipBulk) getModel();
//        if (m.getArchivioAllegati().isEmpty()) {
//            AllegatoGenericoBulk a = new AllegatoGenericoBulk();
//            a.setCrudStatus(OggettoBulk.TO_BE_CREATED); // abilita i campi
//            m.addToArchivioAllegati(a);
//        }
//        getCrudArchivioAllegati().setModelIndex(ctx, 0);
//        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());
//    }

    public FlussoStipendiComponentSession createComponentSession()
            throws EJBException, BusinessProcessException {
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

    // UI: tutti i bottoni disabilitati tranne salva
    @Override public boolean isNewButtonHidden()        { return true; }
    @Override public boolean isDeleteButtonHidden()     { return true; }
    @Override public boolean isSearchButtonHidden()     { return true; }
    @Override public boolean isFreeSearchButtonHidden() { return true; }
    @Override public boolean isSaveButtonEnabled()      { return true; }
}
