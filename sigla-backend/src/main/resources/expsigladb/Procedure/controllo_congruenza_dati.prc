CREATE OR REPLACE procedure CONTROLLO_CONGRUENZA_DATI(aMessage OUT varchar2) AS
	contaanomalie NUMBER := 0;
BEGIN
	--CONTROLLO CHE LE OBBLIGAZIONI SIANO COSTRUITE CORRETTAMENTE
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
			dbms_output.put_line('Obbligazione '||rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_OBBLIGAZIONE||
					' di importo '||ltrim(rtrim(to_char(rec.IM_OBBLIGAZIONE,'999g999g999g999g990d00')))||
					' non coincidente con il totale scadenze '||
					ltrim(rtrim(to_char(rec.TOT_SCADENZE,'999g999g999g999g990d00')))||'.');
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
				dbms_output.put_line('Scadenza '||rec1.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
						rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_OBBLIGAZIONE||
						' di importo '||ltrim(rtrim(to_char(rec1.IM_SCADENZA,'999g999g999g999g990d00')))||
						' non coincidente con il totale ripartito per voce '||
						ltrim(rtrim(to_char(rec1.TOT_SCADVOCE,'999g999g999g999g990d00')))||'.');
			END LOOP;
		END IF;
	END LOOP;

	--CONTROLLO CHE GLI ACCERTAMENTI SIANO COSTRUITI CORRETTAMENTE
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
			dbms_output.put_line('Accertamento '||rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_ACCERTAMENTO||
					' di importo '||ltrim(rtrim(to_char(rec.IM_ACCERTAMENTO,'999g999g999g999g990d00')))||
					' non coincidente con il totale scadenze '||
					ltrim(rtrim(to_char(rec.TOT_SCADENZE,'999g999g999g999g990d00')))||'.');
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
				dbms_output.put_line('Scadenza '||rec1.PG_ACCERTAMENTO_SCADENZARIO||' dell''accertamento '||
						rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_ACCERTAMENTO||
						' di importo '||ltrim(rtrim(to_char(rec1.IM_SCADENZA,'999g999g999g999g990d00')))||
						' non coincidente con il totale ripartito per voce '||
						ltrim(rtrim(to_char(rec1.TOT_SCADVOCE,'999g999g999g999g990d00')))||'.');
			END LOOP;
		END IF;
	END LOOP;

	--CONTROLLO CHE LE OBBLIGAZIONI SIANO COSTRUITE CORRETTAMENTE
	FOR rec IN (SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_OBBLIGAZIONE, PG_OBBLIGAZIONE_SCADENZARIO, IM_SCADENZA,
					   IM_ASSOCIATO_DOC_AMM, IM_ASSOCIATO_DOC_CONTABILE
				FROM OBBLIGAZIONE_SCADENZARIO
				WHERE (IM_ASSOCIATO_DOC_AMM != 0 AND IM_SCADENZA != IM_ASSOCIATO_DOC_AMM)
				OR (IM_ASSOCIATO_DOC_CONTABILE != 0 AND IM_SCADENZA != IM_ASSOCIATO_DOC_CONTABILE)) LOOP
		contaanomalie := contaanomalie + 1;
		IF (rec.IM_ASSOCIATO_DOC_AMM != 0 AND rec.IM_SCADENZA != rec.IM_ASSOCIATO_DOC_AMM) THEN
			dbms_output.put_line('Scadenza '||rec.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
					rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_OBBLIGAZIONE||
					' di importo '||ltrim(rtrim(to_char(rec.IM_SCADENZA,'999g999g999g999g990d00')))||
					' non coincidente con il totale documenti amministrativi associati '||
					ltrim(rtrim(to_char(rec.IM_ASSOCIATO_DOC_AMM,'999g999g999g999g990d00')))||'.');
		ELSIF (rec.IM_ASSOCIATO_DOC_CONTABILE != 0) THEN
			dbms_output.put_line('Scadenza '||rec.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
					rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_OBBLIGAZIONE||
					' di importo '||ltrim(rtrim(to_char(rec.IM_SCADENZA,'999g999g999g999g990d00')))||
					' non coincidente con il totale documenti contabili associati '||
					ltrim(rtrim(to_char(rec.IM_ASSOCIATO_DOC_CONTABILE,'999g999g999g999g990d00')))||'.');
		END IF;
    END LOOP;

	--CONTROLLO CHE GLI ACCERTAMENTI SIANO COSTRUITI CORRETTAMENTE
	FOR rec IN (SELECT CD_CDS, ESERCIZIO, ESERCIZIO_ORIGINALE, PG_ACCERTAMENTO, PG_ACCERTAMENTO_SCADENZARIO, IM_SCADENZA,
					   IM_ASSOCIATO_DOC_AMM, IM_ASSOCIATO_DOC_CONTABILE
				FROM ACCERTAMENTO_SCADENZARIO
				WHERE (IM_ASSOCIATO_DOC_AMM != 0 AND IM_SCADENZA != IM_ASSOCIATO_DOC_AMM)
				OR (IM_ASSOCIATO_DOC_CONTABILE != 0 AND IM_SCADENZA != IM_ASSOCIATO_DOC_CONTABILE)) LOOP
		contaanomalie := contaanomalie + 1;
		IF (rec.IM_ASSOCIATO_DOC_AMM != 0 AND rec.IM_SCADENZA != rec.IM_ASSOCIATO_DOC_AMM) THEN
			dbms_output.put_line('Scadenza '||rec.PG_ACCERTAMENTO_SCADENZARIO||' dell''accertamento '||
					rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_ACCERTAMENTO||
					' di importo '||ltrim(rtrim(to_char(rec.IM_SCADENZA,'999g999g999g999g990d00')))||
					' non coincidente con il totale documenti amministrativi associati '||
					ltrim(rtrim(to_char(rec.IM_ASSOCIATO_DOC_AMM,'999g999g999g999g990d00')))||'.');
		ELSIF (rec.IM_ASSOCIATO_DOC_CONTABILE != 0) THEN
			dbms_output.put_line('Scadenza '||rec.PG_ACCERTAMENTO_SCADENZARIO||' dell''accertamento '||
					rec.CD_CDS||'/'||rec.ESERCIZIO||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.PG_ACCERTAMENTO||
					' di importo '||ltrim(rtrim(to_char(rec.IM_SCADENZA,'999g999g999g999g990d00')))||
					' non coincidente con il totale documenti contabili associati '||
					ltrim(rtrim(to_char(rec.IM_ASSOCIATO_DOC_CONTABILE,'999g999g999g999g990d00')))||'.');
		END IF;
    END LOOP;

	--CONTROLLO CHE I LEGAMI TRA DOCUMENTI E OBBLIGAZIONI SIANO CORRETTI
	FOR rec IN (SELECT * FROM V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO vcdos
				WHERE TOT_IMPEGNO_ASSOCIATO_DOCAMM_CALCOLATO != TOT_IMPEGNO_ASSOCIATO_DOCAMM) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Scadenza '||rec.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
				rec.CD_CDS_OBBLIGAZIONE||'/'||rec.ESERCIZIO_OBBLIGAZIONE||'/'||rec.ESERCIZIO_ORI_OBBLIGAZIONE||'/'||rec.PG_OBBLIGAZIONE||
				'. L''importo associato di documenti amministrativi ('||ltrim(rtrim(to_char(rec.TOT_IMPEGNO_ASSOCIATO_DOCAMM,'999g999g999g999g990d00')))||
				') non coincide con il valore calcolato ('||ltrim(rtrim(to_char(rec.TOT_IMPEGNO_ASSOCIATO_DOCAMM_CALCOLATO,'999g999g999g999g990d00')))||').');

	END LOOP;

	FOR rec IN (SELECT * FROM V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO vcdos
				WHERE TOT_IMPEGNO_ASSOCIATO_DOCCONT_CALCOLATO != TOT_IMPEGNO_ASSOCIATO_DOCCONT) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Scadenza '||rec.PG_OBBLIGAZIONE_SCADENZARIO||' dell''obbligazione '||
				rec.CD_CDS_OBBLIGAZIONE||'/'||rec.ESERCIZIO_OBBLIGAZIONE||'/'||rec.ESERCIZIO_ORI_OBBLIGAZIONE||'/'||rec.PG_OBBLIGAZIONE||
				'. L''importo associato di documenti contabili ('||ltrim(rtrim(to_char(rec.TOT_IMPEGNO_ASSOCIATO_DOCCONT,'999g999g999g999g990d00')))||
				') non coincide con il valore calcolato ('||ltrim(rtrim(to_char(rec.TOT_IMPEGNO_ASSOCIATO_DOCCONT_CALCOLATO,'999g999g999g999g990d00')))||').');
	END LOOP;

	--CONTROLLO CHE I LEGAMI TRA DOCUMENTI E ACCERTAMENTI SIANO CORRETTI
	FOR rec IN (SELECT * FROM V_CONTROLLO_DISPACC_ACCERTAMENTO_SCADENZARIO vcdas
				WHERE TOT_ACCERTAMENTO_ASSOCIATO_CALCOLATO != TOT_ACCERTAMENTO_ASSOCIATO) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Scadenza '||rec.PG_ACCERTAMENTO_SCADENZARIO||' dell''accertamento '||
				rec.CD_CDS_ACCERTAMENTO||'/'||rec.ESERCIZIO_ACCERTAMENTO||'/'||rec.ESERCIZIO_ORI_ACCERTAMENTO||'/'||rec.PG_ACCERTAMENTO||
				'. L''importo associato di documenti amministrativi ('||ltrim(rtrim(to_char(rec.TOT_ACCERTAMENTO_ASSOCIATO,'999g999g999g999g990d00')))||
				') non coincide con il valore calcolato ('||ltrim(rtrim(to_char(rec.TOT_ACCERTAMENTO_ASSOCIATO_CALCOLATO,'999g999g999g999g990d00')))||').');

	END LOOP;

	--CONTROLLO CHE IL LEGAME TRA FATTURE PASSIVE E OBBLIGAZIONI IN TERMINI DI UNITÀ ORGANIZZATIVA SIANO CORRETTI
	FOR rec IN (SELECT DISTINCT fpr.ESERCIZIO, fpr.CD_CDS, fpr.CD_UNITA_ORGANIZZATIVA, fpr.PG_FATTURA_PASSIVA,
								fpr.ESERCIZIO_OBBLIGAZIONE, fpr.ESERCIZIO_ORI_OBBLIGAZIONE, fpr.CD_CDS_OBBLIGAZIONE, fpr.PG_OBBLIGAZIONE,
								o.CD_UNITA_ORGANIZZATIVA UO_OBBLIGAZIONE
				FROM FATTURA_PASSIVA_RIGA fpr
				LEFT JOIN OBBLIGAZIONE o ON o.ESERCIZIO = fpr.ESERCIZIO_OBBLIGAZIONE AND o.ESERCIZIO_ORIGINALE = fpr.ESERCIZIO_ORI_OBBLIGAZIONE AND o.CD_CDS = fpr.CD_CDS_OBBLIGAZIONE AND o.PG_OBBLIGAZIONE = fpr.PG_OBBLIGAZIONE
				WHERE fpr.PG_OBBLIGAZIONE IS NOT NULL
				AND o.CD_UNITA_ORGANIZZATIVA != fpr.CD_UNITA_ORGANIZZATIVA) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Fattura passiva '||rec.ESERCIZIO||'/'||rec.CD_CDS||'/'||rec.CD_UNITA_ORGANIZZATIVA||'/'||rec.PG_FATTURA_PASSIVA||
			' della UO '||rec.CD_UNITA_ORGANIZZATIVA||' collegata all''impegno '||
			rec.CD_CDS_OBBLIGAZIONE||'/'||rec.ESERCIZIO_OBBLIGAZIONE||'/'||rec.ESERCIZIO_ORI_OBBLIGAZIONE||'/'||rec.PG_OBBLIGAZIONE||
				' appartenente ad altra UO '||rec.UO_OBBLIGAZIONE||'.');

	END LOOP;

	--CONTROLLO CHE IL LEGAME TRA FATTURE ATTIVE E GLI ACCERTAMENTI IN TERMINI DI UNITÀ ORGANIZZATIVA SIANO CORRETTI
	FOR rec IN (SELECT DISTINCT fpr.ESERCIZIO, fpr.CD_CDS, fpr.CD_UNITA_ORGANIZZATIVA, fpr.PG_FATTURA_ATTIVA,
								fpr.ESERCIZIO_ACCERTAMENTO, fpr.ESERCIZIO_ORI_ACCERTAMENTO, fpr.CD_CDS_ACCERTAMENTO, fpr.PG_ACCERTAMENTO,
								o.CD_UNITA_ORGANIZZATIVA UO_ACCERTAMENTO
				FROM FATTURA_ATTIVA_RIGA fpr
				LEFT JOIN ACCERTAMENTO o ON o.ESERCIZIO = fpr.ESERCIZIO_ACCERTAMENTO AND o.ESERCIZIO_ORIGINALE = fpr.ESERCIZIO_ORI_ACCERTAMENTO AND o.CD_CDS = fpr.CD_CDS_ACCERTAMENTO AND o.PG_ACCERTAMENTO = fpr.PG_ACCERTAMENTO
				WHERE fpr.PG_ACCERTAMENTO IS NOT NULL
				AND o.CD_UNITA_ORGANIZZATIVA != fpr.CD_UNITA_ORGANIZZATIVA) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Fattura attiva '||rec.ESERCIZIO||'/'||rec.CD_CDS||'/'||rec.CD_UNITA_ORGANIZZATIVA||'/'||rec.PG_FATTURA_ATTIVA||
			' della UO '||rec.CD_UNITA_ORGANIZZATIVA||' collegata all''accertamento '||
			rec.CD_CDS_ACCERTAMENTO||'/'||rec.ESERCIZIO_ACCERTAMENTO||'/'||rec.ESERCIZIO_ORI_ACCERTAMENTO||'/'||rec.PG_ACCERTAMENTO||
				' appartenente ad altra UO '||rec.UO_ACCERTAMENTO||'.');

	END LOOP;

	--CONTROLLO CHE GLI ORDINI SIANO COSTRUITI CORRETTAMENTE
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
			dbms_output.put_line('Ordine '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||
					' di imponibile '||
					ltrim(rtrim(to_char(rec.IM_IMPONIBILE,'999g999g999g999g990d00')))||
					' e iva '||
					ltrim(rtrim(to_char(rec.IM_IVA,'999g999g999g999g990d00')))||
					' non coincidente con il totale riga ordine (imponibile: '||
					ltrim(rtrim(to_char(rec.TOT_IMPONIBILE_RIGA,'999g999g999g999g990d00')))||
					' - iva: '||
					ltrim(rtrim(to_char(rec.TOT_IVA_RIGA,'999g999g999g999g990d00')))||'.');
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
				dbms_output.put_line('Riga '||rec1.RIGA||' dell''ordine '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||
						rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||
						' di imponibile '||
						ltrim(rtrim(to_char(rec1.IM_IMPONIBILE,'999g999g999g999g990d00')))||
						' e iva '||
						ltrim(rtrim(to_char(rec1.IM_IVA,'999g999g999g999g990d00')))||
						' non coincidente con il totale riga consegne (imponibile: '||
						ltrim(rtrim(to_char(rec1.TOT_IMPONIBILE_CONSEGNA,'999g999g999g999g990d00')))||
						' - iva: '||
						ltrim(rtrim(to_char(rec1.TOT_IVA_CONSEGNA,'999g999g999g999g990d00')))||').');
			END LOOP;
		END IF;
	END LOOP;

    FOR rec IN (SELECT * FROM OBBLIGAZIONE o WHERE CD_CDS != substr(CD_UNITA_ORGANIZZATIVA,1,3)) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Obbligazione '||rec.CD_CDS||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.ESERCIZIO_ORIGINALE||'/'||
		rec.PG_OBBLIGAZIONE||'. Il CDS ('||rec.CD_CDS||') non corrisponde alla UO ('||rec.cd_unita_organizzativa||').');
    END LOOP;

    FOR rec IN (SELECT * FROM ACCERTAMENTO a WHERE CD_CDS != substr(CD_UNITA_ORGANIZZATIVA,1,3)) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Accertamento '||rec.CD_CDS||'/'||rec.ESERCIZIO_ORIGINALE||'/'||rec.ESERCIZIO_ORIGINALE||'/'||
		rec.PG_ACCERTAMENTO||'. Il CDS ('||rec.CD_CDS||') non corrisponde alla UO ('||rec.cd_unita_organizzativa||').');
    END LOOP;

    --controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND QUANTITA_EVASA_CALCOLATA != QUANTITA_EVASA_EVASIONE) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. La quantità evasa ('||to_char(rec.QUANTITA_EVASA_EVASIONE,'FM999G999G999G990D999999')||
		') registrata sulla riga di evasione associata ('||rec.CD_CDS_EVASIONE||'/'||rec.CD_MAGAZZINO_EVASIONE||'/'||rec.ESERCIZIO_EVASIONE||'/'||
		rec.CD_NUMERATORE_EVASIONE||'/'||rec.NUMERO_EVASIONE||'/'||rec.RIGA_EVASIONE||
		') non coincide con quella calcolata ('||to_char(rec.QUANTITA_EVASA_CALCOLATA,'FM999G999G999G990D999999')||').');
	END LOOP;

    --controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
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

    --controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND TIPO_CONSEGNA = 'TRA'
                AND PREZZO_UNITARIO_SCARICO_MAGAZZINO_CALCOLATO != PREZZO_UNITARIO_SCARICO_MAGAZZINO) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. Il prezzo unitario ('||rtrim(to_char(rec.PREZZO_UNITARIO_SCARICO_MAGAZZINO,'FM999G999G999G990D999999'),',')||
		') dello scarico del magazzino associato (pg: '||rec.PG_SCARICO_MAGAZZINO||') non coincide con quello calcolato ('||
		rtrim(to_char(rec.PREZZO_UNITARIO_SCARICO_MAGAZZINO_CALCOLATO,'FM999G999G999G990D999999'),',')||').');
    END LOOP;

    --controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND QUANTITA_CARICO_LOTTO_CALCOLATO != QUANTITA_CARICO_LOTTO) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. La quantità di carico ('||rtrim(to_char(rec.QUANTITA_CARICO_LOTTO,'FM999G999G999G990D999999'),',')||
		') del lotto associato ('||rec.CD_CDS_LOTTO||'/'||rec.CD_MAGAZZINO_LOTTO||'/'||rec.ESERCIZIO_LOTTO||'/'||rec.CD_NUMERATORE_LOTTO||'/'||rec.PG_LOTTO||
		') non coincide con quella calcolata ('||rtrim(to_char(rec.QUANTITA_CARICO_LOTTO_CALCOLATO,'FM999G999G999G990D999999'),',')||').');
	END LOOP;

    --controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND GIACENZA_LOTTO_CALCOLATO != GIACENZA_LOTTO) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. La giacenza ('||rtrim(to_char(rec.GIACENZA_LOTTO,'FM999G999G999G990D999999'),',')||
		') del lotto associato ('||rec.CD_CDS_LOTTO||'/'||rec.CD_MAGAZZINO_LOTTO||'/'||rec.ESERCIZIO_LOTTO||'/'||rec.CD_NUMERATORE_LOTTO||'/'||rec.PG_LOTTO||
		') non coincide con quella calcolata ('||rtrim(to_char(rec.GIACENZA_LOTTO_CALCOLATO,'FM999G999G999G990D999999'),',')||').');
	END LOOP;

    --controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND VALORE_UNITARIO_LOTTO != VALORE_UNITARIO_LOTTO_CALCOLATO
                AND (STATO_CARICO_MAGAZZINO != 'ANN' OR GIACENZA_LOTTO != 0)) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. Il valore unitario ('||rtrim(to_char(rec.VALORE_UNITARIO_LOTTO,'FM999G999G999G990D999999'),',')||
		') del lotto associato ('||rec.CD_CDS_LOTTO||'/'||rec.CD_MAGAZZINO_LOTTO||'/'||rec.ESERCIZIO_LOTTO||'/'||rec.CD_NUMERATORE_LOTTO||'/'||rec.PG_LOTTO||
		') non coincide con quello calcolato ('||rtrim(to_char(rec.VALORE_UNITARIO_LOTTO_CALCOLATO,'FM999G999G999G990D999999'),',')||').');
	END LOOP;

	--controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND CD_BENE_SERVIZIO_ORDINE != CD_BENE_SERVIZIO_LOTTO) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. Il codice bene ('||rec.CD_BENE_SERVIZIO_ORDINE||
		') del lotto associato ('||rec.CD_CDS_LOTTO||'/'||rec.CD_MAGAZZINO_LOTTO||'/'||rec.ESERCIZIO_LOTTO||'/'||rec.CD_NUMERATORE_LOTTO||'/'||rec.PG_LOTTO||
		') non coincide con il codice bene ('||rec.CD_BENE_SERVIZIO_ORDINE||') associato all''ordine.');
	END LOOP;

	--controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND PG_CARICO_MAGAZZINO IS NULL) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. Non risulta essere associato il movimento di carico magazzino.');
	END LOOP;

	--controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND ((TIPO_CONSEGNA = 'TRA' AND PG_SCARICO_MAGAZZINO IS NULL) OR
                     (TIPO_CONSEGNA != 'TRA' AND PG_SCARICO_MAGAZZINO IS NOT NULL))) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. Valorizzazione movimento di scarico magazzino non coerente con il tipo di consegna.');
	END LOOP;

	--controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND PG_LOTTO IS NULL) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. Non risulta essere associato alcun lotto.');
	END LOOP;

	--controllo gli ordini escludendo quelli caricati dalla migrazione come già evasi e fatturati
    FOR REC IN (SELECT * FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA
                WHERE STATO_CONSEGNA='EVA'
                AND NOT (ESERCIZIO < 2023 AND STATO_CONSEGNA = 'EVA' AND STATO_FATTURA_CONSEGNA = 'ASS'
                         AND PREZZO_UNITARIO_SCONTATO_FATTURA IS NULL)
                AND ((STATO_FATTURA_CONSEGNA = 'ASS' AND PG_FATTURA_PASSIVA IS NULL) OR
                     (STATO_FATTURA_CONSEGNA != 'ASS' AND PG_FATTURA_PASSIVA IS NOT NULL))) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. Valorizzazione movimento di fattura non coerente con il tipo di associazione indicato sulla consegna.');
	END LOOP;

    FOR REC IN (SELECT * FROM MOVIMENTI_MAG mm
                WHERE CD_TIPO_MOVIMENTO IN ('C19','C20')
                AND PREZZO_UNITARIO != 0
                AND (CD_CDS_LOTTO, CD_MAGAZZINO_LOTTO, ESERCIZIO_LOTTO, CD_NUMERATORE_LOTTO, PG_LOTTO) IN
                    (SELECT CD_CDS_LOTTO, CD_MAGAZZINO_LOTTO, ESERCIZIO_LOTTO, CD_NUMERATORE_LOTTO, PG_LOTTO
                     FROM V_CONTROLLO_ORDINE_ACQ_CONSEGNA vcoac
                     WHERE PREZZO_UNITARIO_SCONTATO_ORDINE_IVATO = PREZZO_UNITARIO_SCONTATO_FATTURA_IVATO )) LOOP
		contaanomalie := contaanomalie + 1;
		dbms_output.put_line('Movimento di magazzino con progressivo '||rec.PG_MOVIMENTO||'. Riporta un valore non nullo anche se in fase di associazione fattura non è cambiato il prezzo.');
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
		dbms_output.put_line('Consegna '||rec.CD_CDS||'/'||rec.CD_UNITA_OPERATIVA||'/'||rec.ESERCIZIO||'/'||rec.CD_NUMERATORE||'/'||rec.NUMERO||'/'||
		rec.RIGA||'/'||rec.CONSEGNA||'. Non risulta essere presente il movimento di magazzino C20 anche se in fase di associazione fattura è cambiato il prezzo.');
    END LOOP;

	--controllo lotti tranne quelli migrati
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
				dbms_output.put_line('Lotto '||recLotto.CD_CDS||'/'||recLotto.CD_MAGAZZINO||'/'||recLotto.ESERCIZIO||'/'||recLotto.CD_NUMERATORE_MAG||'/'||
				recLotto.PG_LOTTO||'. La giacenza ('||recLotto.GIACENZA||') non coincide con il valore calcolato ('||pGiacenza||')');
			END IF;

			IF pQuantitaCarico!=recLotto.QUANTITA_CARICO THEN
				contaanomalie := contaanomalie + 1;
				dbms_output.put_line('Lotto '||recLotto.CD_CDS||'/'||recLotto.CD_MAGAZZINO||'/'||recLotto.ESERCIZIO||'/'||recLotto.CD_NUMERATORE_MAG||'/'||
				recLotto.PG_LOTTO||'. La quantità di carico ('||recLotto.QUANTITA_CARICO||') non coincide con il valore calcolato ('||pQuantitaCarico||')');
			END IF;

			IF pQuantitaValore!=recLotto.QUANTITA_VALORE THEN
				contaanomalie := contaanomalie + 1;
				dbms_output.put_line('Lotto '||recLotto.CD_CDS||'/'||recLotto.CD_MAGAZZINO||'/'||recLotto.ESERCIZIO||'/'||recLotto.CD_NUMERATORE_MAG||'/'||
				recLotto.PG_LOTTO||'. La quantità a valore ('||recLotto.QUANTITA_VALORE||') non coincide con il valore calcolato ('||pQuantitaValore||')');
			END IF;

			IF pValoreUnitario!=recLotto.VALORE_UNITARIO THEN
				contaanomalie := contaanomalie + 1;
				dbms_output.put_line('Lotto '||recLotto.CD_CDS||'/'||recLotto.CD_MAGAZZINO||'/'||recLotto.ESERCIZIO||'/'||recLotto.CD_NUMERATORE_MAG||'/'||
				recLotto.PG_LOTTO||'. Il valore unitario ('||rtrim(to_char(recLotto.VALORE_UNITARIO,'FM999G999G999G990D999999'),',')||
				') non coincide con il valore calcolato ('||rtrim(to_char(pValoreUnitario,'FM999G999G999G990D999999'),',')||')');
			END IF;
		END;
	END LOOP;

	IF contaanomalie = 0 THEN
		aMessage := 'Non sono state rilevate anomalie.';
	ELSE
		aMessage := 'Sono state rilevate '||contaanomalie||' anomalie.';
		IBMERR001.RAISE_ERR_GENERICO(aMessage);
	END IF;
END;
/
