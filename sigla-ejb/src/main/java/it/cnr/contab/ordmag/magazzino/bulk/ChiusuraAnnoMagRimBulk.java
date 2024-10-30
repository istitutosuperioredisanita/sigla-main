/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/02/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.contab.ordmag.anag00.RaggrMagazzinoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;
public class ChiusuraAnnoMagRimBulk extends ChiusuraAnnoMagRimBase {
	/**
	 * [LOTTO_MAG ]
	 **/
	private LottoMagBulk lottoMag;
	/**
	 * [CHIUSURA_ANNO ]
	 **/
	private ChiusuraAnnoBulk chiusuraAnno;
	/**
	 * [RAGGR_MAGAZZINO ]
	 **/
	private RaggrMagazzinoBulk raggrMagazzino;
	/**
	 * [MAGAZZINO ]
	 **/
	private MagazzinoBulk magazzino;
	/**
	 * [CATEGORIA_GRUPPO_INVENT ]
	 **/
	private Categoria_gruppo_inventBulk categoriaGruppoInvent;
	/**
	 * [BENE_SERVIZIO ]
	 **/
	private Bene_servizioBulk beneServizio;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_MAG_RIM
	 **/
	public ChiusuraAnnoMagRimBulk() {
		super();
	}
	public ChiusuraAnnoMagRimBulk(boolean create) {
		super();
		if(create){
			lottoMag= new LottoMagBulk();
			chiusuraAnno = new ChiusuraAnnoBulk();
			raggrMagazzino = new RaggrMagazzinoBulk();
			magazzino = new MagazzinoBulk();
			categoriaGruppoInvent = new Categoria_gruppo_inventBulk();
			beneServizio=new Bene_servizioBulk();
		}
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_MAG_RIM
	 **/
	public ChiusuraAnnoMagRimBulk(String cdCdsLotto, String cdMagazzinoLotto, Integer esercizioLotto, String cdNumeratoreLotto, Integer pgLotto,Integer anno) {
		super(cdCdsLotto, cdMagazzinoLotto, esercizioLotto, cdNumeratoreLotto, pgLotto,anno);
		setLottoMag( new LottoMagBulk(cdCdsLotto,cdMagazzinoLotto,esercizioLotto,cdNumeratoreLotto,pgLotto) );
	}
	public LottoMagBulk getLottoMag() {
		return lottoMag;
	}
	public void setLottoMag(LottoMagBulk lottoMag)  {
		this.lottoMag=lottoMag;
	}
	public ChiusuraAnnoBulk getChiusuraAnno() {
		return chiusuraAnno;
	}
	public void setChiusuraAnno(ChiusuraAnnoBulk chiusuraAnno)  {
		this.chiusuraAnno=chiusuraAnno;
	}
	public RaggrMagazzinoBulk getRaggrMagazzino() {
		return raggrMagazzino;
	}
	public void setRaggrMagazzino(RaggrMagazzinoBulk raggrMagazzino)  {
		this.raggrMagazzino=raggrMagazzino;
	}
	public MagazzinoBulk getMagazzino() {
		return magazzino;
	}
	public void setMagazzino(MagazzinoBulk magazzino)  {
		this.magazzino=magazzino;
	}
	public Categoria_gruppo_inventBulk getCategoriaGruppoInvent() {
		return categoriaGruppoInvent;
	}
	public void setCategoriaGruppoInvent(Categoria_gruppo_inventBulk categoriaGruppoInvent)  {
		this.categoriaGruppoInvent=categoriaGruppoInvent;
	}
	public Bene_servizioBulk getBeneServizio() {
		return beneServizio;
	}
	public void setBeneServizio(Bene_servizioBulk beneServizio)  {
		this.beneServizio=beneServizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cds lotto]
	 **/
	public String getCdCdsLotto() {
		LottoMagBulk lottoMag = this.getLottoMag();
		if (lottoMag == null)
			return null;
		return getLottoMag().getCdCds();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cds lotto]
	 **/
	public void setCdCdsLotto(String cdCdsLotto)  {
		this.getLottoMag().setCdCds(cdCdsLotto);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cd magazzino lotto]
	 **/
	public String getCdMagazzinoLotto() {
		LottoMagBulk lottoMag = this.getLottoMag();
		if (lottoMag == null)
			return null;
		return getLottoMag().getCdMagazzino();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cd magazzino lotto]
	 **/
	public void setCdMagazzinoLotto(String cdMagazzinoLotto)  {
		this.getLottoMag().setCdMagazzino(cdMagazzinoLotto);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizio del Lotto]
	 **/
	public Integer getEsercizioLotto() {
		LottoMagBulk lottoMag = this.getLottoMag();
		if (lottoMag == null)
			return null;
		return getLottoMag().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizio del Lotto]
	 **/
	public void setEsercizioLotto(Integer esercizioLotto)  {
		this.getLottoMag().setEsercizio(esercizioLotto);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [numeratore del Lotto]
	 **/
	public String getCdNumeratoreLotto() {
		LottoMagBulk lottoMag = this.getLottoMag();
		if (lottoMag == null)
			return null;
		return getLottoMag().getCdNumeratoreMag();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numeratore del Lotto]
	 **/
	public void setCdNumeratoreLotto(String cdNumeratoreLotto)  {
		this.getLottoMag().setCdNumeratoreMag(cdNumeratoreLotto);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [progressivo del Lotto]
	 **/
	public Integer getPgLotto() {
		LottoMagBulk lottoMag = this.getLottoMag();
		if (lottoMag == null)
			return null;
		return getLottoMag().getPgLotto();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [progressivo del Lotto]
	 **/
	public void setPgLotto(Integer pgLotto)  {
		this.getLottoMag().setPgLotto(pgLotto);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Progressivo record chiusura]
	 **/
	public Integer getPgChiusura() {
		ChiusuraAnnoBulk chiusuraAnno = this.getChiusuraAnno();
		if (chiusuraAnno == null)
			return null;
		return getChiusuraAnno().getPgChiusura();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Progressivo record chiusura]
	 **/
	public void setPgChiusura(Integer pgChiusura)  {
		this.getChiusuraAnno().setPgChiusura(pgChiusura);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno di chiusura]
	 **/
	public Integer getAnno() {
		ChiusuraAnnoBulk chiusuraAnno = this.getChiusuraAnno();
		if (chiusuraAnno == null)
			return null;
		return getChiusuraAnno().getAnno();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno di chiusura]
	 **/
	public void setAnno(Integer anno)  {
		this.getChiusuraAnno().setAnno(anno);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [M=Magazzino; I=Inventario]
	 **/
	public String getTipoChiusura() {
		ChiusuraAnnoBulk chiusuraAnno = this.getChiusuraAnno();
		if (chiusuraAnno == null)
			return null;
		return getChiusuraAnno().getTipoChiusura();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [M=Magazzino; I=Inventario]
	 **/
	public void setTipoChiusura(String tipoChiusura)  {
		this.getChiusuraAnno().setTipoChiusura(tipoChiusura);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cds raggruppamento magazzino]
	 **/
	public String getCdCdsRaggrMag() {
		RaggrMagazzinoBulk raggrMagazzino = this.getRaggrMagazzino();
		if (raggrMagazzino == null)
			return null;
		return getRaggrMagazzino().getCdCds();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cds raggruppamento magazzino]
	 **/
	public void setCdCdsRaggrMag(String cdCdsRaggrMag)  {
		this.getRaggrMagazzino().setCdCds(cdCdsRaggrMag);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [codice raggruppamento magazzino]
	 **/
	public String getCdRaggrMag() {
		RaggrMagazzinoBulk raggrMagazzino = this.getRaggrMagazzino();
		if (raggrMagazzino == null)
			return null;
		return getRaggrMagazzino().getCdRaggrMagazzino();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [codice raggruppamento magazzino]
	 **/
	public void setCdRaggrMag(String cdRaggrMag)  {
		this.getRaggrMagazzino().setCdRaggrMagazzino(cdRaggrMag);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cds magazzino]
	 **/
	public String getCdCdsMag() {
		MagazzinoBulk magazzino = this.getMagazzino();
		if (magazzino == null)
			return null;
		return getMagazzino().getCdCds();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cds magazzino]
	 **/
	public void setCdCdsMag(String cdCdsMag)  {
		this.getMagazzino().setCdCds(cdCdsMag);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [codice magazzino]
	 **/
	public String getCdMagazzino() {
		MagazzinoBulk magazzino = this.getMagazzino();
		if (magazzino == null)
			return null;
		return getMagazzino().getCdMagazzino();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [codice magazzino]
	 **/
	public void setCdMagazzino(String cdMagazzino)  {
		this.getMagazzino().setCdMagazzino(cdMagazzino);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice categoria/gruppo]
	 **/
	public String getCdCategoriaGruppo() {
		Categoria_gruppo_inventBulk categoriaGruppoInvent = this.getCategoriaGruppoInvent();
		if (categoriaGruppoInvent == null)
			return null;
		return getCategoriaGruppoInvent().getCd_categoria_gruppo();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice categoria/gruppo]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.getCategoriaGruppoInvent().setCd_categoria_gruppo(cdCategoriaGruppo);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del bene o servizio]
	 **/
	public String getCdBeneServizio() {
		Bene_servizioBulk beneServizio = this.getBeneServizio();
		if (beneServizio == null)
			return null;
		return getBeneServizio().getCd_bene_servizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del bene o servizio]
	 **/
	public void setCdBeneServizio(String cdBeneServizio)  {
		this.getBeneServizio().setCd_bene_servizio(cdBeneServizio);
	}
}