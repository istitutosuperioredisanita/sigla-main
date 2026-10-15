package it.cnr.contab.util.servlet;

import io.undertow.server.HttpHandler;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.util.Headers;
import jakarta.servlet.ServletContext;

public class NoBasicPopupExtension implements ServletExtension {

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        deploymentInfo.addInitialHandlerChainWrapper(next -> (HttpHandler) exchange -> {
            String xhr = exchange.getRequestHeaders().getFirst("X-Requested-With");
            String accept = exchange.getRequestHeaders().getFirst(Headers.ACCEPT);
            boolean ajax = "XMLHttpRequest".equalsIgnoreCase(xhr)
                    || (accept != null && accept.contains("application/json"));
            if (ajax) {
                exchange.addResponseCommitListener(ex -> {
                    if (ex.getStatusCode() == 401) {
                        ex.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
                    }
                });
            }
            next.handleRequest(exchange);
        });
    }
}
