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

package it.cnr.contab.config00.action;

import it.cnr.contab.config00.bp.CRUDConfigCtrDispoUOBP;
import it.cnr.contab.config00.bp.CRUDWorkpackageBP;
import it.cnr.contab.config00.consultazioni.bulk.VControlloDispUnitaOrgBulk;
import it.cnr.contab.config00.ejb.Linea_attivitaComponentSession;
import it.cnr.contab.config00.latt.bulk.Insieme_laBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.docamm00.bp.CRUDDocumentoGenericoPassivoBP;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.contab.prevent01.bulk.Pdg_programmaBulk;
import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;
import it.cnr.contab.progettiric00.core.bulk.Progetto_other_fieldBulk;
import it.cnr.jada.action.*;
import it.cnr.jada.bulk.BulkInfo;
import it.cnr.jada.bulk.FieldProperty;
import it.cnr.jada.util.ObjectReplacer;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CRUDAction;
import it.cnr.jada.util.action.SelezionatoreListaBP;

/**
 * <!-- @TODO: da completare -->
 */

public class CRUDConfigCtrDispoUOAction extends CRUDAction {
    public CRUDConfigCtrDispoUOAction() {
        super();
    }


}
