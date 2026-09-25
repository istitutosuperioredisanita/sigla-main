===============================
Contrattazione della Previsione
===============================

Premessa
--------
La contrattazione relativa alla raccolta fabbisogni per l'anno successivo si riferisce alla richiesta, da parte delle singole strutture dell'Ente, di stanziamenti di spesa per ogni Progetto e per ogni voce di bilancio.
La raccolta di Fabbisogni di spesa, per questa specifica gestione della previsione, attivabile in configurazione in modalità puntuale attraverso l'indicazione del flag Contrattazione=Si, prevede un particolare processo riassunto di seguito nei vari step, a partire dall'apertura del nuovo anno, (che inizia circa a metà dell'anno precedente), e termina con la creazione degli stanziamenti.


Previsione di Spesa
-------------------
Di seguito vengono riporatati i passi da gestire per l'intero processo:

1.	Dall'anno in corso effettuare il - ribaltamento delle anagrafiche - indicando valore anno=anno successivo;

 -	E’ escluso dal ribaltamento il riporto dei Progetti al nuovo anno (da farsi quando serve con funzione di ribaltamento specifica);
 -	Impostare in Parametri ENTE PDG CONTRATTAZIONE=’S’ e PDG QUADRATURA FONTI ESTERNE=’N’
 -	Il flag Contrattazione = Si, indicato al punto precedente indicherà questa nuova gestione della previsione. 

2.	Manualmente saranno creati tutti i progetti di funzionamento per il nuovo anno per tutte le UO e le relative Gae (dall'amministrazione centrale);

 -	Possono essere creati anche Progetti solo per impostare la previsione (quindi di sola Previsione), per poter accogliere fabbisogni per progetti di cui non si ha certezza;
 -	Creare le aree progettuali per il nuovo anno (se non incluse nel ribaltamento generale);
 -	Creare Progetti nel nuovo anno, con tipologia Finanziamento, allegare provvedimento, e APPROVARE.

3.	Entrare nel nuovo anno, da 999 e ‘aprire’ l’esercizio contabile per la previsione (cambia stato in alto a destra), ESERCIZIO=AC, 999: Esercizio contabile: da Iniziale a Piano di Gestione Aperto (‘Apri PDGP’);

4.	Entrare su ogni CDS ed effettuare l’apertura dell’esercizio contabile:

 - Esercizio contabile: da Iniziale a Piano di Gestione Aperto (‘Apri PDGP’);
 -	Automaticamente sarà aperto l’esercizio contabile per tutte le UO del cds;

5.	Creare Progetto calderone e sua Gae; 

6.	Inserire in configurazione Ente i valori per la successiva gestione del gestionale: 
  - Progetto calderone (Progressivo Progetto), Cofog, Gae del progetto calderone: (il CDR e codice Gae). Inserimento valori da Funzione di configurazione dopo la loro creazione;

**Nota tecnica:**

Per il progetto calderone mettere nel campo Importo1 della tabella CONFIGURAZIONE_CNR:
 - CHIAVE_PRIMARIA: PROGETTI
 - CHIAVE_SECONDARIA: PROGETTO_CALDERONE
il Progressivo del progetto Calderone;

Per il COFOG mettere nel campo Valore1 della tabella CONFIGURAZIONE_CNR:
 - CHIAVE_PRIMARIA: PROGETTI
 - CHIAVE_SECONDARIA: COFOG_DEFAULT
il codice Cofog

Per la Gae calderone mettere nel campo Valore1 e Valore2 della tabella CONFIGURAZIONE_CNR:
 - il CDR e codice Gae calderone.

7.	Gestione del Decisionale. Entrare sul Bilancio di previsione

  -	Ogni UO entrando in gestione sulla previsione può gestire solo i suoi progetti o i progetti ai quali partecipa. I progetti visibili sono solo quelli creati nel nuovo anno;
  -	Aggiunto il campo note (nuovo) che è riportato anche in griglia dopo il salvataggio

8.	Indicazioni previsioni di spesa (DECISIONALE): 

 -	Ogni UO si aggiunge il progetto di cui vuole effettuare la previsione.
 -	per ogni progetto si entra in Contrattazione Spese e  poi sulla Tab ‘Previsioni di impegno’, si indicano le voci (classificazione ultimo livello voce) e si indica Importo Fonti esterne decentrate.
 -	Le voci sono solo quelle associate al Progetto
 -	E’ proposto a zero l’importo per i pluriennali sui singoli Progetti;
 -	Vengono propostati i dati Cofog, non modificabili (indicato nei parametri);
 -	Vengono proposti i dati non modificabili: Missione e Voce economica. E’ consentito consultare i dati del piano economico del progetto (tab posta affianco alla tab ‘Previsioni di impegno’.
 -	E’ stato aggiunto il campo note a destra all’importo annuale di previsione (per ogni voce), gestibile sia dall’utente che compila la previsione che dal superutente che la valida.
 -	L’utente non Superutente può indicare solo l’importo annuale di previsione, non sono gestibili gli altri dati;
 -	Solo il Superutente modifica i dati e vede tutto

9.	Ogni UO, dopo aver completato la previsione dei Progetti, effettua il passaggio di stato a ‘Chiusura compilazione’.

10.	Successivamente ogni UO (oppure il Superutente per tutti) effettua la ‘chiusura CDR’:
  - cambio stato in alto a destra. Lo stato cambia da Apertura a Chiusura del CDR.
  - Solo la UO Ente può tornare indietro in questa fase.

11.	Quando le UO hanno completato si può passare allo stato  ‘In esame dal centro’.
  - Solo la UO Ente  lo può fare selezionando una o tutte le UO (condizione per la multiselezione è che le UO selezionate abbiano lo stesso stato di partenza).


A questo punto si prosegue con le attività del centro che verifica e rettifica gli importi indicati dai singoli CDR.
In questa fase (con la chiusura dei CDR) le singole UO non possono più operare ma solo consultare i dati inseriti.

La UO Ente Rettifica gli importi selezionando i vari cdr/Progetti nella funzione di ‘Contrattazione’ specifica per questa operazione e abilitata all'Amministrazione Centrale. 
La funzione di Contrattazione mostra l’elenco dei progetti/voci inseriti in fase decisionale e consente la modifica degli importi. 

Sulla tab ‘dettaglio’ vengono visualizzati i progetti e le voci legate alle previsioni decisionali;
Per ogni riga indicata, sotto vengono mostrati gli importi oggetto del fabbisogno indicato dalle UO e la possibilità di confermare o modificare gli importi;

Completato il controllo degli importi si effettua il salvataggio definitivo (per confermare definitivamente tutte le UO devono avere lo stato ‘In esame dal centro’ anche quelle che non devono inserire nessuna previsione);
Con ‘Approva Definitivamente’ tutte le UO passano automaticamente allo stato ‘ESAMINATO DAL CENTRO’, e si abilita per la 999 il pulsante per alimentare automaticamente il Progetto calderone indicato nei parametri.

Con questa operazione i dati vengono riportati automaticamente sul decisionale del progetto Calderone (indicato nei parametri).
Solo il CDR del progetto calderone passa allo stato 'Aperura del CDR' per consentire la modifica degli importi DECISIONALI prima del passaggio al GESTIONALE.
Ritornando sulle UO dopo l’approvazione definitiva ‘dal centro’ queste possono consultare sempre la richiesta inserita e sulla pagina di riepilogo per il singolo progetto (contrattazione spese del decisionale) possono vedere l’importo che gli è stato approvato.

A questo punto i Superutenti (o gli utenti del CDR progetto calderone) possono intervenire sul progetto stesso per modificare gli importi DECISIONALI adeguandoli dopo aver verificato gli importi derivanti dalla somma dei progetti oggetto di contrattazione. Gli importi pluriennali saranno posti uguali all’importo annuale.
Solo il Progetto calderone risulta aperto in modifica.

Dopo aver ‘chiuso alla compilazione’ anche il Progetto calderone si può passare allo stato di ‘chiusura CDR’ e poi allo stato di ‘Apertura del gestionale’ e di ‘Chiusura Gestionale’.
La parte gestionale non è gestibile per nessun progetto. I Progetti compilati per la raccolta di fabbisogno si fermano al Decisionale come detto prima.

Il progetto calderone viene compilato in automatico per la parte gestionale partendo dai dati raggruppati, modificati e confermati sul decisionale e usando la Gae indicata nei parametri.

La chiusura del Gestionale crea gli stanziamenti, quindi, per il solo Progetto calderone (voci associate) e chiude definitivamente la fase previsionale.

NOTA
----

Importi da gestire: Solo Gestione Decentrata, Fonti Esterne
per la funzione di Stampa Bilancio di previsione sarà prevista la possibilità di scegliere (se attiva questa gestione della Contrattazione): 
 - Stampa Bilancio di Previsione Totale: stampa solo Progetto calderone (indicato nei parametri);
 - Stampa dettagli: stampa solo i Progetti di dettaglio eccetto il ‘Calderone’

