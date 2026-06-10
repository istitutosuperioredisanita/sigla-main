
# [![Sistema Informativo Gestione Linee di Attività](https://github.com/istitutosuperioredisanita/sigla-main/blob/master/docs/logo-git.png)](https://contab.cnr.it/SIGLANG)

[![license](https://img.shields.io/badge/License-AGPL%20v3-blue.svg?logo=gnu&style=for-the-badge)](https://github.com/istitutosuperioredisanita/sigla-main/blob/master/LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&style=for-the-badge)](https://adoptium.net/)
[![contributors](https://img.shields.io/github/contributors/istitutosuperioredisanita/sigla-main.svg?logo=github&style=for-the-badge)](https://github.com/istitutosuperioredisanita/sigla-main/contributors/)
[![GitHub release](https://img.shields.io/github/v/release/istitutosuperioredisanita/sigla-main?logo=github&style=for-the-badge)](https://github.com/istitutosuperioredisanita/sigla-main/releases)
[![GitHub Container Size](https://ghcr-badge.egpl.dev/istitutosuperioredisanita/sigla-main/size?tag=latest&label=Container%20Size&style=for-the-badge)](https://github.com/istitutosuperioredisanita/sigla-main/pkgs/container/sigla-main%2Fsigla-wildfly)
[![Release Packages](https://github.com/istitutosuperioredisanita/sigla-main/actions/workflows/release.yml/badge.svg)](https://github.com/istitutosuperioredisanita/sigla-main/actions/workflows/release.yml)
[![sphinx docs](https://github.com/istitutosuperioredisanita/sigla-main/actions/workflows/docs.yml/badge.svg)](https://github.com/istitutosuperioredisanita/sigla-main/actions/workflows/docs.yml)

## MAVEN dependency
| Artifact                             | Version                                                                                                                                                                                                                                                           |
|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Wildfly](https://www.wildfly.org/)  | [![WildFly](https://img.shields.io/badge/WildFly-38.0.0.Final-green?logo=apache-maven&style=for-the-badge)](https://mvnrepository.com/artifact/org.wildfly/wildfly-parent/38.0.0.Final)                                                                           |
| [Wildfly JAR Plugin](https://docs.wildfly.org/bootablejar/) | [![WildFly JAR Plugin](https://img.shields.io/maven-central/v/org.wildfly.plugins/wildfly-jar-maven-plugin?logo=apache-maven&style=for-the-badge&label=Bootable%20JAR%20Plugin)](https://mvnrepository.com/artifact/org.wildfly.plugins/wildfly-jar-maven-plugin) |
| [Spring.io](https://spring.io/)                                                   | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/org.springframework/spring-context/7.0.2.svg?logo=spring&style=for-the-badge)                                                                                                  |
| [Liquibase](https://www.liquibase.org/)                                           | ![Maven Central](https://img.shields.io/maven-central/v/org.liquibase/liquibase-core/3.5.3.svg?style=for-the-badge)                                                                                                                                               |
| [SIOPE+](https://github.com/istitutosuperioredisanita/siopeplus)            | ![Maven Central](https://img.shields.io/maven-central/v/it.cnr.si/siopeplus.svg?style=for-the-badge)                                                                                                                                                              |
| [Storage Cloud](https://github.com/istitutosuperioredisanita/storage-cloud) | ![Maven Central](https://img.shields.io/maven-central/v/it.cnr.si.storage/storage-cloud/2.0.2.svg?style=for-the-badge)                                                                                                                                            |
| [JADA JEE Framework](https://github.com/istitutosuperioredisanita/jada)     | ![Maven Central](https://img.shields.io/maven-central/v/it.cnr.si/jada.svg?style=for-the-badge)                                                                                                                                                                   |

## Introduzione
Al fine di fornire elementi generali della Soluzione applicativa, evidenziamo informazioni di sintesi sul Sistema Contabile SIGLA, di proprietà del CNR, che si occupa di processi amministrativi e contabili, di previsione, gestione e di rendicontazione. Il Sistema si rivolge ad Enti pubblici, in particolare Enti di ricerca, che operano in regime di Contabilità Finanziaria, con obbligo di adozione, in aggiunta, del sistema di contabilità basato su rilevazione dei fatti di gestione in termini economici, patrimoniali ed analitici.

## Premessa
L'attività di progettazione e sviluppo dell'applicazione è stata avviata nel 2001 quando emerse per il CNR la necessità di dotarsi di un nuovo sistema integrato per la gestione della contabilità, in attuazione del Regolamento di disciplina dell'amministrazione e dell'attività contrattuale del CNR, approvato con DPCNR n. 015448 del 14/01/2000, ispirato ai nuovi principi di contabilità pubblica introdotti dalla legge 94/97 e dal Decreto legislativo 279/97.
Una significativa revisione dell'impianto dell'applicazione è stata operata durante il 2004, in attuazione del decreto legislativo n. 127 del 4/06/2003, al fine di migliorare il controllo di gestione delle risorse ed allineare la gestione contabile interna ai criteri di rendicontazione dei progetti (con particolare riferimento a progetti europei e nazionali).
Ad oggi il sistema informativo SIGLA risulta completamente coerente ed adattabile all’impostazione dei bilanci ed alla gestione delle attività previste dalla normativa vigente in materia di contabilità pubblica.

## Informazioni generali del Sistema
Il **S**istema **I**ntegrato per la **G**estione delle **L**inee di **A**ttività è un sistema applicativo modulare, organizzato in componenti funzionali integrate tra loro e gestibili autonomamente l’una dall’altra.

**L’accesso** al sistema, anche tramite web, ai dati e alle funzionalità, è controllato da parte degli amministratori del sistema attraverso la definizione di profili utente che limitano la visibilità e l’utilizzo delle funzioni, nonché la gestione di alcuni dati o l’utilizzo di particolari funzionalità. L’accesso all’applicazione è veicolato anche alla struttura organizzativa dell’Ente a cui si è abilitati.
La soluzione applicativa è ‘Multiente’ e si articola su tre livelli di organizzazione dell’Ente, che nel caso specifico del CNR, sono:
• Centro di Spesa;
• Unità Organizzativa;
• Centro di Responsabilità.

**L’architettura** e la tecnologia utilizzate nello sviluppo consentono una facile manutenzione ed evoluzione del sistema. La possibilità di utilizzare o implementare servizi a supporto delle integrazioni, semplifica il dialogo dell’applicazione stessa con altre applicazioni all’interno dell’Ente e consente di poter utilizzare solo alcune componenti SIGLA, sostituendone alcune con quelle eventualmente già presenti nella realtà di interesse. La documentazione e l’help online previsti completano la semplice usabilità dell’applicativo.
Le interfacce semplici e intuitive dell’applicazione aiutano l’utente ad orientarsi nei vari processi funzionali previsti.

**Sigla** copre diversi aspetti amministrativi e di gestione contabile, e pone alla base di tutti i processi funzionali il controllo dell’uso delle risorse a supporto dell’attività di ricerca, o di una qualsiasi attività pubblica progettuale. L’elemento trasversale alle varie funzionalità è infatti rappresentato dal Progetto articolato in linee di attività.
Organizzato in questo modo il sistema prevede gestisce e controlla l’aspetto analitico, a partire dalla fase di previsione fino alla completa rendicontazione, del Bilancio contabile.

**La possibilità** di estrarre in excel tutti i dati presenti sulle funzioni di consultazione e di produrre report in autonomia, rappresenta una grande utilità per gli utenti che organizzano il proprio lavoro in maniera semplice ed autonoma. Così come la possibilità di schedulare report periodici e di farli recapitare automaticamente al proprio indirizzo mail o a quello di altri collaboratori.

## Componenti Funzionali SIGLA

Le componenti funzionali del Sistema coprono aspetti contabili e aspetti amministrativo-contabili che forniscono automaticamente risultati contabili. Gli argomenti possono essere raggruppati come di seguito indicato e si articolano, chiaramente, in gestioni e funzionalità di dettaglio:

    • Gestione delle utenze e degli aspetti di configurazione del Sistema
        ◦ Amministrazione dei Profili e delle Abilitazioni;
        ◦ Definizione Struttura organizzativa dell’Ente;
        ◦ Configurazione del sistema.

    • Gestione delle Anagrafiche fondamentali
        ◦ Progetti e GAE (gestione azioni elementari);
        ◦ Anagrafica Terzi;
        ◦ Piano dei Conti Finanziario ed Economico;
        ◦ Anagrafiche di base.

    • Gestione della Programmazione Economica e Finanziaria
        ◦ Bilancio di previsione Decisionale/Gestionale;
        ◦ Variazioni e Storni di Bilancio.

    • Gestione contabile;
        ◦ Impegni e Accertamenti;
        ◦ Mandati, Reversali e interfaccia Cassiere;
        ◦ Documenti Amministrativi;
        ◦ Gestione IVA.

    • Gestioni consuntive;
        ◦ Gestione delle rendicontazioni analitiche;
        ◦ Bilancio economico;
        ◦ Operazioni di fine anno.

    • Gestione delle Missioni;
    • Gestione degli Incarichi di collaborazione;
    • Gestione dell’Inventario.

## 👏 Come Contribuire

Lo scopo principale di questo repository è continuare ad evolvere SIGLA. Vogliamo contribuire a questo progetto nel modo più semplice e trasparente possibile e siamo grati alla comunità per ogni contribuito a correggere bug e miglioramenti.

## 📄 Licenza

SIGLA è concesso in licenza GNU AFFERO GENERAL PUBLIC LICENSE, come si trova nel file [LICENSE][l].

[l]: https://github.com/istitutosuperioredisanita/cool-jconon/blob/master/LICENSE

# Startup
Per avviare l'applicazione SIGLA in locale, bisogna prima compilare i sorgenti senza far eseguire i test
```
mvn clean package -DskipTests
```
Successivamente si può avviare l'applicazione con H2 in memoria avendo anche cura di cambiare la password di admin nello script [management-admin-user.cli](src/main/scripts/management-admin-user.cli#L8)
```
java -jar target/sigla-bootable.jar --properties=src/main/resources/application-h2.properties --cli-script=src/main/scripts/management-admin-user.cli
```

# 🐳 Startup Docker

#### _Per avviare una istanza di SIGLA con h2 in memoria_
```
docker run -p 8080:8080 -e SIGLA_DB_DRIVER=h2 -e SIGLA_DB_URL=jdbc:h2:mem:sigladb -e SIGLA_DB_USER=sa -e SIGLA_DB_PASSWORD=sa -e SPRING_PROFILES_ACTIVE=liquibase-iss -ti ghcr.io/istitutosuperioredisanita/sigla-main
```

![Startup](docs/screenshot/startup_h2.png)

#### _Per avviare una istanza di SIGLA con oracle locale_
```
git clone git@github.com:istitutosuperioredisanita/sigla-main.git
cd sigla-main
docker run -d --name sigla-oracle -v $PWD/initdb-oracle:/opt/oracle/scripts/startup/ container-registry.oracle.com/database/express:21.3.0-xe
docker run -p 8080:8080 --link sigla-oracle:db -e LC_ALL="it_IT.UTF-8" -e LANG="it_IT.UTF-8" -e LANGUAGE="it_IT:it" -e SIGLA_DB_DRIVER=oracle -e SIGLA_DB_URL="jdbc:oracle:thin:@db:1521/XEPDB1" -e SIGLA_DB_USER=SIGLA -e SIGLA_DB_PASSWORD=siglapw -ti ghcr.io/istitutosuperioredisanita/sigla-main
```
#### _Per avviare una istanza di SIGLA con postgres locale_
```
git clone git@github.com:istitutosuperioredisanita/sigla-main.git
cd sigla-main
docker run --name sigla-postgres -v $PWD/init-user-postgres-db.sh:/docker-entrypoint-initdb.d/init-user-db.sh -e POSTGRES_PASSWORD=mysecretpassword -d postgres:9.6
docker run -p 8080:8080 --link sigla-postgres:db -e LC_ALL="it_IT.UTF-8" -e LANG="it_IT.UTF-8" -e LANGUAGE="it_IT:it" -e SIGLA_DB_DRIVER=postgresql -e SIGLA_DB_URL="jdbc:postgresql://db:5432/sigladb?schema=public" -e SIGLA_DB_USER=SIGLA -e SIGLA_DB_PASSWORD=mysecretpassword -ti ghcr.io/istitutosuperioredisanita/sigla-main
```

Collegarsi a http://localhost:8080/SIGLA/Login.do username: _ENTE_ password da impostare al primo login.
FINE

## Workflow completo per lo Sviluppo con IntelliJ

### 1. Prima volta — crea il server e il WAR
```shell
mvn clean wildfly:package
```
Questo provisionna WildFly in `target/server` e crea il WAR.

---

### 2. Avvia il server
```shell
mvn wildfly:start -Dwildfly.debug=true -Dwildfly.debug.port=8787 -Dspring.profiles.active=liquibase-iss
```

---

### 3. Deploya il WAR
```shell
mvn wildfly:deploy
```

Questo prende `target/sigla.war` e lo deploya sul server in esecuzione.

---

### 4. Sviluppo — dopo modifiche Java

```
Ctrl+F9  →  IntelliJ compila solo i file modificati
```

Poi scegli:

**A) Hot Code Replacement** (per modifiche piccole al corpo dei metodi):
```
Run → Debugging Actions → Reload Changed Classes
```
Nessun redeploy necessario.

**B) Redeploy completo** (per modifiche a EJB, CDI, configurazioni):
```shell
mvn wildfly:redeploy
```

---

### 5. Ferma il server
```shell
mvn wildfly:shutdown
```

---

## Riepilogo Run Configurations in IntelliJ

| Nome | Command line | Quando |
|---|---|---|
| WildFly Package | `wildfly:package` | Prima volta / cambio dipendenze |
| WildFly Start | `wildfly:start -Dwildfly.debug=true` | Ogni mattina |
| WildFly Deploy | `wildfly:deploy` | Dopo Start |
| WildFly Redeploy | `wildfly:redeploy` | Dopo modifiche significative |
| WildFly Stop | `wildfly:shutdown` | Fine sessione |
| WildFly Debug | Remote JVM Debug su porta 8787 | Dopo Start |
