/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 03/10/2017
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import it.cnr.contab.config00.bulk.Parametri_cnrBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceHome;
import it.cnr.contab.config00.pdcfin.bulk.Voce_fBulk;
import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.inventario00.docs.bulk.Transito_beni_ordiniBulk;
import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.contab.ordmag.anag00.TipoMovimentoMagBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqConsegnaBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.OutdatedResourceException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.Orderable;

public class MovimentiMagHome extends BulkHome {
	public MovimentiMagHome(Connection conn) {
		super(MovimentiMagBulk.class, conn);
	}
	public MovimentiMagHome(Connection conn, PersistentCache persistentCache) {
		super(MovimentiMagBulk.class, conn, persistentCache);
	}
	public Long recuperoProgressivoMovimento(UserContext userContext) throws PersistencyException,it.cnr.jada.comp.ComponentException {
		return new Long(this.fetchNextSequenceValue(userContext,"CNRSEQ00_MOVIMENTI_MAG").longValue());
	}
	public void initializePrimaryKeyForInsert(UserContext userContext, OggettoBulk bulk) throws PersistencyException,it.cnr.jada.comp.ComponentException {
		MovimentiMagBulk movimento = (MovimentiMagBulk)bulk;
		if (movimento.getPgMovimento() == null)
			movimento.setPgMovimento(recuperoProgressivoMovimento(userContext));
	}
	@Override
	public void insert(Persistent persistent, UserContext userContext) throws PersistencyException {
		MovimentiMagBulk movimentiMag = (MovimentiMagBulk)persistent;
    	LottoMagHome lottoHome = (LottoMagHome)getHomeCache().getHome(LottoMagBulk.class);
    	LottoMagBulk lotto;
		try {
			lotto = (LottoMagBulk)lottoHome.findAndLock(movimentiMag.getLottoMag());
		} catch (OutdatedResourceException | BusyResourceException e) {
			throw new PersistencyException("Operazione effettuata al momento da un'altro utente, riprovare successivamente.");
		}
		lotto = lottoHome.aggiornaValori(userContext, lotto, movimentiMag);
		lotto.setToBeUpdated();
		lottoHome.update(lotto, userContext);
		super.insert(persistent, userContext);
	}
	public java.util.List recuperoMovimentiDaLotto(UserContext userContext,Persistent persistent) throws IntrospectionException,PersistencyException {
		MovimentiMagBulk movimentoMag = (MovimentiMagBulk)persistent;
		setFetchPolicy("it.cnr.contab.ordmag.magazzino.comp.MovimentiMagComponent.recuperoMovimentiDaLotto");
		SQLBuilder sql = createSQLBuilder();

		sql.addClause("AND","cdCdsLotto",SQLBuilder.EQUALS, movimentoMag.getCdCdsLotto());
		sql.addClause("AND","cdMagazzinoLotto",SQLBuilder.EQUALS, movimentoMag.getCdMagazzinoLotto());
		sql.addClause("AND","esercizioLotto",SQLBuilder.EQUALS, movimentoMag.getEsercizioLotto());
		sql.addClause("AND","cdNumeratoreLotto",SQLBuilder.EQUALS, movimentoMag.getCdNumeratoreLotto());
		sql.addClause("AND","stato",SQLBuilder.EQUALS, MovimentiMagBulk.STATO_INSERITO);
		sql.addClause("AND","pgLotto",SQLBuilder.EQUALS, movimentoMag.getPgLotto());

		final List l = fetchAll(sql);
		getHomeCache().fetchAll(userContext);
		return l;

	}
	public java.util.List findRigheBollaDiScarico( MovimentiMagBulk movimento ) throws IntrospectionException,PersistencyException 
	{
		PersistentHome evHome = getHomeCache().getHome(BollaScaricoRigaMagBulk.class);
		SQLBuilder sql = evHome.createSQLBuilder();
		sql.addClause("AND","pgMovimento",sql.EQUALS, movimento.getPgMovimento());
		sql.addClause("AND","stato",sql.EQUALS, MovimentiMagBulk.STATO_INSERITO);
		return evHome.fetchAll(sql);	
	}
	public java.util.List findBeniInTransito( MovimentiMagBulk movimento ) throws IntrospectionException,PersistencyException
	{
		PersistentHome evHome = getHomeCache().getHome(Transito_beni_ordiniBulk.class);
		SQLBuilder sql = evHome.createSQLBuilder();
		sql.addClause("AND","id_movimenti_mag",sql.EQUALS, movimento.getPgMovimento());

		return evHome.fetchAll(sql);
	}
	public java.util.List getMovimentiDiChiusura(UserContext uc,Date dataChiusura) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();

		sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.CD_TIPO_MOVIMENTO",SQLBuilder.EQUALS, TipoMovimentoMagBulk.CHIUSURE);
		sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.STATO",SQLBuilder.EQUALS, MovimentiMagBulk.STATO_INSERITO);
		sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.DT_RIFERIMENTO",SQLBuilder.EQUALS, new Timestamp(dataChiusura.getTime()));

		final List   movimenti=fetchAll(sql);
		getHomeCache().fetchAll(uc);

		return movimenti;
	}

	public List<MovimentiMagBulk> getMovimentiCompresiTra(UserContext uc,Date dataInizio,Date dataFine,Integer esercizio,String codRaggrMag,String catGruppo) throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();

		sql.generateJoin(MovimentiMagBulk.class, LottoMagBulk.class, "lottoMag", "LOTTO_MAG");
		sql.generateJoin(MovimentiMagBulk.class, TipoMovimentoMagBulk.class, "tipoMovimentoMag","TIPO_MOVIMENTO_MAG");
		sql.addTableToHeader("BENE_SERVIZIO","BENE_SERVIZIO");
		sql.addSQLJoin("BENE_SERVIZIO.CD_BENE_SERVIZIO","LOTTO_MAG.CD_BENE_SERVIZIO");

		// tipo movimento != CHIUSURE (CH)
		sql.addSQLClause(FindClause.AND,"TIPO_MOVIMENTO_MAG.TIPO",SQLBuilder.NOT_EQUALS, TipoMovimentoMagBulk.CHIUSURE);
		// stato movimento = STATO_INSERITO (INS)
		sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.STATO",SQLBuilder.EQUALS, MovimentiMagBulk.STATO_INSERITO);

		// data riferimento maggiore/uguale della data in input
		sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.DT_RIFERIMENTO",SQLBuilder.LESS_EQUALS, new Timestamp(dataFine.getTime()));
		sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.DT_RIFERIMENTO",SQLBuilder.GREATER_EQUALS, new Timestamp(dataInizio.getTime()));

		sql.addTableToHeader("MAGAZZINO","m");
		sql.addSQLJoin("m.cd_cds","LOTTO_MAG.cd_cds_mag");
		sql.addSQLJoin("m.cd_magazzino","LOTTO_MAG.cd_magazzino_mag");

		if(codRaggrMag != null && !codRaggrMag.equals(Valori_magazzinoBulk.TUTTI)) {
			sql.addSQLClause(FindClause.AND, "m.CD_RAGGR_MAGAZZINO_RIM", SQLBuilder.EQUALS, codRaggrMag);
			sql.addSQLJoin("m.CD_CDS_RAGGR_RIM", "LOTTO_MAG.cd_cds_mag");
		}

		// data carico lotto minore/uguale della data in input
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.DT_CARICO",SQLBuilder.LESS_EQUALS, new Timestamp(dataFine.getTime()));
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.DT_CARICO",SQLBuilder.GREATER_EQUALS, new Timestamp(dataInizio.getTime()));
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.ESERCIZIO",SQLBuilder.EQUALS, esercizio);

		if(catGruppo != null && !catGruppo.equals(Valori_magazzinoBulk.TUTTI)){
			sql.addTableToHeader("CATEGORIA_GRUPPO_INVENT","CATEGORIA_GRUPPO_INVENT");
			sql.addSQLJoin("CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO","BENE_SERVIZIO.CD_CATEGORIA_GRUPPO");
			//categoria gruppo uguale a quella in input
			sql.addSQLClause(FindClause.AND,"CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO",SQLBuilder.EQUALS, catGruppo);
		}

		List<MovimentiMagBulk> movimenti=fetchAll(sql);
		getHomeCache().fetchAll(uc);

		return movimenti;
	}

	public List<MovimentiMagBulk> getMovimentiSuccessiviAData(UserContext uc,Date dataFine,Integer esercizio,String codRaggrMag,String codMag,String catGruppo) throws PersistencyException {
		SQLBuilder sql = createSQLBuilder();

		sql.generateJoin(MovimentiMagBulk.class, LottoMagBulk.class, "lottoMag", "LOTTO_MAG");
		sql.generateJoin(MovimentiMagBulk.class, TipoMovimentoMagBulk.class, "tipoMovimentoMag","TIPO_MOVIMENTO_MAG");
		sql.addTableToHeader("BENE_SERVIZIO","BENE_SERVIZIO");
		sql.addSQLJoin("BENE_SERVIZIO.CD_BENE_SERVIZIO","LOTTO_MAG.CD_BENE_SERVIZIO");

		// tipo movimento != CHIUSURE (CH)
		sql.addSQLClause(FindClause.AND,"TIPO_MOVIMENTO_MAG.TIPO",SQLBuilder.NOT_EQUALS, TipoMovimentoMagBulk.CHIUSURE);
		// stato movimento = STATO_INSERITO (INS)
		sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.STATO",SQLBuilder.EQUALS, MovimentiMagBulk.STATO_INSERITO);

		// data riferimento maggiore/uguale della data in input
		sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.DT_RIFERIMENTO",SQLBuilder.GREATER, new Timestamp(dataFine.getTime()));

		sql.addTableToHeader("MAGAZZINO","m");
		sql.addSQLJoin("m.cd_cds","LOTTO_MAG.cd_cds_mag");
		sql.addSQLJoin("m.cd_magazzino","LOTTO_MAG.cd_magazzino_mag");

		if(codRaggrMag != null && !codRaggrMag.equals(Valori_magazzinoBulk.TUTTI)) {
			sql.addSQLClause(FindClause.AND, "m.CD_RAGGR_MAGAZZINO_RIM", SQLBuilder.EQUALS, codRaggrMag);
			sql.addSQLJoin("m.CD_CDS_RAGGR_RIM", "LOTTO_MAG.cd_cds_mag");
		}
		if(codMag != null && !codMag.equals(Valori_magazzinoBulk.TUTTI)) {
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.CD_MAGAZZINO_MAG",SQLBuilder.EQUALS, codMag);
		}

		// data carico lotto minore/uguale della data in input
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.DT_CARICO",SQLBuilder.LESS_EQUALS, new Timestamp(dataFine.getTime()));
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.ESERCIZIO",SQLBuilder.EQUALS, esercizio);

		if(catGruppo != null && !catGruppo.equals(Valori_magazzinoBulk.TUTTI)){
			sql.addTableToHeader("CATEGORIA_GRUPPO_INVENT","CATEGORIA_GRUPPO_INVENT");
			sql.addSQLJoin("CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO","BENE_SERVIZIO.CD_CATEGORIA_GRUPPO");
			//categoria gruppo uguale a quella in input
			sql.addSQLClause(FindClause.AND,"CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO",SQLBuilder.EQUALS, catGruppo);
		}

		List<MovimentiMagBulk> movimenti=fetchAll(sql);
		getHomeCache().fetchAll(uc);

		return movimenti;
	}
}