--------------------------------------------------------
--  DDL for View V_ASS_INV_BENE_FATTURA
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "V_ASS_INV_BENE_FATTURA" ("CD_CDS_FATT_PASS", "CD_UO_FATT_PASS", "ESERCIZIO_FATT_PASS", "PG_FATTURA_PASSIVA", "PROGRESSIVO_RIGA_FATT_PASS", "PG_INVENTARIO", "NR_INVENTARIO", "PROGRESSIVO", "DS_BENE", "DS_FATTURA", "CD_TERZO", "DS_TERZO", "TIPO", "PG_BUONO_C_S", "TI_DOCUMENTO", "ESERCIZIO", "CD_TIPO_DOCUMENTO_AMM") AS
    select
              null CD_CDS_FATT_PASS,
              null CD_UO_FATT_PASS,
              null ESERCIZIO_FATT_PASS,
              null PG_FATTURA_PASSIVA,
              null PROGRESSIVO_RIGA_FATT_PASS,
              ass.PG_INVENTARIO ,
              ass.NR_INVENTARIO ,
              ass.PROGRESSIVO ,
              ass.DS_BENE,
              null DS_FATTURA,
              null CD_TERZO,
              null DS_TERZO,
              null Tipo,
              ass.PG_BUONO_C_S,
              ass.TI_DOCUMENTO,
              ass.ESERCIZIO,
               Null tipo_generico
               from
              V_BUONO_CARICO_INV ass
              where ass.esercizio_fatt_pass is null and ass.esercizio_fatt_att is null and ass.PG_DOCUMENTO_GENERICO is null
              union
              select
                 ASS.CD_CDS_FATT_PASS           ,
                  ASS.CD_UO_FATT_PASS            ,
                  ASS.ESERCIZIO_FATT_PASS        ,
                  ASS.PG_FATTURA_PASSIVA     ,
                  ASS.PROGRESSIVO_RIGA_FATT_PASS ,
                  ass.PG_INVENTARIO ,
                  ass.NR_INVENTARIO ,
                  ass.PROGRESSIVO ,
                  ass.DS_BENE,
                  F_PASSIVA.DS_FATTURA_PASSIVA DS_FATTURA,
                  F_PASSIVA.CD_TERZO CD_TERZO,
                  Nvl(F_PASSIVA.RAGIONE_SOCIALE,Nvl(f_passiva.cognome,' ')||' '||Nvl(f_passiva.nome,' ')) DS_TERZO,
                  Decode(f_passiva.ti_fattura,'F','Fattura Passiva','C','Nota Credito da Fatt.Pass.','Nota Debito da Fatt.Pass.') Tipo,
                  ass.PG_BUONO_C_S,
                  ass.TI_DOCUMENTO,
                  ass.ESERCIZIO,
                   Null tipo_generico
                   from
                  V_BUONO_CARICO_INV ass
                   left outer join FATTURA_PASSIVA_RIGA F_PASSIVA_RIGA
                         on ASS.CD_CDS_FATT_PASS   		= F_PASSIVA_RIGA.CD_CDS   			And
                         ASS.CD_UO_FATT_PASS   		= F_PASSIVA_RIGA.CD_UNITA_ORGANIZZATIVA   	And
                         ASS.ESERCIZIO_FATT_PASS         	= F_PASSIVA_RIGA.ESERCIZIO			And
                         ASS.PG_FATTURA_PASSIVA           = F_PASSIVA_RIGA.PG_FATTURA_PASSIVA		And
                         ASS.PROGRESSIVO_RIGA_FATT_PASS   = F_PASSIVA_RIGA.PROGRESSIVO_RIGA
                     left outer join  FATTURA_PASSIVA F_PASSIVA
                         on F_PASSIVA_RIGA.CD_CDS                 = F_PASSIVA.CD_CDS   			And
                         F_PASSIVA_RIGA.CD_UNITA_ORGANIZZATIVA = F_PASSIVA.CD_UNITA_ORGANIZZATIVA   	And
                         F_PASSIVA_RIGA.ESERCIZIO		     = F_PASSIVA.ESERCIZIO			And
                         F_PASSIVA_RIGA.PG_FATTURA_PASSIVA     = F_PASSIVA.PG_FATTURA_PASSIVA
                  where ass.esercizio_fatt_pass is not null and ass.esercizio_fatt_att is null and ass.PG_DOCUMENTO_GENERICO is null
          union
              select
                            ASS.CD_CDS_FATT_ATT    	CD_CDS_FATT_PASS,
                            ASS.CD_UO_FATT_ATT      	CD_UO_FATT_PASS,
                            ASS.ESERCIZIO_FATT_ATT   	ESERCIZIO_FATT_PASS,
                            ASS.PG_FATTURA_ATTIVA     	PG_FATTURA_PASSIVA,
                            ASS.PROGRESSIVO_RIGA_FATT_ATT PROGRESSIVO_RIGA_FATT_PASS,
                              ass.PG_INVENTARIO ,
                              ass.NR_INVENTARIO ,
                              ass.PROGRESSIVO ,
                              ass.DS_BENE,
                              F_attiva.DS_FATTURA_ATTIVA DS_FATTURA,
                          F_attiva.CD_TERZO CD_TERZO,
                          Nvl(F_attiva.RAGIONE_SOCIALE,Nvl(f_attiva.cognome,' ')||' '||Nvl(f_attiva.nome,' ')) DS_TERZO,
                          Decode(f_attiva.ti_fattura,'F','Fattura Attiva','C','Nota Credito da Fatt.Att.','Nota Debito da Fatt.Att.') Tipo,
                              ass.PG_BUONO_C_S,
                              ass.TI_DOCUMENTO,
                              ass.ESERCIZIO,
                               Null tipo_generico
                               from
                              V_BUONO_CARICO_INV ass
                               left outer join FATTURA_ATTIVA_RIGA F_ATTIVA_RIGA
                                 on ass.cd_cds_fatt_att   		= F_ATTIVA_RIGA.CD_CDS   			And
                                 ass.cd_uo_fatt_att 		= F_ATTIVA_RIGA.CD_UNITA_ORGANIZZATIVA   	And
                                 ass.esercizio_fatt_att         	= F_ATTIVA_RIGA.ESERCIZIO			And
                                 ass.pg_fattura_attiva           = F_ATTIVA_RIGA.pg_fattura_attiva		And
                                 ASS.PROGRESSIVO_RIGA_FATT_PASS   = F_ATTIVA_RIGA.PROGRESSIVO_RIGA
                             left outer join  FATTURA_ATTIVA F_ATTIVA
                                 on F_ATTIVA_RIGA.CD_CDS                 = F_ATTIVA.CD_CDS   			And
                                 F_ATTIVA_RIGA.CD_UNITA_ORGANIZZATIVA = F_ATTIVA.CD_UNITA_ORGANIZZATIVA   	And
                                 F_ATTIVA_RIGA.ESERCIZIO		     = F_ATTIVA.ESERCIZIO			And
                                 F_ATTIVA_RIGA.PG_FATTURA_ATTIVA     = F_ATTIVA.PG_FATTURA_ATTIVA
                              where ass.esercizio_fatt_pass is  null and ass.esercizio_fatt_att is not null and ass.PG_DOCUMENTO_GENERICO is null
          union
              select
                            ASS.CD_CDS_FATT_ATT    	CD_CDS_FATT_PASS,
                            ASS.CD_UO_FATT_ATT      	CD_UO_FATT_PASS,
                            ASS.ESERCIZIO_FATT_ATT   	ESERCIZIO_FATT_PASS,
                            ASS.PG_FATTURA_ATTIVA     	PG_FATTURA_PASSIVA,
                            ASS.PROGRESSIVO_RIGA_FATT_ATT PROGRESSIVO_RIGA_FATT_PASS,
                              ass.PG_INVENTARIO ,
                              ass.NR_INVENTARIO ,
                              ass.PROGRESSIVO ,
                              ass.DS_BENE,
                              GEN_RIGA.DS_RIGA DS_FATTURA,
                        GEN_RIGA.CD_TERZO CD_TERZO,
                        Nvl(GEN_RIGA.RAGIONE_SOCIALE,Nvl(GEN_RIGA.cognome,' ')||' '||Nvl(GEN_RIGA.nome,' ')) DS_TERZO,
                        Decode(GEN_RIGA.CD_TIPO_DOCUMENTO_AMM,'GENERICO_E','Generico Entrata','GENERICO_S','Generico Spesa','Generico') tipo,
                              ass.PG_BUONO_C_S,
                              ass.TI_DOCUMENTO,
                              ass.ESERCIZIO,
                               GEN_RIGA.CD_TIPO_DOCUMENTO_AMM tipo_generico
                               from
                              V_BUONO_CARICO_INV ass
                               left outer join DOCUMENTO_GENERICO_RIGA GEN_RIGA
                               on             ASS.CD_CDS_DOC_GEN   			= GEN_RIGA.CD_CDS   			And
                                 ASS.CD_UO_DOC_GEN   			= GEN_RIGA.CD_UNITA_ORGANIZZATIVA   	And
                                 ASS.ESERCIZIO_DOC_GEN         		= GEN_RIGA.ESERCIZIO			And
                                 ASS.CD_TIPO_DOCUMENTO_AMM 		= GEN_RIGA.CD_TIPO_DOCUMENTO_AMM	AND
                                 ASS.PG_DOCUMENTO_GENERICO        	= GEN_RIGA.PG_DOCUMENTO_GENERICO	And
                                 ASS.PROGRESSIVO_RIGA_DOC_GEN     	= GEN_RIGA.PROGRESSIVO_RIGA
                              where ass.esercizio_fatt_pass is  null and ass.esercizio_fatt_att is  null and ass.PG_DOCUMENTO_GENERICO is not null;
