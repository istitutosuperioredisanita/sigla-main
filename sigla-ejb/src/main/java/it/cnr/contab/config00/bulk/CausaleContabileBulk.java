/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/12/2024
 */
package it.cnr.contab.config00.bulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;
import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammBulk;
import it.cnr.contab.util.ICancellatoLogicamente;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.jada.util.jsp.Button;

import java.time.format.DateTimeFormatter;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

public class CausaleContabileBulk extends CausaleContabileBase implements ICancellatoLogicamente {
	/**
	 * [TIPO_DOCUMENTO_AMM ]
	 **/
	private Tipo_documento_ammBulk tipoDocumentoAmm =  new Tipo_documento_ammBulk();

	public Dictionary<String, String> tiDocumentoAmmKeys;

	public BulkList<AssCausaleVoceEPBulk> assCausaleVoceEPBulks = new BulkList<AssCausaleVoceEPBulk>();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CAUSALE_CONTABILE
	 **/
	public CausaleContabileBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CAUSALE_CONTABILE
	 **/
	public CausaleContabileBulk(java.lang.String cdCausale) {
		super(cdCausale);
	}
	public Tipo_documento_ammBulk getTipoDocumentoAmm() {
		return tipoDocumentoAmm;
	}
	public void setTipoDocumentoAmm(Tipo_documento_ammBulk tipoDocumentoAmm)  {
		this.tipoDocumentoAmm=tipoDocumentoAmm;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Identificativo delle tipologie di  documenti amministrativi gestiti.]
	 **/
	public java.lang.String getCdTipoDocumentoAmm() {
		Tipo_documento_ammBulk tipoDocumentoAmm = this.getTipoDocumentoAmm();
		if (tipoDocumentoAmm == null)
			return null;
		return getTipoDocumentoAmm().getCd_tipo_documento_amm();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Identificativo delle tipologie di  documenti amministrativi gestiti.]
	 **/
	public void setCdTipoDocumentoAmm(java.lang.String cdTipoDocumentoAmm)  {
		this.getTipoDocumentoAmm().setCd_tipo_documento_amm(cdTipoDocumentoAmm);
	}

	@Override
	public OggettoBulk initializeForInsert(CRUDBP crudbp, ActionContext actioncontext) {
		setDtInizioValidita(EJBCommonServices.getServerDate());
		return super.initializeForInsert(crudbp, actioncontext);
	}

	@Override
	public void validate() throws ValidationException {
		if (Optional.ofNullable(getDtFineValidita())
				.filter(fineValidita -> fineValidita.before(Optional.ofNullable(getDtInizioValidita()).orElse(fineValidita)))
						.isPresent()){
			throw new ValidationException(
					String.format(
						"La data di fine validità %s non può essere inferiore alla data di inizio validità %s!",
						getDtFineValidita().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
						getDtInizioValidita().toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
					)
			);
		}
		super.validate();
	}

	public Dictionary<String, String> getTiDocumentoAmmKeys() {
		return tiDocumentoAmmKeys;
	}

	public void setTiDocumentoAmmKeys(Dictionary<String, String> tiDocumentoAmmKeys) {
		this.tiDocumentoAmmKeys = tiDocumentoAmmKeys;
	}

	@Override
	public BulkCollection[] getBulkLists() {
		return Stream.of(getAssCausaleVoceEPBulks()).toArray(BulkCollection[]::new);
	}

	public int addToAssCausaleVoceEPBulks( AssCausaleVoceEPBulk assCausaleVoceEPBulk ) {
		assCausaleVoceEPBulks.add(assCausaleVoceEPBulk);
		assCausaleVoceEPBulk.setCausaleContabile(this);
		return assCausaleVoceEPBulks.size()-1;
	}

	public AssCausaleVoceEPBulk removeFromAssCausaleVoceEPBulks(int index) {
		return assCausaleVoceEPBulks.remove(index);
	}

	public BulkList<AssCausaleVoceEPBulk> getAssCausaleVoceEPBulks() {
		return assCausaleVoceEPBulks;
	}

	public void setAssCausaleVoceEPBulks(BulkList<AssCausaleVoceEPBulk> assCausaleVoceEPBulks) {
		this.assCausaleVoceEPBulks = assCausaleVoceEPBulks;
	}

	@Override
	public boolean isCancellatoLogicamente() {
		return Optional.ofNullable(getDtFineValidita()).isPresent();
	}

	@Override
	public void cancellaLogicamente() {
		setDtFineValidita(EJBCommonServices.getServerTimestamp());
	}
}