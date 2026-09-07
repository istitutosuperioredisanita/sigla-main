package it.cnr.contab.doccont00.core.bulk;

import it.cnr.si.spring.storage.annotation.StorageProperty;

public interface ObbligazioneParentBulk {
    public String getStorePath();
    @StorageProperty(name="cmis:name")
    public String getCMISFolderName();
    public String getBasePath();
}
