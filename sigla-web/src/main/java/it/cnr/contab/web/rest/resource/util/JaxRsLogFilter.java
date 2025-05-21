package it.cnr.contab.web.rest.resource.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Provider
@PreMatching
public class JaxRsLogFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JaxRsLogFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if ( logger.isDebugEnabled()) {
            if ( !requestContext.getUriInfo().getRequestUri().toString().contains("login") ) {
                logger.debug("URI: " + requestContext.getUriInfo().getRequestUri().toString());
                MultivaluedMap<String, String> headers = requestContext.getHeaders();
                logger.debug("Headers");
                headers.forEach((k, v) -> {
                    logger.debug("key:" + k);
                    v.forEach(item -> {
                        logger.debug("value" + v);
                    });
                });
                logger.debug("Fine Headers");
                if (HttpMethod.POST.matches(requestContext.getMethod())) {
                    InputStream is = requestContext.getEntityStream();
                    System.out.println(is.markSupported());
                    BufferedInputStream stream = new BufferedInputStream(requestContext.getEntityStream());
                    byte[] b = IOUtils.toByteArray(stream);
                    logger.debug("Payload: " + new String(b, StandardCharsets.UTF_8));
                    requestContext.setEntityStream(new ByteArrayInputStream(b));
                }
            }


        }
    }
}
