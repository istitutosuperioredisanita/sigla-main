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

package it.cnr.contab.inventario00.actions;

import it.cnr.contab.inventario00.bp.CRUDChiusuraInventarioBP;
import it.cnr.contab.inventario00.docs.bulk.Chiusura_anno_inventarioBulk;
import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoBulk;
import it.cnr.contab.ordmag.magazzino.ejb.ChiusuraAnnoComponentSession;
import it.cnr.contab.reports.action.ParametricPrintAction;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;

import java.rmi.RemoteException;

/**
 * Insert the type's description here.
 * Creation date: (16/11/2001 13.09.00)
 * @author: Roberto Fantino
 */
public class ChiusuraInventarioAction extends ParametricPrintAction {
	public Forward doPrint(ActionContext context) {

		CRUDChiusuraInventarioBP bp = (CRUDChiusuraInventarioBP) getBusinessProcess(context);

		try {
			Chiusura_anno_inventarioBulk model = (Chiusura_anno_inventarioBulk) bp.getModel();
			fillModel(context);

			ChiusuraAnnoComponentSession chiusuraAnnoComponent = (ChiusuraAnnoComponentSession) bp.createComponentSession(
					"CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);
			ChiusuraAnnoBulk chiusuraAnno =  chiusuraAnnoComponent.verificaChiusuraAnno(context.getUserContext(),model.getAnno(),ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);

			if(chiusuraAnno!= null) {
				model.setPgChiusura(chiusuraAnno.getPgChiusura());
			}else{
				bp.setErrorMessage("Nessuna chiusura da stampare per l'anno selezionato");
				return context.findDefaultForward();
			}

		} catch (FillException | BusinessProcessException e) {
			bp.setErrorMessage(e.getMessage());
		} catch (ComponentException e) {
			e.printStackTrace();
		} catch (PersistencyException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return super.doPrint(context);
	}
	protected ParametricPrintBP getBusinessProcess(ActionContext actioncontext) {
		return (ParametricPrintBP)actioncontext.getBusinessProcess();
	}
}
