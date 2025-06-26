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
 * Created on Oct 4, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.si.spring.storage.annotation.StorageProperty;
import it.cnr.si.spring.storage.annotation.StorageType;

import java.util.Optional;

/**
 * @author 
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@StorageType(name="D:conssost:document")
public class ArchiviaConsSostitutivaBulk extends OggettoBulk {



	private Integer esercizio;


	public ArchiviaConsSostitutivaBulk() {
		super();
	}
	

	@StorageProperty(name="conssost:esercizio")
	public Integer getEsercizio(){
		return this.esercizio;
	}
	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}
	@StorageProperty(name="cmis:name")
	public String getCMISFolderName(){
		return  Optional.ofNullable(getEsercizio())
				.map(esercizio -> String.valueOf(esercizio))
				.orElse("0");
	}
	



}
