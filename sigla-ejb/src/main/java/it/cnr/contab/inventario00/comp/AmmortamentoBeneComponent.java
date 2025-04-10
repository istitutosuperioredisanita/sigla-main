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

package it.cnr.contab.inventario00.comp;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_pluriennaleBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_pluriennaleHome;
import it.cnr.contab.inventario00.docs.bulk.*;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioHome;
import it.cnr.contab.inventario00.tabrif.bulk.Ubicazione_beneBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.*;
import it.cnr.jada.util.RemoteIterator;

import javax.ejb.EJBException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class AmmortamentoBeneComponent
		extends it.cnr.jada.comp.CRUDDetailComponent
			implements  Serializable, Cloneable {
	/**
	 * Inventario_beniComponent constructor comment.
	 */
	public AmmortamentoBeneComponent() {
		super();
	}


	public Boolean isExistAmmortamentoEsercizio(UserContext uc, Integer esercizio) throws RemoteException {
		try {
			Ammortamento_bene_invHome ammortamentoBeneInvHome = (Ammortamento_bene_invHome)getHomeCache(uc).getHome( Ammortamento_bene_invBulk.class);
			return ammortamentoBeneInvHome.isExistAmmortamentoEsercizio(esercizio);
		} catch (ComponentException | PersistencyException ex) {
			throw new RemoteException("Error isExistAmmortamentoEsercizio esercizio : " + esercizio);
		}

	}

	public List<Ammortamento_bene_invBulk> getAllAmmortamentoEsercizio(UserContext uc, Integer esercizio) throws RemoteException {
		try {
			Ammortamento_bene_invHome ammortamentoBeneInvHome = (Ammortamento_bene_invHome)getHomeCache(uc).getHome( Ammortamento_bene_invBulk.class);
			return ammortamentoBeneInvHome.getAllAmmortamentoEsercizio(esercizio);
		} catch (ComponentException | PersistencyException ex) {
			throw new RemoteException("Error getAllAmmortamentoEsercizio esercizio : " + esercizio);
		}
	}

	public Integer getNumeroAnnoAmmortamento(UserContext uc, Long pgInventario, Long nrInventario, Long progressivo) throws RemoteException {
		try {
			Ammortamento_bene_invHome ammortamentoBeneInvHome = (Ammortamento_bene_invHome) getHome(uc, Ammortamento_bene_invBulk.class);
			return ammortamentoBeneInvHome.getNumeroAnnoAmmortamento(pgInventario, nrInventario, progressivo);
		} catch (ComponentException | PersistencyException ex) {
			throw new RemoteException("Error getNumeroAnnoAmmortamento pgInventario : " + pgInventario + " nrInventario:" + nrInventario + " progressivo:" + progressivo);
		}

	}
	public Integer getProgressivoRigaAmmortamento(UserContext uc, Long pgInventario, Long nrInventario, Long progressivo,Integer esercizio) throws RemoteException {
		try {
			Ammortamento_bene_invHome ammortamentoBeneInvHome = (Ammortamento_bene_invHome) getHome(uc, Ammortamento_bene_invBulk.class);
			return ammortamentoBeneInvHome.getProgressivoRigaAmmortamento(pgInventario, nrInventario, progressivo,esercizio);
		} catch (ComponentException | PersistencyException ex) {
			throw new RemoteException("Error getProgressivoRigaAmmortamento pgInventario : " + pgInventario + " nrInventario:" + nrInventario + " progressivo:" + progressivo+" esercizio:"+esercizio);
		}

	}
	public void inserisciAmmortamentoBene(UserContext uc,Ammortamento_bene_invBulk amm) throws ComponentException {
		LoggableStatement ps = null;
		try {
			//lockBulk(uc, amm);

			Ammortamento_bene_invHome ammHome = (Ammortamento_bene_invHome) getHome(uc, Ammortamento_bene_invBulk.class);

			ps = new LoggableStatement(getConnection(uc),
					ammHome.inserimentoSqlAmmortamentoBene(uc,amm), true, this.getClass());
			ps.executeQuery();
			try {
				ps.close();
			} catch (java.sql.SQLException ignored) {
			}
		} catch (SQLException | ComponentException e) {
			throw handleException(e);
		} finally {
			if (ps != null) try {
				ps.close();
			} catch (java.sql.SQLException ignored) {
			}
		}
	}
	public void cancellaAmmortamentiEsercizio(UserContext uc,Integer esercizio) throws ComponentException {
		LoggableStatement ps = null;
		try {
		//	lockBulk(uc, amm);

			Ammortamento_bene_invHome ammHome = (Ammortamento_bene_invHome) getHome(uc, Ammortamento_bene_invBulk.class);

			ps = new LoggableStatement(getConnection(uc),
					ammHome.deleteSqlAmmortamento(uc,esercizio), true, this.getClass());
			ps.execute();
			try {
				ps.close();
			} catch (java.sql.SQLException ignored) {
			}
		} catch ( SQLException | ComponentException e) {
			throw handleException(e);
		} finally {
			if (ps != null) try {
				ps.close();
			} catch (java.sql.SQLException ignored) {
			}
		}
	}
}