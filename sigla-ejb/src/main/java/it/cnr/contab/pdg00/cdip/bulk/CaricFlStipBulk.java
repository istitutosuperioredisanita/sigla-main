package it.cnr.contab.pdg00.cdip.bulk;

import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.bulk.BulkList;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.stream.Collectors;

public class CaricFlStipBulk extends AllegatoGenericoBulk implements AllegatoParentBulk {

    private static final long serialVersionUID = 1L;

    // Campi specifici per il flusso stipendi
    private String tipo_rapporto;
    private Integer progressivo;
    private Integer esercizio;

    // Archivio allegati per implementare AllegatoParentBulk
    private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<AllegatoGenericoBulk>();

    // Costanti per tipo rapporto
    public static final String DIPENDENTE = "D";
    public static final String COLLABORATORE_COORD_E_CONT = "C";

    public static final Dictionary tipo_rapportoKeys;
    static {
        tipo_rapportoKeys = new it.cnr.jada.util.OrderedHashtable();
        tipo_rapportoKeys.put(DIPENDENTE, "Dipendente");
        tipo_rapportoKeys.put(COLLABORATORE_COORD_E_CONT, "Collaboratore");
    }

    // Costruttori
    public CaricFlStipBulk() {
        super();
    }

    public CaricFlStipBulk(String storageKey) {
        super(storageKey);
    }

    // Getters e Setters
    public String getTipo_rapporto() {
        return tipo_rapporto;
    }

    public void setTipo_rapporto(String tipo_rapporto) {
        this.tipo_rapporto = tipo_rapporto;
    }

    public Dictionary getTipo_rapportoKeys() {
        return tipo_rapportoKeys;
    }

    @StorageProperty(name = "flusso_stipendi:progressivo")
    public Integer getProgressivo() {
        return progressivo;
    }

    public void setProgressivo(Integer progressivo) {
        this.progressivo = progressivo;
    }

    @StorageProperty(name = "flusso_stipendi:anno")
    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    // Implementazione AllegatoParentBulk
    @Override
    public BulkList<AllegatoGenericoBulk> getArchivioAllegati() {
        return archivioAllegati;
    }

    @Override
    public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegati) {
        this.archivioAllegati = archivioAllegati;
    }

    @Override
    public int addToArchivioAllegati(AllegatoGenericoBulk allegato) {
        if (allegato != null) {
            archivioAllegati.add(allegato);
        }
        return archivioAllegati.size() - 1;
    }

    public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
        return getArchivioAllegati().remove(index);
    }

    // Metodi di utilità
    public boolean isAllegatoEsistente() {
        return !this.isToBeCreated();
    }

    public static String getStorePathStipendi(String suffix, Integer esercizio, Integer progressivo) {
        return Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniAl(),
                suffix,
                String.valueOf(esercizio),
                String.valueOf(progressivo) // Conversione da Integer a String
        ).stream().collect(
                Collectors.joining(StorageDriver.SUFFIX)
        );
    }
}
