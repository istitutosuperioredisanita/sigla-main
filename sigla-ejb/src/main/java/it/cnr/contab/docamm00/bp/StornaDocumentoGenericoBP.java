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

import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.docamm00.docs.bulk.StornaDocumentoGenericoBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;

public class StornaDocumentoGenericoBP extends BulkBP {
    private final char tiEntrataSpesa;
    private final List<Documento_generico_rigaBulk> documentoGenericoRigaBulks;
    private final Dictionary<String, String> tiCausaleContabileKeys;
    public StornaDocumentoGenericoBP(Character tiEntrataSpesa, List<Documento_generico_rigaBulk> documentoGenericoRigaBulks, Dictionary<String, String> tiCausaleContabileKeys) {
        super();
        this.tiEntrataSpesa = tiEntrataSpesa;
        this.documentoGenericoRigaBulks = documentoGenericoRigaBulks;
        this.tiCausaleContabileKeys = tiCausaleContabileKeys;
    }

    public char getTiEntrataSpesa() {
        return tiEntrataSpesa;
    }

    @Override
    protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
        super.init(config, actioncontext);
        try {
            StornaDocumentoGenericoBulk stornaDocumentoGenericoBulk = new StornaDocumentoGenericoBulk(tiCausaleContabileKeys);
            java.sql.Timestamp date = EJBCommonServices.getServerDate();
            int annoSolare = Documento_genericoBulk.getDateCalendar(date).get(java.util.Calendar.YEAR);
            int esercizioInScrivania = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(actioncontext.getUserContext());
            if (annoSolare != esercizioInScrivania) {
                date = new java.sql.Timestamp(new java.text.SimpleDateFormat("dd/MM/yyyy").parse("31/12/" + esercizioInScrivania).getTime());
            }
            stornaDocumentoGenericoBulk.setDt_registrazione(date);
            stornaDocumentoGenericoBulk.setDt_da_competenza_coge(date);
            stornaDocumentoGenericoBulk.setDt_a_competenza_coge(date);
            setModel(actioncontext, stornaDocumentoGenericoBulk);
        } catch (ParseException e) {
            throw handleException(e);
        }
    }

    @Override
    public RemoteIterator find(ActionContext actioncontext, CompoundFindClause compoundfindclause, OggettoBulk bulk, OggettoBulk context, String property) throws BusinessProcessException {
        try {
            return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(
                    actioncontext,
                    Utility.createCRUDComponentSession().cerca(
                            actioncontext.getUserContext(),
                            compoundfindclause,
                            bulk,
                            new Documento_genericoBulk(),
                            property
                    )
            );
        } catch(Exception e) {
            throw new it.cnr.jada.action.BusinessProcessException(e);
        }
    }

    public Documento_genericoBulk generaStorno(ActionContext actioncontext) throws BusinessProcessException {
        StornaDocumentoGenericoBulk stornaDocumentoGenericoBulk = Optional.ofNullable(getModel())
                .filter(StornaDocumentoGenericoBulk.class::isInstance)
                .map(StornaDocumentoGenericoBulk.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Model not found"));
        try {
            stornaDocumentoGenericoBulk.validateNullable();
            return Utility.createDocumentoGenericoComponentSession().creaDocumentoGenericoDiStorno(
                    actioncontext.getUserContext(),
                    tiEntrataSpesa,
                    stornaDocumentoGenericoBulk,
                    documentoGenericoRigaBulks
            );
        } catch (ValidationException | ComponentException | RemoteException e) {
            throw handleException(e);
        }

    }
}
