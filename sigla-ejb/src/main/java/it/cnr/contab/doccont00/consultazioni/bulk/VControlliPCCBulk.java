/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/05/2024
 */
package it.cnr.contab.doccont00.consultazioni.bulk;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.contab.docamm00.fatturapa.bulk.DocumentoEleTestataBulk;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Dictionary;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VControlliPCCBulk extends OggettoBulk implements Persistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_CONTROLLI_PCC
	 **/
	public VControlliPCCBulk() {
		super();
	}

	//    IDENTIFICATIVO_SDI DECIMAL(0,-127) NOT NULL
	private java.math.BigDecimal identificativoSdi;

	//    NUMERO_DOCUMENTO VARCHAR(20)
	private String numeroDocumento;

	//    DATA_DOCUMENTO TIMESTAMP(7)
	private java.sql.Timestamp dataDocumento;

	//    CODICE_FISCALE VARCHAR(256)
	private String codiceFiscale;

	//    CODICE_DESTINATARIO VARCHAR(6)
	private String codiceDestinatario;

	//    DATA_RICEZIONE TIMESTAMP(7)
	private java.sql.Timestamp dataRicezione;

	//    DATA_SCADENZA TIMESTAMP(7)
	private java.sql.Timestamp dataScadenza;

	//    STATO_DOCUMENTO VARCHAR(60)
	private String statoDocumento;

	//    IMPORTO_DOCUMENTO DECIMAL(17,2)
	private java.math.BigDecimal importoDocumento;

	//    IMPONIBILE DECIMAL(0,-127)
	private java.math.BigDecimal imponibile;

	//    IMPOSTA DECIMAL(0,-127)
	private java.math.BigDecimal imposta;

	//    TIPO_DOCUMENTO VARCHAR(256)
	private String tipoDocumento;

	//    ESERCIZIO DECIMAL(4,0)
	private Integer esercizio;

	//    CD_UNITA_ORGANIZZATIVA VARCHAR(30)
	private String cdUnitaOrganizzativa;

	//    PG_FATTURA_PASSIVA DECIMAL(10,0)
	private Long pgFatturaPassiva;

	//    DT_REGISTRAZIONE TIMESTAMP(7)
	private java.sql.Timestamp dtRegistrazione;

	//    STATO_LIQUIDAZIONE VARCHAR(10)
	private String statoLiquidazione;

	//    CAUSALE VARCHAR(10)
	private String causale;

	//    DT_INIZIO_SOSPENSIONE TIMESTAMP(7)
	private java.sql.Timestamp dtInizioSospensione;

	//    DT_EMISSIONE_MAN TIMESTAMP(7)
	private java.sql.Timestamp dtEmissioneMan;

	//    IM_MANDATO DECIMAL(0,-127)
	private java.math.BigDecimal imMandato;

	//    IM_TOTALE_NC DECIMAL(0,-127)
	private java.math.BigDecimal imTotaleNC;

	//    CD_TIPO_CONTRATTO VARCHAR(5)
	private String cdTipoContratto;

	//    FL_IRREGISTRABILE CHAR(1)
	private String flIrregistrabile;

	//    CD_UO_CUU VARCHAR2(30)
	private String cdUoCUU;
	//    CD_UO_CUU VARCHAR2(30)
	private Boolean flDaCompenso;
	//  ESITO_PCC VARCHAR(30)
	private java.lang.String esitoPCC;
	private Boolean flSplitPayment;
	//    DA_PAGARE DECIMAL(17,2)
	private java.math.BigDecimal daPagare;

	//  ESTERO VARCHAR(30)
	private java.lang.String estero;
	//  TI_ISTITUZ_COMMERC VARCHAR(30)
	private java.lang.String tiIstituzCommerc;

	//  CD_TERZO NUMBER(38)
	private java.lang.Integer cdTerzo;
	//  DS_TERZO VARCHAR(200)
	private java.lang.String dsTerzo;

	private String riepilogo_cds_codice;
	private String riepilogo_cds_descrizione;
	private String riepilogo_uo_codice;
	private String riepilogo_uo_descrizione;
	private BigDecimal riepilogo_totale;

	private Integer riepilogo_stato_esercizio;
	private Integer riepilogo_stato_num_ricevute;
	private BigDecimal riepilogo_stato_importo_ricevute;
	private Integer riepilogo_stato_num_registrate;
	private BigDecimal riepilogo_stato_importo_registrate;
	private Integer riepilogo_stato_num_pagate;
	private BigDecimal riepilogo_stato_importo_pagate;


	public static final java.util.Dictionary<String, String> tiStatoDocumentoKeys = DocumentoEleTestataBulk.tiStatoDocumentoKeys;
	public static final java.util.Dictionary<String, String> tiTipoDocumentoKeys = DocumentoEleTestataBulk.tiTipoDocumentoKeys;
	public static final java.util.Dictionary<String, String> tiCausaleKeys = Fattura_passivaBulk.CAUSALE;
	public static final java.util.Dictionary<String, String> tiStatoLiquidazioneKeys = Fattura_passivaBulk.STATO_LIQUIDAZIONE;
	public static final java.util.Dictionary<String, String> decorrenzaTerminiKeys = DocumentoEleTestataBulk.tiDecorrenzaTerminiKeys;
	public static final java.util.Dictionary<String, String> tiIstituzCommercKeys = TipoIVA.TipoIVAKeys;
	public static final java.util.Dictionary<String, String> tiEsteroKeys = Estero.EsteroKeys;

	public enum Estero {
		EXTRA_UE,INTRA_UE,SAN_MARINO_CON_IVA,SAN_MARINO_SENZA_IVA;
		public final static Dictionary EsteroKeys = new it.cnr.jada.util.OrderedHashtable();
		static {
			for (Estero estero1 : Estero.values()) {
				EsteroKeys.put(estero1.name(), estero1.name().replace("_", " "));
			}
		}

	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Identificativo SDI]
	 **/
	public java.math.BigDecimal getIdentificativoSdi() {
		return identificativoSdi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Identificativo SDI]
	 **/
	public void setIdentificativoSdi(java.math.BigDecimal identificativoSdi)  {
		this.identificativoSdi=identificativoSdi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Destinatario]
	 **/
	public String getCodiceDestinatario() {
		return codiceDestinatario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Destinatario]
	 **/
	public void setCodiceDestinatario(String codiceDestinatario)  {
		this.codiceDestinatario=codiceDestinatario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data Ricezione]
	 **/
	public java.sql.Timestamp getDataRicezione() {
		return dataRicezione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data Ricezione]
	 **/
	public void setDataRicezione(java.sql.Timestamp dataRicezione)  {
		this.dataRicezione=dataRicezione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data Scadenza]
	 **/
	public java.sql.Timestamp getDataScadenza() {
		return dataScadenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data Scadenza]
	 **/
	public void setDataScadenza(java.sql.Timestamp dataScadenza)  {
		this.dataScadenza=dataScadenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Stato Documento]
	 **/
	public String getStatoDocumento() {
		return statoDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Stato Documento]
	 **/
	public void setStatoDocumento(String statoDocumento)  {
		this.statoDocumento=statoDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Importo Documento]
	 **/
	public java.math.BigDecimal getImportoDocumento() {
		return importoDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Importo Documento]
	 **/
	public void setImportoDocumento(java.math.BigDecimal importoDocumento)  {
		this.importoDocumento=importoDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Imponibile]
	 **/
	public java.math.BigDecimal getImponibile() {
		return imponibile;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Imponibile]
	 **/
	public void setImponibile(java.math.BigDecimal imponibile)  {
		this.imponibile=imponibile;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Imposta]
	 **/
	public java.math.BigDecimal getImposta() {
		return imposta;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Imposta]
	 **/
	public void setImposta(java.math.BigDecimal imposta)  {
		this.imposta=imposta;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Tipo Documento]
	 **/
	public String getTipoDocumento() {
		return tipoDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Tipo Documento]
	 **/
	public void setTipoDocumento(String tipoDocumento)  {
		this.tipoDocumento=tipoDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Unità Organizzativa]
	 **/
	public String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Unità Organizzativa]
	 **/
	public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa)  {
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Progressivo Fattura Passiva]
	 **/
	public Long getPgFatturaPassiva() {
		return pgFatturaPassiva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Progressivo Fattura Passiva]
	 **/
	public void setPgFatturaPassiva(Long pgFatturaPassiva)  {
		this.pgFatturaPassiva=pgFatturaPassiva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data Registrazione]
	 **/
	public java.sql.Timestamp getDtRegistrazione() {
		return dtRegistrazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data Registrazione]
	 **/
	public void setDtRegistrazione(java.sql.Timestamp dtRegistrazione)  {
		this.dtRegistrazione=dtRegistrazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Stato Liquidazione]
	 **/
	public String getStatoLiquidazione() {
		return statoLiquidazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Stato Liquidazione]
	 **/
	public void setStatoLiquidazione(String statoLiquidazione)  {
		this.statoLiquidazione=statoLiquidazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data Emissione Mandato]
	 **/
	public java.sql.Timestamp getDtEmissioneMan() {
		return dtEmissioneMan;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data Emissione Mandato]
	 **/
	public void setDtEmissioneMan(java.sql.Timestamp dtEmissioneMan)  {
		this.dtEmissioneMan=dtEmissioneMan;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Importo Mandato]
	 **/
	public java.math.BigDecimal getImMandato() {
		return imMandato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Importo Mandato]
	 **/
	public void setImMandato(java.math.BigDecimal imMandato)  {
		this.imMandato=imMandato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Contratto]
	 **/
	public String getCdTipoContratto() {
		return cdTipoContratto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Contratto]
	 **/
	public void setCdTipoContratto(String cdTipoContratto)  {
		this.cdTipoContratto=cdTipoContratto;
	}

	public String getCodiceFiscale() {
		return codiceFiscale;
	}

	public void setCodiceFiscale(String codiceFiscale) {
		this.codiceFiscale = codiceFiscale;
	}

	public String getCausale() {
		return causale;
	}

	public void setCausale(String causale) {
		this.causale = causale;
	}

	public Timestamp getDtInizioSospensione() {
		return dtInizioSospensione;
	}

	public void setDtInizioSospensione(Timestamp dtInizioSospensione) {
		this.dtInizioSospensione = dtInizioSospensione;
	}

	public Timestamp getDataDocumento() {
		return dataDocumento;
	}

	public void setDataDocumento(Timestamp dataDocumento) {
		this.dataDocumento = dataDocumento;
	}

	public String getFlIrregistrabile() {
		return flIrregistrabile;
	}

	public void setFlIrregistrabile(String flIrregistrabile) {
		this.flIrregistrabile = flIrregistrabile;
	}

	public String getCdUoCUU() {
		return cdUoCUU;
	}

	public void setCdUoCUU(String cdUoCUU) {
		this.cdUoCUU = cdUoCUU;
	}

	public Boolean getFlDaCompenso() {
		return flDaCompenso;
	}

	public void setFlDaCompenso(Boolean flDaCompenso) {
		this.flDaCompenso = flDaCompenso;
	}

	public BigDecimal getImTotaleNC() {
		return imTotaleNC;
	}

	public void setImTotaleNC(BigDecimal imTotaleNC) {
		this.imTotaleNC = imTotaleNC;
	}

	public String getEsitoPCC() {
		return esitoPCC;
	}

	public void setEsitoPCC(String esitoPCC) {
		this.esitoPCC = esitoPCC;
	}

	public Boolean getFlSplitPayment() {
		return flSplitPayment;
	}

	public void setFlSplitPayment(Boolean flSplitPayment) {
		this.flSplitPayment = flSplitPayment;
	}

	public BigDecimal getDaPagare() {
		return daPagare;
	}

	public String getEstero() {
		return estero;
	}

	public void setEstero(String estero) {
		this.estero = estero;
	}

	public String getTiIstituzCommerc() {
		return tiIstituzCommerc;
	}

	public void setTiIstituzCommerc(String tiIstituzCommerc) {
		this.tiIstituzCommerc = tiIstituzCommerc;
	}

	public void setDaPagare(BigDecimal daPagare) {
		this.daPagare = daPagare;
	}

	public Integer getCdTerzo() {
		return cdTerzo;
	}

	public void setCdTerzo(Integer cdTerzo) {
		this.cdTerzo = cdTerzo;
	}

	public String getDsTerzo() {
		return dsTerzo;
	}

	public void setDsTerzo(String dsTerzo) {
		this.dsTerzo = dsTerzo;
	}

	public String getRiepilogo_cds_codice() {
		return riepilogo_cds_codice;
	}

	public void setRiepilogo_cds_codice(String riepilogo_cds_codice) {
		this.riepilogo_cds_codice = riepilogo_cds_codice;
	}

	public String getRiepilogo_cds_descrizione() {
		return riepilogo_cds_descrizione;
	}

	public void setRiepilogo_cds_descrizione(String riepilogo_cds_descrizione) {
		this.riepilogo_cds_descrizione = riepilogo_cds_descrizione;
	}

	public String getRiepilogo_uo_codice() {
		return riepilogo_uo_codice;
	}

	public void setRiepilogo_uo_codice(String riepilogo_uo_codice) {
		this.riepilogo_uo_codice = riepilogo_uo_codice;
	}

	public String getRiepilogo_uo_descrizione() {
		return riepilogo_uo_descrizione;
	}

	public void setRiepilogo_uo_descrizione(String riepilogo_uo_descrizione) {
		this.riepilogo_uo_descrizione = riepilogo_uo_descrizione;
	}

	public BigDecimal getRiepilogo_totale() {
		return riepilogo_totale;
	}

	public void setRiepilogo_totale(BigDecimal riepilogo_totale) {
		this.riepilogo_totale = riepilogo_totale;
	}

	public Integer getRiepilogo_stato_esercizio() {
		return riepilogo_stato_esercizio;
	}

	public void setRiepilogo_stato_esercizio(Integer riepilogo_stato_esercizio) {
		this.riepilogo_stato_esercizio = riepilogo_stato_esercizio;
	}

	public Integer getRiepilogo_stato_num_ricevute() {
		return riepilogo_stato_num_ricevute;
	}

	public void setRiepilogo_stato_num_ricevute(Integer riepilogo_stato_num_ricevute) {
		this.riepilogo_stato_num_ricevute = riepilogo_stato_num_ricevute;
	}

	public BigDecimal getRiepilogo_stato_importo_ricevute() {
		return riepilogo_stato_importo_ricevute;
	}

	public void setRiepilogo_stato_importo_ricevute(BigDecimal riepilogo_stato_importo_ricevute) {
		this.riepilogo_stato_importo_ricevute = riepilogo_stato_importo_ricevute;
	}

	public Integer getRiepilogo_stato_num_registrate() {
		return riepilogo_stato_num_registrate;
	}

	public void setRiepilogo_stato_num_registrate(Integer riepilogo_stato_num_registrate) {
		this.riepilogo_stato_num_registrate = riepilogo_stato_num_registrate;
	}

	public BigDecimal getRiepilogo_stato_importo_registrate() {
		return riepilogo_stato_importo_registrate;
	}

	public void setRiepilogo_stato_importo_registrate(BigDecimal riepilogo_stato_importo_registrate) {
		this.riepilogo_stato_importo_registrate = riepilogo_stato_importo_registrate;
	}

	public Integer getRiepilogo_stato_num_pagate() {
		return riepilogo_stato_num_pagate;
	}

	public void setRiepilogo_stato_num_pagate(Integer riepilogo_stato_num_pagate) {
		this.riepilogo_stato_num_pagate = riepilogo_stato_num_pagate;
	}

	public BigDecimal getRiepilogo_stato_importo_pagate() {
		return riepilogo_stato_importo_pagate;
	}

	public void setRiepilogo_stato_importo_pagate(BigDecimal riepilogo_stato_importo_pagate) {
		this.riepilogo_stato_importo_pagate = riepilogo_stato_importo_pagate;
	}
}