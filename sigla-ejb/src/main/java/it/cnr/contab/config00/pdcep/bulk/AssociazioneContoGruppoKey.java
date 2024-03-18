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
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class AssociazioneContoGruppoKey extends OggettoBulk implements KeyedPersistent {
	private Integer esercizio;
	private String cdPianoGruppi;
	private String cdGruppoEp;
	private String cdVoceEp;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CNR_ASS_CONTO_GRUPPO_EP
	 **/
	public AssociazioneContoGruppoKey() {
		super();
	}
	public AssociazioneContoGruppoKey(Integer esercizio, String cdPianoGruppi, String cdGruppoEp, String cdVoceEp) {
		super();
		this.esercizio=esercizio;
		this.cdPianoGruppi=cdPianoGruppi;
		this.cdGruppoEp=cdGruppoEp;
		this.cdVoceEp=cdVoceEp;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof AssociazioneContoGruppoKey)) return false;
		AssociazioneContoGruppoKey k = (AssociazioneContoGruppoKey) o;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getCdPianoGruppi(), k.getCdPianoGruppi())) return false;
		if (!compareKey(getCdGruppoEp(), k.getCdGruppoEp())) return false;
		if (!compareKey(getCdVoceEp(), k.getCdVoceEp())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getCdPianoGruppi());
		i = i + calculateKeyHashCode(getCdGruppoEp());
		i = i + calculateKeyHashCode(getCdVoceEp());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizio]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizio]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdPianoGruppi]
	 **/
	public void setCdPianoGruppi(String cdPianoGruppi)  {
		this.cdPianoGruppi=cdPianoGruppi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdPianoGruppi]
	 **/
	public String getCdPianoGruppi() {
		return cdPianoGruppi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdGruppoEp]
	 **/
	public void setCdGruppoEp(String cdGruppoEp)  {
		this.cdGruppoEp=cdGruppoEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdGruppoEp]
	 **/
	public String getCdGruppoEp() {
		return cdGruppoEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdVoceEp]
	 **/
	public void setCdVoceEp(String cdVoceEp)  {
		this.cdVoceEp=cdVoceEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdVoceEp]
	 **/
	public String getCdVoceEp() {
		return cdVoceEp;
	}
}