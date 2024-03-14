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

import java.util.Arrays;

public class AssociazioneContoGruppoBulk extends AssociazioneContoGruppoBase {
	public enum PianoGruppi {
		CE("Conto di Economica"),
		SPATT("Stato Patrimoniale Attivo"),
		SPPAS("Stato Patrimoniale Passivo");
		private final String label;

		private PianoGruppi(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}
	public enum Sezione {
		D("Dare"), A("Avere");
		private final String label;

		private Sezione(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}
	public enum Segno {
		POSITIVO("+","Positivo"), NEGATIVO("-", "Negativo");
		private final String value;
		private final String label;

		private Segno(String value, String label) {
			this.label = label;
			this.value = value;
		}
		public String value() {
			return value;
		}

		public String label() {
			return label;
		}
	}
	public final static java.util.Dictionary ti_pianoGruppiKeys = new it.cnr.jada.util.OrderedHashtable();
	public final static java.util.Dictionary ti_sezioneKeys = new it.cnr.jada.util.OrderedHashtable();
	public final static java.util.Dictionary ti_segnoKeys = new it.cnr.jada.util.OrderedHashtable();
	static {
		Arrays.asList(PianoGruppi.values()).stream().forEach(pianoGruppi -> {
			ti_pianoGruppiKeys.put(pianoGruppi.name(), pianoGruppi.label());
		});
		Arrays.asList(Sezione.values()).stream().forEach(sezione -> {
			ti_sezioneKeys.put(sezione.name(), sezione.label());
		});
		Arrays.asList(Segno.values()).stream().forEach(segno -> {
			ti_segnoKeys.put(segno.value(), segno.label());
		});
	}

	/**
	 * [VOCE_EP ]
	 **/
	private Voce_epBulk voceEp =  new Voce_epBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CNR_ASS_CONTO_GRUPPO_EP
	 **/
	public AssociazioneContoGruppoBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CNR_ASS_CONTO_GRUPPO_EP
	 **/
	public AssociazioneContoGruppoBulk(Integer esercizio, String cdPianoGruppi, String cdGruppoEp, String cdVoceEp) {
		super(esercizio, cdPianoGruppi, cdGruppoEp, cdVoceEp);
		setVoceEp( new Voce_epBulk(cdVoceEp,esercizio) );
	}
	public Voce_epBulk getVoceEp() {
		return voceEp;
	}
	public void setVoceEp(Voce_epBulk voceEp)  {
		this.voceEp=voceEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizio]
	 **/
	public Integer getEsercizio() {
		Voce_epBulk voceEp = this.getVoceEp();
		if (voceEp == null)
			return null;
		return getVoceEp().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizio]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.getVoceEp().setEsercizio(esercizio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdVoceEp]
	 **/
	public String getCdVoceEp() {
		Voce_epBulk voceEp = this.getVoceEp();
		if (voceEp == null)
			return null;
		return getVoceEp().getCd_voce_ep();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdVoceEp]
	 **/
	public void setCdVoceEp(String cdVoceEp)  {
		this.getVoceEp().setCd_voce_ep(cdVoceEp);
	}
}