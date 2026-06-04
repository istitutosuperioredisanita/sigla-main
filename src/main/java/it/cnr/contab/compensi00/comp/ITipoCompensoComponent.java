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

import it.cnr.contab.compensi00.tabrif.bulk.Tipo_CompensoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.ICRUDMgr;

/**
 * Insert the type's description here.
 * Creation date: (08/03/2002 11.11.50)
 * @author: Roberto Fantino
 */
public interface ITipoCompensoComponent extends ICRUDMgr {
/**
 * Ricerca lista intervalli di validità Tipi Compneso
 * PreCondition:
 *   Viene richiesta la lista degli intervalli di validità del tipo trattamento
 *        definiti con data inizio = a quella del tipo compenso in processo
 * PostCondition:
 *   Viene restituita la lista dei Tipi compensi o null nel caso il codice tipo compenso
 *        in processo sia null
 *
*/

java.util.List caricaIntervalli(UserContext param0, Tipo_CompensoBulk param1) throws ComponentException;

}
