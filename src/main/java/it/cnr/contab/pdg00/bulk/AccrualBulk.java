/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 04/06/2026
 */
package it.cnr.contab.pdg00.bulk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoStorePath;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.si.spring.storage.StorageDriver;

import java.util.*;
import java.util.stream.Collectors;

public class AccrualBulk extends AccrualBase implements AllegatoParentBulk, AllegatoStorePath {

    public static final String STATO_INSERITO = "INS";
    public static final String STATO_PREDISPOSTO = "PRE";
    public static final String STATO_INVIATO = "INV";

    public enum TIPOFILEXBRL {
        STATO_PATRIMONIALE,CONTO_ECONOMICO,SCHEMA_AGGIUNTIVO;
    }


    public static final Dictionary STATO;

    static {
        STATO = new it.cnr.jada.util.OrderedHashtable();
        STATO.put(STATO_INSERITO, "Inserito");
       // STATO.put(STATO_PREDISPOSTO, "Predisposto");
        STATO.put(STATO_INVIATO, "Inviato");
    }

    private static final String ACCRUAL_FILEFOLDER = "Accrual";

    private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<>();

    public AccrualBulk() {
        super();
    }

    public AccrualBulk(Integer esercizio, String stato, String esito) {
        super(esercizio, stato, esito);
    }
    @Override
    public OggettoBulk initializeForInsert(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {

        super.initializeForEdit(bp,context);
        setEsercizio(CNRUserContext.getEsercizio(context.getUserContext()));
        setStato(STATO_INSERITO);


        return this;
    }


    // =========================================================================
    // ALLEGATI
    // =========================================================================
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

    /**
     * Path documentale degli allegati Accrual.
     *
     * Struttura:
     * Comunicazioni DAL / Contabilita Accrual - MEF / esercizio / Accrual MEF esercizio_stato_esito
     */
    @Override
    @JsonIgnore
    public List<String> getStorePath() {
        String esercizio = Optional.ofNullable(this.getEsercizio())
                .map(String::valueOf)
                .orElse("0");



        return Collections.singletonList(Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                ACCRUAL_FILEFOLDER,
                esercizio,
                "Accrual MEF " + esercizio
        ).stream().collect(
                Collectors.joining(StorageDriver.SUFFIX)
        ));
    }

    private String normalizzaPerPath(String value) {
        return value == null
                ? "ND"
                : value.trim()
                .replace(' ', '_')
                .replace('/', '_')
                .replace('\\', '_')
                .replace(':', '_')
                .replace(';', '_');
    }

    // =========================================================================
    // STATO
    // =========================================================================

    public Dictionary getStatoKeys() {
        OrderedHashtable STATO_RET = (OrderedHashtable) (( OrderedHashtable) STATO).clone();
        if ( STATO_INVIATO.equalsIgnoreCase(this.getStato()))
            STATO_RET.remove(STATO_INSERITO);
        return STATO_RET;
    }

    public Dictionary getStatoKeysForSearch() {
        it.cnr.jada.util.OrderedHashtable d =
                (it.cnr.jada.util.OrderedHashtable) getStatoKeys();

        if (d == null) {
            return null;
        }

        return (it.cnr.jada.util.OrderedHashtable) d.clone();
    }

    private boolean hasStato(String stato) {
        return stato != null && stato.equals(getStato());
    }

    public boolean isInserito() {
        return hasStato(STATO_INSERITO);
    }

    public boolean isPredisposto() {
        return hasStato(STATO_PREDISPOSTO);
    }

    public boolean isInviato() {
        return hasStato(STATO_INVIATO);
    }

    public boolean isModificabile() {
        return isInserito() || isPredisposto();
    }

    public boolean isPredisponibile() {
        return isInserito();
    }

    public boolean isInviabile() {
        return isPredisposto();
    }

    // =========================================================================
    // VALIDAZIONE
    // =========================================================================

    @Override
    public void validate() throws ValidationException {
        if (getEsercizio() == null) {
            throw new ValidationException("Indicare l'Esercizio.");
        }

        super.validate();
    }

    public AllegatoAccrualBulk getAllegatoAccrualXbrl() {
        return Optional.ofNullable(this.getArchivioAllegati()).orElse(new BulkList<>()).
                stream().filter(AllegatoAccrualBulk.class::isInstance)
                .map(AllegatoAccrualBulk.class::cast).
                filter(e->e.isAllegatoAccrualXbr()).findAny().orElse(null);
    }

    public boolean existsAllegatoAccrualXbrl(){
        return Optional.ofNullable(getAllegatoAccrualXbrl()).isPresent();
    }

}