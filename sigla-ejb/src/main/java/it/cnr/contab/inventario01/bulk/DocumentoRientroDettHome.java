/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.persistency.PersistentCache;

import java.sql.Connection;

/**
 * Home per Doc_trasporto_rientro_dettBulk.
 *
 * METODI CHIAVE PER FILTRO UO 
 * - findBeniTrasportabiliPerUO: filtra beni per UO (utente o dipendente)
 * - beneBelongsToUO: verifica appartenenza bene a UO
 * - contaBeniTrasportabiliPerUO: conta beni disponibili per UO
 */
public class DocumentoRientroDettHome extends Doc_trasporto_rientro_dettHome {


	public DocumentoRientroDettHome(Connection conn) {
		super(DocumentoTrasportoDettBulk.class, conn);
	}

	public DocumentoRientroDettHome(Connection conn, PersistentCache persistentCache) {
		super(DocumentoTrasportoDettBulk.class, conn, persistentCache);
	}



}