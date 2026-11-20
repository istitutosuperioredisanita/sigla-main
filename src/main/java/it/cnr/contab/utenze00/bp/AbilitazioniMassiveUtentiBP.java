package it.cnr.contab.utenze00.bp;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.ordmag.anag00.AbilUtenteUopOperBulk;
import it.cnr.contab.ordmag.anag00.TipoOperazioneOrdBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.utenze00.bulk.*;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentIBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.jsp.Button;
import it.cnr.jada.util.upload.UploadedFile;
import it.cnr.si.spring.storage.StorageDriver;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Gestione massiva di accessi, ruoli e abilitazioni ordini degli utenti da file Excel.
 * Il tipo di gestione (ACCESSI, RUOLI, ORDINI_MAGAZZINO) arriva dall'init-param GESTIONE_MASSIVA in utenze01.xml.
 */
public class AbilitazioniMassiveUtentiBP extends AllegatiCRUDBP<AllegatoGenericoBulk, OpMassiveInputDTO> {
    private static final long serialVersionUID = 1L;

    public static final String ACCESSI = "ACCESSI";
    public static final String RUOLI = "RUOLI";
    public static final String ORDINI_MAGAZZINO = "ORDINI_MAGAZZINO";

    private static final String[] SHEETS_ACCESSI = {"UTENTE", "CODICE ACCESSO", "UO"};
    private static final String[] COLONNE_ACCESSI = {"UTENTE", "CD_ACCESSO", "CD_UNITA_ORGANIZZATIVA"};
    private static final String[] SHEETS_RUOLI = {"UTENTE", "RUOLO", "UO"};
    private static final String[] COLONNE_RUOLI = {"UTENTE", "RUOLO", "CD_UNITA_ORGANIZZATIVA"};
    private static final String[] SHEETS_ORDINI = {"UTENTE", "FUNZIONI", "UNITA OPER FUNZIONE"};
    private static final String[] COLONNE_ORDINI = {"UTENTE", "TIPO_FUNZIONE", "UNITA_OPERATIVA"};

    private String gestione;
    private Integer esercizio;
    private OpMassiveUtentiDTO output = new OpMassiveUtentiDTO();

    public AbilitazioniMassiveUtentiBP() {
        super();
    }

    public AbilitazioniMassiveUtentiBP(String s) {
        super(s);
    }

    @Override
    protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
        gestione = config.getInitParameter("GESTIONE_MASSIVA");
        super.init(config, actioncontext);
    }

    public String getGestione() {
        return gestione;
    }

    /**
     * Titolo uguale per le 3 gestioni: la title bar mostra il <title> della JSP, senza il suffisso
     * dello stato (Ricerca/Inserimento) che CRUDBP aggiunge.
     */
    @Override
    public String getFormTitle() {
        return getBulkInfo().getShortDescription();
    }

    /* testo della nota: fogli e colonne attesi per la gestione */
    public String getNotaGestione() {
        if (ACCESSI.equals(gestione))
            return "Il file Excel deve essere composto con:\n" +
                    "foglio 1: UTENTE; colonna: UTENTE\n" +
                    "foglio 2: CODICE ACCESSO; colonna: CD_ACCESSO\n" +
                    "foglio 3: UO; colonna: CD_UNITA_ORGANIZZATIVA";
        if (RUOLI.equals(gestione))
            return "Il file Excel deve essere composto con:\n" +
                    "foglio 1: UTENTE; colonna: UTENTE\n" +
                    "foglio 2: RUOLO ; colonna: RUOLO\n" +
                    "foglio 3: UO; colonna: CD_UNITA_ORGANIZZATIVA";
        if (ORDINI_MAGAZZINO.equals(gestione))
            return "Il file Excel deve essere composto con:\n" +
                    "foglio 1: UTENTE; colonna: UTENTE\n" +
                    "foglio 2: FUNZIONI ; colonna: TIPO_FUNZIONE\n" +
                    "foglio 3: UNITA OPER FUNZIONE; colonna: UNITA_OPERATIVA";
        return "";
    }

    public OpMassiveUtentiDTO getOutput() {
        return output;
    }

    @Override
    protected String getStorePath(OpMassiveInputDTO allegatoParentBulk, boolean create) throws BusinessProcessException {
        return AllegatoParentIBulk.getStorePath(
                "Gestione Utenze" + StorageDriver.SUFFIX + gestione.toUpperCase(), esercizio);
    }

    @Override
    public void initialize(ActionContext actioncontext) throws BusinessProcessException {
        super.initialize(actioncontext);
        esercizio = CNRUserContext.getEsercizio(actioncontext.getUserContext());

        OpMassiveInputDTO model = (OpMassiveInputDTO) this.getModel();
        if (model.getArchivioAllegati().isEmpty()) {
            AllegatoGenericoBulk a = new AllegatoGenericoBulk();
            a.setCrudStatus(OggettoBulk.TO_BE_CREATED);
            model.addToArchivioAllegati(a);
        }
        model.setNota(getNotaGestione());
        getCrudArchivioAllegati().setModelIndex(actioncontext, 0);
        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());
    }

    @Override
    public boolean fillModel(ActionContext actioncontext) throws FillException {
        Boolean b = super.fillModel(actioncontext);
        OpMassiveInputDTO model = (OpMassiveInputDTO) this.getModel();
        UploadedFile uploadedFile =
                ((HttpActionContext) actioncontext).getMultipartParameter("main.ArchivioAllegati.file");
        model.setArchivioAllegati(new BulkList<>());
        AllegatoGenericoBulk a = new AllegatoGenericoBulk();
        a.setCrudStatus(OggettoBulk.TO_BE_CREATED);
        model.addToArchivioAllegati(a);
        if (!(uploadedFile == null || uploadedFile.getFile() == null || uploadedFile.getFile().length() == 0L)) {
            AllegatoGenericoBulk allegato = model.getArchivioAllegati().get(0);
            allegato.setFile(uploadedFile.getFile());
            allegato.setNome(uploadedFile.getName());
        }
        model.setNota(getNotaGestione());
        setStatus(OggettoBulk.TO_BE_CREATED);
        return b;
    }

    @Override
    public void validate(ActionContext actioncontext) throws ValidationException {
        super.validate(actioncontext);
        OpMassiveInputDTO model = (OpMassiveInputDTO) this.getModel();
        if (Optional.ofNullable(model.getArchivioAllegati()).isPresent() && model.getArchivioAllegati().size() > 0)
            model.getArchivioAllegati().get(0).validate();
    }

    @Override
    protected Boolean isPossibileModifica(AllegatoGenericoBulk allegato) {
        return Boolean.FALSE;
    }

    @Override
    protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
        OpMassiveInputDTO model = (OpMassiveInputDTO) getModel();
        BulkList<AllegatoGenericoBulk> nuovo = new BulkList<>();
        AllegatoGenericoBulk a = new AllegatoGenericoBulk();
        a.setCrudStatus(OggettoBulk.TO_BE_CREATED);
        nuovo.add(a);
        model.setArchivioAllegati(nuovo);
        getCrudArchivioAllegati().setModelIndex(actioncontext, 0);
        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());
    }

    @Override
    public boolean isSaveButtonEnabled() {
        return true;
    }

    @Override
    protected Button[] createToolbar() {
        final Properties props = it.cnr.jada.util.Config.getHandler().getProperties(CRUDBP.class);
        return Stream.of(new Button(props, "CRUDToolbar.save")).toArray(Button[]::new);
    }

    @Override
    protected Class<AllegatoGenericoBulk> getAllegatoClass() {
        return AllegatoGenericoBulk.class;
    }

    /**
     * Elabora il file Excel caricato, controlla i codici sul DB, esegue l'operazione massiva
     * di inserimento o cancellazione e archivia il file con il tipo operazione in fondo al nome.
     */
    public void create(ActionContext context) throws BusinessProcessException {
        try {
            OpMassiveInputDTO model = (OpMassiveInputDTO) this.getModel();
            AllegatoGenericoBulk allegato = model.getArchivioAllegati().get(0);
            File file = allegato.getFile();
            if (file == null)
                throw new ApplicationException("Selezionare il file Excel da caricare.");
            String nome = allegato.getNome();
            if (!nome.toLowerCase().endsWith(".xlsx"))
                throw new ApplicationException("Formato non supportato: " + nome + ". Caricare un file .xlsx.");

            try (Workbook wb = WorkbookFactory.create(file)) {
                output = elaboraWorkbook(wb);
            }
            verificaCodici(context, OpMassiveInputDTO.INSERIMENTO.equals(model.getTipoOperazione()));
            String esito = Utility.createUtenteComponentSession().applicaOperazioneMassiva(
                    context.getUserContext(), generaRecordAbilitazioni(), model.getTipoOperazione());

            allegato.setNome(costruisciNomeFile(model, nome));
            archiviaAllegati(context);

            setMessage(costruisciMessaggioElaborazione(output) + " " + esito);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Costruisce il nome con cui il file viene archiviato su Azure:
     * nome_tipo operazione.xlsx (es. utenti_inserimento.xlsx). Se nella stessa cartella
     * esiste gia' un file con quel nome, aggiunge anche il timestamp.
     */
    private String costruisciNomeFile(OpMassiveInputDTO model, String nome) throws BusinessProcessException {
        String tipo = model.getTipoOperazioneKeys().get(model.getTipoOperazione()).toString().toLowerCase();
        int punto = nome.lastIndexOf('.');
        String nuovoNome = nome.substring(0, punto) + "_" + tipo;
        String estensione = nome.substring(punto);
        if (storeService.getStorageObjectByPath(
                getStorePath(model, true) + StorageDriver.SUFFIX + nuovoNome + estensione) != null)
            nuovoNome += "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return nuovoNome + estensione;
    }

    /**
     * Controlla i codici letti dal file: esistenza sul DB (ricerca per chiave nel component)
     * e, in inserimento, anche la validità. Per le UO controlla sempre che siano di livello 2.
     */
    private void verificaCodici(ActionContext ctx, boolean ins) throws Exception {
        Map<String, OggettoBulk> chiavi = new LinkedHashMap<>();
        output.getUtenti().forEach(cd -> chiavi.put("Utente inesistente: " + cd, new UtenteBulk(cd)));
        output.getAccessi().forEach(cd -> chiavi.put("Accesso inesistente: " + cd, new AccessoBulk(cd)));
        output.getRuoli().forEach(cd -> chiavi.put("Ruolo inesistente: " + cd, new RuoloBulk(cd)));
        output.getFunzioni().forEach(cd -> chiavi.put("Funzione inesistente: " + cd, new TipoOperazioneOrdBulk(cd)));
        output.getUnitaOperative().forEach(cd -> chiavi.put("Unita' operativa inesistente: " + cd, new UnitaOperativaOrdBulk(cd)));
        output.getUnitaOrganizzative().stream().filter(cd -> !"*".equals(cd))
                .forEach(cd -> chiavi.put("Unita' organizzativa inesistente: " + cd, new Unita_organizzativaBulk(cd)));

        for (OggettoBulk o : Utility.createUtenteComponentSession().cercaPerChiave(ctx.getUserContext(), chiavi)) {
            if (o instanceof UtenteBulk u && ins && scaduta(u.getDt_fine_validita()))
                throw new ApplicationException("Utente scaduto: " + u.getCd_utente());
            else if (o instanceof TipoOperazioneOrdBulk f && ins && scaduta(f.getDtCancellazione()))
                throw new ApplicationException("Funzione scaduta: " + f.getCdTipoOperazione());
            else if (o instanceof UnitaOperativaOrdBulk uop && ins && scaduta(uop.getDtCancellazione()))
                throw new ApplicationException("Unita' operativa scaduta: " + uop.getCdUnitaOperativa());
            else if (o instanceof Unita_organizzativaBulk uo) {
                if (!Integer.valueOf(2).equals(uo.getLivello()))
                    throw new ApplicationException("Unita' organizzativa deve essere di livello 2: " + uo.getCd_unita_organizzativa());
                if (ins && (uo.getEsercizio_inizio() > esercizio || uo.getEsercizio_fine() < esercizio))
                    throw new ApplicationException("Unita' organizzativa non valida nell'esercizio " + esercizio + ": " + uo.getCd_unita_organizzativa());
            }
        }
    }

    private boolean scaduta(Timestamp fine) {
        return fine != null && fine.before(new Timestamp(System.currentTimeMillis()));
    }

    private String costruisciMessaggioElaborazione(OpMassiveUtentiDTO dto) {
        List<String> partiMessaggio = new ArrayList<>();
        if (!dto.getUtenti().isEmpty()) {
            partiMessaggio.add("Utenti: " + dto.getUtenti().size());
        }
        if (!dto.getUnitaOrganizzative().isEmpty()) {
            partiMessaggio.add("UO: " + dto.getUnitaOrganizzative().size());
        }
        if (!dto.getAccessi().isEmpty()) {
            partiMessaggio.add("Accessi: " + dto.getAccessi().size());
        }
        if (!dto.getRuoli().isEmpty()) {
            partiMessaggio.add("Ruoli: " + dto.getRuoli().size());
        }
        if (!dto.getFunzioni().isEmpty()) {
            partiMessaggio.add("Funzioni: " + dto.getFunzioni().size());
        }
        if (!dto.getUnitaOperative().isEmpty()) {
            partiMessaggio.add("UO: " + dto.getUnitaOperative().size());
        }

        String dettaglio = partiMessaggio.isEmpty() ? "" : " " + String.join(", ", partiMessaggio) + ".";
        return "File elaborato." + dettaglio;
    }

    /**
     * Verifica il numero, il nome e l'ordine dei fogli Excel,
     * controllando anche le relative intestazioni.
     */
    private void validaExcel(Workbook wb) throws ApplicationException {
        String[] sheetsNames;
        String[] colonneNames;
        switch (gestione) {
            case ACCESSI -> {
                sheetsNames = SHEETS_ACCESSI;
                colonneNames = COLONNE_ACCESSI;
            }
            case RUOLI -> {
                sheetsNames = SHEETS_RUOLI;
                colonneNames = COLONNE_RUOLI;
            }
            case ORDINI_MAGAZZINO -> {
                sheetsNames = SHEETS_ORDINI;
                colonneNames = COLONNE_ORDINI;
            }
            default -> throw new IllegalArgumentException("Gestione non valida: " + gestione);
        }

        if (wb.getNumberOfSheets() != 3) {
            throw new ApplicationException("Il file deve contenere 3 fogli.");
        }

        DataFormatter fmt = new DataFormatter(Locale.ITALY);

        for (int i = 0; i < sheetsNames.length; i++) {
            Sheet sheet = wb.getSheetAt(i);
            if (!sheetsNames[i].equals(sheet.getSheetName()))
                throw new ApplicationException("Il Foglio " + (i + 1) + " deve chiamarsi " + sheetsNames[i] + ".");
            Row header = sheet.getRow(0);
            if (header == null || !colonneNames[i].equals(fmt.formatCellValue(header.getCell(0)).trim()))
                throw new ApplicationException("Foglio " + sheetsNames[i] + ": la colonna deve chiamarsi " + colonneNames[i] + ".");

            Row row = sheet.getRow(1);
            if (row == null || fmt.formatCellValue(row.getCell(0)).trim().isEmpty()) {
                throw new ApplicationException("Il Foglio " + sheetsNames[i] + ": non contiene valori.");
            }
        }
    }

    /**
     * Valida il workbook e ne elabora i contenuti.
     */
    private OpMassiveUtentiDTO elaboraWorkbook(Workbook wb) throws ApplicationException {
        validaExcel(wb);
        return leggiFogli(wb);
    }

    /**
     * Legge i valori dei fogli Excel
     */
    private OpMassiveUtentiDTO leggiFogli(Workbook wb) throws ApplicationException {
        OpMassiveUtentiDTO out = new OpMassiveUtentiDTO();
        DataFormatter fmt = new DataFormatter(Locale.ITALY);

        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            Row header = sheet.getRow(0);
            String nomeColonna = (header == null) ? "" : fmt.formatCellValue(header.getCell(0)).trim();

            for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                Row r = sheet.getRow(j);
                if (r == null) continue;
                String valore = fmt.formatCellValue(r.getCell(0)).trim();
                if (valore.isEmpty()) continue;

                switch (nomeColonna) {
                    case "UTENTE" -> out.getUtenti().add(valore);
                    case "CD_ACCESSO" -> out.getAccessi().add(valore);
                    case "CD_UNITA_ORGANIZZATIVA" -> out.getUnitaOrganizzative().add(valore);
                    case "RUOLO" -> out.getRuoli().add(valore);
                    case "TIPO_FUNZIONE" -> out.getFunzioni().add(valore);
                    case "UNITA_OPERATIVA" -> out.getUnitaOperative().add(valore);
                }
            }
        }
        return out;
    }

    /**
     * Genera i record di abilitazione combinando i dati letti dal file Excel
     * in base alla tipologia di gestione selezionata.
     */
    private List<OggettoBulk> generaRecordAbilitazioni() throws ApplicationException {
        List<OggettoBulk> bulks = new ArrayList<>();
        for (String utente : output.getUtenti()) {
            switch (gestione) {
                case ACCESSI -> {
                    for (String accesso : output.getAccessi()) {
                        for (String unita : output.getUnitaOrganizzative()) {
                            Utente_unita_accessoBulk bulk = new Utente_unita_accessoBulk();
                            bulk.setAccesso(new AccessoBulk(accesso));
                            bulk.setCd_unita_organizzativa(unita);
                            bulk.setCd_utente(utente);
                            bulks.add(bulk);
                        }
                    }
                }
                case RUOLI -> {
                    for (String ruolo : output.getRuoli())
                        for (String uo : output.getUnitaOrganizzative()) {
                            Utente_unita_ruoloBulk bulk = new Utente_unita_ruoloBulk();
                            bulk.setRuolo(new RuoloBulk(ruolo));
                            bulk.setCd_unita_organizzativa(uo);
                            bulk.setCd_utente(utente);
                            bulks.add(bulk);
                        }
                }
                case ORDINI_MAGAZZINO -> {
                    for (String unitaOperativa : output.getUnitaOperative())
                        for (String funzione : output.getFunzioni()) {
                            AbilUtenteUopOperBulk abil = new AbilUtenteUopOperBulk(utente, unitaOperativa, funzione);
                            abil.setTuttiMagazzini(Boolean.TRUE);
                            bulks.add(abil);
                        }
                }
                default -> throw new ApplicationException("Gestione non valida: " + gestione);
            }
        }
        return bulks;
    }
}