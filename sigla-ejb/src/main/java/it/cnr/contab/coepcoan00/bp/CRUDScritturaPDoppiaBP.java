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

package it.cnr.contab.coepcoan00.bp;

import it.cnr.contab.coepcoan00.core.bulk.Movimento_cogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_partita_doppiaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bulk.CNRUserInfo;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.SimpleDetailCRUDController;

import java.rmi.RemoteException;
import java.util.Optional;

/**
 * Business Process che gestisce le attività di CRUD per l'oggetto Scrittura_partitadoppiaBulk
 */

public class CRUDScritturaPDoppiaBP extends it.cnr.jada.util.action.SimpleCRUDBP {
    public static final String[] TAB_ECONOMICA = new String[]{"tabEconomica", "Economico/Patrimoniale", "/coepcoan00/tab_doc_economica.jsp"};
    private Unita_organizzativaBulk uoScrivania;

    private final SimpleDetailCRUDController movimentiDare = new SimpleDetailCRUDController("MovimentiDare", it.cnr.contab.coepcoan00.core.bulk.Movimento_cogeBulk.class, "movimentiDareColl", this){
        @Override
        public boolean isEnabled() {
            return super.isEnabled() && isModificabile();
        }

        @Override
        public boolean isShrinkable() {
            return super.isShrinkable() && isModificabile();
        }

        private boolean isModificabile() {
            return Optional.ofNullable(getModel())
                    .filter(Movimento_cogeBulk.class::isInstance)
                    .map(Movimento_cogeBulk.class::cast)
                    .map(Movimento_cogeBulk::getFl_modificabile)
                    .orElse(Boolean.TRUE);
        }

    };
    private final SimpleDetailCRUDController movimentiAvere = new SimpleDetailCRUDController("MovimentiAvere", it.cnr.contab.coepcoan00.core.bulk.Movimento_cogeBulk.class, "movimentiAvereColl", this);
    private Boolean isBloccoScrittureProposte;

    public CRUDScritturaPDoppiaBP() {
        super();
        setTab("tab", "tabScrittura");
    }

    public CRUDScritturaPDoppiaBP(String function) {
        super(function);
        setTab("tab", "tabScrittura");
    }

    /**
     * restituisce il Controller che gestisce la lista dei movimenti avere
     *
     * @return it.cnr.jada.util.action.SimpleDetailCRUDController
     */
    public final it.cnr.jada.util.action.SimpleDetailCRUDController getMovimentiAvere() {
        return movimentiAvere;
    }

    /**
     * restituisce il Controller che gestisce la lista dei movimenti dare
     *
     * @return it.cnr.jada.util.action.SimpleDetailCRUDController
     */
    public final it.cnr.jada.util.action.SimpleDetailCRUDController getMovimentiDare() {
        return movimentiDare;
    }

    public boolean isScritturaReadonly() {
        return isBloccoScrittureProposte &&
                !Scrittura_partita_doppiaBulk.ORIGINE_PRIMA_NOTA_MANUALE.equals(((Scrittura_partita_doppiaBulk) getModel()).getOrigine_scrittura());
    }

    @Override
    protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
        super.basicEdit(actioncontext, oggettobulk, flag);
        if (isScritturaReadonly())
            setStatus(VIEW);
    }

    /* Metodo per riportare il fuoco sul tab iniziale */
    protected void resetTabs(ActionContext context) {
        setTab("tab", "tabScrittura");
    }

    @Override
    protected void initialize(ActionContext actioncontext) throws BusinessProcessException {
        super.initialize(actioncontext);
        try {
            uoScrivania = CNRUserInfo.getUnita_organizzativa(actioncontext);
            isBloccoScrittureProposte = Utility.createConfigurazioneCnrComponentSession().isBloccoScrittureProposte(actioncontext.getUserContext());
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }
    public boolean isUoEnte(){
        return (uoScrivania.getCd_tipo_unita().compareTo(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE)==0);
    }
    public boolean isFromDocumentoOrigine() {
        return Optional.ofNullable(getModel())
                .filter(Scrittura_partita_doppiaBulk.class::isInstance)
                .map(Scrittura_partita_doppiaBulk.class::cast)
                .map(scritturaPartitaDoppiaBulk -> Optional.ofNullable(scritturaPartitaDoppiaBulk.getEsercizio_documento_amm()).isPresent())
                .orElse(Boolean.FALSE);
    }

    public boolean isScritturaAnnullata() {
        return Optional.ofNullable(getModel())
                .filter(Scrittura_partita_doppiaBulk.class::isInstance)
                .map(Scrittura_partita_doppiaBulk.class::cast)
                .map(scritturaPartitaDoppiaBulk -> Optional.ofNullable(scritturaPartitaDoppiaBulk.getPg_scrittura_annullata()).isPresent())
                .orElse(Boolean.FALSE);
    }
}
