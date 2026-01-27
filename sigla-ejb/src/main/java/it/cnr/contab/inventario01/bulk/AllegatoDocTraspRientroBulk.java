package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.si.spring.storage.annotation.StoragePolicy;
import it.cnr.si.spring.storage.annotation.StorageProperty;

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

	@StorageProperty(name="cmis:secondaryObjectTypeIds")
	public List<String> getAspect() {
		List<String> results = new ArrayList<String>();
		results.add("P:cm:titled");

		if (getAspectName() != null && !getAspectName().isEmpty()) {
			results.add(getAspectName());
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
}