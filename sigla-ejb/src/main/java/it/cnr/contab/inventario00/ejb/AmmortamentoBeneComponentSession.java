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

package it.cnr.contab.inventario00.ejb;

import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;

import javax.ejb.Remote;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;

@Remote
public interface AmmortamentoBeneComponentSession extends it.cnr.jada.ejb.CRUDComponentSession {

    Boolean isExistAmmortamentoEsercizio(UserContext uc, Integer esercizio) throws RemoteException, InvocationTargetException;
    List<Ammortamento_bene_invBulk> getAllAmmortamentoEsercizio(UserContext uc, Integer esercizio) throws RemoteException, InvocationTargetException;
    Integer getNumeroAnnoAmmortamento(UserContext uc, Long pgInventario, Long nrInventario, Long progressivo) throws RemoteException;
    Integer getProgressivoRigaAmmortamento(UserContext uc, Long pgInventario, Long nrInventario, Long progressivo,Integer esercizio) throws RemoteException;
    void inserisciAmmortamentoBene(UserContext uc,Ammortamento_bene_invBulk amm) throws ComponentException,RemoteException;
    void cancellaAmmortamentiEsercizio(UserContext uc,Integer esercizio) throws ComponentException,RemoteException;
    void aggiornamentoInventarioBeneConAmmortamento(UserContext uc,Integer esercizio,String azione) throws ComponentException, RemoteException, PersistencyException;
}
