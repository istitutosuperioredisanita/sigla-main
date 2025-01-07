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

package it.cnr.contab.docamm00.bp;

import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.OrderConstants;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CondizioneComplessaBulk;
import it.cnr.jada.util.action.SearchProvider;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.jsp.Button;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class SelezionatoreStornaDocumentoGenericoBP extends SelezionatoreListaBP implements SearchProvider {
	private static final long serialVersionUID = 1L;
	private char tiEntrataSpesa;

	public SelezionatoreStornaDocumentoGenericoBP() {
	}

	public SelezionatoreStornaDocumentoGenericoBP(String s) {
		super(s);
	}

	public char getTiEntrataSpesa() {
		return tiEntrataSpesa;
	}

	@Override
	protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
		try {
			tiEntrataSpesa = config.getInitParameter("tiEntrataSpesa").charAt(0);
			setModel(actioncontext, new Documento_generico_rigaBulk());
			setBulkInfo(BulkInfo.getBulkInfo(Documento_generico_rigaBulk.class));
			setColumns(getBulkInfo().getColumnFieldPropertyDictionary("storno"));
			setMultiSelection(Boolean.TRUE);
            Utility.createCRUDComponentSession().initializeKeysAndOptionsInto(actioncontext.getUserContext(), getModel());
			super.init(config, actioncontext);
			this.openIterator(actioncontext);
			Stream.of("documento_generico.cd_cds_origine","documento_generico.cd_uo_origine","esercizio",
					"cd_tipo_documento_amm", "pg_documento_generico", "progressivo_riga").forEach(s -> {
				setOrderBy(actioncontext, s, OrderConstants.ORDER_ASC);
			});
			reset(actioncontext);
		} catch (ComponentException|RemoteException e) {
			throw handleException(e);
		}
	}

	@Override
	public RemoteIterator search(ActionContext actioncontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk) throws BusinessProcessException {
		try {
			return Utility.createCRUDComponentSession().cerca(
					actioncontext.getUserContext(),
					Optional.ofNullable(compoundfindclause).orElse(new CompoundFindClause()),
					oggettobulk,
					"selectDocumentForReverse",
					tiEntrataSpesa
			);
		} catch (Exception exception) {
			throw new BusinessProcessException(exception);
		}
	}

	public void openIterator(ActionContext actioncontext)
			throws BusinessProcessException {
		try {
			setIterator(actioncontext, search(
					actioncontext,
					Optional.ofNullable(getCondizioneCorrente())
							.map(CondizioneComplessaBulk::creaFindClause)
							.filter(CompoundFindClause.class::isInstance)
							.map(CompoundFindClause.class::cast)
							.orElseGet(CompoundFindClause::new),
					getModel())
			);
		} catch (RemoteException e) {
			throw new BusinessProcessException(e);
		}
	}

	@Override
	protected String getFreeSearchSet() {
		return "storno";
	}

	@Override
	public Button[] createToolbar() {
		final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
		return Stream.concat(Arrays.stream(super.createToolbar()),
                Stream.of(
                        new Button(properties, "Toolbar.storno")
                )).toArray(Button[]::new);
	}
}
