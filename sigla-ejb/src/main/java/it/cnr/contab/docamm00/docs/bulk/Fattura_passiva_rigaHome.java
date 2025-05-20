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

package it.cnr.contab.docamm00.docs.bulk;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.bulk.CigBulk;
import it.cnr.contab.config00.pdcep.bulk.Ass_ev_voceepBulk;
import it.cnr.contab.config00.pdcep.bulk.Ass_ev_voceepHome;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.docamm00.tabrif.bulk.AssCatgrpInventVoceEpBulk;
import it.cnr.contab.docamm00.tabrif.bulk.AssCatgrpInventVoceEpHome;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Fattura_passiva_rigaHome extends BulkHome {
    public Fattura_passiva_rigaHome(Class classe, java.sql.Connection conn) {
        super(classe, conn);
    }

    public Fattura_passiva_rigaHome(Class classe, java.sql.Connection conn, PersistentCache persistentCache) {
        super(classe, conn, persistentCache);
    }

    public Fattura_passiva_rigaHome(java.sql.Connection conn) {
        super(Fattura_passiva_rigaBulk.class, conn);
    }

    public Fattura_passiva_rigaHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Fattura_passiva_rigaBulk.class, conn, persistentCache);
    }

    public java.util.List findAddebitiForObbligazioneExceptFor(
            it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk scadenza,
            Fattura_passivaBulk fattura)
            throws PersistencyException {

        return fetchAll(selectForObbligazioneExceptFor(scadenza, fattura));
    }

    public java.util.List findStorniForObbligazioneExceptFor(
            it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk scadenza,
            Fattura_passivaBulk fattura)
            throws PersistencyException {

        return fetchAll(selectForObbligazioneExceptFor(scadenza, fattura));
    }

    public List<String> findCodiciCIG(Fattura_passivaBulk fattura, MandatoBulk mandato, Mandato_siopeBulk mandatoSiopeBulk) throws PersistencyException {
        return findCIG(fattura, mandato, mandatoSiopeBulk)
                .stream()
                .filter(fattura_passiva_rigaBulk -> Optional.ofNullable(fattura_passiva_rigaBulk.getCig()).isPresent())
                .map(Fattura_passiva_rigaBulk::getCig)
                .map(CigBulk::getCdCig)
                .filter(s -> Optional.ofNullable(s).isPresent())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> findMotiviEsclusioneCIG(Fattura_passivaBulk fattura, MandatoBulk mandato, Mandato_siopeBulk mandatoSiopeBulk) throws PersistencyException {
        return findCIG(fattura, mandato, mandatoSiopeBulk)
                .stream()
                .map(Fattura_passiva_rigaBulk::getMotivo_assenza_cig)
                .filter(s -> Optional.ofNullable(s).isPresent())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Fattura_passiva_rigaBulk> findCIG(Fattura_passivaBulk fattura, MandatoBulk mandato, Mandato_siopeBulk mandato_siopeBulk) throws PersistencyException {
        SQLBuilder sql = createSQLBuilder();
        sql.addTableToHeader("MANDATO");
        sql.addTableToHeader("MANDATO_RIGA");
        sql.addTableToHeader("MANDATO_SIOPE");

        sql.addSQLJoin("MANDATO.CD_CDS", "MANDATO_RIGA.CD_CDS");
        sql.addSQLJoin("MANDATO.ESERCIZIO", "MANDATO_RIGA.ESERCIZIO");
        sql.addSQLJoin("MANDATO.PG_MANDATO", "MANDATO_RIGA.PG_MANDATO");

        sql.addSQLJoin("FATTURA_PASSIVA_RIGA.ESERCIZIO", "MANDATO_RIGA.ESERCIZIO_DOC_AMM");
        sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_CDS", "MANDATO_RIGA.CD_CDS_DOC_AMM");
        sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_UNITA_ORGANIZZATIVA", "MANDATO_RIGA.CD_UO_DOC_AMM");
        sql.addSQLJoin("FATTURA_PASSIVA_RIGA.PG_FATTURA_PASSIVA", "MANDATO_RIGA.PG_DOC_AMM");

        sql.addSQLJoin("MANDATO_SIOPE.CD_CDS", "MANDATO_RIGA.CD_CDS");
        sql.addSQLJoin("MANDATO_SIOPE.ESERCIZIO", "MANDATO_RIGA.ESERCIZIO");
        sql.addSQLJoin("MANDATO_SIOPE.PG_MANDATO", "MANDATO_RIGA.PG_MANDATO");
        sql.addSQLJoin("MANDATO_SIOPE.ESERCIZIO_OBBLIGAZIONE", "MANDATO_RIGA.ESERCIZIO_OBBLIGAZIONE");
        sql.addSQLJoin("MANDATO_SIOPE.ESERCIZIO_ORI_OBBLIGAZIONE", "MANDATO_RIGA.ESERCIZIO_ORI_OBBLIGAZIONE");
        sql.addSQLJoin("MANDATO_SIOPE.PG_OBBLIGAZIONE", "MANDATO_RIGA.PG_OBBLIGAZIONE");
        sql.addSQLJoin("MANDATO_SIOPE.PG_OBBLIGAZIONE_SCADENZARIO", "MANDATO_RIGA.PG_OBBLIGAZIONE_SCADENZARIO");
        sql.addSQLJoin("MANDATO_SIOPE.CD_CDS_DOC_AMM", "MANDATO_RIGA.CD_CDS_DOC_AMM");
        sql.addSQLJoin("MANDATO_SIOPE.CD_UO_DOC_AMM", "MANDATO_RIGA.CD_UO_DOC_AMM");
        sql.addSQLJoin("MANDATO_SIOPE.ESERCIZIO_DOC_AMM", "MANDATO_RIGA.ESERCIZIO_DOC_AMM");
        sql.addSQLJoin("MANDATO_SIOPE.CD_TIPO_DOCUMENTO_AMM", "MANDATO_RIGA.CD_TIPO_DOCUMENTO_AMM");
        sql.addSQLJoin("MANDATO_SIOPE.PG_DOC_AMM", "MANDATO_RIGA.PG_DOC_AMM");



        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.CD_CDS", SQLBuilder.EQUALS, mandato_siopeBulk.getCd_cds());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.ESERCIZIO", SQLBuilder.EQUALS, mandato_siopeBulk.getEsercizio());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.PG_MANDATO", SQLBuilder.EQUALS, mandato_siopeBulk.getPg_mandato());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.ESERCIZIO_OBBLIGAZIONE", SQLBuilder.EQUALS, mandato_siopeBulk.getEsercizio_obbligazione());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.ESERCIZIO_ORI_OBBLIGAZIONE", SQLBuilder.EQUALS, mandato_siopeBulk.getEsercizio_ori_obbligazione());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.PG_OBBLIGAZIONE", SQLBuilder.EQUALS, mandato_siopeBulk.getPg_obbligazione());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.PG_OBBLIGAZIONE_SCADENZARIO", SQLBuilder.EQUALS, mandato_siopeBulk.getPg_obbligazione_scadenzario());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.CD_CDS_DOC_AMM", SQLBuilder.EQUALS, mandato_siopeBulk.getCd_cds_doc_amm());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.CD_UO_DOC_AMM", SQLBuilder.EQUALS, mandato_siopeBulk.getCd_uo_doc_amm());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.ESERCIZIO_DOC_AMM", SQLBuilder.EQUALS, mandato_siopeBulk.getEsercizio_doc_amm());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.CD_TIPO_DOCUMENTO_AMM", SQLBuilder.EQUALS, mandato_siopeBulk.getCd_tipo_documento_amm());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.PG_DOC_AMM", SQLBuilder.EQUALS, mandato_siopeBulk.getPg_doc_amm());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.ESERCIZIO_SIOPE", SQLBuilder.EQUALS, mandato_siopeBulk.getEsercizio_siope());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.TI_GESTIONE", SQLBuilder.EQUALS, mandato_siopeBulk.getTi_gestione());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.CD_SIOPE", SQLBuilder.EQUALS, mandato_siopeBulk.getCd_siope());


        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.ESERCIZIO", SQLBuilder.EQUALS, fattura.getEsercizio());
        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.CD_CDS", SQLBuilder.EQUALS, fattura.getCd_cds());
        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, fattura.getCd_unita_organizzativa());
        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_FATTURA_PASSIVA", SQLBuilder.EQUALS, fattura.getPg_fattura_passiva());
        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.CD_CDS_OBBLIGAZIONE", SQLBuilder.EQUALS, mandato_siopeBulk.getCd_cds_doc_amm());
        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.ESERCIZIO_OBBLIGAZIONE", SQLBuilder.EQUALS, mandato_siopeBulk.getEsercizio_obbligazione());
        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.ESERCIZIO_ORI_OBBLIGAZIONE", SQLBuilder.EQUALS, mandato_siopeBulk.getEsercizio_ori_obbligazione());
        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_OBBLIGAZIONE", SQLBuilder.EQUALS, mandato_siopeBulk.getPg_obbligazione());
        sql.addSQLClause(FindClause.AND, "FATTURA_PASSIVA_RIGA.PG_OBBLIGAZIONE_SCADENZARIO", SQLBuilder.EQUALS, mandato_siopeBulk.getPg_obbligazione_scadenzario());


        sql.addSQLClause(FindClause.AND, "MANDATO.ESERCIZIO", SQLBuilder.EQUALS, mandato.getEsercizio());
        sql.addSQLClause(FindClause.AND, "MANDATO.CD_CDS", SQLBuilder.EQUALS, mandato.getCd_cds());
        sql.addSQLClause(FindClause.AND, "MANDATO.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, mandato.getCd_unita_organizzativa());
        sql.addSQLClause(FindClause.AND, "MANDATO.PG_MANDATO", SQLBuilder.EQUALS, mandato.getPg_mandato());


        return fetchAll(sql);
    }

    protected SQLBuilder selectForObbligazioneExceptFor(
            it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk scadenza,
            Fattura_passivaBulk fattura) {

        SQLBuilder sql = createSQLBuilder();

        sql.addTableToHeader("FATTURA_PASSIVA");
        sql.addSQLJoin("FATTURA_PASSIVA_RIGA.ESERCIZIO", "FATTURA_PASSIVA.ESERCIZIO");
        sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_CDS", "FATTURA_PASSIVA.CD_CDS");
        sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_UNITA_ORGANIZZATIVA", "FATTURA_PASSIVA.CD_UNITA_ORGANIZZATIVA");
        sql.addSQLJoin("FATTURA_PASSIVA_RIGA.PG_FATTURA_PASSIVA", "FATTURA_PASSIVA.PG_FATTURA_PASSIVA");
        sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.CD_CDS_OBBLIGAZIONE", SQLBuilder.EQUALS, scadenza.getCd_cds());
        sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.ESERCIZIO_OBBLIGAZIONE", SQLBuilder.EQUALS, scadenza.getEsercizio());
        sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.ESERCIZIO_ORI_OBBLIGAZIONE", SQLBuilder.EQUALS, scadenza.getEsercizio_originale());
        sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.PG_OBBLIGAZIONE", SQLBuilder.EQUALS, scadenza.getPg_obbligazione());
        sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.PG_OBBLIGAZIONE_SCADENZARIO", SQLBuilder.EQUALS, scadenza.getPg_obbligazione_scadenzario());
        sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.STATO_COFI", SQLBuilder.NOT_EQUALS, Fattura_passiva_rigaBulk.STATO_ANNULLATO);
//FL_BOLLA_DOGANALE, FL_SPEDIZIONIERE
        //sql.addSQLClause("AND","FATTURA_PASSIVA.FL_BOLLA_DOGANALE",sql.EQUALS, Boolean.FALSE);
        //sql.addSQLClause("AND","FATTURA_PASSIVA.FL_SPEDIZIONIERE",sql.EQUALS, Boolean.FALSE);

        if (fattura != null) {
            sql.addSQLClause("AND", "FATTURA_PASSIVA.PG_FATTURA_PASSIVA", SQLBuilder.NOT_EQUALS, fattura.getPg_fattura_passiva());
            sql.addSQLClause("AND", "FATTURA_PASSIVA.CD_CDS", SQLBuilder.EQUALS, fattura.getCd_cds_origine());
            sql.addSQLClause("AND", "FATTURA_PASSIVA.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, fattura.getCd_uo_origine());
            sql.addSQLClause("AND", "FATTURA_PASSIVA.PG_LETTERA", SQLBuilder.ISNULL, null);
        }

         return sql;
    }

    /**
     * Inizializza la chiave primaria di un OggettoBulk per un
     * inserimento. Da usare principalmente per riempire i progressivi
     * automatici.
     *
     * @param fatturaPassiva l'OggettoBulk da inizializzare
     */
    public SQLBuilder selectObbligazioniPer(
            it.cnr.jada.UserContext userContext,
            Fattura_passivaBulk fatturaPassiva,
            java.math.BigDecimal minIm_scadenza)
            throws PersistencyException {

        if (fatturaPassiva == null) return null;

        TerzoBulk fornitore = fatturaPassiva.getFornitore();
        if (fornitore != null) {
            SQLBuilder sql = createSQLBuilder();
            sql.addTableToHeader("FATTURA_PASSIVA");
            sql.addTableToHeader("OBBLIGAZIONE_SCADENZARIO");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.ESERCIZIO", "FATTURA_PASSIVA.ESERCIZIO");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_CDS", "FATTURA_PASSIVA.CD_CDS");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_UNITA_ORGANIZZATIVA", "FATTURA_PASSIVA.CD_UNITA_ORGANIZZATIVA");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.PG_FATTURA_PASSIVA", "FATTURA_PASSIVA.PG_FATTURA_PASSIVA");

            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.CD_CDS_OBBLIGAZIONE", "OBBLIGAZIONE_SCADENZARIO.CD_CDS");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.ESERCIZIO_OBBLIGAZIONE", "OBBLIGAZIONE_SCADENZARIO.ESERCIZIO");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.ESERCIZIO_ORI_OBBLIGAZIONE", "OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.PG_OBBLIGAZIONE", "OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE");
            sql.addSQLJoin("FATTURA_PASSIVA_RIGA.PG_OBBLIGAZIONE_SCADENZARIO", "OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO");

            sql.addSQLClause("AND", "FATTURA_PASSIVA.TI_FATTURA", SQLBuilder.EQUALS, Fattura_passiva_IBulk.TIPO_FATTURA_PASSIVA);
            sql.addSQLClause("AND", "FATTURA_PASSIVA.CD_TERZO", SQLBuilder.EQUALS, fornitore.getCd_terzo());
            //sql.addSQLClause("AND","FATTURA_PASSIVA.PG_LETTERA",sql.ISNULL, null);
            //sql.addSQLClause("AND","FATTURA_PASSIVA.PROTOCOLLO_IVA",sql.ISNULL, null);
            //sql.addSQLClause("AND","FATTURA_PASSIVA.PROTOCOLLO_IVA_GENERALE",sql.ISNULL, null);
            sql.addSQLClause("AND", "FATTURA_PASSIVA.STATO_PAGAMENTO_FONDO_ECO", SQLBuilder.EQUALS, Fattura_passiva_IBulk.NO_FONDO_ECO);
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.STATO_COFI", SQLBuilder.EQUALS, Fattura_passiva_rigaBulk.STATO_CONTABILIZZATO);
            sql.addSQLClause("AND", "FATTURA_PASSIVA.CD_CDS", SQLBuilder.EQUALS, fatturaPassiva.getCd_cds_origine());
            sql.addSQLClause("AND", "FATTURA_PASSIVA.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, fatturaPassiva.getCd_uo_origine());
            sql.addSQLClause("AND", "FATTURA_PASSIVA.ESERCIZIO", SQLBuilder.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext));

            //sql.addSQLClause("AND","FATTURA_PASSIVA.FL_BOLLA_DOGANALE",sql.EQUALS, Boolean.FALSE);
            //sql.addSQLClause("AND","FATTURA_PASSIVA.FL_SPEDIZIONIERE",sql.EQUALS, Boolean.FALSE);

            sql.addSQLClause("AND", "OBBLIGAZIONE_SCADENZARIO.ESERCIZIO", SQLBuilder.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext));
            sql.addSQLClause("AND", "OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA", SQLBuilder.GREATER_EQUALS, minIm_scadenza);

            return sql;
        }
        return null;
    }

    private SQLBuilder selectRigaFor(Fattura_passiva_rigaIBulk rigaFattura) {

        SQLBuilder sql = createSQLBuilder();

        if (rigaFattura != null) {
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.CD_CDS_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getCd_cds());
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.CD_UO_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getCd_unita_organizzativa());
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.ESERCIZIO_RIGA_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getEsercizio());
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.PG_FATTURA_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getPg_fattura_passiva());
            sql.addSQLClause("AND", "FATTURA_PASSIVA_RIGA.PG_RIGA_ASSNCNA_FIN", SQLBuilder.EQUALS, rigaFattura.getProgressivo_riga());
        }
        return sql;
    }

    public SQLBuilder selectModalita(Fattura_passiva_rigaBulk rigaFattura, it.cnr.contab.docamm00.tabrif.bulk.DivisaHome divisaHome, it.cnr.contab.docamm00.tabrif.bulk.DivisaBulk clause) {

        return divisaHome.createSQLBuilder();
    }

    public java.util.List<Fattura_passiva_riga_ecoBulk> findFatturaPassivaRigheEcoList(Fattura_passiva_rigaBulk docRiga) throws PersistencyException {
        PersistentHome home;
        if (docRiga instanceof Nota_di_credito_rigaBulk)
            home = getHomeCache().getHome(Nota_di_credito_riga_ecoBulk.class);
        else if (docRiga instanceof Nota_di_debito_rigaBulk)
            home = getHomeCache().getHome(Nota_di_debito_riga_ecoBulk.class);
        else
            home = getHomeCache().getHome(Fattura_passiva_riga_ecoIBulk.class);

        it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, docRiga.getEsercizio());
        sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, docRiga.getCd_cds());
        sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, docRiga.getCd_unita_organizzativa());
        sql.addClause(FindClause.AND, "pg_fattura_passiva", SQLBuilder.EQUALS, docRiga.getPg_fattura_passiva());
        sql.addClause(FindClause.AND, "progressivo_riga", SQLBuilder.EQUALS, docRiga.getProgressivo_riga());
        return home.fetchAll(sql);
    }

    protected ContoBulk getContoCostoDefault(Fattura_passiva_rigaBulk docRiga) {
        try {
            Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
            //verifico se sulla riga del docamm ci sia un bene inventariabile
            if (Optional.ofNullable(docRiga).isPresent()) {
                Bene_servizioBulk myBeneServizio = (Bene_servizioBulk)fatpasHome.loadIfNeededObject(docRiga.getBene_servizio());

                if (myBeneServizio.getFl_gestione_inventario() && Optional.ofNullable(myBeneServizio.getCd_categoria_gruppo()).isPresent()) {
                    AssCatgrpInventVoceEpHome assCatgrpInventVoceEpHome = (AssCatgrpInventVoceEpHome) getHomeCache().getHome(AssCatgrpInventVoceEpBulk.class);
                    AssCatgrpInventVoceEpBulk result = assCatgrpInventVoceEpHome.findDefaultByCategoria(docRiga.getEsercizio(), myBeneServizio.getCd_categoria_gruppo());
                    return result.getConto();
                }

                //se arrivo qui devo guardare alla voce dell'obbligazione
                if (Optional.ofNullable(docRiga.getObbligazione_scadenziario()).isPresent()) {
                    Obbligazione_scadenzarioBulk obbligScad = (Obbligazione_scadenzarioBulk) fatpasHome.loadIfNeededObject(docRiga.getObbligazione_scadenziario());

                    if (Optional.ofNullable(obbligScad).isPresent()) {
                        ObbligazioneBulk obblig = (ObbligazioneBulk) fatpasHome.loadIfNeededObject(obbligScad.getObbligazione());
                        Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHomeCache().getHome(Ass_ev_voceepBulk.class);
                        List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(obblig.getElemento_voce());
                        return Optional.ofNullable(listAss).orElse(new ArrayList<>())
                                .stream().map(Ass_ev_voceepBulk::getVoce_ep)
                                .findAny().orElse(null);
                    }
                }
            }
            return null;
        } catch (PersistencyException e) {
            throw new DetailedRuntimeException(e);
        }
    }
}
