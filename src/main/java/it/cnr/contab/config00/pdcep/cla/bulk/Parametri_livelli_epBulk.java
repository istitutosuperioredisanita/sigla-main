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
* Created by Generator 1.0
* Date 30/08/2005
*/
package it.cnr.contab.config00.pdcep.cla.bulk;

import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_epHome;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.jada.util.action.CRUDBP;
public class Parametri_livelli_epBulk extends Parametri_livelli_epBase {
	public final static it.cnr.jada.util.OrderedHashtable nrLivelliList;
	public final static it.cnr.jada.util.OrderedHashtable lunghezzaLivelloList;

	static {
		nrLivelliList = new it.cnr.jada.util.OrderedHashtable();
		for (int i = 0; i <= 8; i++)
			nrLivelliList.put(i,i);

		lunghezzaLivelloList = new it.cnr.jada.util.OrderedHashtable();
		for (int i = 1; i <= 5; i++)
			lunghezzaLivelloList.put(i,i);
	};

	public final static java.util.Dictionary tipoKeys;

	static {
		tipoKeys = new it.cnr.jada.util.OrderedHashtable();
		tipoKeys.put(Voce_epHome.ECONOMICA,"Economica");
		tipoKeys.put(Voce_epHome.PATRIMONIALE,"Patrimoniale");
		tipoKeys.put(Voce_epHome.ECONOMICA_ACCRUAL,"Economica Accrual");
		tipoKeys.put(Voce_epHome.PATRIMONIALE_ACCRUAL,"Patrimoniale Accrual");
		tipoKeys.put(Voce_epHome.SIOPE,"Siope");
		tipoKeys.put(Voce_epHome.SIOPE_RENDICONTO,"Siope Rendiconto");
	};

	public Parametri_livelli_epBulk() {
		super();
	}
	public Parametri_livelli_epBulk(java.lang.Integer esercizio, String tipo) {
		super(esercizio,tipo);
	}
	public String getDs_livello(int livello) {
		if (livello==1)  
			return(getDs_livello1());
		else if (livello==2)  
			return(getDs_livello2());
		else if (livello==3)  
			return(getDs_livello3());
		else if (livello==4)  
			return(getDs_livello4());
		else if (livello==5)  
			return(getDs_livello5());
		else if (livello==6)  
			return(getDs_livello6());
		else if (livello==7)  
			return(getDs_livello7());
		else  
			return(getDs_livello8());
	}
	public Integer getLung_livello(int livello) {
		if (livello==1)  
			return(getLung_livello1());
		else if (livello==2)  
			return(getLung_livello2());
		else if (livello==3)  
			return(getLung_livello3());
		else if (livello==4)  
			return(getLung_livello4());
		else if (livello==5)  
			return(getLung_livello5());
		else if (livello==6)  
			return(getLung_livello6());
		else if (livello==7)  
			return(getLung_livello7());
		else  
			return(getLung_livello8());
	}

	/*
	 * Verifica che i campi dei livelli da non gestire non sia siano valorizzati
	 */
	public void validaLivelliValorizzati() throws ValidationException {
		for (int i=1;i<=8;i++) {
			if ((this.getDs_livello(i)!=null || this.getLung_livello(i)!=null) &&
				(this.getLivelli() == null || this.getLivelli()<i))
				throw new ValidationException("Attenzione! I campi relativi al livello " + i + " non devono essere valorizzati.");
		}
	}

	/*
	 * Verifica che tutti i campi dei livelli da gestire per costruire la voce sia siano valorizzati
	 */
	public void validaLivelliNonValorizzati() throws ValidationException {
		for (int i=1;i<=8;i++) {
			if ((this.getDs_livello(i)==null || this.getLung_livello(i)==null) &&
				(this.getLivelli() != null && this.getLivelli()>=i))
				throw new ValidationException("Attenzione! I campi relativi al livello " + i + " devono essere valorizzati.");
		}
	}

	/*
	 * Ritorna ValidationException se:
	 * - Il numero dei livelli economica/patrimoniale inseriti è maggiore di 8
	 * - Non risultano valorizzati campi di un livello da gestire
	 * - Risultano valorizzati campi di un livello da non gestire
	 */
	public void validate() throws ValidationException {
		super.validate();

		if (this.getLivelli() != null && this.getLivelli()>8)
			throw new ValidationException("Attenzione! Il livello massimo consentito è 8.");

		validaLivelliValorizzati();
		validaLivelliNonValorizzati();
	}
}