package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.util.OrderedHashtable;

public class AllegatoDocumentoRientroBulk extends AllegatoDocTraspRientroBulk {
	private static final long serialVersionUID = 1L;

	public static final String P_SIGLA_DOCRIENTRO_ATTACHMENT_ALTRO = "P:sigla_docrientro_attachment:altro";
	public static final String P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO = "P:sigla_docrientro_attachment:docrientro_firmato";
	public static final String P_SIGLA_DOCRIENTRO_ATTACHMENT_DA_FIRMARE = "P:sigla_docrientro_attachment:docrientro_da_firmare";

	private static final OrderedHashtable etichetteAspect = new OrderedHashtable();

	static {
		etichetteAspect.put(P_SIGLA_DOCRIENTRO_ATTACHMENT_ALTRO, "Altro");
		etichetteAspect.put(P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO, "Documento di Rientro");
		etichetteAspect.put(P_SIGLA_DOCRIENTRO_ATTACHMENT_DA_FIRMARE, "Documento di Rientro");
	}

	private String aspectName;

	public AllegatoDocumentoRientroBulk() {
		super();
	}

	public AllegatoDocumentoRientroBulk(String storageKey) {
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
		return P_SIGLA_DOCRIENTRO_ATTACHMENT_ALTRO;
	}

	@Override
	public String getAspectFirmatoName() {
		return P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO;
	}

	@Override
	public String getAspectDaFirmareName() {
		return P_SIGLA_DOCRIENTRO_ATTACHMENT_DA_FIRMARE;
	}

	@Override
	protected OrderedHashtable getEtichetteAspect() {
		return etichetteAspect;
	}
}