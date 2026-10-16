package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.util.OrderedHashtable;

public class AllegatoDocumentoTrasportoBulk extends AllegatoDocTraspRientroBulk {
	private static final long serialVersionUID = 1L;

	public static final String P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO = "P:sigla_doctrasporto_attachment:altro";
	public static final String P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO = "P:sigla_doctrasporto_attachment:doctrasporto_firmato";
	public static final String P_SIGLA_DOCTRASPORTO_ATTACHMENT_DA_FIRMARE = "P:sigla_doctrasporto_attachment:doctrasporto_da_firmare";

	private static final OrderedHashtable etichetteAspect = new OrderedHashtable();

	static {
		etichetteAspect.put(P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO, "Altro");
		etichetteAspect.put(P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO, "Documento di Trasporto");
		etichetteAspect.put(P_SIGLA_DOCTRASPORTO_ATTACHMENT_DA_FIRMARE, "Documento di Trasporto");
	}

	private String aspectName;

	public AllegatoDocumentoTrasportoBulk() {
		super();
	}

	public AllegatoDocumentoTrasportoBulk(String storageKey) {
		super(storageKey);
	}

	@Override
	public String getAspectName() {
		return aspectName;
	}

	@Override
	public void setAspectName(String aspectName) {
		this.aspectName = aspectName;
	}

	@Override
	public String getAspectAltroName() {
		return P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO;
	}

	@Override
	public String getAspectFirmatoName() {
		return P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
	}

	@Override
	public String getAspectDaFirmareName() {
		return P_SIGLA_DOCTRASPORTO_ATTACHMENT_DA_FIRMARE;
	}

	@Override
	protected OrderedHashtable getEtichetteAspect() {
		return etichetteAspect;
	}
}