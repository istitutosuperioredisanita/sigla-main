
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_DETTAGLIO_LOTTI_MAG" ("TIPO_RECORD", "ESERCIZIO", "CD_CDS", "CD_MAGAZZINO", "CD_NUMERATORE_MAG", "PG_LOTTO", "CD_CDS_MAG", "CD_MAGAZZINO_MAG", "DS_MAGAZZINO", "CD_BENE_SERVIZIO", "DS_BENE_SERVIZIO", "CD_CATEGORIA_PADRE", "CD_PROPRIO", "UNITA_MISURA", "GIACENZA_ATTUALE", "QUANTITA_CARICO", "DT_CARICO", "QUANTITA_SCARICO", "DT_SCARICO", "QUANTITA_APERTURA", "DT_MOVIMENTO_CHIUSURA", "ANNO_RIFERIMENTO_MOVIMENTO") AS
  select
l.tipo_Record, l.esercizio,l.cd_cds,l.CD_MAGAZZINO,l.CD_NUMERATORE_MAG,
l.PG_LOTTO,l.CD_CDS_MAG,l.CD_MAGAZZINO_MAG,m.ds_magazzino,
b.cd_bene_servizio,b.ds_bene_servizio,g.cd_categoria_padre,g.cd_proprio,b.unita_misura,l.giacenza_attuale,
l.quantita_carico, l.dt_carico, l.quantita_Scarico, l.dt_scarico,
l.quantita_apertura,l.DT_MOVIMENTO_CHIUSURA,l.anno_riferimento_movimento
from (
select 'LOTTI' tipo_Record,l.esercizio,l.CD_CDS,l.cd_magazzino,l.PG_LOTTO,l.CD_CDS_MAG,
l.CD_MAGAZZINO_MAG,l.cd_bene_servizio,l.CD_NUMERATORE_MAG,l.giacenza giacenza_attuale,
0 quantita_carico, null dt_carico,  0 quantita_Scarico, null dt_scarico,
0 quantita_apertura,null DT_MOVIMENTO_CHIUSURA,null anno_riferimento_movimento
from lotto_mag l
union all
select  'CARICHI' tipo_Record,l.esercizio, l.CD_CDS,l.cd_magazzino,l.PG_LOTTO,l.CD_CDS_MAG,l.CD_MAGAZZINO_MAG,l.cd_bene_servizio,l.CD_NUMERATORE_MAG,0 giacenza_attuale,(mc.coeff_conv*quantita) quantita_Carico,
mc.dt_riferimento dt_carico,  0 quantita_Scarico, null dt_scarico,0 quantita_apertura,null DT_MOVIMENTO_CHIUSURA,to_char ( mc.dt_riferimento,'yyyy') anno_riferimento_movimento
from lotto_mag l inner join
    movimenti_mag mc  on
    mc.CD_CDS_LOTTO=l.CD_CDS
    and mc.CD_MAGAZZINO_LOTTO=l.CD_MAGAZZINO
    and mc.ESERCIZIO_LOTTO=l.ESERCIZIO
    and mc.CD_NUMERATORE_LOTTO=l.CD_NUMERATORE_MAG
    and mc.PG_LOTTO=l.PG_LOTTO
    inner join tipo_movimento_mag tmc
    on mc.cd_tipo_movimento=tmc.cd_tipo_movimento
and cd_cds_tipo_movimento=tmc.cd_cds
where mc.stato!='ANN'
and tmc.tipo in ( 'CM','CA')
and NOT EXISTS
( select msc.pg_movimento from movimenti_mag msc
where msc.pg_movimento_rif=mc.pg_movimento
and msc.cd_tipo_movimento=tmc.cd_tipo_movimento_rif)
union all
select 'SCARICHI' tipo_Record,l.esercizio, l.CD_CDS,l.cd_magazzino,l.PG_LOTTO,l.CD_CDS_MAG,l.CD_MAGAZZINO_MAG,l.cd_bene_servizio,l.CD_NUMERATORE_MAG,0 giacenza_attuale,0 quantita_Carico,
null dt_carico,  (mc.coeff_conv*quantita) quantita_Scarico, mc.dt_riferimento,0 quantita_apertura,null DT_MOVIMENTO_CHIUSURA,to_char ( mc.dt_riferimento,'yyyy') anno_riferimento_movimento
from lotto_mag l inner join
    movimenti_mag mc  on
    mc.CD_CDS_LOTTO=l.CD_CDS
    and mc.CD_MAGAZZINO_LOTTO=l.CD_MAGAZZINO
    and mc.ESERCIZIO_LOTTO=l.ESERCIZIO
    and mc.CD_NUMERATORE_LOTTO=l.CD_NUMERATORE_MAG
    and mc.PG_LOTTO=l.PG_LOTTO
    inner join tipo_movimento_mag tmc
    on mc.cd_tipo_movimento=tmc.cd_tipo_movimento
and cd_cds_tipo_movimento=tmc.cd_cds
where mc.stato!='ANN'
and tmc.tipo in ( 'SA','SM')
and NOT EXISTS
( select msc.pg_movimento from movimenti_mag msc
where msc.pg_movimento_rif=mc.pg_movimento
and msc.cd_tipo_movimento=tmc.cd_tipo_movimento_rif)
union all
select 'CHIUSURA' tipo_Record,l.esercizio, l.CD_CDS,l.cd_magazzino,l.PG_LOTTO,l.CD_CDS_MAG,l.CD_MAGAZZINO_MAG,l.cd_bene_servizio,l.CD_NUMERATORE_MAG,0 giacenza_attuale,0 quantita_Carico,
null dt_carico,  0 quantita_Scarico, null  dt_scarico,mc.quantita quantita_apertura,mc.dt_riferimento DT_MOVIMENTO_CHIUSURA ,to_char ( mc.dt_riferimento,'yyyy') anno_riferimento_movimento
from lotto_mag l 
    inner join  movimenti_mag mc 
    on mc.CD_CDS_LOTTO=l.CD_CDS
    and mc.CD_MAGAZZINO_LOTTO=l.CD_MAGAZZINO
    and mc.ESERCIZIO_LOTTO=l.ESERCIZIO
    and mc.CD_NUMERATORE_LOTTO=l.CD_NUMERATORE_MAG
    and mc.PG_LOTTO=l.PG_LOTTO
   inner join tipo_movimento_mag tmc
    on mc.cd_tipo_movimento=tmc.cd_tipo_movimento
    and cd_cds_tipo_movimento=tmc.cd_cds
    and tmc.tipo='CH'
    where mc.stato!='ANN' ) l
inner join magazzino m on l.cd_magazzino_mag=m.cd_magazzino and l.cd_cds_mag=m.cd_cds
    inner join bene_servizio b on b.cd_bene_servizio=l.cd_bene_servizio
    inner join categoria_gruppo_invent g on b.cd_categoria_gruppo=g.cd_categoria_gruppo
    inner join raggr_magazzino r on r.CD_CDS=m.CD_CDS_RAGGR_RIM and r.CD_RAGGR_MAGAZZINO=m.CD_RAGGR_MAGAZZINO_RIM
    where r.tipo='RIM';
