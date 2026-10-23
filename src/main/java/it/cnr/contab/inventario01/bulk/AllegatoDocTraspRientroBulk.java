package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.si.spring.storage.annotation.StoragePolicy;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AllegatoDocTraspRientroBulk extends AllegatoGenericoBulk {

	private static final long serialVersionUID = 1L;

	public AllegatoDocTraspRientroBulk() {
		super();
	}

	public AllegatoDocTraspRientroBulk(String storageKey) {
		super(storageKey);
	}

	public abstract String getAspectName();

	public abstract void setAspectName(String aspectName);

	/** Aspect "Altro" (selezionabile). */
	public abstract String getAspectAltroName();

	/** Aspect del documento firmato (selezionabile come "Documento di Trasporto/Rientro"). */
	public abstract String getAspectFirmatoName();

	/** Aspect del PDF generato all'invio in firma (non selezionabile). */
	public abstract String getAspectDaFirmareName();

	/** Etichette di tutti gli aspect, usate per la visualizzazione. */
	protected abstract OrderedHashtable getEtichetteAspect();

	public boolean isAspectDaFirmare() {
		String aspect = getAspectName();
		return aspect != null && aspect.equals(getAspectDaFirmareName());
	}

	public boolean isAspectFirmato() {
		String aspect = getAspectName();
		return aspect != null && aspect.equals(getAspectFirmatoName());
	}

	/** Etichetta leggibile dell'aspect (anche per il "da firmare"). */
	public String getAspectNameLabel() {
		String aspect = getAspectName();
		if (aspect == null) {
			return null;
		}
		Object label = getEtichetteAspect().get(aspect);
		return label != null ? label.toString() : aspect;
	}

	/** Opzioni del menu Tipologia: una sola se l'allegato è bloccato, altrimenti "Altro" e "Documento di T/R". */
	public OrderedHashtable getAspectNamesKeys() {
		OrderedHashtable keys = new OrderedHashtable();

		if (isAspectDaFirmare() && isAllegatoBloccato()) {
			keys.put(getAspectDaFirmareName(), getEtichetteAspect().get(getAspectDaFirmareName()));
		} else if (isAspectFirmato() && isAllegatoBloccato()) {
			keys.put(getAspectFirmatoName(), getEtichetteAspect().get(getAspectFirmatoName()));
		} else {
			keys.put(getAspectAltroName(), getEtichetteAspect().get(getAspectAltroName()));
			keys.put(getAspectFirmatoName(), getEtichetteAspect().get(getAspectFirmatoName()));
		}

		return keys;
	}

	@StorageProperty(name = "cmis:secondaryObjectTypeIds")
	public List<String> getAspect() {
		List<String> results = new ArrayList<>();
		results.add("P:cm:titled");

		if (getAspectName() != null && !getAspectName().trim().isEmpty()) {
			results.add(getAspectName().trim());
		}

		return results;
	}

	@Override
	@StoragePolicy(name="P:cm:titled", property=@StorageProperty(name="cm:description"))
	public String getDescrizione() {
		return super.getDescrizione();
	}

	@Override
	public void setDescrizione(String descrizione) {
		super.setDescrizione(descrizione);
	}

	@Override
	@StorageProperty(name="cmis:name")
	public String getNome() {
		return super.getNome();
	}

	@Override
	public void setNome(String nome) {
		super.setNome(nome);
	}

	@StoragePolicy(
			name = "P:sigla_commons_aspect:utente_applicativo_sigla",
			property = @StorageProperty(name = "sigla_commons_aspect:utente_applicativo")
	)
	private String utenteSIGLA;

	public String getUtenteSIGLA() {
		return utenteSIGLA;
	}

	public void setUtenteSIGLA(String utenteSIGLA) {
		this.utenteSIGLA = utenteSIGLA;
	}

	@Override
	public void complete(UserContext userContext) {
		setUtenteSIGLA(CNRUserContext.getUser(userContext));
		super.complete(userContext);
	}

	public boolean isAllegatoEsistente() {
		return !this.isToBeCreated();
	}

	/** Allegato già salvato di un documento bloccato: Tipologia, Nome, Descrizione e File non modificabili. */
	public boolean isAllegatoBloccato() {
		return isAllegatoEsistente() && tipologiaNomeBloccati;
	}

	public boolean isDescrizioneReadonly() {
		return isAllegatoBloccato();
	}

	/** Impostato dal BP in base allo stato del documento; un allegato nuovo resta sempre compilabile. */
	private boolean tipologiaNomeBloccati = false;

	public void setTipologiaNomeBloccati(boolean tipologiaNomeBloccati) {
		this.tipologiaNomeBloccati = tipologiaNomeBloccati;
	}

	public boolean isAspectNameReadonly() {
		return isAllegatoBloccato();
	}

	public boolean isFileReadonly() {
		return isAllegatoBloccato();
	}

	/** Nome: automatico per un allegato nuovo, modificabile per uno esistente se il documento non è bloccato. */
	public boolean isNomeReadonly() {
		return !isAllegatoEsistente() || isAllegatoBloccato();
	}

	/**
	 * Il generico confronta solo la storageKey: due allegati nuovi (key null) risulterebbero uguali
	 * e una riga sparirebbe. I nuovi si confrontano per identità.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		String key = getStorageKey();
		return key != null && key.equals(((AllegatoDocTraspRientroBulk) o).getStorageKey());
	}

	@Override
	public int hashCode() {
		String key = getStorageKey();
		return key != null ? key.hashCode() : System.identityHashCode(this);
	}

	@Override
	public void validate() throws ValidationException {

		if (isToBeCreated()) {

			File file = getFile();
			String fileName = (file != null ? file.getName() : "sconosciuto");

			if (file == null) {
				throw new ValidationException("Attenzione: selezionare un file!");
			}

			if (!file.exists() || file.length() == 0) {
				throw new ValidationException(
						"Attenzione: il file selezionato (" + fileName + ") è vuoto o non accessibile!"
				);
			}

			if (getAspectName() == null || getAspectName().isEmpty()) {
				throw new ValidationException("Attenzione: selezionare la tipologia di File!");
			}

			if (getDescrizione() == null || getDescrizione().trim().isEmpty()) {
				throw new ValidationException("Attenzione: inserire la descrizione del File!");
			}
		}

		super.validate();
	}

}
