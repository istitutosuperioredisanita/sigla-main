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

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.docamm00.storage.StorageFolderAutofattura;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoStorePath;
import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.StrServ;
import it.cnr.jada.util.action.CRUDBP;

import java.util.*;

public class AutofatturaBulk extends AutofatturaBase implements IDocumentoAmministrativoElettronicoBulk, AllegatoParentBulk, AllegatoStorePath {

	private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<>();



	public final static String STATO_INIZIALE = "I";
	public final static String STATO_CONTABILIZZATO = "C";
	public final static String STATO_PARZIALE = "Q";
	public final static String STATO_PAGATO = "P";
	public final static Dictionary SEZIONALI_FLAG_KEYS;

	public final static Dictionary STATO;

	private String sezionaliFlag = Fattura_passivaBulk.SEZIONALI_FLAGS_ALL;



	static {


		STATO = new it.cnr.jada.util.OrderedHashtable();
		STATO.put(STATO_INIZIALE, "Iniziale");
		STATO.put(STATO_CONTABILIZZATO, "Contabilizzato");
		STATO.put(STATO_PARZIALE, "Parziale");
		STATO.put(STATO_PAGATO, "Incassato");

		SEZIONALI_FLAG_KEYS = new it.cnr.jada.util.OrderedHashtable();
		SEZIONALI_FLAG_KEYS.put(Fattura_passivaBulk.SEZIONALI_FLAGS_ALL, "Tutte");
		SEZIONALI_FLAG_KEYS.put(Fattura_passivaBulk.SEZIONALI_FLAGS_IUE, "Intra UE");
		SEZIONALI_FLAG_KEYS.put(Fattura_passivaBulk.SEZIONALI_FLAGS_EUE, "Extra UE");
		SEZIONALI_FLAG_KEYS.put(Fattura_passivaBulk.SEZIONALI_FLAGS_SMC, "S. Marino con IVA");
		SEZIONALI_FLAG_KEYS.put(Fattura_passivaBulk.SEZIONALI_FLAGS_SMS, "S. Marino senza IVA");


	}

	public BulkCollection[] getBulkLists() {

		// Metti solo le liste di oggetti che devono essere resi persistenti

		return new it.cnr.jada.bulk.BulkCollection[] {
				archivioAllegati
		};
	}
	@JsonIgnore
	public Dictionary getStato_cofiKeys() {
		return STATO;
	}

	public Dictionary getTi_istituz_commercKeys() {
		return fattura_passiva.getTi_istituz_commercKeys();
	}

	public Dictionary getTi_istituz_commercKeysForSearch() {
		return fattura_passiva.getTi_istituz_commercKeysForSearch();
	}

	public final static String STATO_IVA_A = "A";
	public final static String STATO_IVA_B = "B";
	public final static String STATO_IVA_C = "C";

	Fattura_passivaBulk fattura_passiva = new Fattura_passiva_IBulk();
	Tipo_sezionaleBulk tipo_sezionale = new Tipo_sezionaleBulk();



	private java.lang.String ti_bene_servizio = null;
	//private boolean autofatturaNeeded = false;
	private TerzoBulk terzoDocumentoElettronico;

	@JsonIgnore
	private java.util.Collection sezionali;




	public AutofatturaBulk() {
		super();
	}
	public AutofatturaBulk(java.lang.String cd_cds,java.lang.String cd_unita_organizzativa,java.lang.Integer esercizio,java.lang.Long pg_autofattura) {
		super(cd_cds,cd_unita_organizzativa,esercizio,pg_autofattura);


	}

	public Dictionary getSezionaliFlags() {
		return SEZIONALI_FLAG_KEYS;
	}



	public OggettoBulk initializeForSearch(CRUDBP bp, it.cnr.jada.action.ActionContext context) {
		super.initializeForSearch(bp,context);


		it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk unita_organizzativa = it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context);
		setCd_uo_origine( null);
		if ( !unita_organizzativa.isUoEnte()) {
			setCd_cds_origine(unita_organizzativa.getUnita_padre().getCd_unita_organizzativa());
			setCd_uo_origine( unita_organizzativa.getCd_unita_organizzativa());
		}
		if (getEsercizio() == null)
			setEsercizio(it.cnr.contab.utenze00.bulk.CNRUserInfo.getEsercizio(context));

		setFl_intra_ue(null);
		setFl_extra_ue(null);
		setFl_san_marino_con_iva(null);
		setFl_san_marino_senza_iva(null);
		setTipo_sezionale(null);
		return this;
	}

	public OggettoBulk initialize(CRUDBP bp, it.cnr.jada.action.ActionContext context) {


		super.initialize(bp, context);

		if (getCd_uo_origine() == null)
			setCd_uo_origine(it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context).getCd_unita_organizzativa());

		return this;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (2/12/2002 4:10:01 PM)
	 */
	public void completeFrom(Fattura_passivaBulk fatturaPassiva) {

		setFattura_passiva(fatturaPassiva);

		setCd_cds_origine(fatturaPassiva.getCd_cds_origine());
		setCd_uo_origine(fatturaPassiva.getCd_uo_origine());

		setDt_registrazione(fatturaPassiva.getDt_registrazione());
		setData_esigibilita_iva(getDt_registrazione());
		setStato_cofi(AutofatturaBulk.STATO_CONTABILIZZATO);
		setStato_coge(Fattura_passivaBulk.NON_PROCESSARE_IN_COGE);

		setFl_liquidazione_differita(Boolean.FALSE);
		setFl_intra_ue(fatturaPassiva.getFl_intra_ue());
		setFl_extra_ue(fatturaPassiva.getFl_extra_ue());
		setFl_san_marino_con_iva(fatturaPassiva.getFl_san_marino_con_iva());
		setFl_san_marino_senza_iva(fatturaPassiva.getFl_san_marino_senza_iva());
		setFl_split_payment(fatturaPassiva.getFl_split_payment());
		setTi_bene_servizio(fattura_passiva.getTi_bene_servizio());

		if (fatturaPassiva.getFl_split_payment())
			setFl_autofattura(fatturaPassiva.getFl_autofattura());
		else
			setFl_autofattura(Boolean.TRUE);

        //setAutofatturaNeeded(
        //        fatturaPassiva.isCommerciale() &&
        //                ((getFl_intra_ue() != null && Boolean.TRUE.equals(getFl_intra_ue())) ||
        //                        (getFl_san_marino_senza_iva() != null && Boolean.TRUE.equals(getFl_san_marino_senza_iva())))
        //);

        setEsercizio(fatturaPassiva.getEsercizio());
        setProtocollo_iva(fatturaPassiva.getProtocollo_iva());
        setProtocollo_iva_generale(fatturaPassiva.getProtocollo_iva_generale());
        setTi_istituz_commerc(fatturaPassiva.getTi_istituz_commerc());
        setTi_fattura(fatturaPassiva.getTi_fattura());
        setToBeCreated();
    }


    public java.lang.String getCd_cds_ft_passiva() {
        it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBase fattura_passiva = this.getFattura_passiva();
        if (fattura_passiva == null)
            return null;
        return fattura_passiva.getCd_cds();
    }
    public java.lang.String getCd_tipo_sezionale() {
        it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk tipo_sezionale = this.getTipo_sezionale();
        if (tipo_sezionale == null)
            return null;
        return tipo_sezionale.getCd_tipo_sezionale();
    }
    public java.lang.String getCd_uo_ft_passiva() {
        it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBase fattura_passiva = this.getFattura_passiva();
        if (fattura_passiva == null)
            return null;
        return fattura_passiva.getCd_unita_organizzativa();
    }
    /**
     * Insert the method's description here.
     * Creation date: (2/12/2002 4:06:00 PM)
     * @return it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk
     */
    public Fattura_passivaBulk getFattura_passiva() {
        return fattura_passiva;
    }



    public java.lang.Long getPg_fattura_passiva() {
        it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBase fattura_passiva = this.getFattura_passiva();
        if (fattura_passiva == null)
            return null;
        return fattura_passiva.getPg_fattura_passiva();
    }
    public String getStatoIVA() {
        return (getProtocollo_iva() == null ||
                getProtocollo_iva_generale() == null) ?
                    "A" : "B";
    }
    /**
     * Insert the method's description here.
     * Creation date: (10/14/2002 5:22:33 PM)
     * @return java.lang.String
     */
    public java.lang.String getTi_bene_servizio() {
        return ti_bene_servizio;
    }
    /**
     * Insert the method's description here.
     * Creation date: (2/12/2002 4:06:00 PM)
     * @return it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk
     */
    public it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk getTipo_sezionale() {
        return tipo_sezionale;
    }

	private Boolean isCommerciale(){
		return TipoIVA.COMMERCIALE.value().equals(getTi_istituz_commerc());
	}
    /**
     * Insert the method's description here.
     * Creation date: (4/8/2003 3:38:07 PM)
     * @return boolean
     */
    public boolean isAutofatturaDiBeni() {
        return Bene_servizioBulk.BENE.equals(getTi_bene_servizio());
    }
    /**
     * Insert the method's description here.
     * Creation date: (4/8/2003 3:38:07 PM)
     * @return boolean
     */
    public boolean isAutofatturaNeeded() {
        return isCommerciale() &&
		            ((getFl_intra_ue() != null && Boolean.TRUE.equals(getFl_intra_ue())) ||
		                        (getFl_san_marino_senza_iva() != null && Boolean.TRUE.equals(getFl_san_marino_senza_iva())));
    }
    public boolean isStampataSuRegistroIVA() {
        return STATO_IVA_B.equalsIgnoreCase(getStatoIVA()) ||
                STATO_IVA_C.equalsIgnoreCase(getStatoIVA());
    }
	/**
	 * Insert the method's description here.
	 * Creation date: (4/8/2003 3:38:07 PM)
	 * @param newAutofatturaNeeded boolean
	 */
	//public void setAutofatturaNeeded(boolean newAutofatturaNeeded) {
	//	autofatturaNeeded = newAutofatturaNeeded;
	//}
	public void setCd_cds_ft_passiva(java.lang.String cd_cds_ft_passiva) {
		this.getFattura_passiva().setCd_cds(cd_cds_ft_passiva);
	}
	public void setCd_tipo_sezionale(java.lang.String cd_tipo_sezionale) {
		this.getTipo_sezionale().setCd_tipo_sezionale(cd_tipo_sezionale);
	}
	public void setCd_uo_ft_passiva(java.lang.String cd_uo_ft_passiva) {
		this.getFattura_passiva().setCd_unita_organizzativa(cd_uo_ft_passiva);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (2/12/2002 4:06:00 PM)
	 * @param newFattura_passiva it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk
	 */
	public void setFattura_passiva(Fattura_passivaBulk newFattura_passiva) {
		fattura_passiva = newFattura_passiva;
	}


	public void setPg_fattura_passiva(java.lang.Long pg_fattura_passiva) {
		this.getFattura_passiva().setPg_fattura_passiva(pg_fattura_passiva);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10/14/2002 5:22:33 PM)
	 * @param newTi_bene_servizio java.lang.String
	 */
	public void setTi_bene_servizio(java.lang.String newTi_bene_servizio) {
		ti_bene_servizio = newTi_bene_servizio;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (2/12/2002 4:06:00 PM)
	 * @param newTipo_sezionale it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk
	 */
	public void setTipo_sezionale(it.cnr.contab.docamm00.tabrif.bulk.Tipo_sezionaleBulk newTipo_sezionale) {
		tipo_sezionale = newTipo_sezionale;
	}

	public String constructCMISNomeFile() {
		StringBuffer nomeFile = new StringBuffer();
		nomeFile = nomeFile.append(getTi_fattura());
		nomeFile = nomeFile.append("-" + this.getCd_unita_organizzativa());
		nomeFile = nomeFile.append("-" + this.getEsercizio().toString() + Utility.lpad(this.getPg_autofattura().toString(), 9, '0'));
		return nomeFile.toString();
	}

	@Override
	public Long getPg_docamm() {
		return this.getPg_autofattura();
	}

	@Override
	public Boolean isFatturaEstera() {
		return this.getFattura_passiva()!=null && (getFattura_passiva().getFl_extra_ue()
					|| getFattura_passiva().getFl_intra_ue() || getFattura_passiva().getFl_san_marino_con_iva() || getFattura_passiva().getFl_san_marino_senza_iva());
	}

	@Override
	public String recuperoIdFatturaAsString() {
		if (getProtocollo_iva() != null) {
			return StrServ.replace(getCd_uo_origine(), ".", "") + getEsercizio() + StrServ.lpad(getCd_tipo_sezionale().substring(2), 4) + getTi_fattura() + StrServ.lpad(getProtocollo_iva().toString(), 5);
		}
		return StrServ.replace(getCd_unita_organizzativa(), ".", "") + getEsercizio() + StrServ.lpad(getPg_autofattura().toString(), 5);
	}

	@Override
	public String getTipoDocumentoElettronico() {
		return Numerazione_doc_ammBulk.TIPO_AUTOFATTURA;
	}

	public TerzoBulk getTerzoDocumentoElettronico() {
		return terzoDocumentoElettronico;
	}

	public void setTerzoDocumentoElettronico(TerzoBulk terzoDocumentoElettronico) {
		this.terzoDocumentoElettronico = terzoDocumentoElettronico;
	}

	@Override
	public Integer getCdTerzoDocumentoElettronico() {
		return Optional.ofNullable(this.getTerzoDocumentoElettronico())
				.map(TerzoBulk::getCd_terzo)
				.orElse(null);
	}

	@Override
	public String getPartita_iva() {
		if (this.getFlTerzoEnte())
			return Optional.ofNullable(this.getTerzoDocumentoElettronico())
					.map(TerzoBulk::getPartita_iva_anagrafico)
					.orElse(null);
		else
			return Optional.ofNullable(this.getFattura_passiva())
					.map(Fattura_passivaBase::getPartita_iva)
					.orElse(null);
	}

	public boolean isDocumentoFatturazioneElettronica() {
		return Optional.ofNullable(getFlFatturaElettronica()).orElse(Boolean.FALSE);
	}

	@JsonIgnore
	public java.util.Collection<Tipo_sezionaleBulk> getSezionali() {
		return sezionali;
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (18/10/2001 14.41.50)
	 * @param newSezionali java.util.Collection
	 */
	public void setSezionali(java.util.Collection newSezionali) {
		sezionali = newSezionali;
	}

	@Override
	public int addToArchivioAllegati(AllegatoGenericoBulk allegato) {
		archivioAllegati.add(allegato);
		return archivioAllegati.size()-1;
	}

	@Override
	public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
		return getArchivioAllegati().remove(index);
	}

	@Override
	public BulkList<AllegatoGenericoBulk> getArchivioAllegati() {
		return archivioAllegati;
	}

	@Override
	public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegati) {
		this.archivioAllegati = archivioAllegati;
	}

	@Override
	public List<String> getStorePath() {
		StorageFolderAutofattura storageFolderAutofattura = new StorageFolderAutofattura(this);
		String path= storageFolderAutofattura.getCMISPathForSearch();
		return new ArrayList<>( Arrays.asList( path));
		/*
		return Collections.singletonList(Arrays.asList(
				SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
				Optional.ofNullable(this)
						.map(s -> this.getCd_uo_origine())
						.orElse(""),
				"AutoFatture",
				this.getEsercizio().toString(),
					"Autofattura "+ this.getEsercizio().toString() +
				Utility.lpad(this.getPg_autofattura().toString(),10,'0')
		).stream().collect(
				Collectors.joining(StorageDriver.SUFFIX)
		));*/
	}

	public String getSezionaliFlag() {

		if (getFl_intra_ue() != null && getFl_intra_ue().booleanValue()) {
			setFl_extra_ue(Boolean.FALSE);
			setFl_san_marino_con_iva(Boolean.FALSE);
			setFl_san_marino_senza_iva(Boolean.FALSE);
			sezionaliFlag = Fattura_passivaBulk.SEZIONALI_FLAGS_IUE;
		} else if (getFl_extra_ue() != null && getFl_extra_ue().booleanValue()) {
			setFl_intra_ue(Boolean.FALSE);
			setFl_san_marino_con_iva(Boolean.FALSE);
			setFl_san_marino_senza_iva(Boolean.FALSE);
			sezionaliFlag = Fattura_passivaBulk.SEZIONALI_FLAGS_EUE;
		} else if (getFl_san_marino_con_iva() != null && getFl_san_marino_con_iva().booleanValue()) {
			setFl_intra_ue(Boolean.FALSE);
			setFl_extra_ue(Boolean.FALSE);
			setFl_san_marino_senza_iva(Boolean.FALSE);
			sezionaliFlag = Fattura_passivaBulk.SEZIONALI_FLAGS_SMC;
		} else if (getFl_san_marino_senza_iva() != null && getFl_san_marino_senza_iva().booleanValue()) {
			setFl_intra_ue(Boolean.FALSE);
			setFl_extra_ue(Boolean.FALSE);
			setFl_san_marino_con_iva(Boolean.FALSE);
			sezionaliFlag = Fattura_passivaBulk.SEZIONALI_FLAGS_SMS;
		} else {
				sezionaliFlag = Fattura_passivaBulk.SEZIONALI_FLAGS_ALL;
		}

		return sezionaliFlag;
	}

	public void setSezionaliFlag(String newSezionaliFlag) {
		sezionaliFlag = newSezionaliFlag;

		switch (sezionaliFlag == null ? 99 : Integer.valueOf(sezionaliFlag).intValue()) {
			case 0: {
				setFl_intra_ue(null);
				setFl_extra_ue(null);
				setFl_san_marino_con_iva(null);
				setFl_san_marino_senza_iva(null);

				break;
			}
			case 1: {
				setFl_intra_ue(Boolean.FALSE);
				setFl_extra_ue(Boolean.FALSE);
				setFl_san_marino_con_iva(Boolean.FALSE);
				setFl_san_marino_senza_iva(Boolean.FALSE);

				break;
			}
			case 2: {
				setFl_intra_ue(Boolean.TRUE);
				setFl_extra_ue(Boolean.FALSE);
				setFl_san_marino_con_iva(Boolean.FALSE);
				setFl_san_marino_senza_iva(Boolean.FALSE);
				setFl_autofattura(Boolean.TRUE);

				break;
			}
			case 3: {
				setFl_intra_ue(Boolean.FALSE);
				setFl_extra_ue(Boolean.TRUE);
				setFl_san_marino_con_iva(Boolean.FALSE);
				setFl_san_marino_senza_iva(Boolean.FALSE);

				break;
			}
			case 4: {
				setFl_intra_ue(Boolean.FALSE);
				setFl_extra_ue(Boolean.FALSE);
				setFl_san_marino_con_iva(Boolean.TRUE);
				setFl_san_marino_senza_iva(Boolean.FALSE);

				break;
			}
			case 5: {
				setFl_intra_ue(Boolean.FALSE);
				setFl_extra_ue(Boolean.FALSE);
				setFl_san_marino_con_iva(Boolean.FALSE);
				setFl_san_marino_senza_iva(Boolean.TRUE);
				setFl_autofattura(Boolean.TRUE);

				break;
			}
			default: {
				setFl_intra_ue(null);
				setFl_extra_ue(null);
				setFl_san_marino_con_iva(null);
				setFl_san_marino_senza_iva(null);

			}
		}
	}
}
