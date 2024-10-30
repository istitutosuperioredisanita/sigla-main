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
 * Created on Jun 23, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.ordmag.magazzino.ejb;

import it.cnr.jada.UserContext;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.sql.CompoundFindClause;

import javax.ejb.Remote;
import java.rmi.RemoteException;

@Remote
public interface MagContoGiudizialeComponentSession extends it.cnr.jada.ejb.CRUDComponentSession {
    it.cnr.jada.util.RemoteIterator findMagContoGiudiziale(UserContext userContext,String columnMapName,CompoundFindClause baseClause, CompoundFindClause findClause) throws it.cnr.jada.comp.ComponentException, RemoteException, IntrospectionException;
}
