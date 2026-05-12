/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.coepcoan00.bp;

import it.cnr.contab.coepcoan00.filter.bulk.FiltroRicercaMastrinoContoBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.jada.util.jsp.Button;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

public class MastrinoContoBP extends BulkBP {
    private boolean uoScrivaniaEnte = false;

    @Override
    protected Button[] createToolbar() {
        final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
        return Arrays.asList(
                        new Button(properties, "CRUDToolbar.startSearch")
                ).stream().toArray(Button[]::new);
    }

    @Override
    protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
        super.init(config, actioncontext);
        Unita_organizzativaBulk uo = it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(actioncontext);
        setUoScrivaniaEnte(uo.getCd_tipo_unita().equals(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE));
        final FiltroRicercaMastrinoContoBulk filtroRicercaBulk = new FiltroRicercaMastrinoContoBulk();
        final ContoBulk contoBulk = new ContoBulk();
        filtroRicercaBulk.setConto(contoBulk);
        if (!isUoScrivaniaEnte()) {
            filtroRicercaBulk.setFiltraUnitaOrganizzativa(Boolean.TRUE);
            filtroRicercaBulk.setUnitaOrganizzativa(uo);
        }
        setModel(actioncontext, filtroRicercaBulk);
    }

    @Override
    public boolean isDirty() {
        return Boolean.FALSE;
    }

    public boolean isStartSearchButtonEnabled() {
        return Boolean.TRUE;
    }
    @Override
    public RemoteIterator find(ActionContext actioncontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String s) throws BusinessProcessException {
        try {
            it.cnr.jada.ejb.CRUDComponentSession cs = Utility.createCRUDComponentSession();
            if (cs == null) return null;
            Optional.of(oggettobulk).filter(ContoBulk.class::isInstance).map(ContoBulk.class::cast).ifPresent(el->{
                el.setEsercizio(CNRUserContext.getEsercizio(actioncontext.getUserContext()));
            });
            return EJBCommonServices.openRemoteIterator(
                    actioncontext,
                    cs.cerca(actioncontext.getUserContext(), compoundfindclause, oggettobulk));
        } catch (it.cnr.jada.comp.ComponentException e) {
            throw handleException(e);
        } catch (java.rmi.RemoteException e) {
            throw handleException(e);
        }
    }

    public boolean isUoScrivaniaEnte() {
        return uoScrivaniaEnte;
    }

    private void setUoScrivaniaEnte(boolean uoScrivaniaEnte) {
        this.uoScrivaniaEnte = uoScrivaniaEnte;
    }
}
