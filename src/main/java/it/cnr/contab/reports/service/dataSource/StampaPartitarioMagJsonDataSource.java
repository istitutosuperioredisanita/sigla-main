package it.cnr.contab.reports.service.dataSource;

import com.google.gson.GsonBuilder;
import it.cnr.contab.ordmag.magazzino.bulk.StampaPartitarioMagBulk;
import it.cnr.contab.ordmag.magazzino.bulk.StampaPartitarioMagHome;

import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class StampaPartitarioMagJsonDataSource extends AbstractDataSourceOffline {
    private final static Logger _log = LoggerFactory.getLogger(StampaPartitarioMagJsonDataSource.class);

    protected  String getJson( Object o){
        return new GsonBuilder().create().toJson(o );
    }

    @Override
    public Class getBulkClass() {
        return StampaPartitarioMagBulk.class;
    }

    public String getDataSourceOffline(UserContext userContext, Print_spoolerBulk print_spoolerBulk, BulkHome bulkHome) throws ComponentException {
        return Optional.ofNullable(bulkHome)
                .filter(StampaPartitarioMagHome.class::isInstance)
                .map(StampaPartitarioMagHome.class::cast)
                .map(stampaPartitarioMagHome -> {
                    return stampaPartitarioMagHome.getJsonDataSource(userContext,print_spoolerBulk);
                }).orElse(null);
    }
}
