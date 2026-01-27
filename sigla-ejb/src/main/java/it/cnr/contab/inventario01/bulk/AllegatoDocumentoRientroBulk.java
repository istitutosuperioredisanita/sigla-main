package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.util.OrderedHashtable;

public class AllegatoDocumentoRientroBulk extends AllegatoDocTraspRientroBulk {
	private static final long serialVersionUID = 1L;

	public static OrderedHashtable aspectNamesKeys = new OrderedHashtable();

	public static final String P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO = "P:sigla_doctrientro_attachment:altro";
	public static final String P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO = "P:sigla_docrientro_attachment:docrientro_firmato";

	static {
		aspectNamesKeys.put(P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO,"Altro");
		aspectNamesKeys.put(P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO,"Doc. Rientro Firmato");
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
	public void setAspectName(String aspectName) {
		this.aspectName = aspectName;
	}

	public static OrderedHashtable getAspectNamesKeys() {
		return aspectNamesKeys;
	}
}