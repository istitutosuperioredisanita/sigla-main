/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.progettiric00.bp;

import it.cnr.contab.pdg00.cdip.bulk.*;
import it.cnr.contab.progettiric00.core.bulk.*;
import it.cnr.contab.progettiric00.dto.RiportaProgettoDto;
import it.cnr.contab.progettiric00.ejb.ProgettoRicercaComponentSession;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;

import it.cnr.contab.util00.bulk.storage.AllegatoParentIBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;

import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.jsp.Button;
import it.cnr.jada.util.upload.UploadedFile;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Stream;

public class RiportaProgettiRicercaBP extends AllegatiCRUDBP<AllegatoGenericoBulk, AllegatoParentIBulk> {
    private transient final static Logger logger = LoggerFactory.getLogger(RiportaProgettiRicercaBP.class);
    private static final long serialVersionUID = 1L;
    private Integer esercizio;

    public RiportaProgettiRicercaBP() {
    }

    public RiportaProgettiRicercaBP(String s) {
        super(s);
    }

    @Override
    protected String getStorePath(AllegatoParentIBulk allegatoParentBulk, boolean create) throws BusinessProcessException {
        return AllegatoParentIBulk.getStorePath("RIPORTA_PROGETTO", esercizio);
    }

    @Override
    public void initialize(ActionContext ctx) throws BusinessProcessException {

        super.initialize(ctx);
        esercizio = CNRUserContext.getEsercizio(ctx.getUserContext());

        AllegatoParentBulk allegatoParentBulk = (AllegatoParentBulk) this.getModel();
        if (allegatoParentBulk.getArchivioAllegati().isEmpty()) {
            AllegatoGenericoBulk a = new AllegatoGenericoBulk();
            a.setCrudStatus(OggettoBulk.TO_BE_CREATED); // abilita i campi di upload
            allegatoParentBulk.addToArchivioAllegati(a);
        }
        getCrudArchivioAllegati().setModelIndex(ctx, 0);
        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());


    }
    public boolean fillModel(ActionContext actioncontext)
            throws FillException {
        Boolean b = super.fillModel(actioncontext);

        UploadedFile uploadedFile =
                ((it.cnr.jada.action.HttpActionContext) actioncontext).getMultipartParameter("main.ArchivioAllegati.file");
        AllegatoParentBulk allegatoParentBulk = (AllegatoParentBulk) this.getModel();

        allegatoParentBulk.setArchivioAllegati( new BulkList<>());
        AllegatoGenericoBulk a = new AllegatoGenericoBulk();
        a.setCrudStatus(OggettoBulk.TO_BE_CREATED); // abilita i campi di upload
        allegatoParentBulk.addToArchivioAllegati(a);
        if (!(uploadedFile == null || uploadedFile.getFile() == null || uploadedFile.getFile().length() == 0L)) {
            AllegatoGenericoBulk allegatoGenericoBulk =null;
            if ( allegatoParentBulk.getArchivioAllegati().isEmpty()){
                allegatoGenericoBulk = new AllegatoGenericoBulk();
                allegatoParentBulk.addToArchivioAllegati(allegatoGenericoBulk);
            }
            allegatoGenericoBulk=allegatoParentBulk.getArchivioAllegati().get(0);
            allegatoGenericoBulk.setFile(uploadedFile.getFile());
            allegatoGenericoBulk.setNome(uploadedFile.getName());

        }
        setStatus(OggettoBulk.TO_BE_CREATED);
        return b;
    }

    @Override
    public void validate(ActionContext actioncontext)
            throws ValidationException {
        super.validate(actioncontext);;
        AllegatoParentBulk allegatoParentBulk = (AllegatoParentBulk) this.getModel();

        if ( Optional.ofNullable(allegatoParentBulk.getArchivioAllegati()).isPresent() &&
                allegatoParentBulk.getArchivioAllegati().size()>0){
            AllegatoGenericoBulk allegatoGenericoBulk= allegatoParentBulk.getArchivioAllegati().get(0);
            allegatoGenericoBulk.validate();
        }
    }

    @Override
    protected Boolean isPossibileModifica(AllegatoGenericoBulk allegato) {
        return Boolean.FALSE;
    }
    @Override
    protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
        // Svuota la lista e lascia SOLO un allegato "vuoto" TO_BE_CREATED per l'upload successivo
        AllegatoParentBulk allegatoParentBulk = (AllegatoParentBulk) getModel();

        BulkList<AllegatoGenericoBulk> nuovo = new BulkList<>();
        AllegatoGenericoBulk a = new AllegatoGenericoBulk();
        a.setCrudStatus(OggettoBulk.TO_BE_CREATED);
        nuovo.add(a);
        allegatoParentBulk.setArchivioAllegati(nuovo);

        getCrudArchivioAllegati().setModelIndex(actioncontext, 0);
        getCrudArchivioAllegati().setSelection(Collections.emptyEnumeration());


    }

    @Override
    public boolean isSaveButtonEnabled() {
        return true;
    }

    protected Button[] createToolbar() {
        final Properties props = it.cnr.jada.util.Config.getHandler().getProperties(CRUDBP.class);
        return Stream.of(new Button(props, "CRUDToolbar.save")).toArray(Button[]::new);
    }


    @Override
    protected Class<AllegatoGenericoBulk> getAllegatoClass() {
        return AllegatoGenericoBulk.class;
    }
    public void create(ActionContext context) throws BusinessProcessException {
        try {
            AllegatoParentBulk allegatoParentBulk = (AllegatoParentBulk) this.getModel();

            File file = allegatoParentBulk.getArchivioAllegati().get(0).getFile();

            try (InputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                processElencoProgettiDaRiportare(context, allegatoParentBulk, is, file.getName());
                setMessage("Progetti Caricati");
            }

        } catch (Exception e) {
            throw handleException(e);
        }
    }
    private void  processElencoProgettiDaRiportare(ActionContext context,AllegatoParentBulk allegatoParentBulk,InputStream in, String filename) throws ComponentException {
        try {
            String nome = Optional.ofNullable(filename).orElse("").toLowerCase(Locale.ITALY);
            if (!nome.endsWith(".xlsx")) {
                throw new ApplicationException("Formato non supportato: " + filename + ". Caricare un file .xlsx.");
            }

            try (XSSFWorkbook wb = new XSSFWorkbook(in)) {
                elaboraWorkbookXlsx(context,allegatoParentBulk, wb);
            } catch (PersistencyException e) {
                throw new ApplicationException("Errore nell'elaborazione del file.", e);
            }
        } catch (IOException ex) {
            throw new ApplicationException("Impossibile aprire il file " + filename, ex);
        } catch (BusinessProcessException bpe) {
            throw new ApplicationException("Errore di processo durante l'elaborazione.", bpe);
        }
    }
    private void elaboraWorkbookXlsx(ActionContext context, AllegatoParentBulk allegatoParentBulk,XSSFWorkbook wb) throws ComponentException, BusinessProcessException, RemoteException, PersistencyException {
        XSSFSheet sheet = null;
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            String name = wb.getSheetName(i);
            if (name == null) continue;
            String up = name.toUpperCase(Locale.ITALY);
            sheet = wb.getSheetAt(i);
        }

        RiportaProgettoDto bulk = new RiportaProgettoDto();
        List<RiportaProgettoDto> listaProgetti = elaboraProgetti(sheet, allegatoParentBulk,bulk);

        if(listaProgetti.isEmpty()){
            throw new ApplicationException("Nessun progetto da riportare");
        }
        getComponentSession().riportaInNuovoProgetto( context.getUserContext(),  listaProgetti,esercizio);
    }

    protected ProgettoRicercaComponentSession getComponentSession() {
        return Utility.createProgettoRicercaComponentSession();

    }

    private List<RiportaProgettoDto> elaboraProgetti(XSSFSheet sheet,  AllegatoParentBulk allegatoParentBulk,RiportaProgettoDto bulk) throws ApplicationException {
        DataFormatter fmt = new DataFormatter();
        int headerRowIndex = findHeaderRow(sheet, fmt, new String[]{"ESERCIZIO_ESTRAZIONE", "CD_PROGETTO", "PG_PROGETTO","NUOVO CODICE PROGETTO"});
        if (headerRowIndex == -1) throw new ApplicationException("Header ESERCIZIO_ESTRAZIONE, CD_PROGETTO, PG_PROGETTO,NUOVO CODICE PROGETT non trovato.");

        List<RiportaProgettoDto> listaProgetti = new ArrayList<RiportaProgettoDto>();
        Stipendi_cofiBulk stipendiCofi = null;

        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow r = sheet.getRow(i);
            if (r == null) continue;

            String esercizioEstrazione = fmt.formatCellValue(r.getCell(0));
            String cdProgetto      = fmt.formatCellValue(r.getCell(1));
            String pgProgetto       = fmt.formatCellValue(r.getCell(2));
            String cdProgettoNew  = fmt.formatCellValue(r.getCell(3));

            if (isAnyEmpty(esercizioEstrazione,cdProgetto, pgProgetto,cdProgettoNew)) continue;


            RiportaProgettoDto  progettoDaRiportare = new RiportaProgettoDto();
            progettoDaRiportare.setEsercizioEstrazione(Integer.valueOf(esercizioEstrazione));
            progettoDaRiportare.setCdProgettoOld(cdProgetto);
            progettoDaRiportare.setPgProgettoOld(Integer.valueOf(pgProgetto));
            progettoDaRiportare.setCdProgettoNew(cdProgettoNew);

            listaProgetti.add(progettoDaRiportare);
        }
        return listaProgetti;
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
    public static boolean isAnyEmpty(String... strings) {
        for (String s : strings) {
            if (s == null || s.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}