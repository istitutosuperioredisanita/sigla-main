package it.cnr.contab.ordmag.ordini.bulk;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.contab.ordmag.anag00.NumerazioneMagBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.ordmag.magazzino.bulk.BollaScaricoMagBulk;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoStorePath;
import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;
import it.cnr.si.spring.storage.StorageDriver;

public class EvasioneOrdineBulk extends EvasioneOrdineBase implements AllegatoParentBulk, AllegatoStorePath {
	/**
	 * [NUMERAZIONE_MAG Definisce i contatori per la numerazione dei magazzini.]
	 **/
	private NumerazioneMagBulk numerazioneMag =  new NumerazioneMagBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: EVASIONE_ORDINE
	 **/

	private UnitaOperativaOrdBulk unitaOperativaAbilitata = new UnitaOperativaOrdBulk();
	
	private String find_cd_terzo; 
	private String find_ragione_sociale; 
	private String find_cd_precedente; 
	private String find_esercizio_ordine; 
	private Timestamp find_data_ordine; 
	private String find_cd_numeratore_ordine; 
	private String find_numero_ordine; 
	private String find_riga_ordine; 
	private String find_consegna_ordine; 
	private String find_cd_uop_ordine;

	protected BulkList<EvasioneOrdineRigaBulk> evasioneOrdineRigheColl= new BulkList<EvasioneOrdineRigaBulk>();
	protected BulkList<OrdineAcqConsegnaBulk> righeConsegnaSelezionate= new BulkList<OrdineAcqConsegnaBulk>();
	protected BulkList<OrdineAcqConsegnaBulk> righeConsegnaDaEvadereColl= new BulkList<OrdineAcqConsegnaBulk>();

	private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<AllegatoGenericoBulk>();

	private MagazzinoBulk magazzinoAbilitato = new MagazzinoBulk();

	private List<BollaScaricoMagBulk> listaBolleScarico;

	public List<BollaScaricoMagBulk> getListaBolleScarico() {
		return listaBolleScarico;
	}

	public void setListaBolleScarico(List<BollaScaricoMagBulk> listaBolleScarico) {
		this.listaBolleScarico = listaBolleScarico;
	}

	private final String EVASIONE_ORDINE_FILEFOLDER = "Evasione Ordini";

	public EvasioneOrdineBulk() {
		super();
	}

	public EvasioneOrdineBulk(java.lang.String cdCds, java.lang.String cdMagazzino, java.lang.Integer esercizio, java.lang.String cdNumeratoreMag, java.lang.Long numero) {
		super(cdCds, cdMagazzino, esercizio, cdNumeratoreMag, numero);
		setNumerazioneMag( new NumerazioneMagBulk(cdCds,cdMagazzino,esercizio,cdNumeratoreMag) );
	}

	public NumerazioneMagBulk getNumerazioneMag() {
		return numerazioneMag;
	}

	public void setNumerazioneMag(NumerazioneMagBulk numerazioneMag)  {
		this.numerazioneMag=numerazioneMag;
	}

	public java.lang.String getCdCds() {
		return Optional.ofNullable(this.getNumerazioneMag())
				.map(NumerazioneMagBulk::getCdCds)
				.orElse(null);
	}

	public void setCdCds(java.lang.String cdCds)  {
		this.getNumerazioneMag().setCdCds(cdCds);
	}

	public java.lang.String getCdMagazzino() {
		return Optional.ofNullable(this.getNumerazioneMag())
				.map(NumerazioneMagBulk::getCdMagazzino)
				.orElse(null);
	}

	public void setCdMagazzino(java.lang.String cdMagazzino)  {
		this.getNumerazioneMag().setCdMagazzino(cdMagazzino);
	}

	public java.lang.Integer getEsercizio() {
		return Optional.ofNullable(this.getNumerazioneMag())
				.map(NumerazioneMagBulk::getEsercizio)
				.orElse(null);
	}

	public void setEsercizio(java.lang.Integer esercizio)  {
		this.getNumerazioneMag().setEsercizio(esercizio);
	}

	public java.lang.String getCdNumeratoreMag() {
		return Optional.ofNullable(this.getNumerazioneMag())
				.map(NumerazioneMagBulk::getCdNumeratoreMag)
				.orElse(null);
	}
	
	public void setCdNumeratoreMag(java.lang.String cdNumeratoreMag)  {
		this.getNumerazioneMag().setCdNumeratoreMag(cdNumeratoreMag);
	}
	
	public BulkList<OrdineAcqConsegnaBulk> getRigheConsegnaDaEvadereColl() {
		return righeConsegnaDaEvadereColl;
	}
	
	public void setRigheConsegnaDaEvadereColl(BulkList<OrdineAcqConsegnaBulk> righeConsegnaDaEvadereColl) {
		this.righeConsegnaDaEvadereColl = righeConsegnaDaEvadereColl;
	}

	public OrdineAcqConsegnaBulk removeFromConsegnaDaEvadereColl(int index) 
	{
		OrdineAcqConsegnaBulk element = righeConsegnaDaEvadereColl.get(index);
		return righeConsegnaDaEvadereColl.remove(index);
	}

	public int addToRigheOrdineColl( OrdineAcqConsegnaBulk nuovoRigo ) {
		nuovoRigo.setStato(OrdineAcqRigaBulk.STATO_INSERITA);
		righeConsegnaDaEvadereColl.add(nuovoRigo);
		return righeConsegnaDaEvadereColl.size()-1;
	}
	
	public BulkCollection[] getBulkLists() {
		return new it.cnr.jada.bulk.BulkCollection[] { 
				evasioneOrdineRigheColl
		};
	}
	
	public List getChildren() {
		return getRigheConsegnaDaEvadereColl();
	}
	
	public UnitaOperativaOrdBulk getUnitaOperativaAbilitata() {
		return unitaOperativaAbilitata;
	}
	
	public void setUnitaOperativaAbilitata(UnitaOperativaOrdBulk unitaOperativaAbilitata) {
		this.unitaOperativaAbilitata = unitaOperativaAbilitata;
	}
	
	public java.lang.String getCdUnitaOperativa() {
		UnitaOperativaOrdBulk uo = this.getUnitaOperativaAbilitata();
		if (uo == null)
			return null;
		return getUnitaOperativaAbilitata().getCdUnitaOperativa();
	}

	public void setCdUnitaOperativa(java.lang.String cdUnitaOperativa)  {
		this.getUnitaOperativaAbilitata().setCdUnitaOperativa(cdUnitaOperativa);
	}

	public String getFind_cd_terzo() {
		return find_cd_terzo;
	}
	
	public void setFind_cd_terzo(String find_cd_terzo) {
		this.find_cd_terzo = find_cd_terzo;
	}
	
	public String getFind_ragione_sociale() {
		return find_ragione_sociale;
	}
	
	public void setFind_ragione_sociale(String find_ragione_sociale) {
		this.find_ragione_sociale = find_ragione_sociale;
	}
	
	public String getFind_cd_precedente() {
		return find_cd_precedente;
	}
	
	public void setFind_cd_precedente(String find_cd_precedente) {
		this.find_cd_precedente = find_cd_precedente;
	}
	
	public String getFind_esercizio_ordine() {
		return find_esercizio_ordine;
	}
	
	public void setFind_esercizio_ordine(String find_esercizio_ordine) {
		this.find_esercizio_ordine = find_esercizio_ordine;
	}
	
	public Timestamp getFind_data_ordine() {
		return find_data_ordine;
	}
	
	public void setFind_data_ordine(Timestamp find_data_ordine) {
		this.find_data_ordine = find_data_ordine;
	}
	
	public String getFind_cd_numeratore_ordine() {
		return find_cd_numeratore_ordine;
	}
	
	public void setFind_cd_numeratore_ordine(String find_cd_numeratore_ordine) {
		this.find_cd_numeratore_ordine = find_cd_numeratore_ordine;
	}
	
	public String getFind_numero_ordine() {
		return find_numero_ordine;
	}
	
	public void setFind_numero_ordine(String find_numero_ordine) {
		this.find_numero_ordine = find_numero_ordine;
	}
	
	public String getFind_riga_ordine() {
		return find_riga_ordine;
	}
	
	public void setFind_riga_ordine(String find_riga_ordine) {
		this.find_riga_ordine = find_riga_ordine;
	}
	
	public String getFind_consegna_ordine() {
		return find_consegna_ordine;
	}
	
	public void setFind_consegna_ordine(String find_consegna_ordine) {
		this.find_consegna_ordine = find_consegna_ordine;
	}
	
	public String getFind_cd_uop_ordine() {
		return find_cd_uop_ordine;
	}

	public void setFind_cd_uop_ordine(String find_cd_uop_ordine) {
		this.find_cd_uop_ordine = find_cd_uop_ordine;
	}
	
	public BulkList<EvasioneOrdineRigaBulk> getEvasioneOrdineRigheColl() {
		return evasioneOrdineRigheColl;
	}
	
	public void setEvasioneOrdineRigheColl(BulkList<EvasioneOrdineRigaBulk> evasioneOrdineRigheColl) {
		this.evasioneOrdineRigheColl = evasioneOrdineRigheColl;
	}
	
	public int addToEvasioneOrdineRigheColl( EvasioneOrdineRigaBulk riga ) {
		evasioneOrdineRigheColl.add(riga);
		riga.setEvasioneOrdine(this);
		if ( riga.getRiga() == null )
			riga.setRiga(Long.valueOf(this.getEvasioneOrdineRigheColl().size() + 1));
		return evasioneOrdineRigheColl.size()-1;
	}
	
	public EvasioneOrdineRigaBulk removeFromEvasioneOrdineRigheColl(int index) {
		return (EvasioneOrdineRigaBulk)evasioneOrdineRigheColl.remove(index);
	}
	
	public BulkList<OrdineAcqConsegnaBulk> getRigheConsegnaSelezionate() {
		return righeConsegnaSelezionate;
	}

	public void setRigheConsegnaSelezionate(BulkList<OrdineAcqConsegnaBulk> righeConsegnaSelezionate) {
		this.righeConsegnaSelezionate = righeConsegnaSelezionate;
	}
	
	public MagazzinoBulk getMagazzinoAbilitato() {
		return magazzinoAbilitato;
	}
	
	public void setMagazzinoAbilitato(MagazzinoBulk magazzinoAbilitato) {
		this.magazzinoAbilitato = magazzinoAbilitato;
	}
	
	public boolean isROMagazzino(){
		return Optional.ofNullable(this.getUnitaOperativaAbilitata())
				.map(e->!Optional.ofNullable(e.getCdUnitaOperativa()).isPresent())
				.orElse(Boolean.FALSE);
	}

	/**
	 * Restituisce la collezione degli allegati
	 */
	@Override
	public BulkList<AllegatoGenericoBulk> getArchivioAllegati() {
		return archivioAllegati;
	}

	/**
	 * Imposta la collezione degli allegati
	 */
	@Override
	public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegati) {
		this.archivioAllegati = archivioAllegati;
	}

	/**
	 * Aggiunge un allegato alla collezione
	 * @param allegato l'allegato da aggiungere
	 * @return l'indice dell'allegato aggiunto
	 */
	@Override
	public int addToArchivioAllegati(AllegatoGenericoBulk allegato) {
		if (allegato != null) {
			archivioAllegati.add(allegato);
		}
		return archivioAllegati.size() - 1;
	}

	/**
	 * Rimuove un allegato dalla collezione
	 * @param index l'indice dell'allegato da rimuovere
	 * @return l'allegato rimosso
	 */
	@Override
	public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
		return getArchivioAllegati().remove(index);
	}

	/**
	 * Restituisce il percorso di storage CMIS per gli allegati dell'evasione ordine.
	 * Formato: PathComunicazioniDal/Evasione Ordini/[Esercizio]/[CD_UO]/[CD_MAGAZZINO]/[NUMERO]/Evasione [Esercizio]_[Numero]
	 *
	 * @return lista contenente il percorso completo
	 */
	@Override
	public List<String> getStorePath() {
		String esercizio = Optional.ofNullable(this.getEsercizio())
				.map(Object::toString)
				.orElse("0");
		String uo = Optional.ofNullable(this.getCdUnitaOperativa())
				.orElse("NO_UO");
		String cdMagazzino = Optional.ofNullable(this.getCdMagazzino())
				.orElse("NO_MAG");
		String numeroStr = Optional.ofNullable(this.getNumero())
				.map(numero -> Utility.lpad(numero.toString(), 10, '0'))
				.orElse("0000000000");

		String path = Arrays.asList(
				SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
				EVASIONE_ORDINE_FILEFOLDER,
				esercizio,
				uo,
				cdMagazzino,
				numeroStr,
				"Evasione " + esercizio + "_" + numeroStr
		).stream().collect(Collectors.joining(StorageDriver.SUFFIX));

		return Collections.singletonList(path);
	}
}