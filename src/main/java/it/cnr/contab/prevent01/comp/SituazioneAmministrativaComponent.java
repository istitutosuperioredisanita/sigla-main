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

package it.cnr.contab.prevent01.comp;

import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.config00.bulk.Configurazione_cnrKey;
import it.cnr.contab.prevent01.bulk.SituazioneAmministrativaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.EuroFormat;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.IPrintMgr;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.util.SendMail;
import it.cnr.jada.util.ejb.EJBCommonServices;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SituazioneAmministrativaComponent extends CRUDComponent implements IPrintMgr {
	private static final Logger logger = LoggerFactory.getLogger(SituazioneAmministrativaComponent.class);

	public SituazioneAmministrativaComponent() {
		super();
	}


	@Override
	public OggettoBulk inizializzaBulkPerStampa(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException {
		((SituazioneAmministrativaBulk) oggettoBulk).setEsercizio(CNRUserContext.getEsercizio(userContext));
		return oggettoBulk;
	}

	@Override
	public OggettoBulk stampaConBulk(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException {
		return oggettoBulk;
	}

	public void sendAlertMail(UserContext userContext) throws ComponentException {
        try {
			List<Configurazione_cnrBulk> configList = ((Configurazione_cnrHome)getHome(userContext, Configurazione_cnrBulk.class))
					.getAllConfigurazioni("*", Configurazione_cnrBulk.PK_SITUAZIONE_AMMINISTRATIVA, Configurazione_cnrBulk.SK_ALERT_MAIL);

			for (Configurazione_cnrBulk config : configList.stream()
					.filter(el->el.getEsercizio().compareTo(0)!=0)
					.filter(el-> Optional.ofNullable(el.getVal01())
							.map(el2->el2.equals("Y")).orElse(Boolean.FALSE))
					.filter(el-> Optional.ofNullable(el.getVal02()).isPresent())
					.filter(el-> Optional.ofNullable(el.getIm01()).isPresent() ||
							Optional.ofNullable(el.getIm02()).isPresent())
					.sorted(Comparator.comparingInt(Configurazione_cnrKey::getEsercizio))
					.toList()) {
				int esercizio = config.getEsercizio();

				callLoadTableStampaRendic(userContext, esercizio);

				String mailTo = config.getVal02();
				BigDecimal percLimite = config.getIm01();
				BigDecimal impLimite = config.getIm02();

				BigDecimal cassa = BigDecimal.ZERO;
				BigDecimal residuiPassivi = BigDecimal.ZERO;

				LoggableStatement ps = new LoggableStatement(getConnection(userContext),
						"SELECT NVL(CASSA_INIZIO_ANNO,0) + NVL(INCASSATI,0) + NVL(RESIDUI_INCASSATI,0) - NVL(PAGATI,0) - NVL(RESIDUI_PAGATI,0)," +
								" NVL(DA_PAGARE,0) + NVL(DA_PAGARE_RESIDUO,0) FROM " +
								"(SELECT NVL(max((select im01 from " +
								EJBCommonServices.getDefaultSchema() +
								"configurazione_cnr where cd_chiave_primaria='BILANCIO_ENTE' AND " +
								"cd_chiave_secondaria='FONDO_CASSA' AND ESERCIZIO = ?" +
								" GROUP BY im01)),0)  as cassa_inizio_anno, " +
								"SUM(DECODE(tipo,'S',RES_RIS_PAG,0)) as residui_pagati, " +
								"SUM(DECODE(tipo,'S',RIS_PAG,0)) as pagati, " +
								"SUM(DECODE(tipo,'E',RES_RIS_PAG,0)) as residui_incassati, " +
								"SUM(DECODE(tipo,'E',RIS_PAG,0))  as incassati, " +
								"SUM(DECODE(tipo,'S',RIM_RIS_PAG,0)) as da_pagare, " +
								"SUM(DECODE(tipo,'S',RES_RIM_RIS_PAG,0)) as da_pagare_residuo, " +
								"SUM(DECODE(tipo,'E',RIM_RIS_PAG,0)) as da_indassare, " +
								"SUM(DECODE(tipo,'E',RES_RIM_RIS_PAG,0)) as da_incassare_residuo " +
								"from " +
								EJBCommonServices.getDefaultSchema() +
								"TMP_STAMPA_RENDICONTO)", true, this.getClass());
				try {
					ps.setObject(1, esercizio);

					ResultSet rs = ps.executeQuery();
					try {
						if (rs.next()) {
							cassa = rs.getBigDecimal(1);
							residuiPassivi = rs.getBigDecimal(2);
						}
					} catch (SQLException e) {
						throw new ComponentException(e);
					} finally {
						try {
							rs.close();
						} catch (SQLException ignored) {
						}
					}
				} catch (SQLException e) {
					throw new ComponentException(e);
				} finally {
					try {
						ps.close();
					} catch (SQLException ignored) {
					}
				}

				if (Optional.ofNullable(percLimite).isPresent()) {
					BigDecimal respasPerc = residuiPassivi.multiply(percLimite).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
					if (respasPerc.compareTo(cassa)>0) {
						logger.info("Situazione Amministrativa Service: sendAlertMail to {} - esercizio: {} respasPerc: {}, cassa: {}", mailTo, esercizio, respasPerc, cassa);
						String subject = "Attenzione! Esercizio " + esercizio + " - cassa inferiore a percentuale residui passivi.";
						String msg = "Esercizio " + esercizio + ": la consistenza di cassa alla fine dell’esercizio (" +
							new EuroFormat().format(cassa) + ") è inferiore al " + percLimite + "% del valore dei residui passivi (" +
									new EuroFormat().format(respasPerc) + ").";
						SendMail.sendMail(subject, msg, InternetAddress.parse(mailTo));
					}
				}

				if (Optional.ofNullable(impLimite).isPresent()) {
					if (cassa.compareTo(impLimite)<0) {
						logger.info("Situazione Amministrativa Service: sendAlertMail to {} - esercizio: {} impLimite: {}, cassa: {}", mailTo, esercizio, impLimite, cassa);
						String subject = "Attenzione! Esercizio " + esercizio + " - cassa inferiore a limite.";
						String msg = "Esercizio " + esercizio + ": la consistenza di cassa alla fine dell’esercizio (" +
								new EuroFormat().format(cassa) + ") è inferiore al limite prestabilito (" +
								new EuroFormat().format(impLimite) + ").";
						SendMail.sendMail(subject, msg, InternetAddress.parse(mailTo));
					}
				}
			}
		} catch (SQLException | PersistencyException | AddressException e) {
			logger.error("Situazione Amministrativa Service: error {}", e.getMessage());
			throw new ComponentException(e.getMessage());
        }
    }

	public void callLoadTableStampaRendic(UserContext usercontext, int esercizio) throws ComponentException
	{
		try {
			LoggableStatement ps = new LoggableStatement(getConnection(usercontext),
					"DELETE FROM " +
							it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
							"TMP_STAMPA_RENDICONTO", true, this.getClass());

			try {
				ps.executeUpdate();
			} finally {
				ps.close();
			}

			LoggableStatement callablestatement =new LoggableStatement( getConnection(usercontext),
					"{  call " + EJBCommonServices.getDefaultSchema()
							+ "PRC_LOAD_TABLE_STAMPA_RENDIC( ?, ?, ?, ?, ?, ? ) }",false,this.getClass());
			try
			{
				callablestatement.setInt(1, esercizio);
				callablestatement.setString(2, "S");
				callablestatement.setInt(3, 1);
				callablestatement.setString(4, "FIN");
				callablestatement.setString(5, "GEST");
				callablestatement.setString(6, "REA");
				callablestatement.execute();
			}
			finally
			{
				callablestatement.close();
			}
		}
		catch(Throwable throwable)
		{
			throw handleException(throwable);
		}
	}
}
