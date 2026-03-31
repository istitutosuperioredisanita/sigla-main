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

package it.cnr.contab.utenze00.bp;

/**
 * Classe che definisce alcune costanti
 */
public class Costanti 
{
	public static final Integer ERRORE_FA_100 = Integer.valueOf(100);
	public static final Integer ERRORE_FA_101 = Integer.valueOf(101);
	public static final Integer ERRORE_FA_102 = Integer.valueOf(102);
	public static final Integer ERRORE_FA_103 = Integer.valueOf(103);
	public static final Integer ERRORE_FA_104 = Integer.valueOf(104);
	public static final Integer ERRORE_FA_105 = Integer.valueOf(105);
	public static final Integer ERRORE_FA_106 = Integer.valueOf(106);
	public static final Integer ERRORE_FA_107 = Integer.valueOf(107);
	public static final Integer ERRORE_FA_108 = Integer.valueOf(108);
	public static final Integer ERRORE_FA_109 = Integer.valueOf(109);
	public static final Integer ERRORE_FA_110 = Integer.valueOf(110);
	public static final Integer ERRORE_FA_111 = Integer.valueOf(111);
	public static final Integer ERRORE_FA_112 = Integer.valueOf(112);
	public static final Integer ERRORE_FA_113 = Integer.valueOf(113);
	public static final Integer ERRORE_FA_114 = Integer.valueOf(114);
	public static final Integer ERRORE_FA_115 = Integer.valueOf(115);
	public static final Integer ERRORE_FA_116 = Integer.valueOf(116);
	public static final Integer ERRORE_FA_117 = Integer.valueOf(117);
	public static final Integer ERRORE_FA_118 = Integer.valueOf(118);
	public static final Integer ERRORE_FA_119 = Integer.valueOf(119);
	public static final Integer ERRORE_FA_120 = Integer.valueOf(120);
	public static final Integer ERRORE_FA_121 = Integer.valueOf(121);
	public static final Integer ERRORE_FA_122 = Integer.valueOf(122);
	public static final Integer ERRORE_FA_123 = Integer.valueOf(123);
	public static final Integer ERRORE_FA_124 = Integer.valueOf(124);
	public static final Integer ERRORE_FA_125 = Integer.valueOf(125);
	public static final Integer ERRORE_FA_126 = Integer.valueOf(126);
	public static final Integer ERRORE_FA_127 = Integer.valueOf(127);
	public static final Integer ERRORE_FA_128 = Integer.valueOf(128);
	public static final Integer ERRORE_FA_129 = Integer.valueOf(129);
	public static final Integer ERRORE_FA_130 = Integer.valueOf(130);
	public static final Integer ERRORE_FA_131 = Integer.valueOf(131);
	public static final Integer ERRORE_FA_132 = Integer.valueOf(132);
	public static final Integer ERRORE_FA_133 = Integer.valueOf(133);
	public static final Integer ERRORE_FA_134 = Integer.valueOf(134);
	public static final Integer ERRORE_FA_135 = Integer.valueOf(135);
	public static final Integer ERRORE_FA_136 = Integer.valueOf(136);
	public static final Integer ERRORE_FA_137 = Integer.valueOf(137);
	public static final Integer ERRORE_FA_138 = Integer.valueOf(138);
	public static final Integer ERRORE_FA_139 = Integer.valueOf(139);
	public static final Integer ERRORE_FA_140 = Integer.valueOf(140);
	public static final Integer ERRORE_FA_141 = Integer.valueOf(141);
	public static final Integer ERRORE_FA_142 = Integer.valueOf(142);
	public static final Integer ERRORE_FA_143 = Integer.valueOf(143);
	public static final Integer ERRORE_FA_144 = Integer.valueOf(144);
	public static final Integer ERRORE_FA_145 = Integer.valueOf(145);
	public static final Integer ERRORE_FA_146 = Integer.valueOf(146);
	public static final Integer ERRORE_FA_147 = Integer.valueOf(147);
	public static final Integer ERRORE_FA_148 = Integer.valueOf(148);
	public static final Integer ERRORE_FA_149 = Integer.valueOf(149);
	public static final Integer ERRORE_FA_150 = Integer.valueOf(150);
	public static final Integer ERRORE_FA_151 = Integer.valueOf(151);
	public static final Integer ERRORE_FA_999 = Integer.valueOf(999);
	
	public final static java.util.Dictionary<Integer,String> erroriFA;
	static {
		erroriFA = new it.cnr.jada.util.OrderedHashtable();
		erroriFA.put(ERRORE_FA_999, "Errore generico");
		erroriFA.put(ERRORE_FA_100, "Errore generico applicativo ");
		erroriFA.put(ERRORE_FA_101, "Parametro necessario alla generazione della Fattura non inserito o non valido.");
		erroriFA.put(ERRORE_FA_102, "Fattura o Nota Credito già inserita");
		erroriFA.put(ERRORE_FA_103, "Fattura di tipo tariffario con codice tariffario non valorizzato");
		erroriFA.put(ERRORE_FA_104, "Estremi del contratto non presenti");
		erroriFA.put(ERRORE_FA_105, "Estremi del cliente non presenti");
		erroriFA.put(ERRORE_FA_106, "Estremi della voce iva non presenti o non validi");
		erroriFA.put(ERRORE_FA_107, "Estremi della voce non presenti o non coerenti");
		erroriFA.put(ERRORE_FA_108, "Estremi del contratto non completi");
		erroriFA.put(ERRORE_FA_109, "Estremi riga fattura di riferimento non presenti");
		erroriFA.put(ERRORE_FA_110, "Estremi banca cliente non inseriti");
		erroriFA.put(ERRORE_FA_111, "Estremi modalita' pagamento cliente non inseriti o non validi");
		erroriFA.put(ERRORE_FA_112, "Formato data registrazione errato; usare dd/mm/yyyy");
		erroriFA.put(ERRORE_FA_113, "Formato numerico errato prezzo unitario");
		erroriFA.put(ERRORE_FA_114, "Formato numerico errato quantita");
		erroriFA.put(ERRORE_FA_115, "Formato numerico errato codice cliente");
		erroriFA.put(ERRORE_FA_116, "Il codice cliente non corrisponde a quello della riga della fattura di riferimento");
		erroriFA.put(ERRORE_FA_117, "L'importo ancora disponibile da stornare e' minore dell'importo della nota credito");
		erroriFA.put(ERRORE_FA_118, "Estremi banca cliente non presenti");
		erroriFA.put(ERRORE_FA_119, "Formato numerico errato");
		erroriFA.put(ERRORE_FA_120, "Il codice IVA non corrisponde a quello della riga della fattura di riferimento");
		erroriFA.put(ERRORE_FA_121, "La causale emissione non corrisponde a quella della riga della fattura di riferimento");
		erroriFA.put(ERRORE_FA_122, "Il tipo sezionale non corrisponde a quello della riga della fattura di riferimento");
		erroriFA.put(ERRORE_FA_123, "Il codice tariffario non corrisponde a quello della riga della fattura di riferimento");
		erroriFA.put(ERRORE_FA_124, "Cliente selezionato NON coerente con il valore liquidazione differita");
		erroriFA.put(ERRORE_FA_125, "Cliente selezionato NON valido");
		erroriFA.put(ERRORE_FA_126, "Valore dei campi intra_ue e/o extra_ue e/o san_marino e/o liquidazione differita non corrispondenti a quelli della riga della fattura di riferimento");
		erroriFA.put(ERRORE_FA_127, "Tipo sezionale non valido");
		erroriFA.put(ERRORE_FA_128, "Tariffario non valido");
		erroriFA.put(ERRORE_FA_129, "Valore dei campi intra_ue e/o extra_ue e/o san_marino non coerenti tra loro");
		erroriFA.put(ERRORE_FA_130, "Cds non valido");
		erroriFA.put(ERRORE_FA_131, "Unita' organizzativa non valida");
		erroriFA.put(ERRORE_FA_132, "Modalita' di pagamento del terzo uo non valida");
		erroriFA.put(ERRORE_FA_133, "Estremi del terzo uo non presenti");
		erroriFA.put(ERRORE_FA_134, "Estremi della banca del terzo uo non presenti");
		erroriFA.put(ERRORE_FA_135, "Codice Uo non coerente con il codice Cds");
		erroriFA.put(ERRORE_FA_136, "Estremi CDR non presenti");
		erroriFA.put(ERRORE_FA_137, "Estremi G.a.e. non presenti");
		erroriFA.put(ERRORE_FA_138, "Tipo G.a.e. non coerente con la voce");
		erroriFA.put(ERRORE_FA_139, "Estremi bene/servizio non presenti");
		erroriFA.put(ERRORE_FA_140, "Cliente selezionato NON coerente con i campi intra_ue e/o extra_ue e/o san_marino");
		erroriFA.put(ERRORE_FA_141, "Tipologia bene/servizio non coerente con il bene/servizio");
		erroriFA.put(ERRORE_FA_142, "Tipologia bene/servizio non coerente con i campi intra_ue e/o extra_ue");
		erroriFA.put(ERRORE_FA_143, "Estremi Modalità erogazione non presenti");
		erroriFA.put(ERRORE_FA_144, "Estremi Modalità incasso non presenti");
		erroriFA.put(ERRORE_FA_145, "Estremi Codici Cpa non presenti");
		erroriFA.put(ERRORE_FA_146, "Estremi Nazione non presenti");
		erroriFA.put(ERRORE_FA_147, "Estremi Nomenclatura Combinata non presenti");
		erroriFA.put(ERRORE_FA_148, "Estremi Natura transazione non presenti");
		erroriFA.put(ERRORE_FA_149, "Estremi Condizione consegna non presenti");
		erroriFA.put(ERRORE_FA_150, "Estremi Modalita trasporto non presenti");
		erroriFA.put(ERRORE_FA_151, "Estremi Provincia non presenti");
		
	}	
	public static final Integer ERRORE_WS_100 = Integer.valueOf(100);
	public static final Integer ERRORE_WS_101 = Integer.valueOf(101);
	public static final Integer ERRORE_WS_102 = Integer.valueOf(102);
	public static final Integer ERRORE_WS_103 = Integer.valueOf(103);
	public static final Integer ERRORE_WS_104 = Integer.valueOf(104);
	public static final Integer ERRORE_WS_105 = Integer.valueOf(105);
	public static final Integer ERRORE_WS_106 = Integer.valueOf(106);
	public static final Integer ERRORE_WS_107 = Integer.valueOf(107);
	public static final Integer ERRORE_WS_108 = Integer.valueOf(108);
	public static final Integer ERRORE_WS_109 = Integer.valueOf(109);
	public static final Integer ERRORE_WS_110 = Integer.valueOf(110);
	public static final Integer ERRORE_WS_111 = Integer.valueOf(111);
	public static final Integer ERRORE_WS_112 = Integer.valueOf(112);
	public static final Integer ERRORE_WS_113 = Integer.valueOf(113);
	public static final Integer ERRORE_WS_114 = Integer.valueOf(114);
	
	public final static java.util.Dictionary<Integer,String> erroriWS;
	static {
		erroriWS = new it.cnr.jada.util.OrderedHashtable();
		erroriWS.put(ERRORE_WS_100, "Errore generico, causa sconosciuta");
		erroriWS.put(ERRORE_WS_101, "Non è stata definita la query per la ricerca");
		erroriWS.put(ERRORE_WS_102, "Non è stato definito il dominio per la ricerca");
		erroriWS.put(ERRORE_WS_103, "Codice terzo per la ricerca non presente o non valido");
		erroriWS.put(ERRORE_WS_104, "Codice della modalità di pagamento per la ricerca non presente o non valido");
		erroriWS.put(ERRORE_WS_105, "Codice UO per la ricerca non presente o non valido");
		erroriWS.put(ERRORE_WS_106, "Non è stato definito il tipo del contratto per la ricerca");
		erroriWS.put(ERRORE_WS_107, "Non è stato definito l'esercizio per la ricerca");
		erroriWS.put(ERRORE_WS_108, "Codice CDR per la ricerca non presente o non valido");
		erroriWS.put(ERRORE_WS_109, "Non è stato definito il tipo gestione della G.a.e. per la ricerca");
		erroriWS.put(ERRORE_WS_110, "Non è stato definito la tipologia del sezionale per la ricerca");
		erroriWS.put(ERRORE_WS_111, "Non è stato definito il tipo documento per la ricerca");
		erroriWS.put(ERRORE_WS_112, "Formato numerico errato codice terzo");
		erroriWS.put(ERRORE_WS_113, "Formato numerico errato esercizio");
		erroriWS.put(ERRORE_WS_114, "Tipologia bene/servizio non valida");
	}	
/**
 * Constants constructor comment.
 */
public Costanti() {
	super();
}
}
