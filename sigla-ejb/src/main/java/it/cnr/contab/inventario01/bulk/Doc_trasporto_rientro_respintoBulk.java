/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/11/2025
 */
package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.persistency.Keyed;

public class Doc_trasporto_rientro_respintoBulk extends Doc_trasporto_rientro_respintoBase implements Keyed {

	public static final String OPERAZIONE_TRASPORTO = "T";
	public static final String OPERAZIONE_RIENTRO = "R";
	public static final String FASE_RIFIUTO_FIRMA = "RFI";


	/**
	 * Riferimento al documento di trasporto/rientro (OBBLIGATORIO)
	 * Questo oggetto è usato per delegare i campi della chiave
	 */
	private Doc_trasporto_rientroBulk docTrasportoRientro;

	public Doc_trasporto_rientro_respintoBulk() {
		super();
		//setDocTrasportoRientro(new DocumentoRientroBulk());
	}


	public Doc_trasporto_rientro_respintoBulk(
			Long pgInventario,
			String tiDocumento,
			Integer esercizio,
			Long pgDocTrasportoRientro) {
		super();
		/*
		setDocTrasportoRientro(new DocumentoRientroBulk(
				pgInventario,
				tiDocumento,
				esercizio,
				pgDocTrasportoRientro));

		 */
	}

	// Getter e Setter

	public Long getPgInventario() {
		Doc_trasporto_rientroBulk doc = getDocTrasportoRientro();
		if (doc == null) return null;
		return doc.getPgInventario();
	}

	public void setPgInventario(Long pgInventario) {
		getDocTrasportoRientro().setPgInventario(pgInventario);
	}

	public String getTiDocumento() {
		Doc_trasporto_rientroBulk doc = getDocTrasportoRientro();
		if (doc == null) return null;
		return doc.getTiDocumento();
	}

	public void setTiDocumento(String tiDocumento) {
		getDocTrasportoRientro().setTiDocumento(tiDocumento);
	}


	public Integer getEsercizio() {
		Doc_trasporto_rientroBulk doc = getDocTrasportoRientro();
		if (doc == null) return null;
		return doc.getEsercizio();
	}

	public void setEsercizio(Integer esercizio) {
		getDocTrasportoRientro().setEsercizio(esercizio);
	}

	public Long getPgDocTrasportoRientro() {
		Doc_trasporto_rientroBulk doc = getDocTrasportoRientro();
		if (doc == null) return null;
		return doc.getPgDocTrasportoRientro();
	}

	public void setPgDocTrasportoRientro(Long pgDocTrasportoRientro) {
		getDocTrasportoRientro().setPgDocTrasportoRientro(pgDocTrasportoRientro);
	}

	public Doc_trasporto_rientroBulk getDocTrasportoRientro() {
		return docTrasportoRientro;
	}

	public void setDocTrasportoRientro(Doc_trasporto_rientroBulk docTrasportoRientro) {
		this.docTrasportoRientro = docTrasportoRientro;
	}
}
