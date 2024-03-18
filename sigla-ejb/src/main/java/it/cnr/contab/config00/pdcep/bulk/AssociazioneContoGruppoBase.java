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

package it.cnr.contab.config00.pdcep.bulk;
import it.cnr.jada.persistency.Keyed;
public class AssociazioneContoGruppoBase extends AssociazioneContoGruppoKey implements Keyed {
//    SEZIONE CHAR(1) NOT NULL
	private String sezione;
 
//    DS_ASSOCIAZIONE VARCHAR(200)
	private String dsAssociazione;
 
//    SEGNO CHAR(1)
	private String segno;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CNR_ASS_CONTO_GRUPPO_EP
	 **/
	public AssociazioneContoGruppoBase() {
		super();
	}
	public AssociazioneContoGruppoBase(Integer esercizio, String cdPianoGruppi, String cdGruppoEp, String cdVoceEp) {
		super(esercizio, cdPianoGruppi, cdGruppoEp, cdVoceEp);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [sezione]
	 **/
	public String getSezione() {
		return sezione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [sezione]
	 **/
	public void setSezione(String sezione)  {
		this.sezione=sezione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dsAssociazione]
	 **/
	public String getDsAssociazione() {
		return dsAssociazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dsAssociazione]
	 **/
	public void setDsAssociazione(String dsAssociazione)  {
		this.dsAssociazione=dsAssociazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [segno]
	 **/
	public String getSegno() {
		return segno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [segno]
	 **/
	public void setSegno(String segno)  {
		this.segno=segno;
	}
}