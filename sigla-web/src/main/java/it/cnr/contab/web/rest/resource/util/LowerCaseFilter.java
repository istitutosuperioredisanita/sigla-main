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
import java.io.IOException;


@Provider
@PreMatching
public class LowerCaseFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LowerCaseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.debug("URI: " + requestContext.getUriInfo().getRequestUri().toString());
            MultivaluedMap<String, String> headers=requestContext.getHeaders();
            logger.debug("Headers" );
            headers.forEach((k, v)->{
                logger.debug("key:"+k );
                v.forEach(item-> {
                logger.debug("value"+v);
                });
            });
            logger.debug("Fine Headers" );
            if (HttpMethod.POST.matches(requestContext.getMethod())) {
                BufferedInputStream stream = new BufferedInputStream(requestContext.getEntityStream());
                String payload = IOUtils.toString(stream, "UTF-8");
                logger.debug("Payload: " + payload);
                requestContext.setEntityStream(IOUtils.toInputStream(payload, "UTF-8"));
            }
    }
}
