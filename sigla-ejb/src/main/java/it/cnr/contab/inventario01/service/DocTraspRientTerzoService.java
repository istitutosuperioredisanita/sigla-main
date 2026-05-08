package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.TelefonoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoHome;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class DocTraspRientTerzoService {

    private final DataSource dataSource;

    public DocTraspRientTerzoService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //TODO verrà chiamato togliendo il caso di firmatario fisso (davide.mirra@iss.it)
    public String getEmailTerzo(Integer cdTerzo) throws Exception {
        if (cdTerzo == null) {
            return null;
        }

        try (Connection conn = dataSource.getConnection()) {
            TerzoHome home = new TerzoHome(conn);
            TerzoBulk terzo = (TerzoBulk) home.findByPrimaryKey(new TerzoBulk(cdTerzo));

            if (terzo == null) {
                return null;
            }

            if (terzo.getEmail() != null && !terzo.getEmail().isEmpty()) {
                Object obj = terzo.getEmail().get(0);
                if (obj instanceof TelefonoBulk) {
                    return ((TelefonoBulk) obj).getRiferimento();
                }
                return String.valueOf(obj);
            }

            if (terzo.getPec() != null && !terzo.getPec().isEmpty()) {
                Object obj = terzo.getPec().get(0);
                if (obj instanceof TelefonoBulk) {
                    return ((TelefonoBulk) obj).getRiferimento();
                }
                return String.valueOf(obj);
            }

            return null;
        }
    }
}