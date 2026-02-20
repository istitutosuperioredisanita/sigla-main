--------------------------------------------------------
--  DDL for Package Body CHECK_CONGRUENZA_DATI
--------------------------------------------------------

CREATE OR REPLACE PACKAGE BODY "CHECK_CONGRUENZA_DATI" As
 -- ==================================================================================================
 -- Inserimento di un record in ACCONTO_CLASSIFIC_CORI_ALTRI
 -- ==================================================================================================
 Procedure job_check_congruenza_dati(job number, pg_exec number, next_date date) is
     contaanomalie NUMBER := 0;
     aTSNow date;
     aEndT date;
     aStartT date;
     aStart varchar2(80);
     aEnd varchar2(80);
     aDelta varchar2(80);
     aUser varchar2(20);
 BEGIN
    aStartT:=sysdate;
    aStart:=to_char(sysdate,'YYYYMMDD HH:MI:SS');
    aUser:=IBMUTL200.getUserFromLog(pg_exec);
    IBMUTL210.logStartExecutionUpd(pg_exec, TIPO_LOG_CONGR_DATI, job, 'Batch di congruenza Dati. Start:'||to_char(aTSNow,'YYYY/MM/DD HH-MI-SS'),'Controllo Congruenza Dati');
    IBMUTL200.logInf(pg_exec,'CHECK CONGRUENZA DATI START  at: '||aStart,'','');

    FOR rec IN (SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_OBBLIGAZIONE,
   						SUM(IM_OBBLIGAZIONE) IM_OBBLIGAZIONE, SUM(TOT_SCADENZE) TOT_SCADENZE, SUM(TOT_SCADVOCE) TOT_SCADVOCE FROM (
   					SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_OBBLIGAZIONE, IM_OBBLIGAZIONE, 0 TOT_SCADENZE, 0 TOT_SCADVOCE FROM OBBLIGAZIONE o
   					UNION ALL
   					SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_OBBLIGAZIONE, 0, os.IM_SCADENZA, 0 FROM OBBLIGAZIONE_SCADENZARIO os
   					UNION ALL
   					SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_OBBLIGAZIONE, 0, 0, osv.IM_VOCE FROM OBBLIGAZIONE_SCAD_VOCE osv)
   					GROUP BY CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_OBBLIGAZIONE
   					HAVING SUM(IM_OBBLIGAZIONE) != SUM(TOT_SCADENZE) OR SUM(IM_OBBLIGAZIONE) != SUM(TOT_SCADVOCE)) LOOP
   		IF rec.IM_OBBLIGAZIONE != rec.TOT_SCADENZE THEN
   			contaanomalie := contaanomalie + 1;
             IBMUTL200.logInf(pg_exec, 'Obbligazione '||rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_OBBLIGAZIONE||
   					' di importo '||ltrim(rtrim(to_char(rec.IM_OBBLIGAZIONE,'999g999g999g999g990d00')))||
   					' non coincidente con il totale scadenze '||
   					ltrim(rtrim(to_char(rec.TOT_SCADENZE,'999g999g999g999g990d00')))||'.', '' ,'');
        ELSE
   			FOR rec1 IN (SELECT PG_OBBLIGAZIONE_SCADENZARIO, SUM(IM_SCADENZA) IM_SCADENZA, SUM(TOT_SCADVOCE) TOT_SCADVOCE FROM (
   							SELECT PG_OBBLIGAZIONE_SCADENZARIO, os.IM_SCADENZA, 0 TOT_SCADVOCE FROM OBBLIGAZIONE_SCADENZARIO os
   							WHERE CD_CDS = rec.CD_CDS
   							AND   ESERCIZIO = rec.ESERCIZIO
   							AND   ESERCIZIO_ORIGINALE = rec.ESERCIZIO_ORIGINALE
   							AND   PG_OBBLIGAZIONE = rec.PG_OBBLIGAZIONE
   							UNION ALL
   							SELECT PG_OBBLIGAZIONE_SCADENZARIO, 0, osv.IM_VOCE FROM OBBLIGAZIONE_SCAD_VOCE osv
   							WHERE CD_CDS = rec.CD_CDS
   							AND   ESERCIZIO = rec.ESERCIZIO
   							AND   ESERCIZIO_ORIGINALE = rec.ESERCIZIO_ORIGINALE
   							AND   PG_OBBLIGAZIONE = rec.PG_OBBLIGAZIONE)
   							GROUP BY PG_OBBLIGAZIONE_SCADENZARIO
   							HAVING SUM(IM_SCADENZA) != SUM(TOT_SCADVOCE)) LOOP
   				contaanomalie := contaanomalie + 1;
                 IBMUTL200.logInf(pg_exec, 'Scadenza '||rec1.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
   						rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_OBBLIGAZIONE||
   						' di importo '||ltrim(rtrim(to_char(rec1.IM_SCADENZA,'999g999g999g999g990d00')))||
   						' non coincidente con il totale ripartito per voce '||
   						ltrim(rtrim(to_char(rec1.TOT_SCADVOCE,'999g999g999g999g990d00')))||'.', '' ,'');
            END LOOP;
        END IF;
    END LOOP;


    FOR rec IN (SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_ACCERTAMENTO,
   						SUM(IM_ACCERTAMENTO) IM_ACCERTAMENTO, SUM(TOT_SCADENZE) TOT_SCADENZE, SUM(TOT_SCADVOCE) TOT_SCADVOCE FROM (
   					SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_ACCERTAMENTO, IM_ACCERTAMENTO, 0 TOT_SCADENZE, 0 TOT_SCADVOCE FROM ACCERTAMENTO o
   					UNION ALL
   					SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_ACCERTAMENTO, 0, os.IM_SCADENZA, 0 FROM ACCERTAMENTO_SCADENZARIO os
   					UNION ALL
   					SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_ACCERTAMENTO, 0, 0, osv.IM_VOCE FROM ACCERTAMENTO_SCAD_VOCE osv)
   					GROUP BY CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_ACCERTAMENTO
   					HAVING SUM(IM_ACCERTAMENTO) != SUM(TOT_SCADENZE) OR SUM(IM_ACCERTAMENTO) != SUM(TOT_SCADVOCE)) LOOP
   		IF rec.IM_ACCERTAMENTO != rec.TOT_SCADENZE THEN
   			contaanomalie := contaanomalie + 1;
             IBMUTL200.logInf(pg_exec, 'Accertamento '||rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_ACCERTAMENTO||
   					' di importo '||ltrim(rtrim(to_char(rec.IM_ACCERTAMENTO,'999g999g999g999g990d00')))||
   					' non coincidente con il totale scadenze '||
   					ltrim(rtrim(to_char(rec.TOT_SCADENZE,'999g999g999g999g990d00')))||'.', '' ,'');

        ELSE
   			FOR rec1 IN (SELECT PG_ACCERTAMENTO_SCADENZARIO, SUM(IM_SCADENZA) IM_SCADENZA, SUM(TOT_SCADVOCE) TOT_SCADVOCE FROM (
   							SELECT PG_ACCERTAMENTO_SCADENZARIO, os.IM_SCADENZA, 0 TOT_SCADVOCE FROM ACCERTAMENTO_SCADENZARIO os
   							WHERE CD_CDS = rec.CD_CDS
   							AND   ESERCIZIO = rec.ESERCIZIO
   							AND   ESERCIZIO_ORIGINALE = rec.ESERCIZIO_ORIGINALE
   							AND   PG_ACCERTAMENTO = rec.PG_ACCERTAMENTO
   							UNION ALL
   							SELECT PG_ACCERTAMENTO_SCADENZARIO, 0, osv.IM_VOCE FROM ACCERTAMENTO_SCAD_VOCE osv
   							WHERE CD_CDS = rec.CD_CDS
   							AND   ESERCIZIO = rec.ESERCIZIO
   							AND   ESERCIZIO_ORIGINALE = rec.ESERCIZIO_ORIGINALE
   							AND   PG_ACCERTAMENTO = rec.PG_ACCERTAMENTO)
   							GROUP BY PG_ACCERTAMENTO_SCADENZARIO
   							HAVING SUM(IM_SCADENZA) != SUM(TOT_SCADVOCE)) LOOP
   				contaanomalie := contaanomalie + 1;
                 IBMUTL200.logInf(pg_exec, 'Scadenza '||rec1.PG_ACCERTAMENTO_SCADENZARIO||' dell''accertamento '||
   						rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_ACCERTAMENTO||
   						' di importo '||ltrim(rtrim(to_char(rec1.IM_SCADENZA,'999g999g999g999g990d00')))||
   						' non coincidente con il totale ripartito per voce '||
   						ltrim(rtrim(to_char(rec1.TOT_SCADVOCE,'999g999g999g999g990d00')))||'.', '' ,'');
            END LOOP;
        END IF;
    END LOOP;

    FOR rec IN (SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_OBBLIGAZIONE, PG_OBBLIGAZIONE_SCADENZARIO, IM_SCADENZA,
   					   IM_ASSOCIATO_DOC_AMM, IM_ASSOCIATO_DOC_CONTABILE
   				FROM OBBLIGAZIONE_SCADENZARIO
   				WHERE (IM_ASSOCIATO_DOC_AMM != 0 AND IM_SCADENZA != IM_ASSOCIATO_DOC_AMM)
   				OR (IM_ASSOCIATO_DOC_CONTABILE != 0 AND IM_SCADENZA != IM_ASSOCIATO_DOC_CONTABILE)) LOOP
   		contaanomalie := contaanomalie + 1;
   		IF (rec.IM_ASSOCIATO_DOC_AMM != 0 AND rec.IM_SCADENZA != rec.IM_ASSOCIATO_DOC_AMM) THEN
          IBMUTL200.logInf(pg_exec, 'Scadenza '||rec.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
   					rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_OBBLIGAZIONE||
   					' di importo '||ltrim(rtrim(to_char(rec.IM_SCADENZA,'999g999g999g999g990d00')))||
   					' non coincidente con il totale documenti amministrativi associati '||
   					ltrim(rtrim(to_char(rec.IM_ASSOCIATO_DOC_AMM,'999g999g999g999g990d00')))||'.', '' ,'');

   		ELSIF (rec.IM_ASSOCIATO_DOC_CONTABILE != 0) THEN
          IBMUTL200.logInf(pg_exec, 'Scadenza '||rec.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
   					rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_OBBLIGAZIONE||
   					' di importo '||ltrim(rtrim(to_char(rec.IM_SCADENZA,'999g999g999g999g990d00')))||
   					' non coincidente con il totale documenti contabili associati '||
   					ltrim(rtrim(to_char(rec.IM_ASSOCIATO_DOC_CONTABILE,'999g999g999g999g990d00')))||'.', '' ,'');
        END IF;
    END LOOP;

    FOR rec IN (SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_ACCERTAMENTO, PG_ACCERTAMENTO_SCADENZARIO, IM_SCADENZA,
   					   IM_ASSOCIATO_DOC_AMM, IM_ASSOCIATO_DOC_CONTABILE
   				FROM ACCERTAMENTO_SCADENZARIO
   				WHERE (IM_ASSOCIATO_DOC_AMM != 0 AND IM_SCADENZA != IM_ASSOCIATO_DOC_AMM)
   				OR (IM_ASSOCIATO_DOC_CONTABILE != 0 AND IM_SCADENZA != IM_ASSOCIATO_DOC_CONTABILE)) LOOP
   		contaanomalie := contaanomalie + 1;
   		IF (rec.IM_ASSOCIATO_DOC_AMM != 0 AND rec.IM_SCADENZA != rec.IM_ASSOCIATO_DOC_AMM) THEN
         IBMUTL200.logInf(pg_exec, 'Scadenza '||rec.PG_ACCERTAMENTO_SCADENZARIO||' dell''accertamento '||
   					rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_ACCERTAMENTO||
   					' di importo '||ltrim(rtrim(to_char(rec.IM_SCADENZA,'999g999g999g999g990d00')))||
   					' non coincidente con il totale documenti amministrativi associati '||
   					ltrim(rtrim(to_char(rec.IM_ASSOCIATO_DOC_AMM,'999g999g999g999g990d00')))||'.', '' ,'');

   		ELSIF (rec.IM_ASSOCIATO_DOC_CONTABILE != 0) THEN
         IBMUTL200.logInf(pg_exec, 'Scadenza '||rec.PG_ACCERTAMENTO_SCADENZARIO||' dell''accertamento '||
   					rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_ACCERTAMENTO||
   					' di importo '||ltrim(rtrim(to_char(rec.IM_SCADENZA,'999g999g999g999g990d00')))||
   					' non coincidente con il totale documenti contabili associati '||
   					ltrim(rtrim(to_char(rec.IM_ASSOCIATO_DOC_CONTABILE,'999g999g999g999g990d00')))||'.', '' ,'');
        END IF;
    END LOOP;

    FOR rec IN (SELECT * FROM V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO vcdos
   				WHERE TOT_IMPEGNO_ASSOCIATO_DOCAMM_CALCOLATO != TOT_IMPEGNO_ASSOCIATO_DOCAMM) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Scadenza '||rec.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
   				rec.CD_CDS_OBBLIGAZIONE||'/'||rec.ESERCIZIO_OBBLIGAZIONE||'/'||rec.ESERCIZIO_ORI_OBBLIGAZIONE||'/'||rec.PG_OBBLIGAZIONE||
   				'. L''importo associato di documenti amministrativi ('||ltrim(rtrim(to_char(rec.TOT_IMPEGNO_ASSOCIATO_DOCAMM,'999g999g999g999g990d00')))||
   				') non coincide con il valore calcolato ('||ltrim(rtrim(to_char(rec.TOT_IMPEGNO_ASSOCIATO_DOCAMM_CALCOLATO,'999g999g999g999g990d00')))||').', '' ,'');
    END LOOP;

    FOR rec IN (SELECT * FROM V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO vcdos
   				WHERE TOT_IMPEGNO_ASSOCIATO_DOCCONT_CALCOLATO != TOT_IMPEGNO_ASSOCIATO_DOCCONT) LOOP
   		contaanomalie := contaanomalie + 1;
          IBMUTL200.logInf(pg_exec, 'Scadenza '||rec.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
   				rec.CD_CDS_OBBLIGAZIONE||'/'||rec.ESERCIZIO_OBBLIGAZIONE||'/'||rec.ESERCIZIO_ORI_OBBLIGAZIONE||'/'||rec.PG_OBBLIGAZIONE||
   				'. L''importo associato di documenti contabili ('||ltrim(rtrim(to_char(rec.TOT_IMPEGNO_ASSOCIATO_DOCCONT,'999g999g999g999g990d00')))||
   				') non coincide con il valore calcolato ('||ltrim(rtrim(to_char(rec.TOT_IMPEGNO_ASSOCIATO_DOCCONT_CALCOLATO,'999g999g999g999g990d00')))||').', '' ,'');

    END LOOP;

    FOR rec IN (SELECT * FROM V_CONTROLLO_DISPACC_ACCERTAMENTO_SCADENZARIO vcdas
   				WHERE TOT_ACCERTAMENTO_ASSOCIATO_CALCOLATO != TOT_ACCERTAMENTO_ASSOCIATO) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Scadenza '||rec.PG_ACCERTAMENTO_SCADENZARIO||' dell''accertamento '||
   				rec.CD_CDS_ACCERTAMENTO||'/'||rec.ESERCIZIO_ACCERTAMENTO||'/'||rec.ESERCIZIO_ORI_ACCERTAMENTO||'/'||rec.PG_ACCERTAMENTO||
   				'. L''importo associato di documenti amministrativi ('||ltrim(rtrim(to_char(rec.TOT_ACCERTAMENTO_ASSOCIATO,'999g999g999g999g990d00')))||
   				') non coincide con il valore calcolato ('||ltrim(rtrim(to_char(rec.TOT_ACCERTAMENTO_ASSOCIATO_CALCOLATO,'999g999g999g999g990d00')))||').', '' ,'');


    END LOOP;

    FOR rec IN (SELECT DISTINCT fpr.ESERCIZIO, fpr.CD_CDS, fpr.CD_UNITA_ORGANIZZATIVA, fpr.PG_FATTURA_PASSIVA,
   								fpr.ESERCIZIO_OBBLIGAZIONE, fpr.ESERCIZIO_ORI_OBBLIGAZIONE, fpr.CD_CDS_OBBLIGAZIONE, fpr.PG_OBBLIGAZIONE,
   								o.CD_UNITA_ORGANIZZATIVA UO_OBBLIGAZIONE
   				FROM FATTURA_PASSIVA_RIGA fpr
   				LEFT JOIN OBBLIGAZIONE o ON o.ESERCIZIO = fpr.ESERCIZIO_OBBLIGAZIONE AND o.ESERCIZIO_ORIGINALE = fpr.ESERCIZIO_ORI_OBBLIGAZIONE AND o.CD_CDS = fpr.CD_CDS_OBBLIGAZIONE AND o.PG_OBBLIGAZIONE = fpr.PG_OBBLIGAZIONE
   				WHERE fpr.PG_OBBLIGAZIONE IS NOT NULL
   				AND o.CD_UNITA_ORGANIZZATIVA != fpr.CD_UNITA_ORGANIZZATIVA) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Fattura passiva '||rec.ESERCIZIO||'/'||rec.CD_CDS||'/'||rec.CD_UNITA_ORGANIZZATIVA||'/'||rec.PG_FATTURA_PASSIVA||
   			' della UO '||rec.CD_UNITA_ORGANIZZATIVA||' collegata all''impegno '||
   			rec.CD_CDS_OBBLIGAZIONE||'/'||rec.ESERCIZIO_OBBLIGAZIONE||'/'||rec.ESERCIZIO_ORI_OBBLIGAZIONE||'/'||rec.PG_OBBLIGAZIONE||
   				' appartenente ad altra UO '||rec.UO_OBBLIGAZIONE||'.', '' ,'');
    END LOOP;

    FOR rec IN (SELECT DISTINCT fpr.ESERCIZIO, fpr.CD_CDS, fpr.CD_UNITA_ORGANIZZATIVA, fpr.PG_FATTURA_ATTIVA,
   								fpr.ESERCIZIO_ACCERTAMENTO, fpr.ESERCIZIO_ORI_ACCERTAMENTO, fpr.CD_CDS_ACCERTAMENTO, fpr.PG_ACCERTAMENTO,
   								o.CD_UNITA_ORGANIZZATIVA UO_ACCERTAMENTO
   				FROM FATTURA_ATTIVA_RIGA fpr
   				LEFT JOIN ACCERTAMENTO o ON o.ESERCIZIO = fpr.ESERCIZIO_ACCERTAMENTO AND o.ESERCIZIO_ORIGINALE = fpr.ESERCIZIO_ORI_ACCERTAMENTO AND o.CD_CDS = fpr.CD_CDS_ACCERTAMENTO AND o.PG_ACCERTAMENTO = fpr.PG_ACCERTAMENTO
   				WHERE fpr.PG_ACCERTAMENTO IS NOT NULL
   				AND o.CD_UNITA_ORGANIZZATIVA != fpr.CD_UNITA_ORGANIZZATIVA) LOOP
   		contaanomalie := contaanomalie + 1;
          IBMUTL200.logInf(pg_exec, 'Fattura attiva '||rec.ESERCIZIO||'/'||rec.CD_CDS||'/'||rec.CD_UNITA_ORGANIZZATIVA||'/'||rec.PG_FATTURA_ATTIVA||
   			' della UO '||rec.CD_UNITA_ORGANIZZATIVA||' collegata all''accertamento '||
   			rec.CD_CDS_ACCERTAMENTO||'/'||rec.ESERCIZIO_ACCERTAMENTO||'/'||rec.ESERCIZIO_ORI_ACCERTAMENTO||'/'||rec.PG_ACCERTAMENTO||
   				' appartenente ad altra UO '||rec.UO_ACCERTAMENTO||'.', '' ,'');
    END LOOP;

    FOR rec IN (SELECT CD_CDS, CD_UNITA_OPERATIVA, ESERCIZIO, CD_NUMERATORE, NUMERO,
   						SUM(IM_IMPONIBILE) IM_IMPONIBILE, SUM(IM_IVA) IM_IVA,
   						SUM(TOT_IMPONIBILE_RIGA) TOT_IMPONIBILE_RIGA, SUM(TOT_IVA_RIGA) TOT_IVA_RIGA,
   						SUM(TOT_IMPONIBILE_CONSEGNA) TOT_IMPONIBILE_CONSEGNA, SUM(TOT_IVA_CONSEGNA) TOT_IVA_CONSEGNA FROM (
   					SELECT CD_CDS, CD_UNITA_OPERATIVA, ESERCIZIO, CD_NUMERATORE, NUMERO, IM_IMPONIBILE, IM_IVA, 0 TOT_IMPONIBILE_RIGA, 0 TOT_IVA_RIGA, 0 TOT_IMPONIBILE_CONSEGNA, 0 TOT_IVA_CONSEGNA FROM ORDINE_ACQ oa
   					UNION ALL
   					SELECT CD_CDS, CD_UNITA_OPERATIVA, ESERCIZIO, CD_NUMERATORE, NUMERO, 0, 0, oar.IM_IMPONIBILE, oar.IM_IVA, 0, 0 FROM ORDINE_ACQ_RIGA oar
   					UNION ALL
   					SELECT CD_CDS, CD_UNITA_OPERATIVA, ESERCIZIO, CD_NUMERATORE, NUMERO, 0, 0, 0, 0, oac.IM_IMPONIBILE, oac.IM_IVA FROM ORDINE_ACQ_CONSEGNA oac)
   					GROUP BY CD_CDS, CD_UNITA_OPERATIVA, ESERCIZIO, CD_NUMERATORE, NUMERO
   					HAVING SUM(IM_IMPONIBILE) != SUM(TOT_IMPONIBILE_RIGA) OR SUM(IM_IVA) != SUM(TOT_IVA_RIGA) OR
   						   SUM(IM_IMPONIBILE) != SUM(TOT_IMPONIBILE_CONSEGNA) OR SUM(IM_IVA) != SUM(TOT_IVA_CONSEGNA)) LOOP
   		IF rec.IM_IMPONIBILE != rec.TOT_IMPONIBILE_RIGA OR rec.IM_IVA != rec.TOT_IVA_RIGA THEN
   			contaanomalie := contaanomalie + 1;
             IBMUTL200.logInf(pg_exec, 'Ordine '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||
   					' di imponibile '||
   					ltrim(rtrim(to_char(rec.IM_IMPONIBILE,'999g999g999g999g990d00')))||
   					' e iva '||
   					ltrim(rtrim(to_char(rec.IM_IVA,'999g999g999g999g990d00')))||
   					' non coincidente con il totale riga ordine (imponibile: '||
   					ltrim(rtrim(to_char(rec.TOT_IMPONIBILE_RIGA,'999g999g999g999g990d00')))||
   					' - iva: '||
   					ltrim(rtrim(to_char(rec.TOT_IVA_RIGA,'999g999g999g999g990d00')))||'.', '' ,'');
        ELSE
   			FOR rec1 IN (SELECT RIGA, SUM(IM_IMPONIBILE) IM_IMPONIBILE, SUM(IM_IVA) IM_IVA,
   							SUM(TOT_IMPONIBILE_CONSEGNA) TOT_IMPONIBILE_CONSEGNA, SUM(TOT_IVA_CONSEGNA) TOT_IVA_CONSEGNA FROM (
   							SELECT RIGA, oar.IM_IMPONIBILE, oar.IM_IVA, 0 TOT_IMPONIBILE_CONSEGNA, 0 TOT_IVA_CONSEGNA FROM ORDINE_ACQ_RIGA oar
   							WHERE CD_CDS = rec.CD_CDS
   							AND   CD_UNITA_OPERATIVA = rec.CD_UNITA_OPERATIVA
   							AND   ESERCIZIO = rec.ESERCIZIO
   							AND   CD_NUMERATORE = rec.CD_NUMERATORE
   							AND   NUMERO = rec.NUMERO
   							UNION ALL
   							SELECT oac.RIGA, 0, 0, nvl(fo.IM_IMPONIBILE, oac.IM_IMPONIBILE), nvl(fo.IM_IVA, oac.IM_IVA) FROM ORDINE_ACQ_CONSEGNA oac
   							LEFT JOIN FATTURA_ORDINE fo ON fo.CD_CDS_ORDINE = oac.CD_CDS AND fo.CD_UNITA_OPERATIVA = oac.CD_UNITA_OPERATIVA AND
   							                         fo.ESERCIZIO_ORDINE = oac.ESERCIZIO AND fo.CD_NUMERATORE = oac.CD_NUMERATORE AND
   							                         fo.NUMERO = oac.NUMERO AND fo.RIGA = oac.RIGA AND fo.CONSEGNA = oac.CONSEGNA
   							WHERE oac.CD_CDS = rec.CD_CDS
   							AND   oac.CD_UNITA_OPERATIVA = rec.CD_UNITA_OPERATIVA
   							AND   oac.ESERCIZIO = rec.ESERCIZIO
   							AND   oac.CD_NUMERATORE = rec.CD_NUMERATORE
   							AND   oac.NUMERO = rec.NUMERO)
   							GROUP BY RIGA
   							HAVING SUM(IM_IMPONIBILE) != SUM(TOT_IMPONIBILE_CONSEGNA) OR SUM(IM_IVA) != SUM(TOT_IVA_CONSEGNA)) LOOP
   				contaanomalie := contaanomalie + 1;
                 IBMUTL200.logInf(pg_exec, 'Riga '||rec1.RIGA||' dell''ordine '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||
   						rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||
   						' di imponibile '||
   						ltrim(rtrim(to_char(rec1.IM_IMPONIBILE,'999g999g999g999g990d00')))||
   						' e iva '||
   						ltrim(rtrim(to_char(rec1.IM_IVA,'999g999g999g999g990d00')))||
   						' non coincidente con il totale riga consegne (imponibile: '||
   						ltrim(rtrim(to_char(rec1.TOT_IMPONIBILE_CONSEGNA,'999g999g999g999g990d00')))||
   						' - iva: '||
   						ltrim(rtrim(to_char(rec1.TOT_IVA_CONSEGNA,'999g999g999g999g990d00')))||').', '' ,'');
            END LOOP;
        END IF;
    END LOOP;

    FOR rec IN (SELECT * FROM OBBLIGAZIONE o WHERE CD_CDS != substr(CD_UNITA_ORGANIZZATIVA,1,3)) LOOP
   		contaanomalie := contaanomalie + 1;
          IBMUTL200.logInf(pg_exec, 'Obbligazione '||rec.CD_CDS||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.ESERCIZIO_ORIGINALE||'/'||
   		rec.PG_OBBLIGAZIONE||'. Il CDS ('||rec.CD_CDS||') non corrisponde alla UO ('||rec.cd_unita_organizzativa||').', '' ,'');
    END LOOP;

    FOR rec IN (SELECT * FROM ACCERTAMENTO a WHERE CD_CDS != substr(CD_UNITA_ORGANIZZATIVA,1,3)) LOOP
   		contaanomalie := contaanomalie + 1;
          IBMUTL200.logInf(pg_exec, 'Accertamento '||rec.CD_CDS||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.ESERCIZIO_ORIGINALE||'/'||
   		rec.PG_ACCERTAMENTO||'. Il CDS ('||rec.CD_CDS||') non corrisponde alla UO ('||rec.cd_unita_organizzativa||').', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND QUANTITA_EVASA_CALCOLATA != QUANTITA_EVASA_EVASIONE) LOOP
   		contaanomalie := contaanomalie + 1;
          IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. La quantità evasa ('||to_char(rec.QUANTITA_EVASA_EVASIONE,'FM999G999G999G990D999999')||
   		') registrata sulla riga di evasione associata ('||rec.CD_CDS_EVASIONE||'/'||rec.CD_MAGAZZINO_EVASIONE||'/'||rec.ESERCIZIO_EVASIONE||'/'||
   		rec.CD_NUMERATORE_EVASIONE||'/'||rec.NUMERO_EVASIONE||'/'||rec.RIGA_EVASIONE||
   		') non coincide con quella calcolata ('||to_char(rec.QUANTITA_EVASA_CALCOLATA,'FM999G999G999G990D999999')||').', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND PREZZO_UNITARIO_CARICO_MAGAZZINO_CALCOLATO != PREZZO_UNITARIO_CARICO_MAGAZZINO) LOOP
   		contaanomalie := contaanomalie + 1;
   		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Il prezzo unitario ('||rtrim(to_char(rec.PREZZO_UNITARIO_CARICO_MAGAZZINO,'FM999G999G999G990D999999'),',')||
   		') del carico del magazzino associato (pg: '||rec.PG_CARICO_MAGAZZINO||') non coincide con quello calcolato ('||
   		rtrim(to_char(rec.PREZZO_UNITARIO_CARICO_MAGAZZINO_CALCOLATO,'FM999G999G999G990D999999'),',')||').');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND TIPO_CONSEGNA = 'TRA'
                   AND PREZZO_UNITARIO_SCARICO_MAGAZZINO_CALCOLATO != PREZZO_UNITARIO_SCARICO_MAGAZZINO) LOOP
   		contaanomalie := contaanomalie + 1;
         --qui
          IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Il prezzo unitario ('||rtrim(to_char(rec.PREZZO_UNITARIO_SCARICO_MAGAZZINO,'FM999G999G999G990D999999'),',')||
   		') dello scarico del magazzino associato (pg: '||rec.PG_SCARICO_MAGAZZINO||') non coincide con quello calcolato ('||
   		rtrim(to_char(rec.PREZZO_UNITARIO_SCARICO_MAGAZZINO_CALCOLATO,'FM999G999G999G990D999999'),',')||').', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND QUANTITA_CARICO_LOTTO_CALCOLATO != QUANTITA_CARICO_LOTTO) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. La quantità di carico ('||rtrim(to_char(rec.QUANTITA_CARICO_LOTTO,'FM999G999G999G990D999999'),',')||
   		') del lotto associato ('||rec.CD_CDS_LOTTO||'/'||rec.CD_MAGAZZINO_LOTTO||'/'||rec.ESERCIZIO_LOTTO||'/'||rec.CD_NUMERATORE_LOTTO||'/'||rec.PG_LOTTO||
   		') non coincide con quella calcolata ('||rtrim(to_char(rec.QUANTITA_CARICO_LOTTO_CALCOLATO,'FM999G999G999G990D999999'),',')||').', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND GIACENZA_LOTTO_CALCOLATO != GIACENZA_LOTTO) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. La giacenza ('||rtrim(to_char(rec.GIACENZA_LOTTO,'FM999G999G999G990D999999'),',')||
   		') del lotto associato ('||rec.CD_CDS_LOTTO||'/'||rec.CD_MAGAZZINO_LOTTO||'/'||rec.ESERCIZIO_LOTTO||'/'||rec.CD_NUMERATORE_LOTTO||'/'||rec.PG_LOTTO||
   		') non coincide con quella calcolata ('||rtrim(to_char(rec.GIACENZA_LOTTO_CALCOLATO,'FM999G999G999G990D999999'),',')||').', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND VALORE_UNITARIO_LOTTO != VALORE_UNITARIO_LOTTO_CALCOLATO
                   AND (STATO_CARICO_MAGAZZINO != 'ANN' OR GIACENZA_LOTTO != 0)) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Il valore unitario ('||rtrim(to_char(rec.VALORE_UNITARIO_LOTTO,'FM999G999G999G990D999999'),',')||
   		') del lotto associato ('||rec.CD_CDS_LOTTO||'/'||rec.CD_MAGAZZINO_LOTTO||'/'||rec.ESERCIZIO_LOTTO||'/'||rec.CD_NUMERATORE_LOTTO||'/'||rec.PG_LOTTO||
   		') non coincide con quello calcolato ('||rtrim(to_char(rec.VALORE_UNITARIO_LOTTO_CALCOLATO,'FM999G999G999G990D999999'),',')||').', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND CD_BENE_SERVIZIO_ORDINE != CD_BENE_SERVIZIO_LOTTO) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Il codice bene ('||rec.CD_BENE_SERVIZIO_ORDINE||
   		') del lotto associato ('||rec.CD_CDS_LOTTO||'/'||rec.CD_MAGAZZINO_LOTTO||'/'||rec.ESERCIZIO_LOTTO||'/'||rec.CD_NUMERATORE_LOTTO||'/'||rec.PG_LOTTO||
   		') non coincide con il codice bene ('||rec.CD_BENE_SERVIZIO_ORDINE||') associato all''ordine.', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND PG_CARICO_MAGAZZINO IS NULL) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Non risulta essere associato il movimento di carico magazzino.', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND ((TIPO_CONSEGNA in ( 'TRA','FMA') AND PG_SCARICO_MAGAZZINO IS NULL) OR
                        (TIPO_CONSEGNA  not in ( 'TRA','FMA') AND PG_SCARICO_MAGAZZINO IS NOT NULL))) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Valorizzazione movimento di scarico magazzino non coerente con il tipo di consegna.', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND PG_LOTTO IS NULL) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Non risulta essere associato alcun lotto.', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                   WHERE STATO_CONSEGNA='EVA'
                   AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                            AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                   AND ((STATO_FATTURA_CONSEGNA = 'ASS' AND PG_FATTURA_PASSIVA IS NULL) OR
                        (STATO_FATTURA_CONSEGNA != 'ASS' AND PG_FATTURA_PASSIVA IS NOT NULL))) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Valorizzazione movimento di fattura non coerente con il tipo di associazione indicato sulla consegna.', '' ,'');
    END LOOP;

    FOR REC IN (SELECT * FROM MOVIMENTI_MAG mm
                   WHERE CD_TIPO_MOVIMENTO IN ('C19','C20')
                   AND PREZZO_UNITARIO != 0
                   AND (CD_CDS_LOTTO, CD_MAGAZZINO_LOTTO, ESERCIZIO_LOTTO, CD_NUMERATORE_LOTTO, PG_LOTTO) IN
                       (SELECT CD_CDS_LOTTO, CD_MAGAZZINO_LOTTO, ESERCIZIO_LOTTO, CD_NUMERATORE_LOTTO, PG_LOTTO
                        FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA vcoac
                        WHERE PREZZO_UNITARIO_SCONTATO_ORDINE_IVATO = PREZZO_UNITARIO_SCONTATO_FATTURA_IVATO )) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Movimento di magazzino con progressivo '||rec.PG_MOVIMENTO||'. Riporta un valore non nullo anche se in fase di associazione fattura non è cambiato il prezzo.', '' ,'');
    END LOOP;

    FOR REC IN (SELECT *
                   FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA vcoac
                   WHERE vcoac.PREZZO_UNITARIO_SCONTATO_ORDINE_IVATO != vcoac.PREZZO_UNITARIO_SCONTATO_FATTURA_IVATO
                   AND NOT EXISTS(SELECT * FROM MOVIMENTI_MAG mm
                                  WHERE MM.CD_TIPO_MOVIMENTO IN ('C19','C20')
                                  AND MM.PREZZO_UNITARIO != 0
                                  AND MM.CD_CDS_LOTTO = vcoac.CD_CDS_LOTTO
                                  AND mm.CD_MAGAZZINO_LOTTO = vcoac.CD_MAGAZZINO_LOTTO
                                  AND mm.ESERCIZIO_LOTTO = vcoac.ESERCIZIO_LOTTO
                                  AND mm.CD_NUMERATORE_LOTTO = vcoac.CD_NUMERATORE_LOTTO
                                  AND mm.PG_LOTTO = vcoac.PG_LOTTO)) LOOP
   		contaanomalie := contaanomalie + 1;
         IBMUTL200.logInf(pg_exec, 'Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
   		rec.RIGA||'/'||rec.CONSEGNA||'. Non risulta essere presente il movimento di magazzino C19 0 C20 anche se in fase di associazione fattura è cambiato il prezzo.', '' ,'');
    END LOOP;


    FOR recLotto IN (SELECT * FROM LOTTO_MAG lm
   	                 LEFT JOIN BENE_SERVIZIO bs ON bs.CD_BENE_SERVIZIO = lm.CD_BENE_SERVIZIO
   	                 WHERE lm.ESERCIZIO >=2023
   	                 ORDER BY lm.CD_CDS, LM.CD_MAGAZZINO, lm.ESERCIZIO, lm.CD_NUMERATORE_MAG, lm.PG_LOTTO) LOOP
   		DECLARE
            pGiacenza LOTTO_MAG.GIACENZA%TYPE := 0;
   			pQuantitaValore LOTTO_MAG.QUANTITA_VALORE%TYPE := 0;
   			pQuantitaCarico LOTTO_MAG.QUANTITA_CARICO%TYPE := 0;
   			pValoreUnitario LOTTO_MAG.VALORE_UNITARIO%TYPE := 0;
        BEGIN
            FOR recMag IN (SELECT mm.QUANTITA*mm.COEFF_CONV QUANTITA_CONVERTITA,
   								  ROUND((mm.PREZZO_UNITARIO/mm.COEFF_CONV),6) PREZZO_UNITARIO_CONVERTITO,
   			                      tmm.QTA_CARICO_LOTTO, tmm.MOD_AGG_QTA_MAGAZZINO, tmm.MOD_AGG_QTA_VAL_MAGAZZINO, tmm.MOD_AGG_VALORE_LOTTO
   						   FROM MOVIMENTI_MAG mm
   			               LEFT JOIN TIPO_MOVIMENTO_MAG tmm ON tmm.CD_CDS = mm.CD_CDS_TIPO_MOVIMENTO AND tmm.CD_TIPO_MOVIMENTO = mm.CD_TIPO_MOVIMENTO
   			               WHERE mm.CD_CDS_LOTTO = recLotto.CD_CDS
   			               AND mm.CD_MAGAZZINO_LOTTO = recLotto.CD_MAGAZZINO
   			               AND mm.ESERCIZIO_LOTTO = recLotto.ESERCIZIO
   			               AND mm.CD_NUMERATORE_LOTTO = recLotto.CD_NUMERATORE_MAG
   			               AND mm.PG_LOTTO = recLotto.PG_LOTTO
   			               ORDER BY mm.PG_MOVIMENTO ASC) LOOP
   				IF recMag.MOD_AGG_QTA_MAGAZZINO='0' THEN
   					pGiacenza := 0;
   				ELSIF recMag.MOD_AGG_QTA_MAGAZZINO IN ('-','D') THEN
   					pGiacenza := pGiacenza - recMag.QUANTITA_CONVERTITA;
   				ELSIF recMag.MOD_AGG_QTA_MAGAZZINO='+' THEN
   					pGiacenza := pGiacenza + recMag.QUANTITA_CONVERTITA;
   				ELSIF recMag.MOD_AGG_QTA_MAGAZZINO='S' THEN
   					pGiacenza := recMag.QUANTITA_CONVERTITA;
                END IF;

   				IF recMag.QTA_CARICO_LOTTO='0' THEN
   					pQuantitaCarico := 0;
   				ELSIF recMag.QTA_CARICO_LOTTO IN ('-','D') THEN
   					pQuantitaCarico := pQuantitaCarico - recMag.QUANTITA_CONVERTITA;
   				ELSIF recMag.QTA_CARICO_LOTTO='+' THEN
   					pQuantitaCarico := pQuantitaCarico + recMag.QUANTITA_CONVERTITA;
   				ELSIF recMag.QTA_CARICO_LOTTO='S' THEN
   					pQuantitaCarico := recMag.QUANTITA_CONVERTITA;
                END IF;

   				IF recMag.MOD_AGG_QTA_VAL_MAGAZZINO='0' THEN
   					pQuantitaValore := 0;
   				ELSIF recMag.MOD_AGG_QTA_VAL_MAGAZZINO IN ('-','D') THEN
   					pQuantitaValore := pQuantitaValore - recMag.QUANTITA_CONVERTITA;
   				ELSIF recMag.MOD_AGG_QTA_VAL_MAGAZZINO='+' THEN
   					pQuantitaValore := pQuantitaValore + recMag.QUANTITA_CONVERTITA;
   				ELSIF recMag.MOD_AGG_QTA_VAL_MAGAZZINO='S' THEN
   					pQuantitaValore := recMag.QUANTITA_CONVERTITA;
                END IF;

   				IF recMag.MOD_AGG_VALORE_LOTTO='0' THEN
   					pValoreUnitario := 0;
   				ELSIF recMag.MOD_AGG_VALORE_LOTTO IN ('-') THEN
   					pValoreUnitario := pValoreUnitario - recMag.PREZZO_UNITARIO_CONVERTITO;
   				ELSIF recMag.MOD_AGG_VALORE_LOTTO='+' THEN
   					pValoreUnitario := pValoreUnitario + recMag.PREZZO_UNITARIO_CONVERTITO;
   				ELSIF recMag.MOD_AGG_VALORE_LOTTO='S' THEN
   					pValoreUnitario := recMag.PREZZO_UNITARIO_CONVERTITO;
                END IF;
            END LOOP;

   			IF pGiacenza!=recLotto.GIACENZA THEN
   				contaanomalie := contaanomalie + 1;
                 IBMUTL200.logInf(pg_exec, 'Lotto '||recLotto.CD_CDS||'/'||recLotto.CD_MAGAZZINO||'/'||recLotto.ESERCIZIO||'/'||recLotto.CD_NUMERATORE_MAG||'/'||
   				recLotto.PG_LOTTO||'. La giacenza ('||recLotto.GIACENZA||') non coincide con il valore calcolato ('||pGiacenza||')', '' ,'');
            END IF;

   			IF pQuantitaCarico!=recLotto.QUANTITA_CARICO THEN
   				contaanomalie := contaanomalie + 1;
                 IBMUTL200.logInf(pg_exec, 'Lotto '||recLotto.CD_CDS||'/'||recLotto.CD_MAGAZZINO||'/'||recLotto.ESERCIZIO||'/'||recLotto.CD_NUMERATORE_MAG||'/'||
   				recLotto.PG_LOTTO||'. La quantità di carico ('||recLotto.QUANTITA_CARICO||') non coincide con il valore calcolato ('||pQuantitaCarico||')', '' ,'');
            END IF;

   			IF pQuantitaValore!=recLotto.QUANTITA_VALORE THEN
   				contaanomalie := contaanomalie + 1;
                 IBMUTL200.logInf(pg_exec, 'Lotto '||recLotto.CD_CDS||'/'||recLotto.CD_MAGAZZINO||'/'||recLotto.ESERCIZIO||'/'||recLotto.CD_NUMERATORE_MAG||'/'||
   				recLotto.PG_LOTTO||'. La quantità a valore ('||recLotto.QUANTITA_VALORE||') non coincide con il valore calcolato ('||pQuantitaValore||')', '' ,'');
            END IF;

   			IF pValoreUnitario!=recLotto.VALORE_UNITARIO THEN
   				contaanomalie := contaanomalie + 1;
                 IBMUTL200.logInf(pg_exec, 'Lotto '||recLotto.CD_CDS||'/'||recLotto.CD_MAGAZZINO||'/'||recLotto.ESERCIZIO||'/'||recLotto.CD_NUMERATORE_MAG||'/'||
   				recLotto.PG_LOTTO||'. Il valore unitario ('||rtrim(to_char(recLotto.VALORE_UNITARIO,'FM999G999G999G990D999999'),',')||
   				') non coincide con il valore calcolato ('||rtrim(to_char(pValoreUnitario,'FM999G999G999G990D999999'),',')||')', '' ,'');
            END IF;
        END;
    END LOOP;
     aEndT:=sysdate;
     aEnd:=to_char(aEndT,'YYYYMMDD HH:MI:SS');
     aDelta:=to_char((aEndT-aStartT)*24*3600,'999999');
     IBMUTL200.logInf(pg_exec,'CHECK COngruenza Dati END at: '||aEnd||' tot exec time(s):'||aDelta,'','');

 /*
   	IF contaanomalie = 0 THEN
   		aMessage := 'Non sono state rilevate anomalie.';
   	ELSE
   		aMessage := 'Sono state rilevate '||contaanomalie||' anomalie.';
   		IBMERR001.RAISE_ERR_GENERICO(aMessage);
   	END IF;
     */

  END job_check_congruenza_dati;

  PROCEDURE GET_INV_FROM_FATTINV(aEs IN NUMBER,
                                 aContoEp IN VARCHAR2,
                                 P_CD_CDS_FATT_PASS IN FATTURA_ORDINE.CD_CDS%TYPE,
								 P_CD_UO_FATT_PASS IN FATTURA_ORDINE.CD_UNITA_ORGANIZZATIVA%TYPE,
								 P_ESERCIZIO_FATT_PASS IN FATTURA_ORDINE.ESERCIZIO%TYPE,
								 P_PG_FATTURA_PASSIVA IN FATTURA_ORDINE.PG_FATTURA_PASSIVA%TYPE,
								 P_PROGRESSIVO_RIGA_FATT_PASS IN FATTURA_ORDINE.PROGRESSIVO_RIGA%TYPE,
								 P_TOTINV OUT NUMBER,
								 P_CONTAINV OUT NUMBER,
                                 P_DESC_INVENTARI OUT VARCHAR2) IS
		P_INVINI NUMBER := 0;
   		P_INVVAR NUMBER := 0;
		REC_FATTURA_ORDINE FATTURA_ORDINE%ROWTYPE;
  BEGIN
	    P_TOTINV := 0;
        P_CONTAINV := 0;
   		P_DESC_INVENTARI := NULL;
        FOR RECINV IN (SELECT DISTINCT aibf.PG_INVENTARIO, aibf.NR_INVENTARIO, aibf.PROGRESSIVO
					   FROM ASS_INV_BENE_FATTURA aibf
					   WHERE aibf.CD_CDS_FATT_PASS = P_CD_CDS_FATT_PASS
					   AND   aibf.CD_UO_FATT_PASS = P_CD_UO_FATT_PASS
					   AND   aibf.ESERCIZIO_FATT_PASS = P_ESERCIZIO_FATT_PASS
					   AND   aibf.PG_FATTURA_PASSIVA = P_PG_FATTURA_PASSIVA
					   AND   aibf.PROGRESSIVO_RIGA_FATT_PASS = NVL(P_PROGRESSIVO_RIGA_FATT_PASS, aibf.PROGRESSIVO_RIGA_FATT_PASS)
					   ORDER BY 1,2,3) LOOP
			P_CONTAINV := P_CONTAINV + 1;
            SELECT NVL(sum(vibd.valore_iniziale), 0), NVL(sum(vibd.incremento_valore), 0)
            INTO P_INVINI, P_INVVAR
            FROM V_INVENTARIO_BENE_DET vibd
             LEFT JOIN ASS_CATGRP_INVENT_VOCE_EP acive ON acive.ESERCIZIO = aEs AND acive.CD_CATEGORIA_GRUPPO = vibd.CD_CATEGORIA_GRUPPO AND acive.FL_DEFAULT = 'Y'
            WHERE ((vibd.ESERCIZIO_CARICO_BENE = aEs AND vibd.TIPORECORD = 'VALORE') OR (vibd.ESERCIZIO_BUONO_CARICO = aEs AND vibd.TIPORECORD = 'INCREMENTO'))
              AND   acive.CD_VOCE_EP = aContoEp
              AND   NR_INVENTARIO = RECINV.NR_INVENTARIO
              AND   PG_INVENTARIO = RECINV.PG_INVENTARIO
              AND   PROGRESSIVO = RECINV.PROGRESSIVO;

            P_TOTINV := P_TOTINV + P_INVINI + P_INVVAR;
		  	P_DESC_INVENTARI := P_DESC_INVENTARI||'- (INV: '||RECINV.PG_INVENTARIO||'/'||RECINV.NR_INVENTARIO||'/'||RECINV.PROGRESSIVO||
		  			' - INVINI: '||TO_CHAR(P_INVINI,'fm999g999g999g990d00')||
		  			' - INVVAR: '||TO_CHAR(P_INVVAR,'fm999g999g999g990d00')||')';
        END LOOP;
  END GET_INV_FROM_FATTINV;

  PROCEDURE GET_INV_FROM_ORDINE(aEs IN NUMBER,
                                aContoEp IN VARCHAR2,
                                P_ID_MOVIMENTI_MAG IN EVASIONE_ORDINE_RIGA.ID_MOVIMENTI_MAG%TYPE,
								P_CD_CDS_ORDINE IN ORDINE_ACQ_CONSEGNA.CD_CDS%TYPE,
	  							P_CD_UNITA_OPERATIVA IN ORDINE_ACQ_CONSEGNA.CD_UNITA_OPERATIVA%TYPE,
								P_ESERCIZIO_ORDINE IN ORDINE_ACQ_CONSEGNA.ESERCIZIO%TYPE,
								P_CD_NUMERATORE IN ORDINE_ACQ_CONSEGNA.CD_NUMERATORE%TYPE,
								P_NUMERO IN ORDINE_ACQ_CONSEGNA.NUMERO%TYPE,
								P_RIGA IN ORDINE_ACQ_CONSEGNA.RIGA%TYPE,
								P_CONSEGNA IN ORDINE_ACQ_CONSEGNA.CONSEGNA%TYPE,
								P_TOTINV OUT NUMBER,
								P_CONTAINV OUT NUMBER,
                                P_DESC_INVENTARI OUT VARCHAR2) IS
	  P_INVINI NUMBER := 0;
   	  P_INVVAR NUMBER := 0;
	  REC_FATTURA_ORDINE FATTURA_ORDINE%ROWTYPE;
  BEGIN
	  P_TOTINV := 0;
      P_CONTAINV := 0;
      IF P_ID_MOVIMENTI_MAG IS NOT NULL THEN
          FOR RECINV IN (SELECT DISTINCT ib.PG_INVENTARIO, ib.NR_INVENTARIO, ib.PROGRESSIVO
                       FROM MOVIMENTI_MAG mm
                       LEFT OUTER JOIN TRANSITO_BENI_ORDINI tbo ON tbo.ID_MOVIMENTI_MAG = mm.PG_MOVIMENTO
		               LEFT OUTER JOIN INVENTARIO_BENI ib ON ib.ID_TRANSITO_BENI_ORDINI = tbo.ID
                       WHERE mm.PG_MOVIMENTO = P_ID_MOVIMENTI_MAG
		               AND   mm.stato != 'ANN'
	                   AND   tbo.stato != 'ANN'
	                   ORDER BY 1,2,3) LOOP
              P_CONTAINV := P_CONTAINV + 1;
            SELECT NVL(sum(vibd.valore_iniziale), 0), NVL(sum(vibd.incremento_valore), 0)
            INTO P_INVINI, P_INVVAR
            FROM V_INVENTARIO_BENE_DET vibd
             LEFT JOIN ASS_CATGRP_INVENT_VOCE_EP acive ON acive.ESERCIZIO = aEs AND acive.CD_CATEGORIA_GRUPPO = vibd.CD_CATEGORIA_GRUPPO AND acive.FL_DEFAULT = 'Y'
            WHERE ((vibd.ESERCIZIO_CARICO_BENE = aEs AND vibd.TIPORECORD = 'VALORE') OR (vibd.ESERCIZIO_BUONO_CARICO = aEs AND vibd.TIPORECORD = 'INCREMENTO'))
              AND   acive.CD_VOCE_EP = aContoEp
              AND   PG_INVENTARIO = RECINV.PG_INVENTARIO
              AND   NR_INVENTARIO = RECINV.NR_INVENTARIO
              AND   PROGRESSIVO = RECINV.PROGRESSIVO;

            P_TOTINV := P_TOTINV + P_INVINI + P_INVVAR;
            P_DESC_INVENTARI := P_DESC_INVENTARI||'- (INV: '||RECINV.PG_INVENTARIO||'/'||RECINV.NR_INVENTARIO||'/'||RECINV.PROGRESSIVO||
                      ' - INVINI: '||TO_CHAR(P_INVINI,'fm999g999g999g990d00')||
                      ' - INVVAR: '||TO_CHAR(P_INVVAR,'fm999g999g999g990d00')||')';
        END LOOP;

        IF P_CONTAINV = 0 THEN
            SELECT * INTO REC_FATTURA_ORDINE
            FROM FATTURA_ORDINE fo
            WHERE fo.CD_CDS_ORDINE = P_CD_CDS_ORDINE
              AND   fo.CD_UNITA_OPERATIVA = P_CD_UNITA_OPERATIVA
              AND   fo.ESERCIZIO_ORDINE = P_ESERCIZIO_ORDINE
              AND   fo.CD_NUMERATORE = P_CD_NUMERATORE
              AND   fo.NUMERO = P_NUMERO
              AND   fo.RIGA = P_RIGA
              AND   fo.CONSEGNA = P_CONSEGNA;

            GET_INV_FROM_FATTINV(aEs, aContoEp, REC_FATTURA_ORDINE.CD_CDS, REC_FATTURA_ORDINE.CD_UNITA_ORGANIZZATIVA, REC_FATTURA_ORDINE.ESERCIZIO,
                      REC_FATTURA_ORDINE.PG_FATTURA_PASSIVA, REC_FATTURA_ORDINE.PROGRESSIVO_RIGA, P_TOTINV, P_CONTAINV, P_DESC_INVENTARI);
        END IF;
    END IF;
  END GET_INV_FROM_ORDINE;

  Procedure job_check_quadratura_coge_inv_inner(job number, pg_exec number, next_date date, aEs number, aContoEp VARCHAR2, P_MAKELOG VARCHAR2) is
    TOTINVNOFAT         NUMBER := 0;
    TOTINVNOFATANTE     NUMBER := 0;
    TOTDUPLIC	        NUMBER := 0;

	CONTA               NUMBER := 0;
    SALDO1 	            NUMBER := 0;

    TOTGECPDCNS         NUMBER := 0;
    TOTINVCNS           NUMBER := 0;

    TOTINV2	            MOVIMENTO_COGE.IM_MOVIMENTO%TYPE :=0;

    REC_INVENTARIO_BENI INVENTARIO_BENI%ROWTYPE;
    aTSNow date;
    aEndT date;
    aStartT date;
    aStart varchar2(80);
    aEnd varchar2(80);
    aDelta varchar2(80);
    aUser varchar2(20);
    messaggio varchar2(10000);
  BEGIN
    If P_MAKELOG='Y' then
	    aStartT:=sysdate;
	    aStart:=to_char(sysdate,'YYYYMMDD HH:MI:SS');
	    aUser:=IBMUTL200.getUserFromLog(pg_exec);
        IBMUTL210.logStartExecutionUpd(pg_exec, TIPO_LOG_CONGR_DATI, job, 'Batch di quadratura coge inventari. Start:'||to_char(aTSNow,'YYYY/MM/DD HH-MI-SS'),'Controllo Congruenza Dati');
        IBMUTL200.logInf(pg_exec,'CHECK QUADRATURA COGE INVENTARI CONTO '||aEs||'/'||aContoEp||' - START  at: '||aStart,'','');
    End If;
    FOR REC in (SELECT vibd.PG_INVENTARIO, vibd.NR_INVENTARIO, vibd.PROGRESSIVO, NVL(sum(vibd.valore_iniziale), 0) TOTINI, NVL(sum(vibd.incremento_valore), 0) TOTVAR
				FROM V_INVENTARIO_BENE_DET vibd
				LEFT JOIN ASS_CATGRP_INVENT_VOCE_EP acive ON acive.ESERCIZIO = aEs AND acive.CD_CATEGORIA_GRUPPO = vibd.CD_CATEGORIA_GRUPPO AND acive.FL_DEFAULT = 'Y'
				WHERE ((vibd.ESERCIZIO_CARICO_BENE = aEs AND vibd.TIPORECORD = 'VALORE') OR (vibd.ESERCIZIO_BUONO_CARICO = aEs AND vibd.TIPORECORD = 'INCREMENTO'))
				AND not exists(SELECT '1' FROM ASS_INV_BENE_FATTURA aibf WHERE aibf.esercizio_fatt_pass = aEs AND aibf.PG_INVENTARIO = vibd.PG_INVENTARIO AND aibf.NR_INVENTARIO = vibd.NR_INVENTARIO AND aibf.PROGRESSIVO = vibd.PROGRESSIVO)
				AND EXISTS(SELECT '1' FROM INVENTARIO_BENI ib WHERE ib.PG_INVENTARIO = vibd.PG_INVENTARIO AND IB.NR_INVENTARIO = vibd.NR_INVENTARIO AND ib.PROGRESSIVO = vibd.PROGRESSIVO AND ib.ID_TRANSITO_BENI_ORDINI IS NULL)
				AND acive.CD_VOCE_EP = aContoEp
				GROUP BY vibd.PG_INVENTARIO, vibd.NR_INVENTARIO, vibd.PROGRESSIVO
				ORDER BY 1,2) LOOP
        messaggio := 'Beni Inventariati senza Fattura e/o Transito: '||REC.PG_INVENTARIO||'/'||REC.NR_INVENTARIO||'/'||REC.PROGRESSIVO||' - TOTINI: '||TO_CHAR(REC.TOTINI,'fm999g999g999g990d00')||' - TOTVAR: '||TO_CHAR(REC.TOTVAR,'fm999g999g999g990d00');
        IF P_MAKELOG='Y' THEN
            IBMUTL200.logErr(pg_exec, messaggio, '' ,'');
        ELSE
            dbms_output.put_line(messaggio);
        END IF;
		TOTINVNOFAT := TOTINVNOFAT + REC.TOTINI + REC.TOTVAR;
   		CONTA := CONTA + 1;
    END LOOP;
    IF P_MAKELOG='Y' THEN
        IBMUTL200.logInf(pg_exec, 'Totale Inventario non fatturato: '||TO_CHAR(TOTINVNOFAT,'fm999g999g999g990d00'), '' ,'');
        IBMUTL200.logInf(pg_exec, 'Numero Beni Inventariati senza fattura: '||CONTA, '' ,'');
    ELSE
        dbms_output.put_line('--------------------------------------------------');
        dbms_output.put_line('Totale Inventario non fatturato: '||TO_CHAR(TOTINVNOFAT,'fm999g999g999g990d00'));
        dbms_output.put_line('Numero Beni Inventariati senza fattura: '||CONTA);
        dbms_output.put_line('--------------------------------------------------');
    END IF;

   	CONTA := 0;
    FOR REC in (SELECT vibd.PG_INVENTARIO, vibd.NR_INVENTARIO, vibd.PROGRESSIVO, NVL(sum(vibd.valore_iniziale), 0) TOTINI, NVL(sum(vibd.incremento_valore), 0) TOTVAR
				FROM V_INVENTARIO_BENE_DET vibd
				LEFT JOIN ASS_CATGRP_INVENT_VOCE_EP acive ON acive.ESERCIZIO = aEs AND acive.CD_CATEGORIA_GRUPPO = vibd.CD_CATEGORIA_GRUPPO AND acive.FL_DEFAULT = 'Y'
				WHERE ((vibd.ESERCIZIO_CARICO_BENE = aEs AND vibd.TIPORECORD = 'VALORE') OR (vibd.ESERCIZIO_BUONO_CARICO = aEs AND vibd.TIPORECORD = 'INCREMENTO'))
				AND not exists(SELECT '1' FROM ASS_INV_BENE_FATTURA aibf WHERE aibf.esercizio_fatt_pass = aEs AND aibf.PG_INVENTARIO = vibd.PG_INVENTARIO AND aibf.NR_INVENTARIO = vibd.NR_INVENTARIO AND aibf.PROGRESSIVO = vibd.PROGRESSIVO)
				AND EXISTS(SELECT '1' FROM INVENTARIO_BENI ib
				           LEFT JOIN TRANSITO_BENI_ORDINI tbo ON TBO.ID = ib.ID_TRANSITO_BENI_ORDINI
				           LEFT JOIN EVASIONE_ORDINE_RIGA eor ON eor.ID_MOVIMENTI_MAG = tbo.ID_MOVIMENTI_MAG
						   WHERE ib.PG_INVENTARIO = vibd.PG_INVENTARIO AND IB.NR_INVENTARIO = vibd.NR_INVENTARIO AND ib.PROGRESSIVO = vibd.PROGRESSIVO AND ib.ID_TRANSITO_BENI_ORDINI IS NOT NULL
						   AND tbo.STATO = 'TRA'
						   AND eor.ESERCIZIO < aEs)
				AND acive.CD_VOCE_EP = aContoEp
				GROUP BY vibd.PG_INVENTARIO, vibd.NR_INVENTARIO, vibd.PROGRESSIVO
				ORDER BY 1,2) LOOP
		messaggio := 'Beni Inventariati con evasione prima del '||aEs||': '||REC.PG_INVENTARIO||'/'||REC.NR_INVENTARIO||'/'||REC.PROGRESSIVO||' - TOTINI: '||TO_CHAR(REC.TOTINI,'fm999g999g999g990d00')||' - TOTVAR: '||TO_CHAR(REC.TOTVAR,'fm999g999g999g990d00');
        IF P_MAKELOG='Y' THEN
            IBMUTL200.logErr(pg_exec, messaggio, '' ,'');
        ELSE
            dbms_output.put_line(messaggio);
        END IF;
		TOTINVNOFATANTE := TOTINVNOFATANTE + REC.TOTINI + REC.TOTVAR;
   		CONTA := CONTA + 1;
    END LOOP;
    IF P_MAKELOG='Y' THEN
        IBMUTL200.logInf(pg_exec, 'Totale Inventario evaso prima del '||aEs||': '||TO_CHAR(TOTINVNOFATANTE,'fm999g999g999g990d00'), '' ,'');
        IBMUTL200.logInf(pg_exec, 'Numero Beni Inventariati evasi prima del '||aEs||': '||CONTA, '' ,'');
    ELSE
        dbms_output.put_line('--------------------------------------------------');
        dbms_output.put_line('Totale Inventario evaso prima del '||aEs||': '||TO_CHAR(TOTINVNOFATANTE,'fm999g999g999g990d00'));
        dbms_output.put_line('Numero Beni Inventariati evasi prima del '||aEs||': '||CONTA);
        dbms_output.put_line('--------------------------------------------------');
    END IF;

   	CONTA := 0;
    FOR REC IN (SELECT oac.CD_CDS CD_CDS_ORDINE, oac.CD_UNITA_OPERATIVA, oac.ESERCIZIO ESERCIZIO_ORDINE,
   					   oac.CD_NUMERATORE, oac.NUMERO, oac.RIGA, oac.CONSEGNA,
   					   eor.CD_CDS CD_CDS_EVA, eor.CD_MAGAZZINO CD_MAGAZZINO_EVA, eor.ESERCIZIO ESERCIZIO_EVA,
					   eor.CD_NUMERATORE_MAG CD_NUMERATORE_MAG_EVA,	eor.NUMERO NUMERO_EVA, eor.RIGA RIGA_EVA,
   	           		   fo.CD_CDS CD_CDS_FATTURA, fo.CD_UNITA_ORGANIZZATIVA, fo.ESERCIZIO ESERCIZIO_FATTURA,
					   fo.PG_FATTURA_PASSIVA, fo.PROGRESSIVO_RIGA,
					   eor.ID_MOVIMENTI_MAG
				FROM ORDINE_ACQ_CONSEGNA oac
				LEFT OUTER JOIN ORDINE_ACQ_RIGA oar ON oar.CD_CDS = oac.CD_CDS AND oar.CD_UNITA_OPERATIVA = oac.CD_UNITA_OPERATIVA
					AND oar.ESERCIZIO = oac.ESERCIZIO AND oar.CD_NUMERATORE = oac.CD_NUMERATORE AND oar.NUMERO = oac.NUMERO
					AND oar.RIGA = oac.RIGA
				LEFT OUTER JOIN EVASIONE_ORDINE_RIGA eor ON eor.CD_CDS_ORDINE = oac.CD_CDS AND eor.CD_UNITA_OPERATIVA = oac.CD_UNITA_OPERATIVA
					AND eor.ESERCIZIO_ORDINE = oac.ESERCIZIO AND eor.CD_NUMERATORE_ORDINE = oac.CD_NUMERATORE AND eor.NUMERO_ORDINE = oac.NUMERO
					AND eor.RIGA_ORDINE = oac.RIGA AND eor.CONSEGNA = oac.CONSEGNA
				LEFT OUTER JOIN FATTURA_ORDINE fo ON fo.CD_CDS_ORDINE = oac.CD_CDS AND fo.CD_UNITA_OPERATIVA = oac.CD_UNITA_OPERATIVA
					AND fo.ESERCIZIO_ORDINE = oac.ESERCIZIO AND fo.CD_NUMERATORE = oac.CD_NUMERATORE AND fo.NUMERO = oac.NUMERO
					AND fo.RIGA = oac.RIGA AND fo.CONSEGNA = oac.CONSEGNA
				WHERE (eor.ESERCIZIO = aEs OR (eor.ESERCIZIO > aEs AND NVL(fo.ESERCIZIO,9999) = aEs))
				AND oac.CD_VOCE_EP = aContoEp
				AND oac.STATO != 'INS'
				AND eor.STATO != 'ANN'
				ORDER BY 1,2,3,4,5,6) LOOP
		DECLARE
            TOTINV 	MOVIMENTO_COGE.IM_MOVIMENTO%TYPE := 0;
		   	CONTA_INV NUMBER := 0;
		   	P_INVENTARI   varchar(20000);
			IMP_MOVIMENTO_COGE MOVIMENTO_COGE.IM_MOVIMENTO%TYPE := 0;
   			INVINI MOVIMENTO_COGE.IM_MOVIMENTO%TYPE := 0;
   			INVVAR MOVIMENTO_COGE.IM_MOVIMENTO%TYPE := 0;
   			CONTA_COGE NUMBER := 0;
        BEGIN
            SELECT NVL(SUM(DECODE(mc.SEZIONE,'D',mc.IM_MOVIMENTO,-mc.IM_MOVIMENTO)),0), COUNT(0)
            INTO IMP_MOVIMENTO_COGE, CONTA_COGE
            FROM MOVIMENTO_COGE mc
                 LEFT OUTER JOIN SCRITTURA_PARTITA_DOPPIA spd ON spd.ESERCIZIO = mc.ESERCIZIO AND spd.CD_CDS = mc.CD_CDS
            AND spd.CD_UNITA_ORGANIZZATIVA = mc.CD_UNITA_ORGANIZZATIVA AND spd.PG_SCRITTURA = mc.PG_SCRITTURA
            WHERE spd.CD_CDS_DOCUMENTO = REC.CD_CDS_ORDINE
              AND   spd.CD_UNITA_OPERATIVA = REC.CD_UNITA_OPERATIVA
              AND   spd.ESERCIZIO_DOCUMENTO_AMM = REC.ESERCIZIO_ORDINE
              AND   spd.CD_NUMERATORE_ORDINE = REC.CD_NUMERATORE
              AND   spd.PG_NUMERO_DOCUMENTO = REC.NUMERO
              AND   spd.RIGA_ORDINE = REC.RIGA
              AND   spd.CONSEGNA = REC.CONSEGNA
              AND   mc.CD_VOCE_EP = aContoEp;

            TOTGECPDCNS := TOTGECPDCNS + IMP_MOVIMENTO_COGE;

            messaggio := NULL;
  			IF REC.ESERCIZIO_EVA IS NULL THEN
				messaggio := 'Consegna '||REC.CD_CDS_ORDINE||'/'||REC.CD_UNITA_OPERATIVA||'/'||REC.ESERCIZIO_ORDINE||'/'||
					REC.CD_NUMERATORE||'/'||REC.NUMERO||'/'||REC.RIGA||'/'||REC.CONSEGNA||
					' con stato EVASA ma non associata ad evasione (Manca EVASIONE_ORDINE_RIGA).';
  			ELSIF (REC.ID_MOVIMENTI_MAG IS NULL) THEN
				messaggio := 'Consegna '||REC.CD_CDS_ORDINE||'/'||REC.CD_UNITA_OPERATIVA||'/'||REC.ESERCIZIO_ORDINE||'/'||
					REC.CD_NUMERATORE||'/'||REC.NUMERO||'/'||REC.RIGA||'/'||REC.CONSEGNA||
					' senza movimento di magazzino (MANCA EVASIONE_ORDINE_RIGA.ID_MOVIMENTI_MAG).';
            ELSE
  				GET_INV_FROM_ORDINE(aEs, aContoEp, REC.ID_MOVIMENTI_MAG, REC.CD_CDS_ORDINE, REC.CD_UNITA_OPERATIVA, REC.ESERCIZIO_ORDINE, REC.CD_NUMERATORE, REC.NUMERO, REC.RIGA, REC.CONSEGNA, TOTINV, CONTA_INV, P_INVENTARI);
  				TOTINVCNS := TOTINVCNS + TOTINV;
  				IF CONTA_INV = 0 THEN
  					messaggio := 'Consegna '||REC.CD_CDS_ORDINE||'/'||REC.CD_UNITA_OPERATIVA||'/'||REC.ESERCIZIO_ORDINE||'/'||
						REC.CD_NUMERATORE||'/'||REC.NUMERO||'/'||REC.RIGA||'/'||REC.CONSEGNA||
  						' associata a movimento di magazzino '||REC.ID_MOVIMENTI_MAG||' che non risulta collegato né a transito né a fattura. (Manca TRANSITO_BENI_ORDINI e ASS_INV_BENE_FATTURA)'||
	  					' **** DIFF: '||TO_CHAR(IMP_MOVIMENTO_COGE-TOTINV,'fm999g999g999g990d00');
  				ELSIF TOTINV!=IMP_MOVIMENTO_COGE THEN
					messaggio := 'Consegna: '||REC.CD_CDS_ORDINE||'/'||REC.CD_UNITA_OPERATIVA||'/'||REC.ESERCIZIO_ORDINE||'/'||
							REC.CD_NUMERATORE||'/'||REC.NUMERO||'/'||REC.RIGA||'/'||REC.CONSEGNA||
						' - Importo Prima Nota ('||TO_CHAR(IMP_MOVIMENTO_COGE,'fm999g999g999g990d00')||
						') differente dal valore degli inventari associati ('||TO_CHAR(TOTINV,'fm999g999g999g990d00')||')'||
  						' **** DIFF: '||TO_CHAR(IMP_MOVIMENTO_COGE-TOTINV,'fm999g999g999g990d00')||' **** '||P_INVENTARI;
                END IF;
	            IF messaggio IS NOT NULL THEN
	                IF P_MAKELOG='Y' THEN
	                    IBMUTL200.logErr(pg_exec, messaggio, '' ,'');
                    ELSE
	                    dbms_output.put_line(messaggio);
                    END IF;
                END IF;
            END IF;
        END;
    END LOOP;
    IF P_MAKELOG='Y' THEN
        IBMUTL200.logInf(pg_exec, 'Totale Prime Note associate a Consegne (Dare-Avere): '||TO_CHAR(TOTGECPDCNS,'fm999g999g999g990d00'), '' ,'');
        IBMUTL200.logInf(pg_exec, 'Totale Valore Inventari associati a Consegne: '||TO_CHAR(TOTINVCNS,'fm999g999g999g990d00'), '' ,'');
        IBMUTL200.logInf(pg_exec, 'Differenza tra PN e INV Inventari associati a Consegne (PN-INV): '||TO_CHAR(TOTGECPDCNS-TOTINVCNS,'fm999g999g999g990d00'), '' ,'');
    ELSE
        dbms_output.put_line('--------------------------------------------------');
        dbms_output.put_line('Totale Prime Note associate a Consegne (Dare-Avere): '||TO_CHAR(TOTGECPDCNS,'fm999g999g999g990d00'));
        dbms_output.put_line('Totale Valore Inventari associati a Consegne: '||TO_CHAR(TOTINVCNS,'fm999g999g999g990d00'));
        dbms_output.put_line('Differenza tra PN e INV Inventari associati a Consegne (PN-INV): '||TO_CHAR(TOTGECPDCNS-TOTINVCNS,'fm999g999g999g990d00'));
        dbms_output.put_line('--------------------------------------------------');
        dbms_output.put_line('');
    END IF;

	CONTA := 0;
    FOR REC IN (SELECT spd.CD_TIPO_DOCUMENTO, spd.ESERCIZIO_DOCUMENTO_AMM, spd.CD_CDS_DOCUMENTO, spd.CD_UO_DOCUMENTO, spd.PG_NUMERO_DOCUMENTO,
					   mc.CD_CDS, mc.ESERCIZIO, mc.CD_UNITA_ORGANIZZATIVA, mc.PG_SCRITTURA,
					   DECODE(mc.SEZIONE,'D',mc.IM_MOVIMENTO,-mc.IM_MOVIMENTO) IM_SALDO
	 			FROM SCRITTURA_PARTITA_DOPPIA spd
	 			LEFT JOIN MOVIMENTO_COGE mc ON SPD.ESERCIZIO = MC.ESERCIZIO AND SPD.CD_CDS = MC.CD_CDS
					AND SPD.CD_UNITA_ORGANIZZATIVA = MC.CD_UNITA_ORGANIZZATIVA AND SPD.PG_SCRITTURA = MC.PG_SCRITTURA
				WHERE spd.ESERCIZIO=aEs
				AND   spd.CD_TIPO_DOCUMENTO != 'ORDINE_CNS'
			    AND   spd.ORIGINE_SCRITTURA NOT IN ('APERTURA', 'PRIMA_NOTA_MANUALE','CHIUSURA', 'PRECHIUSURA')
				AND   spd.ATTIVA = 'Y'
				AND   mc.CD_VOCE_EP = aContoEp) LOOP
		DECLARE
            TOTINV 	MOVIMENTO_COGE.IM_MOVIMENTO%TYPE := 0;
		    CONTA_INV NUMBER := 0;
		    P_INVENTARI   varchar(20000);
        BEGIN
			IF (REC.CD_TIPO_DOCUMENTO='FATTURA_P') THEN
				--Verifico se la fattura è da ordini... in caso affermativo si tratta di scarto riscontro a valore e segno solo movimento coge
                SELECT count(0) INTO conta
                FROM FATTURA_ORDINE fo
                WHERE fo.CD_CDS = REC.CD_CDS_DOCUMENTO
                  AND   fo.CD_UNITA_ORGANIZZATIVA = REC.CD_UO_DOCUMENTO
                  AND   fo.ESERCIZIO = REC.ESERCIZIO_DOCUMENTO_AMM
                  AND   fo.PG_FATTURA_PASSIVA = REC.PG_NUMERO_DOCUMENTO;

                IF CONTA = 0 THEN
			  		GET_INV_FROM_FATTINV(aEs, aContoEp, REC.CD_CDS_DOCUMENTO, REC.CD_UO_DOCUMENTO, REC.ESERCIZIO_DOCUMENTO_AMM, REC.PG_NUMERO_DOCUMENTO, NULL, TOTINV, CONTA_INV, P_INVENTARI);
                END IF;
            END IF;
            messaggio := NULL;
			IF CONTA_INV = 0 THEN
				messaggio := 'Scrittura Prima Nota non a fronte ordine ('||
						REC.CD_TIPO_DOCUMENTO||'/'||REC.ESERCIZIO_DOCUMENTO_AMM||'/'||REC.CD_CDS_DOCUMENTO||'/'||REC.CD_UO_DOCUMENTO||'/'||REC.PG_NUMERO_DOCUMENTO||
						'): '||REC.CD_CDS||'/'||REC.ESERCIZIO||'/'||REC.CD_UNITA_ORGANIZZATIVA||'/'||REC.PG_SCRITTURA||
						' SALDO (D-A): '||TO_CHAR(REC.IM_SALDO,'fm999g999g999g990d00');
            ELSIF NVL(REC.IM_SALDO,0) != NVL(TOTINV,0) THEN
				messaggio := 'Scrittura Prima Nota non a fronte ordine ('||
						REC.CD_TIPO_DOCUMENTO||'/'||REC.ESERCIZIO_DOCUMENTO_AMM||'/'||REC.CD_CDS_DOCUMENTO||'/'||REC.CD_UO_DOCUMENTO||'/'||REC.PG_NUMERO_DOCUMENTO||
						'): '||REC.CD_CDS||'/'||REC.ESERCIZIO||'/'||REC.CD_UNITA_ORGANIZZATIVA||'/'||REC.PG_SCRITTURA||
						' SALDO (D-A): '||TO_CHAR(REC.IM_SALDO,'fm999g999g999g990d00')||' - FATTURA INV: '||TO_CHAR(TOTINV,'fm999g999g999g990d00')||' - '||P_INVENTARI;
            END IF;
            IF messaggio is not null THEN
                IF P_MAKELOG='Y' THEN
                    IBMUTL200.logErr(pg_exec, messaggio, '' ,'');
                ELSE
                    dbms_output.put_line(messaggio);
                END IF;
            END IF;
			TOTINV2 := TOTINV2 + TOTINV;
		    SALDO1 := SALDO1 + REC.IM_SALDO;
	   		CONTA := CONTA + 1;
        END;
    END LOOP;
    IF P_MAKELOG='Y' THEN
        IBMUTL200.logInf(pg_exec, 'Totale Saldo Prime Note non associate a Consegne (Dare-Avere): '||TO_CHAR(SALDO1,'fm999g999g999g990d00'), '' ,'');
        IBMUTL200.logInf(pg_exec, 'Totale Inventari di Fatture non associate a Consegne: '||TO_CHAR(TOTINV2,'fm999g999g999g990d00'), '' ,'');
        IBMUTL200.logInf(pg_exec, 'Differenza tra PN ed INV di Fatture non associate a Consegne (PN-INV): '||TO_CHAR(SALDO1-TOTINV2,'fm999g999g999g990d00'), '' ,'');
        IBMUTL200.logInf(pg_exec, 'Numero Movimenti senza ordine: '||CONTA, '' ,'');
    ELSE
        dbms_output.put_line('--------------------------------------------------');
        dbms_output.put_line('Totale Saldo Prime Note non associate a Consegne (Dare-Avere): '||TO_CHAR(SALDO1,'fm999g999g999g990d00'));
        dbms_output.put_line('Totale Inventari di Fatture non associate a Consegne: '||TO_CHAR(TOTINV2,'fm999g999g999g990d00'));
        dbms_output.put_line('Differenza tra PN ed INV di Fatture non associate a Consegne (PN-INV): '||TO_CHAR(SALDO1-TOTINV2,'fm999g999g999g990d00'));
        dbms_output.put_line('Numero Movimenti senza ordine: '||CONTA);
        dbms_output.put_line('--------------------------------------------------');
    END IF;

  	CONTA := 0;
    FOR REC IN (SELECT aibf.PG_INVENTARIO, aibf.NR_INVENTARIO, aibf.PROGRESSIVO, count(0)
				FROM ASS_INV_BENE_FATTURA aibf
				WHERE aibf.ESERCIZIO_FATT_PASS = aEs
				and EXISTS (SELECT '1' FROM ASS_INV_BENE_FATTURA aibf2
				            WHERE aibf2.PG_INVENTARIO = aibf.PG_INVENTARIO
				            AND   aibf2.NR_INVENTARIO = aibf.NR_INVENTARIO
				            AND   aibf2.PROGRESSIVO = aibf.PROGRESSIVO
				            AND   (aibf2.CD_CDS_FATT_PASS != aibf.CD_CDS_FATT_PASS OR
				                   aibf2.CD_UO_FATT_PASS != aibf.CD_UO_FATT_PASS OR
				                   aibf2.ESERCIZIO_FATT_PASS != aibf.ESERCIZIO_FATT_PASS OR
				                   aibf2.PG_FATTURA_PASSIVA != aibf.PG_FATTURA_PASSIVA OR
				                   aibf2.PROGRESSIVO_RIGA_FATT_PASS != aibf.PROGRESSIVO_RIGA_FATT_PASS))
				AND (aibf.PG_INVENTARIO, aibf.NR_INVENTARIO, aibf.PROGRESSIVO) IN
				(SELECT vibd.PG_INVENTARIO, vibd.NR_INVENTARIO, vibd.PROGRESSIVO
				 FROM V_INVENTARIO_BENE_DET vibd
				 LEFT JOIN ASS_CATGRP_INVENT_VOCE_EP acive ON acive.ESERCIZIO = aEs AND acive.CD_CATEGORIA_GRUPPO = vibd.CD_CATEGORIA_GRUPPO AND acive.FL_DEFAULT = 'Y'
				WHERE ((vibd.ESERCIZIO_CARICO_BENE = aEs AND vibd.TIPORECORD = 'VALORE') OR (vibd.ESERCIZIO_BUONO_CARICO = aEs AND vibd.TIPORECORD = 'INCREMENTO'))
				AND acive.CD_VOCE_EP = aContoEp)
				GROUP BY aibf.PG_INVENTARIO, aibf.NR_INVENTARIO, aibf.PROGRESSIVO
				HAVING COUNT(0) > 1) LOOP
        DECLARE
            DESCFATTURE VARCHAR2(20000);
			TOTINV 	MOVIMENTO_COGE.IM_MOVIMENTO%TYPE := 0;
			CONTA_INV NUMBER := 0;
			P_INVENTARI   varchar(20000);
			CONTA_FAT NUMBER := 0;
        BEGIN
	  		CONTA := CONTA + 1;
            FOR REC1 IN (SELECT *
					     FROM ASS_INV_BENE_FATTURA aibf
						 WHERE aibf.ESERCIZIO_FATT_PASS = aEs
						 and   aibf.PG_INVENTARIO = REC.PG_INVENTARIO
						 AND   aibf.NR_INVENTARIO = REC.NR_INVENTARIO
						 AND   aibf.PROGRESSIVO = REC.PROGRESSIVO) LOOP
			    CONTA_FAT := CONTA_FAT + 1;
				GET_INV_FROM_FATTINV(aEs, aContoEp, REC1.CD_CDS_FATT_PASS, REC1.CD_UO_FATT_PASS, REC1.ESERCIZIO_FATT_PASS, REC1.PG_FATTURA_PASSIVA, NULL, TOTINV, CONTA_INV, P_INVENTARI);
				DESCFATTURE := DESCFATTURE||' - ('||REC1.ESERCIZIO_FATT_PASS||'/'||REC1.CD_CDS_FATT_PASS||'/'||REC1.CD_UO_FATT_PASS||'/'||REC1.PG_FATTURA_PASSIVA||'/'||REC1.PROGRESSIVO_RIGA_FATT_PASS||')';
            END LOOP;
  			TOTDUPLIC := TOTDUPLIC + (TOTINV*(CONTA_FAT-1));
			messaggio := 'Bene inventariato ('||REC.PG_INVENTARIO||'/'||REC.NR_INVENTARIO||'/'||REC.PROGRESSIVO||') associato a più fatture '||DESCFATTURE||' - FATTURA INV: '||TO_CHAR(TOTINV,'fm999g999g999g990d00');
            IF P_MAKELOG='Y' THEN
                IBMUTL200.logErr(pg_exec, messaggio, '' ,'');
            ELSE
                dbms_output.put_line(messaggio);
            END IF;
        END;
    END LOOP;
    IF P_MAKELOG='Y' THEN
        IBMUTL200.logInf(pg_exec, 'Totale Valore Beni inventariati associati a più fatture: '||TO_CHAR(TOTDUPLIC,'fm999g999g999g990d00'), '' ,'');
        IBMUTL200.logInf(pg_exec, 'Numero Beni duplicati: '||CONTA, '' ,'');
    ELSE
        dbms_output.put_line('--------------------------------------------------');
        dbms_output.put_line('Totale Valore Beni inventariati associati a più fatture: '||TO_CHAR(TOTDUPLIC,'fm999g999g999g990d00'));
        dbms_output.put_line('Numero Beni duplicati: '||CONTA);
        dbms_output.put_line('--------------------------------------------------');
    END IF;

	CONTA := 0;
	DECLARE
        TOTGECPD    NUMBER := 0;
	    TOTMAGAZ     NUMBER := 0;
    BEGIN
        FOR REC IN (SELECT oac.CD_CDS, oac.CD_UNITA_OPERATIVA, oac.ESERCIZIO, oac.CD_NUMERATORE, oac.NUMERO, oac.RIGA, oac.CONSEGNA,
					       decode(SEZIONE,'D',mc.IM_MOVIMENTO,-mc.IM_MOVIMENTO) IM_MOVIMENTO,
					       ROUND(sum(nvl(mm.PREZZO_UNITARIO*MM.QUANTITA,0)),2) IM_MAGAZZINO
					FROM ORDINE_ACQ_CONSEGNA oac
					LEFT OUTER JOIN ORDINE_ACQ_RIGA oar ON oar.CD_CDS = oac.CD_CDS AND oar.CD_UNITA_OPERATIVA = oac.CD_UNITA_OPERATIVA
						AND oar.ESERCIZIO = oac.ESERCIZIO AND oar.CD_NUMERATORE = oac.CD_NUMERATORE AND oar.NUMERO = oac.NUMERO
						AND oar.RIGA = oac.RIGA
					LEFT OUTER JOIN EVASIONE_ORDINE_RIGA eor ON eor.CD_CDS_ORDINE = oac.CD_CDS AND eor.CD_UNITA_OPERATIVA = oac.CD_UNITA_OPERATIVA
						AND eor.ESERCIZIO_ORDINE = oac.ESERCIZIO AND eor.CD_NUMERATORE_ORDINE = oac.CD_NUMERATORE AND eor.NUMERO_ORDINE = oac.NUMERO
						AND eor.RIGA_ORDINE = oac.RIGA AND eor.CONSEGNA = oac.CONSEGNA
					LEFT OUTER JOIN MOVIMENTI_MAG mm ON mm.PG_MOVIMENTO = eor.ID_MOVIMENTI_MAG
					LEFT OUTER JOIN SCRITTURA_PARTITA_DOPPIA spd ON spd.CD_CDS_DOCUMENTO = oac.CD_CDS AND spd.CD_UNITA_OPERATIVA = oac.CD_UNITA_OPERATIVA
						AND spd.ESERCIZIO_DOCUMENTO_AMM = oac.ESERCIZIO AND spd.CD_NUMERATORE_ORDINE = oac.CD_NUMERATORE
						AND spd.PG_NUMERO_DOCUMENTO = oac.NUMERO AND spd.RIGA_ORDINE = oac.RIGA AND spd.CONSEGNA = oac.CONSEGNA
					LEFT OUTER JOIN MOVIMENTO_COGE mc ON spd.ESERCIZIO = mc.ESERCIZIO AND spd.CD_CDS = mc.CD_CDS
						AND spd.CD_UNITA_ORGANIZZATIVA = mc.CD_UNITA_ORGANIZZATIVA AND spd.PG_SCRITTURA = mc.PG_SCRITTURA
						AND mc.CD_VOCE_EP = oac.CD_VOCE_EP
					LEFT OUTER JOIN FATTURA_ORDINE fo ON fo.CD_CDS_ORDINE = oac.CD_CDS AND fo.CD_UNITA_OPERATIVA = oac.CD_UNITA_OPERATIVA
						AND fo.ESERCIZIO_ORDINE = oac.ESERCIZIO AND fo.CD_NUMERATORE = oac.CD_NUMERATORE AND fo.NUMERO = oac.NUMERO
						AND fo.RIGA = oac.RIGA AND fo.CONSEGNA = oac.CONSEGNA
					WHERE (eor.ESERCIZIO = aEs OR (eor.ESERCIZIO > aEs AND NVL(fo.ESERCIZIO,9999) = aEs))
					AND oac.CD_VOCE_EP = aContoEp
					AND oac.STATO != 'INS'
					AND mm.stato != 'ANN'
					GROUP BY oac.CD_CDS, oac.CD_UNITA_OPERATIVA, oac.ESERCIZIO, oac.CD_NUMERATORE, oac.NUMERO, oac.RIGA, oac.CONSEGNA,
							 decode(SEZIONE,'D',mc.IM_MOVIMENTO,-mc.IM_MOVIMENTO)
					HAVING decode(SEZIONE,'D',mc.IM_MOVIMENTO,-mc.IM_MOVIMENTO) != ROUND(sum(nvl(mm.PREZZO_UNITARIO*MM.QUANTITA,0)),2)) LOOP
		    messaggio := 'Ordine: '||REC.CD_CDS||'/'||REC.CD_UNITA_OPERATIVA||'/'||REC.ESERCIZIO||'/'||REC.CD_NUMERATORE||'/'||REC.NUMERO||'/'||REC.RIGA||'/'||
					REC.CONSEGNA||' - Importo Prima Nota ('||TO_CHAR(REC.IM_MOVIMENTO,'fm999g999g999g990d00')||') differente dal movimento magazzino ('||
					TO_CHAR(REC.IM_MAGAZZINO,'fm999g999g999g990d00')||')';
            IF P_MAKELOG='Y' THEN
                IBMUTL200.logErr(pg_exec, messaggio, '' ,'');
            ELSE
                dbms_output.put_line(messaggio);
            END IF;
            TOTGECPD := TOTGECPD + REC.IM_MOVIMENTO;
		   	TOTMAGAZ := TOTMAGAZ + rec.IM_MAGAZZINO;
        END LOOP;

        IF P_MAKELOG='Y' THEN
            IBMUTL200.logInf(pg_exec, 'Totale Prima Nota (Dare-Avere): '||TO_CHAR(TOTGECPD,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, 'Totale Magazzino: '||TO_CHAR(TOTMAGAZ,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, 'Differenza PN e MAG (PN-MAG): '||TO_CHAR(TOTGECPD-TOTMAGAZ,'fm999g999g999g990d00'), '' ,'');
        ELSE
            dbms_output.put_line('--------------------------------------------------');
            dbms_output.put_line('Totale Prima Nota (Dare-Avere): '||TO_CHAR(TOTGECPD,'fm999g999g999g990d00'));
            dbms_output.put_line('Totale Magazzino: '||TO_CHAR(TOTMAGAZ,'fm999g999g999g990d00'));
            dbms_output.put_line('Differenza PN e MAG (PN-MAG): '||TO_CHAR(TOTGECPD-TOTMAGAZ,'fm999g999g999g990d00'));
            dbms_output.put_line('--------------------------------------------------');
        END IF;
    END;

	DECLARE
        TOTINVENTARIO NUMBER := 0;
		TOTMOVIMENTI NUMBER := 0;
    BEGIN
        SELECT NVL(SUM(VALORE_INCREMENTO),0)
        INTO TOTINVENTARIO
        FROM CHIUSURA_ANNO_INVENTARIO cai
         LEFT JOIN ASS_CATGRP_INVENT_VOCE_EP acive ON acive.ESERCIZIO = cai.ANNO AND acive.CD_CATEGORIA_GRUPPO = cai.CD_CATEGORIA_GRUPPO AND acive.FL_DEFAULT = 'Y'
        WHERE ANNO=aEs AND cai.TIPO_CHIUSURA = 'I'--AND cai.CD_TIPO_AMMORTAMENTO IN ('F07','F09','F10','F12')
        AND   acive.CD_VOCE_EP = aContoEp;

        SELECT NVL(SUM(DECODE(SEZIONE, 'D', IM_MOVIMENTO, -IM_MOVIMENTO)), 0)
        INTO TOTMOVIMENTI
        FROM MOVIMENTO_COGE mc
         LEFT JOIN SCRITTURA_PARTITA_DOPPIA spd ON SPD.CD_CDS = mc.CD_CDS and spd.ESERCIZIO = mc.ESERCIZIO AND spd.CD_UNITA_ORGANIZZATIVA = mc.CD_UNITA_ORGANIZZATIVA
        AND spd.PG_SCRITTURA = mc.PG_SCRITTURA
        WHERE mc.ESERCIZIO=aEs
        AND   mc.CD_VOCE_EP = aContoEp
        AND   spd.ORIGINE_SCRITTURA NOT IN ('APERTURA', 'PRIMA_NOTA_MANUALE','CHIUSURA', 'PRECHIUSURA')
        AND   spd.ATTIVA = 'Y';

        IF P_MAKELOG='Y' THEN
            IBMUTL200.logInf(pg_exec, '---------------QUADRATURA-------------------------', '' ,'');
            IBMUTL200.logInf(pg_exec, 'TOTALE MOVIMENTI COGE REGISTRATI: '||TO_CHAR(TOTMOVIMENTI,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, '+ Totale Valore Inventario non fatturato: '||TO_CHAR(TOTINVNOFAT,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, '+ Totale Inventario evaso prima del '||aEs||': '||TO_CHAR(TOTINVNOFATANTE,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, '- Diff. tra PN e INV Inventari associati a Consegne (PN-INV): '||TO_CHAR(TOTGECPDCNS-TOTINVCNS,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, '- Diff. tra PN ed INV di FATT non associate a Consegne (PN-INV): '||TO_CHAR(SALDO1-TOTINV2,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, '- Totale Valore Beni inventariati associati a più fatture: '||TO_CHAR(TOTDUPLIC,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, '= TOTALE MOVIMENTI COGE DOPO RETTIFICA: '||TO_CHAR(TOTMOVIMENTI+TOTINVNOFAT+TOTINVNOFATANTE-TOTGECPDCNS+TOTINVCNS-SALDO1+TOTINV2-TOTDUPLIC,'fm999g999g999g990d00'), '' ,'');
            IBMUTL200.logInf(pg_exec, 'TOTALE MOVIMENTI INVENTARIO REGISTRATI: '||TO_CHAR(TOTINVENTARIO,'fm999g999g999g990d00'), '' ,'');
		    IF TOTMOVIMENTI+TOTINVNOFAT+TOTINVNOFATANTE-TOTGECPDCNS+TOTINVCNS-SALDO1+TOTINV2-TOTDUPLIC!=TOTINVENTARIO THEN
                IBMUTL200.logInf(pg_exec, '---------------ERRORE-------------------------', '' ,'');
                IBMUTL200.logInf(pg_exec, 'SQUADRATURA COGE - INVENTARIO: '||TO_CHAR(TOTMOVIMENTI+TOTINVNOFAT+TOTINVNOFATANTE-TOTGECPDCNS+TOTINVCNS-SALDO1+TOTINV2-TOTINVENTARIO-TOTDUPLIC,'fm999g999g999g990d00'), '' ,'');
            ELSE
                IBMUTL200.logInf(pg_exec, '------------- QUADRATURA PERFETTA-----------------', '' ,'');
            END IF;
        ELSE
            dbms_output.put_line('-------------DATI REGISTRATI-----------------------');
            dbms_output.put_line('TOTINVENTARIO: '||TO_CHAR(TOTINVENTARIO,'fm999g999g999g990d00'));
            dbms_output.put_line('TOTMOVIMENTICOGE: '||TO_CHAR(TOTMOVIMENTI,'fm999g999g999g990d00'));
            dbms_output.put_line('--------------------------------------------------');
            dbms_output.put_line('--------------------------------------------------');

            dbms_output.put_line('--------------------------------------------------');
            dbms_output.put_line('---------------QUADRATURA-------------------------');
            dbms_output.put_line('TOTALE MOVIMENTI COGE REGISTRATI:                                  '||TO_CHAR(TOTMOVIMENTI,'fm999g999g999g990d00'));
            dbms_output.put_line('Totale Valore Inventario non fatturato:                          + '||TO_CHAR(TOTINVNOFAT,'fm999g999g999g990d00'));
            dbms_output.put_line('Totale Inventario evaso prima del '||aEs||':                          + '||TO_CHAR(TOTINVNOFATANTE,'fm999g999g999g990d00'));
            dbms_output.put_line('Diff. tra PN e INV Inventari associati a Consegne (PN-INV):      - '||TO_CHAR(TOTGECPDCNS-TOTINVCNS,'fm999g999g999g990d00'));
            dbms_output.put_line('Diff. tra PN ed INV di FATT non associate a Consegne (PN-INV):   - '||TO_CHAR(SALDO1-TOTINV2,'fm999g999g999g990d00'));
            dbms_output.put_line('Totale Valore Beni inventariati associati a più fatture:         - '||TO_CHAR(TOTDUPLIC,'fm999g999g999g990d00'));
            dbms_output.put_line('TOTALE MOVIMENTI COGE DOPO RETTIFICA:                            = '||TO_CHAR(TOTMOVIMENTI+TOTINVNOFAT+TOTINVNOFATANTE-TOTGECPDCNS+TOTINVCNS-SALDO1+TOTINV2-TOTDUPLIC,'fm999g999g999g990d00'));
            dbms_output.put_line('TOTALE MOVIMENTI INVENTARIO REGISTRATI:                            '||TO_CHAR(TOTINVENTARIO,'fm999g999g999g990d00'));
		    IF TOTMOVIMENTI+TOTINVNOFAT+TOTINVNOFATANTE-TOTGECPDCNS+TOTINVCNS-SALDO1+TOTINV2-TOTDUPLIC!=TOTINVENTARIO THEN
			    dbms_output.put_line('--------------------------------------------------');
			    dbms_output.put_line('---------------ERRORE-------------------------');
			    dbms_output.put_line('SQUADRATURA COGE - INVENTARIO: '||TO_CHAR(TOTMOVIMENTI+TOTINVNOFAT+TOTINVNOFATANTE-TOTGECPDCNS+TOTINVCNS-SALDO1+TOTINV2-TOTINVENTARIO-TOTDUPLIC,'fm999g999g999g990d00'));
			    dbms_output.put_line('--------------------------------------------------');
            ELSE
			    dbms_output.put_line('--------------------------------------------------');
			    dbms_output.put_line('------------- QUADRATURA PERFETTA-----------------');
			    dbms_output.put_line('--------------------------------------------------');
            END IF;
        END IF;
    END;
    aEndT:=sysdate;
    aEnd:=to_char(aEndT,'YYYYMMDD HH:MI:SS');
    aDelta:=to_char((aEndT-aStartT)*24*3600,'999999');
    IBMUTL200.logInf(pg_exec,'CHECK Congruenza Dati END at: '||aEnd||' tot exec time(s):'||aDelta,'','');
 END job_check_quadratura_coge_inv_inner;

 Procedure job_check_quadratura_coge_inv(job number, pg_exec number, next_date date, aEs number, aContoEp VARCHAR2) is
 BEGIN
  	job_check_quadratura_coge_inv_inner(job, pg_exec, next_date, aEs, aContoEp, 'Y');
 END job_check_quadratura_coge_inv;

 Procedure job_check_quadratura_coge_inv2(aEs number, aContoEp VARCHAR2) IS
 BEGIN
    job_check_quadratura_coge_inv_inner(null, null, null, aEs, aContoEp, 'N');
 END job_check_quadratura_coge_inv2;
END;