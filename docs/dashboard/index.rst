=========
Cruscotto
=========

Il **Cruscotto** riguarda una serie di grafici utili all'analisi dell’Attività Amministrativa.
L'analisi riguarda indicatori di efficienza, sostenibilità e resilienza per la valutazione complessiva delle performance gestionali dell'amministrazione.

La funzionalità si riferisce ad un anno di gestione specifico, scelto all'accesso e modificabile, e riepiloga in prima pagina alcuni dati fondamentali che mostrano l'andamento economico generale dell'Ente attraverso grafici dai quali si accede a dati più dettagliati.

La pagina presenta alcuni grafici principali e una serie di indici riepilogativi.


Grafici principali
------------------

**Trend Fondi di Funzionamento per UO**

Il grafico analizza i Fondi di Funzionamento accordati alle varie strutture (Unità Organizzative) rispetto ad un CDS specifico (preimpostato con il CDS principale, definito in anagrafica come SAC, Sede Amministrazione Centrale) e indicato in alto insieme all'anno di gestione utilizzato come riferimento.

Sull'asse orizzontale sono indicate le Unità Organizzative (Codice UO) e sull'asse verticale gli importi secondo una scala crescente.
Scorrendo la barra orizzontale posta sotto il grafico è possibile resringere o allargare il campo di visualizzazione e quindi aumentare o diminuire la risoluzione grafica dei dati mostrati.
Il CDS e l'anno di riferimento sono indicati nel titolo del grafico.

Navigando sugli istogrammi verticali del grafico, mostrati in ordine di importo decrescente, vengono visualizzate informazioni relative a:

* Codice e destrizione della Unitò Organizzativa;
* Importo assegnato (assegnato per tutti i Progetti di funzionamento) per la UO;
* Importo stanziato (stanziato di tutti i Progetti di funzionamento) per la UO;
* Importo residuo da stanziare (differenza tra assegnato e stanziato) per la UO.


**Funzionamento per Voce**

Il grafico analizza i Fondi di Funzionamento accordati alle varie strutture (Cenri di Spesa e Unità Organizzative). I criteri di selezione predefiniti sono gli stessi utilizzati per il grafico precedente:  'Trend Fondi di funzionamento per UO', ma in questo caso il raggruppamento dei dati è per Voce di spesa e si riferisce a tutte le stutture dell'Ente.

Sull'asse orizzontale sono indicati gli importi secondo una scala crescente e sull'asse vericale sono indicate le Voci di spesa.

Tramite la barra verticale, posta a destra del grafico, è possibile resringere/aumentare il campo di visualizzazione e quindi aumentare/diminuire la quantità dei dati mostrati, oppure è possibile scorrere i dati per moostrare quelli non inclusi nella prima pagina del grafico mostrata.
L'anno di riferimento è indicato nel titolo del grafico.

Navigando sugli istogrammi orizzontali del grafico, mostrati in ordine di importo decrescente, vengono visualizzate informazioni relative a:

* Codice e destrizione della voce di spesa
* Importo utilizzato (impegnato di tutti i Progetti di funzionamento) relativo a tutte le strutture dell'Ente per la voce in esame;
* Importo pagato (pagato relativo agli importi impegnati di cui al punto precedente) per tutti i CDS/UO dell'Ente;
* Per l'importo pagato è indicata una percentuale del pagato rispetto all'utilizzato.

Il tutto è sempre riferito all'anno di accesso al sistema Sigla, indicato nel titolo del grafico.


**Cespiti**

Il grafico analizza il valore dei beni patrimoniali (cespiti) di tutte le strutture dell'Ente, raggruppati per Centro di Spesa (CDS).

Sull'asse orizzontale sono indicati i Centri di Spesa (CDS) dell'Ente, compreso la struttura ENTE (999) che raccoglie eventualmente i beni non assegnati direttamente agli altri CDS.
Sull'asse verticale vengono mostrati, a sinistra il numero di beni e a destra gli importi, entrambi secondo una scala crescente di valori.
Scorrendo la barra orizzontale posta sotto il grafico è possibile resringere o allargare il campo di visualizzazione e quindi aumentare o diminuire la risoluzione grafica dei dati mostrati.
L'anno di riferimento è indicato nel titolo del grafico.

Gli Istogrammi verticali mostrano il numero di beni assegnati alle strutture dell'Ente (CDS), mentre la linea orizzontale di diverso colore rappresenta il valore dei cespiti per ogni CDS.

Quindi i dati oggetto di analisi sono:

* Numero Beni per ogni CDS;
* Valore totale dei beni (valore dei Beni, per l'anno di esercizio specifico, considerando il valore iniziale per ogni bene comprensivo di eventuali variazioni di valore positivo/negativo) per il livello più alto dell'organigramma dell'Ente (CDS);

I beni presi in considerazione sono quelli indicati nell'inventario per l'anno di riferimento, escludendo quindi beni scaricati o trasferiti, anche totalmente ammortizzati, e considerando come valore del bene quello iniziale comprensivo di variazioni positive e negative verificatesi sempre nell'anno di riferimento.


**Nota importante di configurazione**

Per i primi due grafici di funzionamento sono stati selezionati solo Progetti di Funzionamento (apparteneti ad una specifica Area Progettuale indicata in configurazione).
Sono state escluse dalla selezione le UO che non hanno fondi di funzionamento (indicate in configurazione).
Sono stati esclusi alcuni progetti usati come 'Progetti di appoggio' la cui tipologia è indicata in configurazione.

Per il Terzo grafico relativo ai cespiti sono stati considerati i beni del Registro inventario per l'anno selezionato


----------------------------------------------------------------------------------------------------------------



Box Indici riepilogativi 
------------------------
(da aggiungere sulla prima pagina del dashboard)

**Assegnato nell'anno**

Rappresenta l'importo assegnato di tutti i progetti dell'anno di gestione. E' un dato inserito in fase di creazione del singolo progetto che rappresenta fondamentalmente la **spesa** consentita da utilizzare per ogni progetto.
Viene mostrata, sotto l'importo assegnato, una percentuale di scostamento positivo o negativo rispetto allo stesso dato dell'anno precedente.

**Utilizzato nell'anno**

Rappresenta la somma dell'importo utilizzato (impegnato) di tutti i Progetti dell'anno di gestione. Naturalmente questo importo rappresenta una parte o tutto l'importo assegnato (indicato al punto precedente).
Sotto l'importo utilizzato viene mostrata una percentuale di utilizzo rispetto all'importo assegnato.

**Tempestività dei Pagamenti**

Rappresenta, a livello di Ente, l'indice annuale di tempestività di pagamento ossia la velocità con cui vengono eseguiti i pagamenti di fatture elettroniche ricevute.
Il calcolo dei giorni di pagamento viene effettuato confrontando la data di ricezione del singolo documento rispetto alla data di pagamento. Un documento deve essere pagato entro 30 giorni dalla sua ricezione (tranne alcune eccezioni). Rispettando questo termine l'indice sarà uguale a zero (risultato atteso per la normativa vigente RGS). Quanto più breve sarà il periodo intercorso tra la ricezione di un documento ed il suo pagamento, più l'indice di pagamento risulterà inferiore allo zero (migliore è la prestazione dell'ente in termini di velocità nei pagamenti).
Se l'indice è maggiore di zero indica un ritardo nei pagamenti (tempi superiori ai 30 giorni consentiti), e più è positivo l'indice, più questo indica una peggiore velocità da parte dell'ente nel pagamento ai fornitori. 
La visualizzazione è riferita all'esercizio in corso (modificabile per consultare esercizi diversi).

**UO sotto soglia**

Rappresenta, per un CDS specifico (indicato nel grafico che segue: Trend Fondi di Funzionamento), il numero di UO che hanno utilizzato meno del 30% dei fondi assegnati. Quardando il grafico di dettaglio sottostante si può analizzare la situazione dell'assegnato e utilizzato per ogni UO del CDS specifico (modificabile per analizzare la situazione di tutti i CDS).

**Fatture pagate nell'anno**

Rappresenta la somma delle Fatture elettroniche pagate nell'anno alla data di consultazione.
Si precisa che l'importo riguarda esclusivamente pagamenti relativi a Fatture elettroniche ricevute (non si tiene conto di eventuali documenti generici, compensi o altre tipologie di documenti passivi).

--------------------------------------------------------------------------------------------------------------

.. toctree::

.. only:: html

.. toctree::
   :numbered:
   :caption: Indice dei contenuti
   :maxdepth: 2

   fondi_funzionamento/index
   indice_tempestivita
   acquisti_per_struttura
   acquisti_per_stato
   cespiti_per_struttura

