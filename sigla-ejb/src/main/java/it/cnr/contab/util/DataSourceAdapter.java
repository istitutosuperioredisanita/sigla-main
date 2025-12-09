package it.cnr.contab.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataSourceAdapter implements javax.activation.DataSource {
    private final jakarta.activation.DataSource jakartaDataSource;

    public DataSourceAdapter(jakarta.activation.DataSource jakartaDataSource) {
        this.jakartaDataSource = jakartaDataSource;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return jakartaDataSource.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return jakartaDataSource.getOutputStream();
    }

    @Override
    public String getContentType() {
        return jakartaDataSource.getContentType();
    }

    @Override
    public String getName() {
        return jakartaDataSource.getName();
    }
}