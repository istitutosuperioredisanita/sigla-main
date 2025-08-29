--------------------------------------------------------
--  DDL for View V_FATT_ORDINE_DET
--------------------------------------------------------
 CREATE OR REPLACE  VIEW "V_FATT_ORDINE_DET" ("CD_CDS", "CD_UNITA_ORGANIZZATIVA", "ESERCIZIO", "PG_FATTURA_PASSIVA", "PROGRESSIVO_RIGA", "CD_CDS_ORDINE", "UOP_ORDINE", "ESERCIZIO_ORDINE", "CD_NUM_ORDINE", "NUM_ORDINE", "RIGA_ORDINE", "CONS_ORDINE", "CDS_EVASIONE", "MAG_EVAZIONE", "ESE_EVASIONE", "NUMMAG_EVASIONE", "NUM_EVASIONE", "RIGA_EVASIONE", "DATA_BOLLA", "NUMERO_BOLLA", "ID_TRANSITO", "TI_DOCUMENTO", "ESER_BUONO_CAR_SCA", "PG_BUONO_C_S", "PG_INVENTARIO", "NR_INVENTARIO", "PROGRESSIVO", "PG_RIGA_ASS") AS
  (
  select fo.cd_cds,fo.cd_unita_organizzativa,fo.esercizio,fo.pg_fattura_passiva,fo.progressivo_riga,
fo.cd_cds_ordine,
fo.CD_UNITA_OPERATIVA uop_ordine,
fo.ESERCIZIO_ORDINE,
fo.CD_NUMERATORE cd_num_ordine,
fo.numero num_ordine,
fo.riga riga_ordine,
fo.consegna cons_ordine,
e.CD_CDS cds_evasione,
e.CD_MAGAZZINO mag_evazione,
e.ESERCIZIO ese_evasione,
e.CD_NUMERATORE_MAG numMag_evasione,
e.numero num_evasione,
er.riga riga_evasione,
e.data_bolla,
e.numero_bolla,
t.id id_transito,
b.TI_DOCUMENTO,
b.ESERCIZIO eser_buono_car_sca,
b.PG_BUONO_C_S,
i.pg_inventario,i.nr_inventario,i.progressivo,fi.pg_riga_ass
from fattura_ordine fo
left outer join evasione_ordine_riga er
on fo.esercizio_ordine=er.esercizio_ordine
and fo.cd_cds_ordine=er.cd_cds_ordine
and fo.cd_numeratore=er.cd_numeratore_ordine
and fo.cd_unita_operativa=er.cd_unita_operativa
and  fo.numero=er.numero_ordine
and fo.riga=er.riga_ordine
and fo.consegna=er.consegna
and er.stato='INS'
left outer join evasione_ordine e
on e.CD_CDS=er.CD_CDS
and e.CD_MAGAZZINO=er.CD_MAGAZZINO
and e.ESERCIZIO=er.ESERCIZIO
and e.CD_NUMERATORE_MAG=er.CD_NUMERATORE_MAG
and e.NUMERO=er.NUMERO
and e.stato='INS'
left outer join transito_beni_ordini t on
    t.ID_MOVIMENTI_MAG=er.ID_MOVIMENTI_MAG
left outer join inventario_beni i
    on i.id_transito_beni_ordini=t.id
left outer join BUONO_CARICO_SCARICO_DETT d
on d.pg_inventario=i.pg_inventario
and d.nr_inventario=i.nr_inventario
and d.progressivo=i.progressivo
left outer join BUONO_CARICO_SCARICO b
on b.pg_inventario=d.pg_inventario
and b.TI_DOCUMENTO=d.TI_DOCUMENTO
and b.ESERCIZIO=d.ESERCIZIO
and b.PG_BUONO_C_S=d.PG_BUONO_C_S
left outer join TIPO_CARICO_SCARICO ts
on ts.CD_TIPO_CARICO_SCARICO=b.CD_TIPO_CARICO_SCARICO
and ts.FL_DA_ORDINI='Y'
left outer join
(select cd_cds_fatt_pass,cd_uo_fatt_pass, esercizio_fatt_pass,pg_fattura_passiva,progressivo_riga_fatt_pass,pg_inventario,nr_inventario,progressivo,ti_documento,PG_BUONO_C_S,max(pg_riga) pg_riga_ass from ass_inv_bene_fattura
where pg_fattura_passiva is not null
group by cd_cds_fatt_pass,cd_uo_fatt_pass, esercizio_fatt_pass,pg_fattura_passiva,progressivo_riga_fatt_pass,pg_inventario,nr_inventario,progressivo,ti_documento,PG_BUONO_C_S) fi
on fi.cd_cds_fatt_pass=fo.cd_cds
and fi.cd_uo_fatt_pass=fo.cd_unita_organizzativa
and fi.esercizio_fatt_pass=fo.esercizio
and fi.pg_fattura_passiva=fo.pg_fattura_passiva
and fi.progressivo_riga_fatt_pass=fo.progressivo_riga
and fi.pg_inventario=i.pg_inventario
and fi.nr_inventario=i.nr_inventario
and fi.progressivo=i.progressivo
and fi.ti_documento=b.ti_documento
and fi.PG_BUONO_C_S=b.PG_BUONO_C_S);
