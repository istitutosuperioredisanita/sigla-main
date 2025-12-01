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

package it.cnr.contab.pdg00.bp;

import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofiBulk;
import it.cnr.contab.pdg00.ejb.CostiDipendenteComponentSession;
import it.cnr.contab.pdg00.ejb.FlussoStipendiComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.jsp.Button;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

public class ContabilizzazioneFlussoStipendialeMensileBP extends it.cnr.jada.util.action.SelezionatoreListaBP {
    /**
     * CostiStipendialiMensiliBP constructor comment.
     */
    public ContabilizzazioneFlussoStipendialeMensileBP() {
        super();
    }

    /**
     * CostiStipendialiMensiliBP constructor comment.
     *
     * @param function java.lang.String
     */
    public ContabilizzazioneFlussoStipendialeMensileBP(String function) {
        super(function);
        table.setMultiSelection(true);
    }

    /**
     * Crea il riferimento alla componente CNRPDG00_EJB_CostiDipendenteComponentSession
     *
     * @return Remote interface della componente
     * @throws EJBException    Se si verifica qualche eccezione applicativa per cui non è possibile effettuare l'operazione
     * @throws RemoteException Se si verifica qualche eccezione di sistema per cui non è possibile effettuare l'operazione
     */
    public CostiDipendenteComponentSession createComponentSession() throws jakarta.ejb.EJBException, java.rmi.RemoteException {
        return it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPDG00_EJB_CostiDipendenteComponentSession", CostiDipendenteComponentSession.class);
    }

    public FlussoStipendiComponentSession createFlussoStipendiComponentSession() throws jakarta.ejb.EJBException, java.rmi.RemoteException {
        return it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPDG00_EJB_FlussoStipendiComponentSession", FlussoStipendiComponentSession.class);
    }

    public it.cnr.jada.util.jsp.Button[] createToolbar() {
		final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
		return Arrays.asList(
						new Button(properties, "Toolbar.new"),
						new Button(properties, "Toolbar.contabilizza"),
						new Button(properties, "Toolbar.apriCompenso"),
						new Button(properties, "Toolbar.apriDocumentoGenerico"),
						new Button(properties, "Toolbar.apriMandato"),
                       new Button(properties, "Toolbar.delete")
				).stream().toArray(Button[]::new);
    }

    public String getFormTitle() {
        return "Contabilizzazione flussi stipendiali mensili";
    }

    protected void init(Config config, ActionContext context) throws BusinessProcessException {
        super.init(config, context);
        try {
            setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(Stipendi_cofiBulk.class));
            refresh(context);
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    public void refresh(ActionContext context) throws BusinessProcessException {
        try {
            setIterator(context, createComponentSession().listaStipendi_cofi(context.getUserContext()));
            setOrderBy(context, "mese", 1);
            reset(context);
        } catch (Throwable e) {
            throw handleException(e);
        }
    }

    public void creaNuovaRiga(ActionContext context, Integer mese, String tipoFlusso)  throws BusinessProcessException {
        Stipendi_cofiBulk stipendiCofiBulk = new Stipendi_cofiBulk();
        stipendiCofiBulk.setEsercizio(CNRUserContext.getEsercizio(context.getUserContext()));
        stipendiCofiBulk.setProg_flusso(mese);
        stipendiCofiBulk.setMese_reale(mese);
        stipendiCofiBulk.setTipo_flusso(tipoFlusso);
        stipendiCofiBulk.setStato(Stipendi_cofiBulk.STATO_NON_LIQUIDATO);
        stipendiCofiBulk.setToBeCreated();
        try {
            Utility.createCRUDComponentSession().creaConBulk(context.getUserContext(), stipendiCofiBulk);
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }

    public boolean isEliminaButtonHidden() {
       Stipendi_cofiBulk stipendiCofiBulk= (Stipendi_cofiBulk)this.getModel();
       if (Optional.ofNullable(stipendiCofiBulk).isPresent())
            return stipendiCofiBulk.isLiquidato();
       return Boolean.TRUE;

    }
    public boolean isEliminaButtonEnabled() {
        Stipendi_cofiBulk stipendiCofiBulk= (Stipendi_cofiBulk)this.getModel();
        if (Optional.ofNullable(stipendiCofiBulk).isPresent())
            return !stipendiCofiBulk.isLiquidato();
        return Boolean.FALSE;
    }
}
