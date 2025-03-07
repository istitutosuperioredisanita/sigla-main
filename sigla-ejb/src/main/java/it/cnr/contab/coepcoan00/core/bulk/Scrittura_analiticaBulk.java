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

package it.cnr.contab.coepcoan00.core.bulk;

import it.cnr.contab.config00.sto.bulk.*;
import it.cnr.contab.anagraf00.core.bulk.*;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.action.MessageToUser;
import it.cnr.jada.bulk.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Scrittura_analiticaBulk extends Scrittura_analiticaBase {
	protected Unita_organizzativaBulk uo = new Unita_organizzativaBulk();
	protected TerzoBulk terzo = new TerzoBulk();
	protected CdsBulk cds = new CdsBulk();
	protected CdsBulk cdsDocumento;
	protected Unita_organizzativaBulk uoDocumento;
	private String ti_istituz_commerc;

	public final static Dictionary tipoKeys = TipoIVA.TipoIVAKeys;

	protected BulkList movimentiColl = new BulkList();

	public final static String TIPO_COAN = "COAN";
	public final static String TIPO_PRIMA_SCRITTURA = "P";
	public final static String STATO_DEFINITIVO = "D";

	public final static java.util.Dictionary STATO_ATTIVA;	
	public final static String ATTIVA_YES = "Y";
	public final static String ATTIVA_NO = "N";	
	static
	{
		STATO_ATTIVA = new it.cnr.jada.util.OrderedHashtable();
		STATO_ATTIVA.put(ATTIVA_YES,"Y");
		STATO_ATTIVA.put(ATTIVA_NO,"N");
	}

	public final static java.util.Dictionary ti_origineKeys = new it.cnr.jada.util.OrderedHashtable();
	static {
		Arrays.stream(OrigineScritturaEnum.values()).forEach(origine -> ti_origineKeys.put(origine.name(), origine.label()));
	}

	public Scrittura_analiticaBulk() {
	super();
}
	public Scrittura_analiticaBulk(java.lang.String cd_cds,java.lang.String cd_unita_organizzativa,java.lang.Integer esercizio,java.lang.Long pg_scrittura) {
		super(cd_cds,cd_unita_organizzativa,esercizio,pg_scrittura);
		setCds(new it.cnr.contab.config00.sto.bulk.CdsBulk(cd_cds));
	}
	/**
	 * Metodo per l'aggiunta di un elemento <code>Movimento_coanBulk</code> alla <code>Collection</code>
	 * dei movimenti
	 * @param movimento Movimento_coanBulk Il movimento
	 * @return Il movimento con agguiornati i dati relativi alla sezione, stato, scrittura
	 */
	public int addToMovimentiColl( Movimento_coanBulk movimento ) {
		if (!Optional.ofNullable(movimento.getTi_istituz_commerc()).isPresent() &&
				!Optional.ofNullable(getTi_istituz_commerc()).isPresent()) {
			throw new MessageToUser("Specificare il Tipo in testata, Istituzionale/Commerciale!");
		}
		this.movimentiColl.add(movimento);
		movimento.setScrittura( this );
		movimento.setStato(Movimento_coanBulk.STATO_DEFINITIVO);
		movimento.setFl_modificabile(Boolean.TRUE);
		movimento.setTi_istituz_commerc(
				Optional.ofNullable(movimento.getTi_istituz_commerc())
						.orElse(getTi_istituz_commerc())
		);
		return movimentiColl.size()-1;
	}
	/**
	 * Restituisce un array di <code>BulkCollection</code> contenenti oggetti
	 * bulk da rendere persistenti insieme al ricevente.
	 * L'implementazione standard restituisce <code>null</code>.
	 */
	public BulkCollection[] getBulkLists() {
		 return new it.cnr.jada.bulk.BulkCollection[] {
				movimentiColl };

	}
	public java.lang.String getCd_cds() {
		it.cnr.contab.config00.sto.bulk.CdsBulk cds = this.getCds();
		if (cds == null)
			return null;
		return cds.getCd_unita_organizzativa();
	}
	public java.lang.Integer getCd_terzo() {
		it.cnr.contab.anagraf00.core.bulk.TerzoBulk terzo = this.getTerzo();
		if (terzo == null)
			return null;
		return terzo.getCd_terzo();
	}
	public java.lang.String getCd_unita_organizzativa() {
		it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk uo = this.getUo();
		if (uo == null)
			return null;
		return uo.getCd_unita_organizzativa();
	}
	/**
	 * @return it.cnr.contab.config00.sto.bulk.CdsBulk
	 */
	public it.cnr.contab.config00.sto.bulk.CdsBulk getCds() {
		return cds;
	}
	// calcolo il totale degli importi dei singoli dettagli
	public java.math.BigDecimal getImTotaleMov()
	{
		Movimento_coanBulk mov;
		java.math.BigDecimal tot = new java.math.BigDecimal(0);
        for (Object o : getMovimentiColl()) {
            mov = (Movimento_coanBulk) o;
            if (mov.getIm_movimento() != null)
                tot = tot.add(mov.getIm_movimento());
        }
		return tot;
	}
	/**
	 * @return it.cnr.jada.bulk.BulkList
	 */
	public it.cnr.jada.bulk.BulkList<Movimento_coanBulk> getMovimentiColl() {
		return movimentiColl;
	}
	/**
	 * Il metodo restituisce il dictionary per la gestione degli stati ATTIVA
	 * e NON ATTIVA
	 */
	public java.util.Dictionary getStato_attivaKeys()
	{
		return STATO_ATTIVA;
	}
	/**
	 * @return it.cnr.contab.anagraf00.core.bulk.TerzoBulk
	 */
	public it.cnr.contab.anagraf00.core.bulk.TerzoBulk getTerzo() {
		return terzo;
	}
	/**
	 * @return it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk
	 */
	public it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk getUo() {
		return uo;
	}
	/**
	 * Metodo per inizializzare un Oggetto Bulk in fase di inserimento.
	 */
	public OggettoBulk initializeForInsert(it.cnr.jada.util.action.CRUDBP bp,it.cnr.jada.action.ActionContext context) {
		setEsercizio(it.cnr.contab.utenze00.bulk.CNRUserInfo.getEsercizio(context));
		setUo(it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa( context));
		setCd_uo_documento(it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa( context).getCd_unita_organizzativa());
		setOrigine_scrittura(OrigineScritturaEnum.PRIMA_NOTA_MANUALE.name());
		setAttiva( ATTIVA_YES );
		setTi_scrittura( TIPO_PRIMA_SCRITTURA);
		setStato( STATO_DEFINITIVO);
	//	setCds(getUo().getUnita_padre());
		return this;
	}
	/**
	 * Metodo per inizializzare un Oggetto Bulk in fase di ricerca.
	 */
	public OggettoBulk initializeForSearch(it.cnr.jada.util.action.CRUDBP bp,it.cnr.jada.action.ActionContext context) {
		setEsercizio(it.cnr.contab.utenze00.bulk.CNRUserInfo.getEsercizio(context));
		setUo(it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa( context));
	//	setOrigine_scrittura( ORIGINE_CAUSALE );
		setAttiva( ATTIVA_YES );
	//	setCds(getUo().getUnita_padre());
		return this;
	}
	public boolean isROTerzo() {
		return 	getTerzo() != null &&
			   getTerzo().getCrudStatus() != UNDEFINED;

	}
	/**
	 * Metodo per l'eliminazione di un elemento <code>Movimento_coanBulk</code> dalla <code>Collection</code>
	 * dei movimenti della Scrittura analitica
	 * @param index L'indice del movimento da eliminare
	 * @return Movimento_coanBulk Il movimento rimosso
	 */
	public  Movimento_coanBulk removeFromMovimentiColl( int index )
	{
		return (Movimento_coanBulk) movimentiColl.remove( index );
	}
	public void setCd_cds(java.lang.String cd_cds) {
		this.getCds().setCd_unita_organizzativa(cd_cds);
	}
	public void setCd_terzo(java.lang.Integer cd_terzo) {
		this.getTerzo().setCd_terzo(cd_terzo);
	}
	public void setCd_unita_organizzativa(java.lang.String cd_unita_organizzativa) {
		this.getUo().setCd_unita_organizzativa(cd_unita_organizzativa);
	}
	/**
	 * @param newCds it.cnr.contab.config00.sto.bulk.CdsBulk
	 */
	public void setCds(it.cnr.contab.config00.sto.bulk.CdsBulk newCds) {
		cds = newCds;
	}
	/**
	 * @param newMovimentiColl it.cnr.jada.bulk.BulkList
	 */
	public void setMovimentiColl(it.cnr.jada.bulk.BulkList newMovimentiColl) {
		movimentiColl = newMovimentiColl;
	}
	/**
	 * @param newTerzo it.cnr.contab.anagraf00.core.bulk.TerzoBulk
	 */
	public void setTerzo(it.cnr.contab.anagraf00.core.bulk.TerzoBulk newTerzo) {
		terzo = newTerzo;
	}
	/**
	 * @param newUo it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk
	 */
	public void setUo(it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk newUo) {
		uo = newUo;
	}
	/**
	 * Effettua una validazione formale del contenuto dello stato dell'oggetto
	 * bulk. Viene invocato da <code>CRUDBP</code> in
	 * seguito ad una richiesta di salvataggio.
	 * @exception it.cnr.jada.bulk.ValidationException Se la validazione fallisce.
	 *		Contiene il messaggio da visualizzare all'utente per la notifica
	 *		dell'errore di validazione.
	 * @see it.cnr.jada.util.action.CRUDBP
	 */
	public void validate() throws ValidationException
	{
		// Controllo sul campo DESCRIZIONE
		if ( getDs_scrittura() == null )
			throw new ValidationException( "E' necessario inserire la descrizione della scrittura analitica");

		// Controllo sulla validità dei singoli movimenti
        for (Object o : getMovimentiColl()) ((Movimento_coanBulk) o).validate();
	}

	public CdsBulk getCdsDocumento() {
		return cdsDocumento;
	}

	public void setCdsDocumento(CdsBulk cdsDocumento) {
		this.cdsDocumento = cdsDocumento;
	}

	@Override
	public String getCd_cds_documento() {
		return Optional.ofNullable(getCdsDocumento())
				.map(CdsBulk::getCd_unita_organizzativa)
				.orElse(super.getCd_cds_documento());
	}

	@Override
	public void setCd_cds_documento(String cd_cds_documento) {
		if (!Optional.ofNullable(getCdsDocumento()).isPresent())
			setCdsDocumento(new CdsBulk());
		getCdsDocumento().setCd_unita_organizzativa(cd_cds_documento);
	}

	public Unita_organizzativaBulk getUoDocumento() {
		return uoDocumento;
	}

	public void setUoDocumento(Unita_organizzativaBulk uoDocumento) {
		this.uoDocumento = uoDocumento;
	}

	@Override
	public String getCd_uo_documento() {
		return Optional.ofNullable(getUoDocumento())
				.map(Unita_organizzativaBulk::getCd_unita_organizzativa)
				.orElse(super.getCd_uo_documento());
	}

	@Override
	public void setCd_uo_documento(String cd_uo_documento) {
		if (!Optional.ofNullable(getUoDocumento()).isPresent())
			setUoDocumento(new Unita_organizzativaBulk());
		getUoDocumento().setCd_unita_organizzativa(cd_uo_documento);
	}

	public void setTi_istituz_commerc(String ti_istituz_commerc) {
		this.ti_istituz_commerc = ti_istituz_commerc;
	}

	public String getTi_istituz_commerc() {
		return Optional.ofNullable(ti_istituz_commerc)
				.orElseGet(() -> {
					return getMovimentiColl()
							.stream()
							.map(Movimento_coanBase::getTi_istituz_commerc)
							.filter(tiIstituzCommerc -> Optional.ofNullable(tiIstituzCommerc).isPresent())
							.distinct()
							.findAny()
							.orElse(null);
				});
	}

	public boolean isScritturaAttiva() {
		return ATTIVA_YES.equals(this.getAttiva());
	}

	public List<Movimento_coanBulk> getMovimentiDareColl() {
		return this.getMovimentiColl().stream().filter(Movimento_coanBulk::isSezioneDare).collect(Collectors.toList());
	}

	public List<Movimento_coanBulk> getMovimentiAvereColl() {
		return this.getMovimentiColl().stream().filter(Movimento_coanBulk::isSezioneAvere).collect(Collectors.toList());
	}

	public java.math.BigDecimal getImTotaleAvere() {
		return getMovimentiAvereColl().stream()
				.map(mcb -> Optional.ofNullable(mcb.getIm_movimento()).orElse(BigDecimal.ZERO))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public java.math.BigDecimal getImTotaleDare() {
		return getMovimentiDareColl().stream()
				.map(mcb -> Optional.ofNullable(mcb.getIm_movimento()).orElse(BigDecimal.ZERO))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public java.math.BigDecimal getDifferenza() {
		return getImTotaleDare().subtract(this.getImTotaleAvere()).abs();
	}

}
