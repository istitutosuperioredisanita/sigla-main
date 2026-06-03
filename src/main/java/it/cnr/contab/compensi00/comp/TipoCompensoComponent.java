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

package it.cnr.contab.compensi00.comp;

import it.cnr.contab.compensi00.docs.bulk.CompensoBulk;
import it.cnr.contab.compensi00.tabrif.bulk.Tipo_CompensoBulk;
import it.cnr.contab.compensi00.tabrif.bulk.Tipo_CompensoHome;
import it.cnr.contab.compensi00.tabrif.bulk.Tipo_trattamentoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;

import java.io.Serializable;

/**
 * Insert the type's description here.
 * Creation date: (08/03/2002 11.10.39)
 * @author: Roberto Fantino
 */
public class TipoCompensoComponent extends it.cnr.jada.comp.CRUDComponent implements ITipoCompensoComponent, Cloneable,Serializable{
/**
 * TipoTrattamentoComponent constructor comment.
 */
public TipoCompensoComponent() {
	super();
}
/**
 * Ricerca lista intervalli di validità Tipi Trattamento
 * PreCondition:
 *   Viene richiesta la lista degli intervalli di validità del tipo trattamento
 *        definiti con data inizio = a quella del tipo trattamento in processo
 * PostCondition:
 *   Viene restituita la lista dei Tipi trattamento o null nel caso il codice tipo trattamento
 *        in processo sia null
 *
*/
public java.util.List caricaIntervalli(UserContext userContext, Tipo_CompensoBulk tipoCompenso) throws ComponentException{

	try{

		if (tipoCompenso.getCdTrattamento()==null)
			return null;

		Tipo_CompensoHome home = (Tipo_CompensoHome)getHome(userContext, Tipo_CompensoBulk.class);
		return home.caricaIntervalli(tipoCompenso);

	}catch (it.cnr.jada.persistency.PersistencyException ex){
		throw handleException(tipoCompenso,ex);
	}
}
public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk) throws ComponentException{

	super.validaCreaConBulk(userContext, bulk);

	Tipo_CompensoBulk tipoCompenso = (Tipo_CompensoBulk)bulk;
	validaTipoCompenso(userContext, tipoCompenso);

	return inserisciTipoCompenso(userContext, tipoCompenso);
}
/**
 * Cancellazione di un intervallo di validità con data anteriore alla data odierna
 * PreCondition:
 *   La data di inizio dell'intervallo è anteriore alla data odierna
 * PostCondition:
 *   La data di fine validità dell'intervallo viene posta uguale alla data corrente + 1
 *        Tutti gli intervalli successivi a quello in processo vengono eliminati fisicamente
 *
 * Cancellazione di un intervallo di validità con data uguale alla data odierna
 * PreCondition:
 *   La data di inizio dell'intervallo è anteriore alla data odierna
 * PostCondition:
 *        Tutti gli intervalli successivi a quello in processo vengono eliminati fisicamente
 */
public void eliminaConBulk(UserContext userContext,OggettoBulk bulk) throws ComponentException {

	try{
		
		Tipo_trattamentoBulk tratt = (Tipo_trattamentoBulk)bulk;
		java.sql.Timestamp dataOdierna = it.cnr.jada.util.ejb.EJBCommonServices.getServerDate();

		// cancellazione logica dell'oggetto attivo
		if (tratt.getDt_ini_validita().compareTo(dataOdierna)<=0){
			tratt.setDt_fin_validita(CompensoBulk.incrementaData(dataOdierna));
			updateBulk(userContext, tratt);
		}

		// cancellazione fisica di tutti gli intervalli successivi
		for(java.util.Iterator i = tratt.getIntervalli().iterator();i.hasNext();){
			Tipo_trattamentoBulk el = (Tipo_trattamentoBulk)i.next();
			if ( el.getDt_ini_validita().compareTo(dataOdierna)>0 && 
				 el.getDt_ini_validita().compareTo(tratt.getDt_ini_validita())>=0 )
				deleteBulk(userContext,el);
		}
	}catch(jakarta.ejb.EJBException ex){
		throw handleException(ex);
	}catch(it.cnr.jada.persistency.PersistencyException ex){
		throw handleException(ex);
	}

}
public OggettoBulk inizializzaBulkPerModifica(UserContext userContext,OggettoBulk bulk) throws ComponentException {

	Tipo_CompensoBulk tipoCompensoBulk = (Tipo_CompensoBulk)super.inizializzaBulkPerModifica(userContext,bulk);
	tipoCompensoBulk.setIntervalli(caricaIntervalli(userContext, tipoCompensoBulk));

	return tipoCompensoBulk;
}
/**
 * Inserimento di un nuovo intervallo di validità di tipo compenso (primo intervallo)
 * PreCondition:
 *   La lista degli intervalli di validità esistenti per il record in processo è vuota
 *        Il controllo di validità date è superato
 * PostCondition:
 *   Viene creato per il tipo compenso il nuovo intervallo di validità (che è anche il primo)
 *
 *      Data di inizio validità nuovo intervallo non corretta
 * PreCondition:
 *   La lista degli intervalli di validità esistenti per il record in processo non è vuota
 *        Il controllo di validità date è superato
 *               La data di inizio validità dell'intervallo in processo <> dalla data di fine dell'ultimo intervallo + 1
 *        oppure la data di inizio validità dell'intervallo in processo <= della data odierna *        oppure la data di inizio validità dell'intervallo in processo > ultima data fine di intervallo esistente
 * PostCondition:
 *        Viene sollevata un'eccezione
 *
 * Inserimento di un nuovo intervallo di validità di record (intervallo n+1-esimo) futuro
 * PreCondition:
 *   La lista degli intervalli di validità esistenti per il record in processo non è vuota
 *        Il controllo di validità date è superato
 *        La data di inizio validità dell'intervallo in processo = alla data di fine dell'ultimo intervallo + 1
 *        La data di inizio validità dell'intervallo in processo > ultima data fine di intervallo esistente
 * PostCondition:
 *   Viene creato per il tipo trattamento il nuovo intervallo di validità
 *
 * Inserimento di un nuovo intervallo non valido (intervallo n+1-esimo) a spaccatura dell'intervallo corrente
 * PreCondition:
 *   La lista degli intervalli di validità esistenti per il record in processo non è vuota
 *        La data di inizio dell'intervallo nuovo è contenuta in un intervallo esistente 
 *        L'intervallo non è l'ultimo e la data di fine validità dell'intervallo in processo è maggiore della data di fine validità dell'intervallo corrente
 * PostCondition:
 *   Viene sollevata un'eccezione
 *
 * Inserimento di un nuovo intervallo di validità di record (intervallo n+1-esimo) a spaccatura del corrente
 * PreCondition:
 *   La lista degli intervalli di validità esistenti per il record in processo non è vuota
 *        La data di inizio dell'intervallo nuovo è contenuta in un intervallo esistente 
 *        La data di fine validità del nuovo intervallo è DATA_INFINITO
 * PostCondition:
 *   Viene aggiornata la data fine dell'intervallo corrente al giorno precedente alla data di inizio dell'intervallo nuovo
 *        Se la data di fine validità del nuovo intervallo è DATA_INFINITO, viene posta uguale alla data di fine dell'intervallo corrente
 *
 * Richiesta di Inserimento di un nuovo intervallo a copertura di intervalli temporali "occupati" parzialmente
 * PreCondition:
 *   La lista degli intervalli di validità esistenti per il record in processo non è vuota
 *        La data di inizio dell'intervallo nuovo non è contenuta in un intervallo esistente 
 *        La data di fine non è precedente ad ogni data di inizio validità di intervalli esistenti
 *      PostCondition:
 *   Viene sollevata un'eccezione
 *
 * Inserimento di un nuovo intervallo a copertura di intervalli temporali non "occupati"
 * PreCondition:
 *   La lista degli intervalli di validità esistenti per il record in processo non è vuota
 *        La data di inizio dell'intervallo nuovo non è contenuta in un intervallo esistente 
 *        La data di fine è precedente ad ogni data di inizio validità di intervalli esistenti
 * PostCondition:
 *   Viene inserito il nuovo intervallo
 *
 */
private Tipo_CompensoBulk inserisciTipoCompenso(UserContext userContext, Tipo_CompensoBulk tipoCompenso) throws ComponentException{

	try{

		// Carico tutti gli intervalli definiti per quel tipo Compenso
		Tipo_CompensoHome tipoCompensoHome = (Tipo_CompensoHome)getHome(userContext, Tipo_CompensoBulk.class);
		java.util.List l = caricaIntervalli(userContext, tipoCompenso);

		if (l.isEmpty()){
			validaDate(userContext, tipoCompenso);
			insertBulk(userContext, tipoCompenso);
			return tipoCompenso;
		}
			
		java.sql.Timestamp dataOdierna = tipoCompensoHome.getServerDate();
		Tipo_CompensoBulk ultimo = (Tipo_CompensoBulk)l.get(l.size()-1);

		// caso 1: inserimenti futuri
		validaDate(userContext, tipoCompenso);
		if (tipoCompenso.getDtInizioValidita().after(ultimo.getDtFineValidita())){
			if (tipoCompenso.getDtInizioValidita().after(dataOdierna)){
				if (tipoCompenso.getDtInizioValidita().equals(CompensoBulk.incrementaData(ultimo.getDtFineValidita()))){
					insertBulk(userContext, tipoCompenso);
				}else
					throw new it.cnr.jada.comp.ApplicationException("La Data Inizio Validita non è valida.\nGli intervalli devono essere contigui");
			}else
				throw new it.cnr.jada.comp.ApplicationException("La Data Inizio Validita deve essere superiore alla data odierna");

		}else{

			// caso 2: inserimenti in intervallo corrente
			Tipo_CompensoBulk corrente = tipoCompensoHome.findIntervallo(tipoCompenso);

			if (corrente != null){
				if (tipoCompenso.getDtInizioValidita().after(dataOdierna)){
					if (tipoCompenso.getDtFineValidita().equals(it.cnr.contab.config00.esercizio.bulk.EsercizioHome.DATA_INFINITO))
						tipoCompenso.setDtFineValidita(corrente.getDtInizioValidita());

					if (!isUltimoIntervallo(userContext, corrente)){
						if(tipoCompenso.getDtFineValidita().after(corrente.getDtFineValidita()))
							throw new it.cnr.jada.comp.ApplicationException("La Data Fine Validita non è valida");
					}
					corrente.setDtFineValidita(CompensoBulk.decrementaData(tipoCompenso.getDtInizioValidita()));
					updateBulk(userContext, corrente);
					insertBulk(userContext, tipoCompenso);
				}else
					throw new it.cnr.jada.comp.ApplicationException("La Data Inizio Validita deve essere superiore alla data odierna");
		
			}else{

				// caso 3: riempimento "buchi" in intervalli preesistenti
				for(int i = 0;i<l.size();i++){
					Tipo_CompensoBulk el = (Tipo_CompensoBulk)l.get(i);
					if (tipoCompenso.getDtFineValidita().before(el.getDtInizioValidita())){
						insertBulk(userContext, tipoCompenso);
						return(tipoCompenso);
					}else if (tipoCompenso.getDtFineValidita().before(el.getDtFineValidita()))
						throw new it.cnr.jada.comp.ApplicationException("Intervallo non valido.\nSovrapposizione con intervalli preesistenti");
				}
			}
		}
			
		return tipoCompenso;

	}catch (it.cnr.jada.persistency.PersistencyException ex){
		throw handleException(tipoCompenso,ex);
	}
}
/**
  *    L'intervallo in processo è l'ultimo intervallo esistente
  *    PreCondition:
  *       La data di inizio validità dell'intervallo in processo >= della massima data di inizio di intervalli
  *    PostCondition:
  *       Viene ritornato TRUE
  *
  *    L'intervallo in processo non è l'ultimo intervallo esistente
  *    PreCondition:
  *       La data di inizio validità dell'intervallo in processo < della massima data di inizio di intervalli
  *    PostCondition:
  *       Viene ritornato FALSE
 */
public boolean isUltimoIntervallo(UserContext userContext, Tipo_CompensoBulk tipoCompensoBulk) throws ComponentException{

	try{
		Tipo_CompensoHome tipoCompensoHome = (Tipo_CompensoHome)getHome(userContext, tipoCompensoBulk);

		it.cnr.jada.persistency.sql.SQLBuilder sql = tipoCompensoHome.createSQLBuilder();
		java.sql.Timestamp maxData = (java.sql.Timestamp)tipoCompensoHome.findMax(tipoCompensoBulk, "dtInizioValidita", null, false);
		if (maxData == null)
			return(true);

		if (!tipoCompensoBulk.getDtInizioValidita().before(maxData) )
			return (true);

		return (false);
	}catch(it.cnr.jada.persistency.PersistencyException ex){
		throw handleException(ex);
	}catch(it.cnr.jada.bulk.BusyResourceException ex){
		throw handleException(ex);
	}
}
/**
 * Modifica di intervallo ponendo la data fine nel futuro
 * PreCondition:
 *   Il controllo di validità date è superato
 * PostCondition:
 *   Viene aggiornato l'intervallo in processo
 *
 *      Modifica di intervallo ponendo la data fine < alla data odierna
 * PreCondition: La data di fine intervallo = alla data odierna
 * PostCondition:
 *        Viene sollevata un'eccezione
 *
 *      Modifica di intervallo ponendo la data fine nel passato
 * PreCondition: La data di fine intervallo = alla data odierna
 * PostCondition:
 *        La data di fine validità dell'intervallo corrente viene posta = data odierna
 *        Viene creato il nuovo intervallo con data di inizio validità = alla data odierna + 1
 *
 */
public OggettoBulk modificaConBulk(UserContext userContext,OggettoBulk bulk) throws ComponentException {

	try{
		super.validaModificaConBulk(userContext, bulk);
		
		Tipo_CompensoBulk tipo_compenso = (Tipo_CompensoBulk)bulk;
		Tipo_CompensoHome tipoCompensoHome = (Tipo_CompensoHome)getHome(userContext, tipo_compenso);
		java.sql.Timestamp dataOdierna = tipoCompensoHome.getServerDate();

		// Intervallo futuro
		if (tipo_compenso.getDtInizioValidita().after(dataOdierna)){
			validaDate(userContext, tipo_compenso);
			updateBulk(userContext, tipo_compenso);
		}else{

		// Intervallo corrente: spezzo in due
			Tipo_CompensoBulk current = (Tipo_CompensoBulk)tipoCompensoHome.findByPrimaryKey(tipo_compenso, true);
			if (!isUltimoIntervallo(userContext, current) && tipo_compenso.getDtInizioValidita()==null){
				tipo_compenso.setDtFineValidita(current.getDtInizioValidita());
			}
			validaDate(userContext, tipo_compenso);

			if (!tipo_compenso.getDtFineValidita().before(dataOdierna)){
				current.setDtFineValidita(dataOdierna);
				updateBulk(userContext, current);
				tipo_compenso.setDtInizioValidita(CompensoBulk.incrementaData(dataOdierna));
				insertBulk(userContext, tipo_compenso);
			}else{
				throw new it.cnr.jada.comp.ApplicationException("La data fine validità deve essere superiore alla data odierna");
			}
		}

		return tipo_compenso;
	
	}catch(it.cnr.jada.persistency.PersistencyException ex){
		throw handleException(ex);
	}
}
/**
  *    Controlli di validazione del periodo di inizio/fine validita' 
  *    del nuovo record non superati
  *    PreCondition:
  *  validazione periodo inizio/fine non superata (data inizio validita del nuovo 
  *  record non definita o maggiore della data di fine validità)
  *    PostCondition:
  *      Viene sollevata un'eccezione
  *
  *    Impostazione data di fine periodo INFINITO
  *    PreCondition:
  *  la data di fine validità periodo non impostata
  *    PostCondition:
  *      La data di fine periodo viene impostata a DATA_INFINITO
  */
private void validaDate(UserContext userContext, Tipo_CompensoBulk tipoCompensoBulk) throws ComponentException{

	if (tipoCompensoBulk.getDtInizioValidita()==null)
		throw new it.cnr.jada.comp.ApplicationException("Il campo Data Inizio Validità deve essere valorizzato");

	if (tipoCompensoBulk.getDtFineValidita()==null)
		tipoCompensoBulk.setDtFineValidita(it.cnr.contab.config00.esercizio.bulk.EsercizioHome.DATA_INFINITO);


	if (tipoCompensoBulk.getDtInizioValidita().after(tipoCompensoBulk.getDtFineValidita()))
		throw new it.cnr.jada.comp.ApplicationException("La Data Fine Validità deve essere superiore alla Data Inizio Validità");
}
/**
  *    Controlli di validazione del record tipo trattamento non superati 
  *    PreCondition:
  *       Il codice o la descrizione del tipo trattamento non sono definiti
  *    PostCondition:
  *       Viene sollevata un'eccezione
 */
private void validaTipoCompenso(UserContext userContext, Tipo_CompensoBulk tipoCompenso) throws ComponentException{

	// controllo su campo CD_CONTRIBUTO_RITENUTA
	if ( tipoCompenso.getCdTiCompenso() == null )
		throw new it.cnr.jada.comp.ApplicationException( "Il campo Codice deve essere valorizzato !" );
	
	// controllo su campo DS_CONTRIBUTO_RITENUTA
	if ( tipoCompenso.getDsTiCompenso() == null )
		throw new it.cnr.jada.comp.ApplicationException( "Il campo Descrizione deve essere valorizzato !" );
}
}
