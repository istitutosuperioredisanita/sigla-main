package it.cnr.contab.doccont00.ejb;

import it.cnr.contab.config00.pdcfin.bulk.V_voce_f_partita_giroBulk;
import it.cnr.contab.doccont00.tabrif.bulk.CambiaVocePGiroBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;

import jakarta.ejb.Remote;

@Remote
public interface CambiaVocePGiroComponentSession extends CRUDComponentSession {
    it.cnr.jada.util.RemoteIterator cercaReversali(UserContext param0, CambiaVocePGiroBulk param1, V_voce_f_partita_giroBulk param2, CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;
    it.cnr.jada.util.RemoteIterator cercaMandati(UserContext param0, CambiaVocePGiroBulk param1, V_voce_f_partita_giroBulk param2, CompoundFindClause param3) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

    void elabora(UserContext param0, CambiaVocePGiroBulk param1) throws it.cnr.jada.comp.ComponentException,java.rmi.RemoteException;

}
