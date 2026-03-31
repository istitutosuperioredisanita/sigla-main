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

package it.cnr.contab.coepcoan00.consultazioni.bp;

import it.cnr.contab.coepcoan00.core.bulk.MastrinoContoBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.DateUtils;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CondizioneComplessaBulk;
import it.cnr.jada.util.action.SearchProvider;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.jsp.Button;
import it.cnr.jada.util.jsp.TableCustomizer;


import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;

public class ConsultazioneMastrinoContoBP<T extends ContoBulk> extends SelezionatoreListaBP implements SearchProvider, TableCustomizer {
    public static class MapFilter implements Serializable {
        protected List<ContoBulk> contiSelected = new ArrayList<>();
        protected List<MastrinoContoBulk> mastriniSelected = new ArrayList<>();
        protected Boolean filtraUnitaOrganizzativa = Boolean.FALSE;
        protected Unita_organizzativaBulk unitaOrganizzativaBulk;
        protected Timestamp fromDataMovimento;
        protected Timestamp toDataMovimento;
        protected Boolean isDetail;

        public List<ContoBulk> getContiSelected() {
            return contiSelected;
        }

        public void setContiSelected(List<ContoBulk> contiSelected) {
            this.contiSelected = contiSelected;
        }

        public List<MastrinoContoBulk> getMastriniSelected() {
            return mastriniSelected;
        }

        public void setMastriniSelected(List<MastrinoContoBulk> mastriniSelected) {
            this.mastriniSelected = mastriniSelected;
        }

        public Boolean getFiltraUnitaOrganizzativa() {
            return filtraUnitaOrganizzativa;
        }

        public void setFiltraUnitaOrganizzativa(Boolean filtraUnitaOrganizzativa) {
            this.filtraUnitaOrganizzativa = filtraUnitaOrganizzativa;
        }

        public Unita_organizzativaBulk getUnitaOrganizzativaBulk() {
            return unitaOrganizzativaBulk;
        }

        public void setUnitaOrganizzativaBulk(Unita_organizzativaBulk unitaOrganizzativaBulk) {
            this.unitaOrganizzativaBulk = unitaOrganizzativaBulk;
        }

        public Timestamp getFromDataMovimento() {
            return fromDataMovimento;
        }

        public void setFromDataMovimento(Timestamp fromDataMovimento) {
            this.fromDataMovimento = fromDataMovimento;
        }

        public Timestamp getToDataMovimento() {
            return toDataMovimento;
        }

        public void setToDataMovimento(Timestamp toDataMovimento) {
            this.toDataMovimento = toDataMovimento;
        }

        public Boolean getDetail() {
            return isDetail;
        }

        public void setDetail(Boolean detail) {
            isDetail = detail;
        }
    }

    protected String columnSet;
    protected MapFilter mapFilter = new MapFilter();

    public ConsultazioneMastrinoContoBP(MapFilter mapFilter) {
        this.mapFilter = mapFilter;
    }

    public ConsultazioneMastrinoContoBP(List<ContoBulk> contiSelected, Boolean filtraUnitaOrganizzativa, Unita_organizzativaBulk unitaOrganizzativaBulk, Timestamp fromDataMovimento, Timestamp toDataMovimento, Boolean isDetail) {
        this.getMapFilter().setContiSelected(contiSelected);
        this.getMapFilter().setFiltraUnitaOrganizzativa(filtraUnitaOrganizzativa);
        this.getMapFilter().setUnitaOrganizzativaBulk(unitaOrganizzativaBulk);
        this.getMapFilter().setFromDataMovimento(Optional.ofNullable(fromDataMovimento).orElseGet(()->{
            Date date = DateUtils.firstDateOfTheYear(1900);
            long time = date.getTime();
            return new Timestamp(time);
        }));
        this.getMapFilter().setToDataMovimento(Optional.ofNullable(toDataMovimento).orElseGet(()->{
            Date date = DateUtils.firstDateOfTheYear(3000);
            long time = date.getTime();
            return new Timestamp(time);
        }));
        this.getMapFilter().setDetail(isDetail);
        if (this.getMapFilter().getFiltraUnitaOrganizzativa())
            this.columnSet = "mastrino_conto_tot_uo";
        else
            this.columnSet = "mastrino_conto_tot_all";
    }

    @Override
    protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
        super.init(config, actioncontext);
        setBulkInfo(BulkInfo.getBulkInfo(MastrinoContoBulk.class));
        setColumns(getBulkInfo().getColumnFieldPropertyDictionary(Optional.ofNullable(columnSet).orElse("mastrino_conto")));
        setModel(actioncontext, new MastrinoContoBulk());
        setMultiSelection(!this.getMapFilter().getDetail());
    }

    public it.cnr.jada.ejb.CRUDComponentSession createComponentSession() throws jakarta.ejb.EJBException, RemoteException, BusinessProcessException {
        return (it.cnr.jada.ejb.CRUDComponentSession) createComponentSession("JADAEJB_CRUDComponentSession", it.cnr.jada.ejb.CRUDComponentSession.class);
    }

    public Button[] createToolbar() {
        Button[] toolbar = super.createToolbar();
        if (this.getMapFilter().getDetail())
            return toolbar;
        Button[] newToolbar = new Button[ toolbar.length + 1];
        int i;
        for ( i = 0; i < toolbar.length; i++ )
            newToolbar[i] = toolbar[i];
        newToolbar[ i ] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()),"Toolbar.dettagli");
        return newToolbar;
    }

    public RemoteIterator search(
            ActionContext actioncontext,
            CompoundFindClause compoundfindclause,
            OggettoBulk oggettobulk)
            throws BusinessProcessException {
        try {
            return createComponentSession().cerca(
                    actioncontext.getUserContext(),
                    compoundfindclause,
                    (OggettoBulk) getBulkInfo().getBulkClass().newInstance(),
                    "selectByClauseForMastrinoConto",
                    Arrays.asList(mapFilter).toArray()
                );
        } catch (ComponentException | RemoteException | IllegalAccessException | InstantiationException e) {
            throw handleException(e);
        }
    }

    public RemoteIterator openIterator(ActionContext actioncontext)
            throws BusinessProcessException {
        try {
            final RemoteIterator remoteIterator = search(
                    actioncontext,
                    Optional.ofNullable(getCondizioneCorrente())
                            .map(CondizioneComplessaBulk::creaFindClause)
                            .filter(CompoundFindClause.class::isInstance)
                            .map(CompoundFindClause.class::cast)
                            .orElseGet(() -> new CompoundFindClause()),
                    getModel());
            setIterator(actioncontext, remoteIterator);
            return remoteIterator;
        } catch (RemoteException e) {
            throw new BusinessProcessException(e);
        }
    }

    @Override
    public void writeHTMLTable(PageContext pagecontext, String s, String s1) throws IOException, ServletException {
        super.writeHTMLTable(pagecontext, s, s1);
    }

    @Override
    public boolean isOrderableBy(String s) {
        return Boolean.FALSE;
    }
    @Override
    public String getRowCSSClass(Object obj, boolean even) {
        return Optional.ofNullable(obj)
                .filter(MastrinoContoBulk.class::isInstance)
                .map(MastrinoContoBulk.class::cast)
                .map(mastrinoContoBulk -> {
                    switch (mastrinoContoBulk.getCd_riga()) {
                        case "T1_UO" : {
                            if (this.isMapUoDetail())
                                return "shadow font-weight-bold font-italic " +
                                        (mastrinoContoBulk.getDifferenza().equals(BigDecimal.ZERO) ? "text-primary" : "text-danger");
                            return null;
                        }
                        case "T2_ALL" : {
                            if (this.isMapAllDetail() || this.isMapUo() || this.isMapUoDetail())
                                return "shadow font-weight-bold font-italic " +
                                        (mastrinoContoBulk.getDifferenza().equals(BigDecimal.ZERO) ? "text-primary" : "text-danger");
                            return null;
                        }
                        default:
                            return null;
                    }
                }).orElse(null);

    };

    private boolean isMapAllDetail() {
        return this.mapFilter.getDetail() && !this.mapFilter.getFiltraUnitaOrganizzativa();
    }

    private boolean isMapUoDetail() {
        return this.mapFilter.getDetail() && this.mapFilter.getFiltraUnitaOrganizzativa();
    }

    private boolean isMapUo() {
        return !this.mapFilter.getDetail() && this.mapFilter.getFiltraUnitaOrganizzativa();
    }

    private boolean isMapAll() {
        return !this.mapFilter.getDetail() && !this.mapFilter.getFiltraUnitaOrganizzativa();
    }
    @Override
    public String getRowStyle(Object obj) {
        return null;
    }

    @Override
    public boolean isRowEnabled(Object obj) {
        return true;
    }

    @Override
    public boolean isRowReadonly(Object obj) {
        return false;
    }

    @Override
    public String getTableClass() {
        return null;
    }

    @Override
    public void writeTfoot(JspWriter jspwriter) throws IOException {
    }

    public MapFilter getMapFilter() {
        return mapFilter;
    }
}
