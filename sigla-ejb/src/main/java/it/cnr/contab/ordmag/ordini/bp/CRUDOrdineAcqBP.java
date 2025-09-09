/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.ordmag.ordini.bp;

import it.cnr.contab.chiusura00.ejb.RicercaDocContComponentSession;
import it.cnr.contab.coepcoan00.bp.CRUDScritturaPDoppiaBP;
import it.cnr.contab.coepcoan00.bp.DetailEcoCogeCRUDController;
import it.cnr.contab.config00.contratto.bulk.Dettaglio_contrattoBulk;
import it.cnr.contab.config00.esercizio.bulk.EsercizioBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.docamm00.bp.*;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.docamm00.ejb.FatturaPassivaComponentSession;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.doccont00.bp.IDefferedUpdateSaldiBP;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.IDefferUpdateSaldi;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.ordmag.magazzino.bulk.ParametriSelezioneMovimentiBulk;
import it.cnr.contab.ordmag.magazzino.ejb.MovimentiMagComponentSession;
import it.cnr.contab.ordmag.ordini.bulk.*;
import it.cnr.contab.ordmag.ordini.ejb.OrdineAcqComponentSession;
import it.cnr.contab.ordmag.ordini.service.OrdineAcqCMISService;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Report;
import it.cnr.contab.reports.service.PrintService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.GenerazioneReportException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.CollapsableDetailCRUDController;
import it.cnr.jada.util.action.FormController;
import it.cnr.jada.util.action.SimpleDetailCRUDController;
import it.cnr.jada.util.jsp.Button;
import it.cnr.jada.util.upload.UploadedFile;
import it.cnr.si.spring.storage.StorageException;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Gestisce le catene di elementi correlate con il documento in uso.
 */
public class CRUDOrdineAcqBP extends AllegatiCRUDBP<AllegatoOrdineBulk, OrdineAcqBulk> implements IDocumentoAmministrativoBP, VoidableBP, IDefferedUpdateSaldiBP, IGenericSearchDocAmmBP, IDocAmmCogeCoanBP {

	private static final DateFormat PDF_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

	private final SimpleDetailCRUDController dettaglioObbligazioneController;
	private it.cnr.contab.doccont00.core.bulk.OptionRequestParameter userConfirm = null;
	private boolean carryingThrough = false;
	protected it.cnr.contab.docamm00.docs.bulk.Risultato_eliminazioneVBulk deleteManager = null;
	private boolean isDeleting = false;
	private boolean dettaglioContrattoCollapse = false;
	private Integer esercizioInScrivania;
	private boolean annoSolareInScrivania = true;
	private boolean ribaltato;
	private boolean riportaAvantiIndietro = false;
	public StoreService getStoreService(){
		return storeService;
	}
	public boolean isDettaglioContrattoCollapse() {
		return dettaglioContrattoCollapse;
	}
	private boolean attivaEconomica = false;
	private boolean attivaFinanziaria = false;
	private boolean attivaAnalitica = false;

	public void setDettaglioContrattoCollapse(boolean dettaglioContrattoCollapse) {
		this.dettaglioContrattoCollapse = dettaglioContrattoCollapse;
	}

	public boolean isInputReadonly() {
		OrdineAcqBulk ordine = (OrdineAcqBulk)getModel();
		if(ordine == null || isSearching())
			return super.isInputReadonly();
		return 	super.isInputReadonly() || isRibaltato() ||
                (!ordine.isStatoInserito() && !ordine.isStatoInApprovazione() &&
                        ((ordine.isStatoAllaFirma() && !ordine.isToBeUpdated()) || !ordine.isStatoAllaFirma()));
	}

	private final SimpleDetailCRUDController righe= new OrdineAcqRigaCRUDController("Righe", OrdineAcqRigaBulk.class, "righeOrdineColl", this){
		public void validateForDelete(ActionContext context, OggettoBulk oggetto) throws ValidationException  {
			OrdineAcqRigaBulk riga = (OrdineAcqRigaBulk)oggetto;
			if (riga.getDspObbligazioneScadenzario() != null && riga.getDspObbligazioneScadenzario().getPg_obbligazione() != null){
				throw new ValidationException( "Impossibile cancellare una riga associata ad impegni");
			}
		}

		@Override
		public OggettoBulk removeDetail(int i) {
			List list = getDetails();
			OrdineAcqRigaBulk dettaglio =(OrdineAcqRigaBulk)list.get(i);
			for (int k=0;k<dettaglio.getRigheConsegnaColl().size();k++) {
				dettaglio.removeFromRigheConsegnaColl(k);
			}
			BulkList<AllegatoGenericoBulk> listaDettagliAllegati = dettaglio.getArchivioAllegati();
			if (listaDettagliAllegati != null && !listaDettagliAllegati.isEmpty()){
				int k;
				for ( k = 0; k < listaDettagliAllegati.size(); k++ ){
					AllegatoGenericoBulk all = listaDettagliAllegati.get(k);
					all.setToBeDeleted();
				}
			}

			return super.removeDetail(i);
		}

		@Override
		public int addDetail(OggettoBulk oggettobulk) throws BusinessProcessException {
			int index = super.addDetail(oggettobulk);
			OrdineAcqRigaBulk dettaglio =(OrdineAcqRigaBulk)oggettobulk;
			dettaglio.setDspMagazzino(dettaglio.getOrdineAcq().getUnicoMagazzinoAbilitato());
			if (dettaglio.getDspMagazzino() != null){
				dettaglio.setDspLuogoConsegna(dettaglio.getDspMagazzino().getLuogoConsegnaMag());
			}
			return index;
		}
	};

	private final SimpleDetailCRUDController consegne = new SimpleDetailCRUDController("Consegne",OrdineAcqConsegnaBulk.class,"righeConsegnaColl",righe){
		@Override
		public int addDetail(OggettoBulk oggettobulk) throws BusinessProcessException {
			OrdineAcqConsegnaBulk consegna = (OrdineAcqConsegnaBulk)oggettobulk;
			OrdineAcqRigaBulk dettaglio =(OrdineAcqRigaBulk)getRighe().getModel();
			consegna.setTipoConsegna(dettaglio.getTipoConsegnaDefault());
			consegna.setMagazzino(dettaglio.getOrdineAcq().getUnicoMagazzinoAbilitato());
			if (consegna.getMagazzino() != null){
				consegna.setLuogoConsegnaMag(consegna.getMagazzino().getLuogoConsegnaMag());
			}
			if (dettaglio.getDspConto() != null){
				consegna.setContoBulk(dettaglio.getDspConto());
			}

			return super.addDetail(oggettobulk);
		}

		@Override
		public void writeHTMLToolbar(PageContext pagecontext, boolean canAddToCRUD, boolean canFilter, boolean canRemoveFromCRUD, boolean closedToolbar) throws IOException, ServletException {
			super.writeHTMLToolbar(pagecontext, canAddToCRUD, canFilter, canRemoveFromCRUD, false);
			final Optional<OrdineAcqConsegnaBulk> ordineAcqConsegnaBulk = Optional.ofNullable(getModel())
					.filter(OrdineAcqConsegnaBulk.class::isInstance)
					.map(OrdineAcqConsegnaBulk.class::cast);
			if (ordineAcqConsegnaBulk.isPresent()) {
				try {
					if (ordineAcqConsegnaBulk.get().isStatoConsegnaEvasa() || ordineAcqConsegnaBulk.get().isStatoConsegnaEvasaForzatamente()) {
						final Button button = new Button(Config.getHandler().getProperties(CRUDOrdineAcqBP.class), "Toolbar.visualizzaMovimento");
						button.writeToolbarButton(pagecontext.getOut(), true, HttpActionContext.isFromBootstrap(pagecontext));
					}
					if (ordineAcqConsegnaBulk.get().isStatoConsegnaInserita() && ordineAcqConsegnaBulk.get().getOrdineAcqRiga().getOrdineAcq().isOrdineDefinitivo()) {
						final Button button = new Button(Config.getHandler().getProperties(CRUDOrdineAcqBP.class), "Toolbar.evadiConsegna");
						button.writeToolbarButton(pagecontext.getOut(), true, HttpActionContext.isFromBootstrap(pagecontext));
					}
					if (ordineAcqConsegnaBulk.filter(bulk -> bulk.getStatoFatt().equalsIgnoreCase(OrdineAcqConsegnaBulk.STATO_FATT_ASSOCIATA_TOTALMENTE))
							.isPresent()) {
						if (ordineAcqConsegnaBulk.get().getFatturaOrdineBulk()==null) {
							final Button button = new Button(Config.getHandler().getProperties(CRUDOrdineAcqBP.class), "Toolbar.disassociaFattura");
							button.writeToolbarButton(pagecontext.getOut(), true, HttpActionContext.isFromBootstrap(pagecontext));
						} else {
							final Button button = new Button(Config.getHandler().getProperties(CRUDOrdineAcqBP.class), "Toolbar.visualizzaFattura");
							button.writeToolbarButton(pagecontext.getOut(), true, HttpActionContext.isFromBootstrap(pagecontext));
						}
					}
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			}
			super.closeButtonGROUPToolbar(pagecontext);
		}
	};

	private final ObbligazioniCRUDController obbligazioniController = new ObbligazioniCRUDController("Obbligazioni",	Obbligazione_scadenzarioBulk.class,"ordineObbligazioniHash", this) {
		@Override
		public boolean isGrowable() {
					return false;
				}
	};

	private final SimpleDetailCRUDController dettaglioAllegatiController = new SimpleDetailCRUDController("AllegatiDettaglio", AllegatoOrdineDettaglioBulk.class,"archivioAllegati",righe)
	{
		@Override
		protected void validate(ActionContext actioncontext, OggettoBulk oggettobulk) throws ValidationException {
			AllegatoOrdineDettaglioBulk allegato = (AllegatoOrdineDettaglioBulk)oggettobulk;
			UploadedFile file = ((it.cnr.jada.action.HttpActionContext)actioncontext).getMultipartParameter("main.Righe.AllegatiDettaglio.file");
			if (!(file == null || file.getName().equals(""))) {
				allegato.setFile(file.getFile());
				allegato.setContentType(file.getContentType());
				allegato.setNome(allegato.parseFilename(file.getName()));
				allegato.setAspectName(OrdineAcqCMISService.ASPECT_ORDINI_DETTAGLIO);
				allegato.setToBeUpdated();
				getParentController().setDirty(true);
			}
			oggettobulk.validate();
			super.validate(actioncontext, oggettobulk);
		}

		@Override
		public OggettoBulk removeDetail(int i) {
			if (!getModel().isNew()){
				List list = getDetails();
				AllegatoOrdineDettaglioBulk all =(AllegatoOrdineDettaglioBulk)list.get(i);
				if (isPossibileCancellazioneDettaglioAllegato(all)) {
					return super.removeDetail(i);
				} else {
					return null;
				}
			}
			return super.removeDetail(i);
		}

		@Override
		public int addDetail(OggettoBulk oggettobulk) throws BusinessProcessException {
			int add = super.addDetail(oggettobulk);
			AllegatoOrdineDettaglioBulk all =(AllegatoOrdineDettaglioBulk)oggettobulk;
			all.setIsDetailAdded(true);
			return add;
		}
	};

	private final SimpleDetailCRUDController proposeRigheEcoTestata = new SimpleDetailCRUDController("Proposta Dati Analitici", OrdineAcqEcoBulk.class,"righeEconomica",this, true);
	private final CollapsableDetailCRUDController resultRigheEcoTestata = new ResultRigheEcoTestataCRUDController("Dati Analitici", OrdineAcqEcoBulk.class,"resultRigheEconomica",this, true);

	private final CollapsableDetailCRUDController childrenAnaColl = new DetailEcoCogeCRUDController(OrdineAcqRigaEcoBulk.class, righe);
	private final CollapsableDetailCRUDController resultRigheEcoDettaglio = new ResultRigheEcoDettaglioCRUDController("Dati Coge/Coan", OrdineAcqRigaEcoBulk.class,"resultRigheEconomica",righe);

	public final it.cnr.jada.util.action.SimpleDetailCRUDController getConsegne() {
		return consegne;
	}

	public CRUDOrdineAcqBP() {
		this(OrdineAcqConsegnaBulk.class);
	}

	protected void setTab() {
		setTab("tab","tabOrdineAcq");
		setTab("tabOrdineAcqDettaglio","tabOrdineDettaglio");
	}

	public CRUDOrdineAcqBP(Class dettObbligazioniControllerClass) {
		super("Tr");
		setTab();
		dettaglioObbligazioneController = new SimpleDetailCRUDController("DettaglioObbligazioni", dettObbligazioniControllerClass,"ordineObbligazioniHash", obbligazioniController) {
			public java.util.List getDetails() {
				OrdineAcqBulk ordine = (OrdineAcqBulk) CRUDOrdineAcqBP.this.getModel();
				java.util.Vector lista = new java.util.Vector();
				if (ordine != null) {
					java.util.Hashtable h = ordine.getOrdineObbligazioniHash();
					if (h != null && getParentModel() != null)
						lista = (java.util.Vector) h.get(getParentModel());
				}
				return lista;
			}
			@Override
			public boolean isGrowable() {
				return false;
			}
			@Override
			public boolean isShrinkable() {
				return super.isShrinkable() && !((it.cnr.jada.util.action.CRUDBP) getParentController()
								.getParentController()).isSearching();
			}
		};

	}

	/**
	 * CRUDAnagraficaBP constructor comment.
	 * 
	 * @param function
	 *            java.lang.String
	 */
	public CRUDOrdineAcqBP(String function)	throws BusinessProcessException {
		super(function + "Tr");
		setTab();
		dettaglioObbligazioneController = new SimpleDetailCRUDController("DettaglioObbligazioni", OrdineAcqConsegnaBulk.class, "ordineObbligazioniHash", obbligazioniController) {
			public java.util.List getDetails() {
				OrdineAcqBulk ordine = (OrdineAcqBulk) CRUDOrdineAcqBP.this.getModel();
				java.util.Vector lista = new java.util.Vector();
				if (ordine != null) {
					java.util.Hashtable h = ordine.getOrdineObbligazioniHash();
					if (h != null && getParentModel() != null)
						lista = (java.util.Vector) h.get(getParentModel());
				}
				return lista;
			}
			@Override
			public boolean isGrowable() {
				return false;
			}
			@Override
			public boolean isShrinkable() {
				return super.isShrinkable() && !((it.cnr.jada.util.action.CRUDBP) getParentController()
								.getParentController()).isSearching();
			}
		};
	}

	public void create(it.cnr.jada.action.ActionContext context) throws	it.cnr.jada.action.BusinessProcessException {
		try {
			getModel().setToBeCreated();
			setModel(context, createComponentSession().creaConBulk(context.getUserContext(),getModel()));
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public final SimpleDetailCRUDController getRighe() {
		return righe;
	}

	private void allegatoStampaOrdine(UserContext userContext) throws Exception {
		OrdineAcqBulk ordineAcq = (OrdineAcqBulk)getModel();
		StorageObject s = (( OrdineAcqCMISService)storeService).getStorageObjectStampaOrdine(ordineAcq);
		if ( !OrdineAcqBulk.STATO_ALLA_FIRMA.equals(ordineAcq.getStato())
			&& !OrdineAcqBulk.STATO_DEFINITIVO.equals(ordineAcq.getStato())
			&& s!=null)
			storeService.delete(s);
		if ( OrdineAcqBulk.STATO_ALLA_FIRMA.equals(ordineAcq.getStato()) &&
				s==null){
				File f = stampaOrdine(userContext,ordineAcq);

				AllegatoOrdineBulk allegatoStampaOrdine = new AllegatoOrdineBulk();
					allegatoStampaOrdine.setFile(f);
					allegatoStampaOrdine.setContentType( new MimetypesFileTypeMap().getContentType(f.getName()));
					allegatoStampaOrdine.setNome(f.getName());
					allegatoStampaOrdine.setDescrizione(f.getName());
					allegatoStampaOrdine.setTitolo( f.getName());
					allegatoStampaOrdine.setCrudStatus( OggettoBulk.TO_BE_CREATED);
					allegatoStampaOrdine.setAspectName(OrdineAcqCMISService.ASPECT_STAMPA_ORDINI);
					allegatoStampaOrdine.setOrdine(ordineAcq);
					ordineAcq.addToArchivioAllegati(allegatoStampaOrdine);
		}
	}

	public void update(it.cnr.jada.action.ActionContext context) throws	it.cnr.jada.action.BusinessProcessException {
		try {
			getModel().setToBeUpdated();
			setModel(context,createComponentSession().modificaConBulk(context.getUserContext(),getModel()));
			allegatoStampaOrdine(context.getUserContext());
			archiviaAllegati(context);
			archiviaAllegatiDettaglio();
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	@Override
	protected String getStorePath(OrdineAcqBulk allegatoParentBulk, boolean create) throws BusinessProcessException{
		return ( (OrdineAcqCMISService)storeService).getStorePath(allegatoParentBulk);
	}

	@Override
	protected Class<AllegatoOrdineBulk> getAllegatoClass() {
		return AllegatoOrdineBulk.class;
	}


	@Override
	public StoreService getBeanStoreService(ActionContext actioncontext) throws BusinessProcessException{
		return SpringUtil.getBean("ordineAcqCMISService", OrdineAcqCMISService.class);
	}


	@Override
	protected boolean excludeChild(StorageObject storageObject) throws ApplicationException{
		if (storeService.hasAspect(storageObject,OrdineAcqCMISService.ASPECT_STAMPA_ORDINI))
			return true;
		return super.excludeChild(storageObject);
	}

	@Override
	protected void completeAllegato(AllegatoOrdineBulk allegato, StorageObject storageObject) throws ApplicationException {
		allegato.setAspectName(Optional.ofNullable(storageObject.<List<String>>getPropertyValue(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value()))
				.map(list -> list.stream().filter(
						o -> AllegatoOrdineBulk.aspectNamesKeys.get(o) != null
						).findAny().orElse(OrdineAcqCMISService.ASPECT_ALLEGATI_ORDINI)
				).orElse(null));
		super.completeAllegato(allegato, storageObject);
	}

	@Override
	public String getAllegatiFormName() {
		super.getAllegatiFormName();
		return "allegatiOrdine";
	}
	public void scaricaAllegato(ActionContext actioncontext) throws IOException, ServletException, ApplicationException {
		AllegatoOrdineBulk allegato = (AllegatoOrdineBulk)getCrudArchivioAllegati().getModel();
		StorageObject storageObject = storeService.getStorageObjectBykey(allegato.getStorageKey());
		InputStream is = storeService.getResource(storageObject.getKey());
		((HttpActionContext)actioncontext).getResponse().setContentLength(storageObject.<BigInteger>getPropertyValue(StoragePropertyNames.CONTENT_STREAM_LENGTH.value()).intValue());
		((HttpActionContext)actioncontext).getResponse().setContentType(storageObject.getPropertyValue(StoragePropertyNames.CONTENT_STREAM_MIME_TYPE.value()));
		OutputStream os = ((HttpActionContext)actioncontext).getResponse().getOutputStream();
		((HttpActionContext)actioncontext).getResponse().setDateHeader("Expires", 0);
		byte[] buffer = new byte[((HttpActionContext)actioncontext).getResponse().getBufferSize()];
		int buflength;
		while ((buflength = is.read(buffer)) > 0) {
			os.write(buffer,0,buflength);
		}
		is.close();
		os.flush();
	}
	public String getNomeAllegatoDettaglio() {
		AllegatoOrdineDettaglioBulk dettaglio = (AllegatoOrdineDettaglioBulk)getDettaglioAllegatiController().getModel();
		if (dettaglio!= null){
			return dettaglio.getNome();
		}
		return "";
	}
	public void scaricaDocumentoDettaglioCollegato(ActionContext actioncontext) throws Exception {
		AllegatoOrdineDettaglioBulk dettaglio = (AllegatoOrdineDettaglioBulk)getDettaglioAllegatiController().getModel();
		if (dettaglio!= null){
			StorageObject storageObject = storeService.getStorageObjectBykey(dettaglio.getStorageKey());
			InputStream is = storeService.getResource(storageObject.getKey());
			((HttpActionContext)actioncontext).getResponse().setContentLength(storageObject.<BigInteger>getPropertyValue(StoragePropertyNames.CONTENT_STREAM_LENGTH.value()).intValue());
			((HttpActionContext)actioncontext).getResponse().setContentType(storageObject.getPropertyValue(StoragePropertyNames.CONTENT_STREAM_MIME_TYPE.value()));
			OutputStream os = ((HttpActionContext)actioncontext).getResponse().getOutputStream();
			((HttpActionContext)actioncontext).getResponse().setDateHeader("Expires", 0);
			byte[] buffer = new byte[((HttpActionContext)actioncontext).getResponse().getBufferSize()];
			int buflength;
			while ((buflength = is.read(buffer)) > 0) {
				os.write(buffer,0,buflength);
			}
			is.close();
			os.flush();
		} else {
			throw new it.cnr.jada.action.MessageToUser( "Documenti non presenti sul documentale per la riga selezionata" );
		}

	}
	@Override
	protected Boolean isPossibileCancellazione(AllegatoGenericoBulk allegato) {
		return true;
	}

	protected Boolean isPossibileCancellazioneDettaglioAllegato(AllegatoGenericoBulk allegato) {
		return true;
	}

	public SimpleDetailCRUDController getDettaglioAllegatiController() {
		return dettaglioAllegatiController;
	}

	@Override
	protected Boolean isPossibileModifica(AllegatoGenericoBulk allegato){
		return true;
	}

	@Override
	protected void gestioneCancellazioneAllegati(AllegatoParentBulk allegatoParentBulk) throws ApplicationException {
		OrdineAcqBulk ordine = (OrdineAcqBulk)allegatoParentBulk;
		super.gestioneCancellazioneAllegati(allegatoParentBulk);
	}

	public void gestionePostSalvataggio(it.cnr.jada.action.ActionContext context) throws	it.cnr.jada.action.BusinessProcessException {
		try {
			OrdineAcqBulk ordine = (OrdineAcqBulk)getModel(); 
//			((OrdineAcqComponentSession)createComponentSession()).gestioneStampaOrdine(context.getUserContext(), ordine);
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public void stampaRichiesta(ActionContext actioncontext) throws Exception {
		OrdineAcqBulk ordine = (OrdineAcqBulk)getModel();
		InputStream is = (( OrdineAcqCMISService)storeService).getStreamOrdine(ordine);
		if (is != null){
			((HttpActionContext)actioncontext).getResponse().setContentType("application/pdf");
			OutputStream os = ((HttpActionContext)actioncontext).getResponse().getOutputStream();
			((HttpActionContext)actioncontext).getResponse().setDateHeader("Expires", 0);
			byte[] buffer = new byte[((HttpActionContext)actioncontext).getResponse().getBufferSize()];
			int buflength;
			while ((buflength = is.read(buffer)) > 0) {
				os.write(buffer,0,buflength);
			}
			is.close();
			os.flush();
		}
	}

	public boolean isStampaOrdineButtonHidden() {
		OrdineAcqBulk ordine = (OrdineAcqBulk)getModel();
		return (ordine == null || ordine.getNumero() == null);
	}

	private String preparaFileNamePerStampa(String reportName) {
		String fileName = reportName;
		fileName = fileName.replace('/', '_');
		fileName = fileName.replace('\\', '_');
		if (fileName.startsWith("_"))
			fileName = fileName.substring(1);
		if (fileName.endsWith(".jasper"))
			fileName = fileName.substring(0, fileName.length() - 7);
		fileName = fileName + ".pdf";
		return fileName;
	}

	private String getOutputFileNameOrdine(String reportName, OrdineAcqBulk ordine) {
		String fileName = preparaFileNamePerStampa(reportName);
		fileName = PDF_DATE_FORMAT.format(new java.util.Date()) + '_' + ordine.recuperoIdOrdineAsString() + '_' + fileName;
		return fileName;
	}

	public void stampaOrdine(ActionContext actioncontext) throws Exception {
		OrdineAcqBulk ordine = (OrdineAcqBulk) getModel();
		((HttpActionContext)actioncontext).getResponse().setContentType("application/pdf");
		OutputStream os = ((HttpActionContext)actioncontext).getResponse().getOutputStream();
		((HttpActionContext)actioncontext).getResponse().setDateHeader("Expires", 0);
		InputStream is = ((OrdineAcqCMISService)storeService).getStreamOrdine( ordine);
		if ( is==null) {
			UserContext userContext = actioncontext.getUserContext();
			File f = stampaOrdine(userContext, ordine);
			IOUtils.copy(Files.newInputStream(f.toPath()), os);
		}else{
			IOUtils.copy(is, os);
		}

		os.flush();

	}
	private static final String NOME_REPORT_JASPER="ordini_acq.jasper";
	public File stampaOrdine(
			UserContext userContext,
			OrdineAcqBulk ordine) throws ComponentException {
		try {
			String nomeFileOrdineOut = getOutputFileNameOrdine(NOME_REPORT_JASPER, ordine);
			File output = new File(System.getProperty("tmp.dir.SIGLAWeb") + "/tmp/", File.separator + nomeFileOrdineOut);
			Print_spoolerBulk print = new Print_spoolerBulk();
			print.setFlEmail(false);
			print.setReport("/ordmag/ordini/" + NOME_REPORT_JASPER);
			print.setNomeFile(nomeFileOrdineOut);
			print.setUtcr(userContext.getUser());
			print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());
			print.addParam("CD_CDS", ordine.getCdCds(), String.class);
			print.addParam("CD_UNITA_OPERATIVA", ordine.getCdUnitaOperativa(), String.class);
			print.addParam("ESERCIZIO", ordine.getEsercizio(), Integer.class);
			print.addParam("CD_NUMERATORE", ordine.getCdNumeratore(), String.class);
			print.addParam("NUMERO", ordine.getNumero(), Integer.class);
			Report report = SpringUtil.getBean("printService", PrintService.class).executeReport(userContext, print);

			FileOutputStream f = new FileOutputStream(output);
			f.write(report.getBytes());
			return output;
		} catch (IOException e) {
			throw new GenerazioneReportException("Generazione Stampa non riuscita", e);
		}
	}
	public boolean isSalvaDefinitivoButtonHidden() {
		OrdineAcqBulk ordine = (OrdineAcqBulk)getModel();
		return (ordine == null || ordine.getNumero() == null || !ordine.isStatoAllaFirma());
	}

	@Override
	public boolean isNewButtonEnabled() {
		return super.isNewButtonEnabled() && !isRibaltato();
	}

	public boolean isDeleteButtonEnabled() {
		return super.isDeleteButtonEnabled() && !isRibaltato();
	}

	@Override
	public boolean isSaveButtonEnabled() {
		return super.isSaveButtonEnabled() &&
				Optional.ofNullable(getModel())
						.filter(OrdineAcqBulk.class::isInstance)
						.map(OrdineAcqBulk.class::cast)
						.map(ordineAcqBulk -> !ordineAcqBulk.isStatoDefinitivo())
						.orElse(Boolean.TRUE);
	}

	protected it.cnr.jada.util.jsp.Button[] createToolbar() {
		Button[] toolbar = super.createToolbar();
		Button[] newToolbar = new Button[ toolbar.length + 1 ];
		for ( int i = 0; i< toolbar.length; i++ )
			newToolbar[ i ] = toolbar[ i ];
		newToolbar[ toolbar.length ] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()),"CRUDToolbar.stampa");
		return newToolbar;

	}
	public boolean areBottoniObbligazioneAbilitati() {
		OrdineAcqBulk ordine = (OrdineAcqBulk) getModel();
		return 	ordine != null &&
				!isSearching() && 
				!isViewing() ;
	}

	public boolean isBottoneObbligazioneAggiornaManualeAbilitato() {
		OrdineAcqBulk ordine = (OrdineAcqBulk) getModel();
		return(	ordine != null && !isSearching() &&
				!isViewing() );
	}

	public IDefferUpdateSaldi getDefferedUpdateSaldiBulk() {
		if (isDeleting() && getParent() != null)
			return getDefferedUpdateSaldiParentBP()
					.getDefferedUpdateSaldiBulk();
		return (IDefferUpdateSaldi) getDocumentoAmministrativoCorrente();
	}

	public IDefferedUpdateSaldiBP getDefferedUpdateSaldiParentBP() {
		if (isDeleting() && getParent() != null)
			return ((IDefferedUpdateSaldiBP) getParent())
					.getDefferedUpdateSaldiParentBP();
		return this;
	}

	public it.cnr.contab.docamm00.docs.bulk.Risultato_eliminazioneVBulk getDeleteManager() {
		if (deleteManager == null)
			deleteManager = new it.cnr.contab.docamm00.docs.bulk.Risultato_eliminazioneVBulk();
		else
			deleteManager.reset();
		return deleteManager;
	}

	@Override
	public boolean isModelVoided() {
		return !isSearching() && getModel() != null
				&& ((Voidable) getModel()).isAnnullato();
	}
	public it.cnr.jada.util.RemoteIterator findObbligazioni(
			it.cnr.jada.UserContext userContext,
			it.cnr.contab.docamm00.docs.bulk.Filtro_ricerca_obbligazioniVBulk filtro)
			throws it.cnr.jada.action.BusinessProcessException {
		try {
			return ((OrdineAcqComponentSession)createComponentSession()).cercaObbligazioni(userContext, filtro);
		} catch (ComponentException | RemoteException e) {
			throw handleException(e);
		}
	}

	public it.cnr.jada.util.RemoteIterator findObbligazioniAttributes(
			it.cnr.jada.action.ActionContext actionContext,
			it.cnr.jada.persistency.sql.CompoundFindClause clauses,
			it.cnr.jada.bulk.OggettoBulk bulk,
			it.cnr.jada.bulk.OggettoBulk context, java.lang.String property)
			throws it.cnr.jada.action.BusinessProcessException {
		try {
			return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(actionContext, createComponentSession().cerca(actionContext.getUserContext(),
							clauses, bulk, context, property));
		} catch (ComponentException | RemoteException e) {
			throw handleException(e);
		}
	}

	@Override
	public Accertamento_scadenzarioBulk getAccertamento_scadenziario_corrente() {
		return null;
	}

	@Override
	public IDocumentoAmministrativoBulk getBringBackDocAmm() {
		return getDocumentoAmministrativoCorrente();
	}

	@Override
	public IDocumentoAmministrativoBulk getDocumentoAmministrativoCorrente() {
		return (IDocumentoAmministrativoBulk) getModel();
	}

	@Override
	public Obbligazione_scadenzarioBulk getObbligazione_scadenziario_corrente() {
		if (getObbligazioniController() == null)
			return null;
		return (Obbligazione_scadenzarioBulk) getObbligazioniController()
				.getModel();
	}
	@Override
	public boolean isAutoGenerated() {
		return false;
	}
	@Override
	public boolean isDeleting() {
		return isDeleting;
	}
	@Override
	public boolean isManualModify() {
		return true;
	}
	@Override
	public void setIsDeleting(boolean newIsDeleting) {
		isDeleting = newIsDeleting;
	}
	@Override
	public void validaObbligazionePerDocAmm(ActionContext actionContext, OggettoBulk bulk)	throws BusinessProcessException {
	}
	public ObbligazioniCRUDController getObbligazioniController() {
		return obbligazioniController;
	}
	public void setDeleteManager(it.cnr.contab.docamm00.docs.bulk.Risultato_eliminazioneVBulk deleteManager) {
		this.deleteManager = deleteManager;
	}
	public it.cnr.contab.doccont00.core.bulk.OptionRequestParameter getUserConfirm() {
		return userConfirm;
	}
	public void setUserConfirm(it.cnr.contab.doccont00.core.bulk.OptionRequestParameter userConfirm) {
		this.userConfirm = userConfirm;
	}
	public boolean isCarryingThrough() {
		return carryingThrough;
	}
	public void setCarryingThrough(boolean carryingThrough) {
		this.carryingThrough = carryingThrough;
	}
	public SimpleDetailCRUDController getDettaglioObbligazioneController() {
		return dettaglioObbligazioneController;
	}
	public void delete(ActionContext context) throws it.cnr.jada.action.BusinessProcessException
	{
		int crudStatus = getModel().getCrudStatus();
		try
		{
			OrdineAcqBulk ordine = (OrdineAcqBulk) getModel();
			if ( !ordine.isStatoInserito())
				throw new ApplicationException( "Non è possibile cancellare un ordine in stato diverso da inserito");
			((OrdineAcqComponentSession)createComponentSession()).eliminaConBulk(context.getUserContext(),ordine);
			this.commitUserTransaction();
		} catch(Exception e) {
			getModel().setCrudStatus(crudStatus);
			throw handleException(e);
		}
	}

	public ContoBulk recuperoContoDefault(
			ActionContext context,
			Categoria_gruppo_inventBulk categoria_gruppo_inventBulk)
			throws it.cnr.jada.action.BusinessProcessException {
		try {
			return ((OrdineAcqComponentSession)createComponentSession()).recuperoContoDefault(context.getUserContext(), categoria_gruppo_inventBulk);
		} catch (ComponentException | PersistencyException | RemoteException e) {
			throw handleException(e);
		}
	}

	public Dettaglio_contrattoBulk recuperoDettaglioContratto(
			ActionContext context,
			OrdineAcqRigaBulk riga)
			throws it.cnr.jada.action.BusinessProcessException {
		try {
			return ((OrdineAcqComponentSession)createComponentSession()).recuperoDettaglioContratto(context.getUserContext(), riga);
		} catch (ComponentException | PersistencyException | RemoteException e) {
			throw handleException(e);
		}
	}

	public boolean isVisibleMotivoAssenzaCig() {
		return Optional.ofNullable(getModel())
				.filter(OrdineAcqBulk.class::isInstance)
				.map(OrdineAcqBulk.class::cast)
				.flatMap(ordineAcqBulk -> Optional.ofNullable(ordineAcqBulk.getCig()))
				.map(cigBulk -> !Optional.ofNullable(cigBulk.getCdCig()).isPresent())
				.orElse(Boolean.TRUE);
	}

	private void archiviaAllegatiDettaglio() throws ApplicationException, BusinessProcessException {
		OrdineAcqBulk ordine = (OrdineAcqBulk)getModel();
		for (OrdineAcqRigaBulk dettaglio : ordine.getRigheOrdineColl()) {
			for (AllegatoGenericoBulk allegato : dettaglio.getArchivioAllegati()) {
				if (allegato.isToBeCreated()){
					try {
						storeService.storeSimpleDocument(allegato,
								new FileInputStream(allegato.getFile()),
								allegato.getContentType(),
								allegato.getNome(),
								((OrdineAcqCMISService)storeService).getStorePathDettaglio(dettaglio));
						allegato.setCrudStatus(OggettoBulk.NORMAL);
					} catch (FileNotFoundException e) {
						throw handleException(e);
					} catch (StorageException e) {
						if (e.getType().equals(StorageException.Type.CONSTRAINT_VIOLATED))
							throw new ApplicationException("File ["+allegato.getNome()+"] gia' presente. Inserimento non possibile!");
						throw handleException(e);
					}
				}else if (allegato.isToBeUpdated()) {
					if (isPossibileModifica(allegato)) {
						try {
							if (allegato.getFile() != null) {
								storeService.updateStream(allegato.getStorageKey(),
										new FileInputStream(allegato.getFile()),
										allegato.getContentType());
							}
							storeService.updateProperties(allegato, storeService.getStorageObjectBykey(allegato.getStorageKey()));
							allegato.setCrudStatus(OggettoBulk.NORMAL);
						} catch (FileNotFoundException e) {
							throw handleException(e);
						}
					}
				}
			}
			for (Iterator<AllegatoOrdineDettaglioBulk> iterator = dettaglio.getArchivioAllegati().deleteIterator(); iterator.hasNext();) {
				AllegatoOrdineDettaglioBulk allegato = iterator.next();
				if (allegato.isToBeDeleted()){
					storeService.delete(allegato.getStorageKey());
					allegato.setCrudStatus(OggettoBulk.NORMAL);
				}
			}
		}

		for (Iterator<OrdineAcqRigaBulk> iterator = ordine.getRigheOrdineColl().deleteIterator(); iterator.hasNext();) {
			OrdineAcqRigaBulk dettaglio = iterator.next();
			for (AllegatoGenericoBulk allegato : dettaglio.getArchivioAllegati()) {
				if (allegato.isToBeDeleted()) {
					storeService.delete(allegato.getStorageKey());
					allegato.setCrudStatus(OggettoBulk.NORMAL);
				}
			}
		}
	}

	@Override
	protected OggettoBulk initializeModelForEditAllegati(ActionContext actioncontext, OggettoBulk oggettobulk, String path) throws BusinessProcessException {
		OrdineAcqBulk ordine = (OrdineAcqBulk)oggettobulk;
/*
		for (OrdineAcqRigaBulk dettaglio : ordine.getRigheOrdineColl()) {
			dettaglio.setArchivioAllegati(((OrdineAcqCMISService)storeService).recuperoAllegatiDettaglioOrdine(dettaglio));
		}
*/

		return super.initializeModelForEditAllegati(actioncontext, oggettobulk, path,false);
	}



	@Override
	protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
		super.basicEdit(actioncontext, oggettobulk, flag);
		if (this.isInputReadonly())
			this.setStatus(FormController.VIEW);
	}

	public void reset(ActionContext context) throws BusinessProcessException {
		if (it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(
				context.getUserContext()).intValue() != Fattura_passivaBulk
				.getDateCalendar(null).get(java.util.Calendar.YEAR))
			resetForSearch(context);
		else {
			super.reset(context);
		}
	}

	@Override
	protected void resetTabs(ActionContext actioncontext) {
		super.resetTabs(actioncontext);
		setTab("tab", "tabOrdineAcq");
		setTab("tabOrdineAcqDettagli", "tabOrdineDettaglio");
	}

	@Override
	public String getColumnsetForGenericSearch() {
		return "default";
	}

	@Override
	public String getPropertyForGenericSearch() {
		return "fornitore";
	}

	@Override
	public CRUDComponentSession initializeModelForGenericSearch(BulkBP bp, ActionContext context) throws BusinessProcessException {
		return createComponentSession();
	}

	public void deleteAssociazioneConsegnaFattura(ActionContext context) throws BusinessProcessException {
		final OrdineAcqConsegnaBulk ordineAcqConsegnaBulk = Optional.ofNullable(getConsegne().getModel())
				.filter(OrdineAcqConsegnaBulk.class::isInstance)
				.map(OrdineAcqConsegnaBulk.class::cast)
				.orElseThrow(() -> new BusinessProcessException("Consegna non trovata"));
		ordineAcqConsegnaBulk.setToBeUpdated();
		ordineAcqConsegnaBulk.setStatoFatt(OrdineAcqConsegnaBulk.STATO_FATT_NON_ASSOCIATA);
		try {
			((CRUDComponentSession)createComponentSession("JADAEJB_CRUDComponentSession")).modificaConBulk(context.getUserContext(), ordineAcqConsegnaBulk);
		} catch (ComponentException|RemoteException e) {
			throw handleException(e);
		}
	}

	public Integer getEsercizioInScrivania() {
		return esercizioInScrivania;
	}

	public void setEsercizioInScrivania(Integer esercizioInScrivania) {
		this.esercizioInScrivania = esercizioInScrivania;
	}

	public boolean isAnnoSolareInScrivania() {
		return annoSolareInScrivania;
	}

	public void setAnnoSolareInScrivania(boolean annoSolareInScrivania) {
		this.annoSolareInScrivania = annoSolareInScrivania;
	}

	public boolean isRiportaAvantiIndietro() {
		return riportaAvantiIndietro;
	}

	public void setRiportaAvantiIndietro(boolean riportaAvantiIndietro) {
		this.riportaAvantiIndietro = riportaAvantiIndietro;
	}

	public boolean isRibaltato() {
		return ribaltato;
	}

	public void setRibaltato(boolean ribaltato) {
		this.ribaltato = ribaltato;
	}

	public boolean initRibaltato(it.cnr.jada.action.ActionContext context)
			throws it.cnr.jada.action.BusinessProcessException {
		try {
			return (((RicercaDocContComponentSession) createComponentSession(
					"CNRCHIUSURA00_EJB_RicercaDocContComponentSession",
					RicercaDocContComponentSession.class)).isRibaltato(context
					.getUserContext()));
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
		try {
			super.init(config, context);

			int solaris = Fattura_passivaBulk.getDateCalendar(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate())
					.get(java.util.Calendar.YEAR);

			int esercizioScrivania = CNRUserContext.getEsercizio(context.getUserContext());
			setEsercizioInScrivania(esercizioScrivania);
			setAnnoSolareInScrivania(solaris == this.getEsercizioInScrivania());
			attivaEconomica = Utility.createConfigurazioneCnrComponentSession().isAttivaEconomica(context.getUserContext(), esercizioScrivania);
			attivaFinanziaria = Utility.createConfigurazioneCnrComponentSession().isAttivaFinanziaria(context.getUserContext(), esercizioScrivania);
			attivaAnalitica = Utility.createConfigurazioneCnrComponentSession().isAttivaAnalitica(context.getUserContext(), esercizioScrivania);
			resultRigheEcoTestata.setCollapsed(Boolean.FALSE);

			setRibaltato(initRibaltato(context));
			if (!isAnnoSolareInScrivania()) {
				String cds = it.cnr.contab.utenze00.bp.CNRUserContext.getCd_cds(context.getUserContext());
				try {
					FatturaPassivaComponentSession session = Utility.createFatturaPassivaComponentSession();
					boolean esercizioScrivaniaAperto = session.verificaStatoEsercizio(context.getUserContext(),
									new EsercizioBulk(cds, this.getEsercizioInScrivania()));
					boolean esercizioSuccessivoAperto = session.verificaStatoEsercizio(context.getUserContext(),
									new EsercizioBulk(cds, this.getEsercizioInScrivania() + 1));
					setRiportaAvantiIndietro(esercizioScrivaniaAperto && esercizioSuccessivoAperto && isRibaltato());
				} catch (Throwable t) {
					throw handleException(t);
				}
			} else
				setRiportaAvantiIndietro(false);
		} catch (javax.ejb.EJBException e) {
			setAnnoSolareInScrivania(false);
		} catch (ComponentException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

	private static final String[] TAB_ORDINE_MAIN = new String[]{ "tabOrdineAcq","Ordine d'Acquisto","/ordmag/ordini/tab_ordine_acq.jsp" };
	private static final String[] TAB_ORDINE_PROPOSE_DETAIL_COGECOAN = new String[]{ "tabOrdineProposeDetailEcoCoge","Proposta Dati Coge/Coan","/ordmag/ordini/tab_ordine_propose_detail_eco_coge.jsp" };
	private static final String[] TAB_ORDINE_RESULT_DETAIL_COGECOAN = new String[]{ "tabOrdineResultDetailEcoCoge","Dati Coge/Coan","/ordmag/ordini/tab_ordine_result_detail_eco_coge.jsp" };
	private static final String[] TAB_ORDINE_FORNITORE = new String[]{ "tabOrdineFornitore","Fornitore","/ordmag/ordini/tab_ordine_fornitore.jsp" };
	private static final String[] TAB_ORDINE_DETTAGLI = new String[]{ "tabOrdineAcqDettaglio","Dettaglio","/ordmag/ordini/tab_ordine_acq_dettagli.jsp" };
	private static final String[] TAB_ORDINE_OBBLIGAZIONI = new String[]{ "tabOrdineAcqObbligazioni","Obbligazioni Collegate","/ordmag/ordini/tab_ordine_acq_obbligazioni.jsp" };
	private static final String[] TAB_ORDINE_ALLEGATI = new String[]{ "tabAllegati","Allegati","/ordmag/ordini/tab_ordine_acq_allegati.jsp" };

	public String[][] getTabs() {
		TreeMap<Integer, String[]> pages = new TreeMap<Integer, String[]>();
		int i = 0;
		pages.put(i++, TAB_ORDINE_MAIN);
		pages.put(i++, TAB_ORDINE_FORNITORE);
		pages.put(i++, TAB_ORDINE_DETTAGLI);
		if (this.isAttivaFinanziaria())
			pages.put(i++, TAB_ORDINE_OBBLIGAZIONI);
		else if (this.isAttivaEconomica() && this.isAttivaAnalitica()) {
			if (Optional.ofNullable(this.getModel())
					.filter(OrdineAcqBulk.class::isInstance)
					.map(OrdineAcqBulk.class::cast)
					.map(el->el.isStatoOriginaleInserito()||el.isStatoOriginaleInApprovazione())
					.orElse(Boolean.FALSE))
				pages.put(i++, TAB_ORDINE_PROPOSE_DETAIL_COGECOAN);
			else if (Optional.ofNullable(this.getModel())
					.filter(OrdineAcqBulk.class::isInstance)
					.map(OrdineAcqBulk.class::cast)
					.map(el->el.isStatoOriginaleAllaFirma()||el.isStatoOriginaleDefinitivo())
					.orElse(Boolean.FALSE))
				pages.put(i++, TAB_ORDINE_RESULT_DETAIL_COGECOAN);
		}
		pages.put(i++, TAB_ORDINE_ALLEGATI);

		String[][] tabs = new String[i][3];
		for (int j = 0; j < i; j++)
			tabs[j] = new String[]{pages.get(j)[0], pages.get(j)[1], pages.get(j)[2]};
		return tabs;
	}

	private static final String[] TAB_ORDINE_RIGA_DETTAGLIO = new String[]{ "tabOrdineDettaglio","Dettaglio Riga","/ordmag/ordini/tab_ordine_acq_dettaglio.jsp" };
	private static final String[] TAB_ORDINE_RIGA_CONSEGNA = new String[]{ "tabOrdineConsegna","Consegne","/ordmag/ordini/tab_ordine_acq_consegna.jsp" };
	private static final String[] TAB_ORDINE_RIGA_RESULT_DETAIL_COGECOAN = new String[]{ "tabOrdineRigaResultDetailEcoCoge","Dati Coge/Coan","/ordmag/ordini/tab_ordine_riga_result_detail_eco_coge.jsp" };
	private static final String[] TAB_ORDINE_RIGA_ALLEGATI = new String[]{ "tabOrdineDettaglioAllegati","Allegati","/ordmag/ordini/tab_ordine_acq_dettaglio_allegati.jsp" };

	public String[][] getTabsDettagli() {
		OrdineAcqRigaBulk rigaOrdine = Optional.ofNullable(this.getRighe().getModel())
				.filter(OrdineAcqRigaBulk.class::isInstance)
				.map(OrdineAcqRigaBulk.class::cast)
				.orElse(null);

		TreeMap<Integer, String[]> pages = new TreeMap<Integer, String[]>();
		int i = 0;
		pages.put(i++, TAB_ORDINE_RIGA_DETTAGLIO);
		pages.put(i++, TAB_ORDINE_RIGA_CONSEGNA);

		if (attivaEconomica || attivaAnalitica) {
			if (Optional.ofNullable(this.getModel())
					.filter(OrdineAcqBulk.class::isInstance)
					.map(OrdineAcqBulk.class::cast)
					.map(el->el.isStatoOriginaleInserito()||el.isStatoOriginaleInApprovazione())
					.orElse(Boolean.FALSE))
				pages.put(i++, CRUDScritturaPDoppiaBP.TAB_DATI_COGECOAN);
			else if (Optional.ofNullable(this.getModel())
					.filter(OrdineAcqBulk.class::isInstance)
					.map(OrdineAcqBulk.class::cast)
					.map(el->el.isStatoOriginaleAllaFirma()||el.isStatoOriginaleDefinitivo())
					.orElse(Boolean.FALSE))
				pages.put(i++, TAB_ORDINE_RIGA_RESULT_DETAIL_COGECOAN);
		}

		if (rigaOrdine != null && rigaOrdine.getNumero() != null)
			pages.put(i++, TAB_ORDINE_RIGA_ALLEGATI);

		String[][] tabs = new String[i][3];
		for (int j = 0; j < i; j++)
			tabs[j] = new String[]{pages.get(j)[0], pages.get(j)[1], pages.get(j)[2]};
		return tabs;
	}

	public SimpleDetailCRUDController getProposeRigheEcoTestata() {
		return proposeRigheEcoTestata;
	}

	public CollapsableDetailCRUDController getResultRigheEcoTestata() {
		return resultRigheEcoTestata;
	}

	public CollapsableDetailCRUDController getResultRigheEcoDettaglio() {
		return resultRigheEcoDettaglio;
	}

	@Override
	public CollapsableDetailCRUDController getChildrenAnaColl() {
		return childrenAnaColl;
	}

	@Override
	public FormController getControllerDetailEcoCoge() {
		return this.getRighe();
	}

	@Override
	public boolean isAttivaEconomica() {
		return attivaEconomica;
	}

	@Override
	public boolean isAttivaFinanziaria() {
		return attivaFinanziaria;
	}

	@Override
	public boolean isAttivaAnalitica() {
		return attivaAnalitica;
	}

	@Override
	public OggettoBulk initializeModelForInsert(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
		oggettobulk = super.initializeModelForInsert(actioncontext, oggettobulk);
		((OrdineAcqBulk)oggettobulk).setAttivaFinanziaria(isAttivaFinanziaria());
		return oggettobulk;
	}

	@Override
	public OggettoBulk initializeModelForEdit(ActionContext actioncontext, OggettoBulk oggettobulk) throws BusinessProcessException {
		oggettobulk = super.initializeModelForEdit(actioncontext, oggettobulk);
		((OrdineAcqBulk)oggettobulk).setAttivaFinanziaria(isAttivaFinanziaria());
		return oggettobulk;
	}

	public void aggiornaAnaliticaRigaOrdine(ActionContext actionContext, OrdineAcqRigaBulk riga, boolean reloadAll) throws BusinessProcessException {
		try {
			if (reloadAll) {
				//cancello tutta la eco
				for (int k=0;k<riga.getRigheEconomica().size();k++)
					riga.removeFromRigheEconomica(k);
				if (riga.getDspConto()!=null) {
					//carico analitica se presente su testata
					for (OrdineAcqEcoBulk ordineEco : riga.getOrdineAcq().getRigheEconomica()) {
						boolean copy = riga.getDspConto().equalsByPrimaryKey(ordineEco.getVoce_ep());
						if (!copy && ordineEco.getVoce_analitica()!=null) {
							//Verifico se voce analitica è associata a voce economica
							List<ContoBulk> contiAssociati = Utility.createPDCContoAnaliticoComponentSession().findContiAnaliticiAssociatiList(actionContext.getUserContext(), ordineEco.getVoce_analitica());
							copy = contiAssociati.stream()
									.anyMatch(el -> el.getCd_voce_ep().equals(riga.getDspConto().getCd_voce_ep()));
						}
						if (copy) {
							OrdineAcqRigaEcoBulk rigaEco = new OrdineAcqRigaEcoBulk();
							rigaEco.setVoce_analitica(ordineEco.getVoce_analitica());
							rigaEco.setLinea_attivita(ordineEco.getLinea_attivita());
							rigaEco.setImporto(BigDecimal.ZERO);
							riga.addToRigheEconomica(rigaEco);
						}
					}
				}
			}
			//Alimento gli importi
			if (!riga.getRigheEconomica().isEmpty()) {
				for (OrdineAcqRigaEcoBulk rigaEco : riga.getRigheEconomica()) {
					rigaEco.setImporto(riga.getImCostoEco().divide(BigDecimal.valueOf(riga.getRigheEconomica().size()),2, RoundingMode.HALF_UP));
					rigaEco.setToBeUpdated();
				}
				if (riga.getImCostoEcoDaRipartire().compareTo(BigDecimal.ZERO)!=0)
					riga.getRigheEconomica().stream()
							.filter(el->el.getImporto().add(riga.getImCostoEcoDaRipartire()).compareTo(BigDecimal.ZERO)>=0)
							.findFirst()
							.ifPresent(el->el.setImporto(el.getImporto().add(riga.getImCostoEcoDaRipartire())));
			}
		} catch (ComponentException | RemoteException e) {
			throw handleException(e);
		}
	}

	public RemoteIterator ricercaMovimenti(ActionContext actioncontext) throws BusinessProcessException {
		try {
			MovimentiMagComponentSession cs = Utility.createMovimentiMagComponentSession();
			if (cs == null) return null;
			OrdineAcqConsegnaBulk cons = (OrdineAcqConsegnaBulk)this.getConsegne().getModel();
			if (cons!=null) {
				ParametriSelezioneMovimentiBulk parametriSelezioneMovimentiBulk = new ParametriSelezioneMovimentiBulk();
				parametriSelezioneMovimentiBulk.setOrdineAcqConsegnaBulk(cons);
				return cs.ricercaMovimenti(actioncontext.getUserContext(), parametriSelezioneMovimentiBulk);
			}
			throw new ApplicationException("E' necessario indicare almeno un criterio di selezione");
		} catch (ComponentException | RemoteException e) {
			throw handleException(e);
		}

    }
}
