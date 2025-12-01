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

package it.cnr.contab.config00.util;

/**
 * Classe che definisce alcune costanti
 */
public class Constants 
{
	public static final Integer LIVELLO_CDS = Integer.valueOf( 1 );
	public static final Integer LIVELLO_UO =  Integer.valueOf( 2 );

	
	public static final String  TIPO_CDS = new String( "C" );
	public static final String  TIPO_UO  = new String( "U" );
	public static final String  TIPO_CDR = new String( "R" );	

	public static final Integer ERRORE_SIP_100 = Integer.valueOf(100);
	public static final Integer ERRORE_SIP_101 = Integer.valueOf(101);
	public static final Integer ERRORE_SIP_102 = Integer.valueOf(102);
	public static final Integer ERRORE_SIP_103 = Integer.valueOf(103);
	public static final Integer ERRORE_SIP_104 = Integer.valueOf(104);
	public static final Integer ERRORE_SIP_105 = Integer.valueOf(105);
	public static final Integer ERRORE_SIP_106 = Integer.valueOf(106);
	public static final Integer ERRORE_SIP_107 = Integer.valueOf(107);
	public static final Integer ERRORE_SIP_108 = Integer.valueOf(108);
	public static final Integer ERRORE_SIP_109 = Integer.valueOf(109);
	public static final Integer ERRORE_SIP_110 = Integer.valueOf(110);
	public static final Integer ERRORE_SIP_111 = Integer.valueOf(111);
	public static final Integer ERRORE_SIP_112 = Integer.valueOf(112);
	public static final Integer ERRORE_SIP_113 = Integer.valueOf(113);
	public static final Integer ERRORE_SIP_114 = Integer.valueOf(114);
	public static final Integer ERRORE_SIP_115 = Integer.valueOf(115);
	public static final Integer ERRORE_SIP_116 = Integer.valueOf(116);
	public static final Integer ERRORE_SIP_117 = Integer.valueOf(117);
	public static final Integer ERRORE_SIP_118 = Integer.valueOf(118);
	public static final Integer ERRORE_SIP_119 = Integer.valueOf(119);
	public static final Integer ERRORE_SIP_120 = Integer.valueOf(120);
	public final static java.util.Dictionary<Integer,String> erroriSIP;
	static {
		erroriSIP = new it.cnr.jada.util.OrderedHashtable();
		erroriSIP.put(ERRORE_SIP_100, "Errore generico, causa sconosciuta");
		erroriSIP.put(ERRORE_SIP_101, "Non è stata definita la query per la ricerca");
		erroriSIP.put(ERRORE_SIP_102, "Non è stato definito il dominio per la ricerca");
		erroriSIP.put(ERRORE_SIP_103, "Non è stato definito il tipo di servizio");
		erroriSIP.put(ERRORE_SIP_104, "Non è stato definito il tipo di soggetto terzo: persona fisica o persona giuridica");
		erroriSIP.put(ERRORE_SIP_105, "I dati per l'inserimento di un nuovo soggetto terzo non consentono la generazione di un nuovo codice terzo");
		erroriSIP.put(ERRORE_SIP_106, "Il soggetto terzo che si sta cercando di aggiungere risulta già presente all'interno del database, poichè Codice Fiscale risulta già inserito");
		erroriSIP.put(ERRORE_SIP_107, "Dati incompleti per l'inserimento di un nuovo soggetto terzo");
		erroriSIP.put(ERRORE_SIP_108, "Il soggetto terzo che si sta cercando di eliminare non risulta essere attualmente presente nel database");
		erroriSIP.put(ERRORE_SIP_109, "Non è consentita l'eliminazione del soggetto terzo specificato");
		erroriSIP.put(ERRORE_SIP_110, "Errore di autenticazione");
		erroriSIP.put(ERRORE_SIP_111, "Errore interno del sistema, il sistema potrebbe avere un problema tecnico in corso");
		erroriSIP.put(ERRORE_SIP_112, "Servizio non disponibile (il S.I.C. ha reso momentaneamente inattivo il servizio)");
		erroriSIP.put(ERRORE_SIP_113, "Non è stata definita l'Unita Organizzativa per la ricerca");
		erroriSIP.put(ERRORE_SIP_114, "Non è stato definito l'Esercizio per la ricerca");
		erroriSIP.put(ERRORE_SIP_115, "Non è stato definito il periodo della rendicontazione");
		erroriSIP.put(ERRORE_SIP_116, "Formato date errato per il periodo della rendicontazione");
		erroriSIP.put(ERRORE_SIP_117, "Non è stato definito il CDR per la ricerca");
		erroriSIP.put(ERRORE_SIP_118, "Non è stato definito il tipo: 'E' Entrata o 'S' Spesa");
		erroriSIP.put(ERRORE_SIP_119, "Formato Matricola errato");
		erroriSIP.put(ERRORE_SIP_120, "Non è stato definito un parametro obbligatorio");
	}	
	
	public static final String  RICHIESTE_IN_CORSO = "C";
	public static final String  RICHIESTE_SCADUTE = "S";

	public static final Integer ERRORE_INC_100 = Integer.valueOf(100);
	public static final Integer ERRORE_INC_101 = Integer.valueOf(101);
	public static final Integer ERRORE_INC_102 = Integer.valueOf(102);
	public static final Integer ERRORE_INC_103 = Integer.valueOf(103);
	public static final Integer ERRORE_INC_104 = Integer.valueOf(104);
	public static final Integer ERRORE_INC_105 = Integer.valueOf(105);
	public static final Integer ERRORE_INC_106 = Integer.valueOf(106);
	
	public final static java.util.Dictionary<Integer,String> erroriINC;
	static {
		erroriINC = new it.cnr.jada.util.OrderedHashtable();
		erroriINC.put(ERRORE_INC_100, "Errore generico, causa sconosciuta");
		erroriINC.put(ERRORE_INC_101, "Non è stata definita la query per la ricerca");
		erroriINC.put(ERRORE_INC_102, "Non è stato definito il dominio per la ricerca");
		erroriINC.put(ERRORE_INC_103, "Non è stato definito il tipo di servizio");
		erroriINC.put(ERRORE_INC_104, "Il parametro per la ricerca non è consentito");
		erroriINC.put(ERRORE_INC_105, "Errore di autenticazione");
		erroriINC.put(ERRORE_INC_106, "Errore interno del sistema, il sistema potrebbe avere un problema tecnico in corso");
	}	
	public static final Integer ERRORE_CON_200 = Integer.valueOf(200);
	public static final Integer ERRORE_CON_201 = Integer.valueOf(201);
	public static final Integer ERRORE_CON_202 = Integer.valueOf(202);
	public static final Integer ERRORE_CON_203 = Integer.valueOf(203);
	public static final Integer ERRORE_CON_204 = Integer.valueOf(204);
	public static final Integer ERRORE_CON_205 = Integer.valueOf(205);
	
	
	public final static java.util.Dictionary<Integer,String> erroriCON;
	static {
		erroriCON = new it.cnr.jada.util.OrderedHashtable();
		erroriCON.put(ERRORE_CON_200, "Errore generico, causa sconosciuta");
		erroriCON.put(ERRORE_CON_201, "Non è stata definito un parametro obbligatorio");
		erroriCON.put(ERRORE_CON_202, "Uno o più paramentri sono malformati");
		erroriCON.put(ERRORE_CON_203, "Errore di autenticazione");
		erroriCON.put(ERRORE_CON_204, "Errore interno del sistema, il sistema potrebbe avere un problema tecnico");
		erroriCON.put(ERRORE_CON_205, "Servizio attualmente non disponibile");
		
	}
/**
 * Constants constructor comment.
 */
public Constants() {
	super();
}
}
