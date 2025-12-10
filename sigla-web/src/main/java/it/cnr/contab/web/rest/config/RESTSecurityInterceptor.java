/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.web.rest.config;

import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.UtilService;
import it.cnr.contab.utenze00.bp.RESTUserContext;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util.servlet.RESTServlet;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.exception.UnauthorizedException;
import it.cnr.contab.web.rest.resource.util.AbstractResource;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJBException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Provider
public class RESTSecurityInterceptor implements ContainerRequestFilter, ContainerResponseFilter, ExceptionMapper<Exception> {

	public static final String ACCOUNT = "/account";
	public static final String TREE = "/tree";
	public static final String CONTEXT = "/context";
	public static final String USERNAME_CNR = "username_cnr";
	public static final String OPTIONS_METHOD_EXCEPTION = "org.jboss.resteasy.spi.DefaultOptionsMethodException";
	private Logger LOGGER = LoggerFactory.getLogger(RESTSecurityInterceptor.class);

	@Context
	private ResourceInfo resourceInfo;
    @Context
    private Providers providers;
	@Context
	private HttpServletRequest httpServletRequest;
	private static final String AUTHORIZATION_PROPERTY = "Authorization";
	private final static Map<String, String> UNAUTHORIZED_MAP = Collections.singletonMap("ERROR", "User cannot access the resource.");

	private static final String REALM = "SIGLA";
	private static final String AUTHENTICATION_SCHEME = "Basic";

	@Override
	public void filter(ContainerRequestContext requestContext) {

		final Method method = resourceInfo.getResourceMethod();
		final Class<?> declaring = resourceInfo.getResourceClass();
		UtilService us = SpringUtil.getBean(UtilService.class);
		String[] rolesAllowed = null;
		boolean denyAll;
		boolean permitAll;
		boolean allUserAllowedWithoutAbort = declaring.isAnnotationPresent(AllUserAllowedWithoutAbort.class);
		RolesAllowed allowed = declaring.getAnnotation(RolesAllowed.class),
			methodAllowed = method.getAnnotation(RolesAllowed.class);
		if (methodAllowed != null) allowed = methodAllowed;
		if (allowed != null)
		{
			rolesAllowed = allowed.value();
		}
		AccessoAllowed accessoAllowed = declaring.getAnnotation(AccessoAllowed.class),
				accessoMethodAllowed = method.getAnnotation(AccessoAllowed.class);
		if (accessoMethodAllowed != null) accessoAllowed = accessoMethodAllowed;

		
		denyAll = (declaring.isAnnotationPresent(DenyAll.class)
				&& !method.isAnnotationPresent(RolesAllowed.class)
				&& !method.isAnnotationPresent(PermitAll.class)) || method.isAnnotationPresent(DenyAll.class);

		permitAll = (declaring.isAnnotationPresent(PermitAll.class)
				&& !method.isAnnotationPresent(RolesAllowed.class)
				&& !method.isAnnotationPresent(DenyAll.class)) || method.isAnnotationPresent(PermitAll.class);
		
		UtenteBulk utenteBulk = null;		
		if (rolesAllowed != null || accessoAllowed != null || allUserAllowedWithoutAbort) {
			final MultivaluedMap<String, String> headers = requestContext.getHeaders();
			final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
			if (Optional.ofNullable(authorization).filter(s -> !s.isEmpty()).isEmpty() && allUserAllowedWithoutAbort) {
				try {
					AbstractResource.getUserContext(requestContext.getSecurityContext(), httpServletRequest);
					return;
				} catch (BadRequestException e) {
					requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
					return;
				}
			}
			try {
                String username = null;
                // 1) Try Bearer token (JWT)
                String authHeader = requestContext.getHeaderString(RESTServlet.AUTHORIZATION);
                if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                    String bearer = authHeader.substring(7).trim();
                    try {
                        com.nimbusds.jwt.SignedJWT signedJWT = com.nimbusds.jwt.SignedJWT.parse(bearer);
                        com.nimbusds.jwt.JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

                        // prima prova a leggere la claim custom USERNAME_CNR
                        Object usernameCnrObj = claims.getClaim(RESTSecurityInterceptor.USERNAME_CNR);
                        username = null;
                        if (usernameCnrObj instanceof String) {
                            username = (String) usernameCnrObj;
                        } else {
                            username = claims.getStringClaim("preferred_username");
                        }

                        if (username != null) {
                            utenteBulk = BasicAuthentication.findUtenteBulk(username);
                        }
                    } catch (java.text.ParseException e) {
                        LOGGER.warn("Errore parsing JWT", e);
                    }
                }

				if(utenteBulk == null) {
					utenteBulk = BasicAuthentication.authenticate(httpServletRequest, authorization);
				}
				if (utenteBulk == null && !requestContext.getUriInfo().getPath().startsWith(ACCOUNT)){
					if (username != null)
						requestContext.abortWith(
								Response.status(Response.Status.UNAUTHORIZED)
										.build());
					else
						requestContext.abortWith(
							Response.status(Response.Status.UNAUTHORIZED)
									.header(HttpHeaders.WWW_AUTHENTICATE,
											AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
									.build());
					return;
				}
				if (!requestContext.getUriInfo().getPath().startsWith(ACCOUNT) &&
						!requestContext.getUriInfo().getPath().startsWith(TREE) &&
						!requestContext.getUriInfo().getPath().startsWith(CONTEXT))
					requestContext.setSecurityContext(new SIGLASecurityContext(requestContext, utenteBulk.getCd_utente()));
			} catch (UnauthorizedException e) {
				LOGGER.error("ERROR for REST SERVICE", e);
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
				return;
			} catch (Exception e) {
				LOGGER.error("ERROR for REST SERVICE", e);
				requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build());
				return;
			}			
		}		
		if (rolesAllowed != null || denyAll || permitAll) {
			if (denyAll) {
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity(UNAUTHORIZED_MAP).build());
				return;
			}
			if (permitAll) return;
		    if (rolesAllowed != null) {
				Set<String> rolesSet = new HashSet<String>(
						Arrays.asList(rolesAllowed));
				try {
					if (!isUserAllowed(utenteBulk, rolesSet)) {
						final String message = "User " + utenteBulk.getCd_utente() + " doesn't have the following roles: " + rolesSet;
						LOGGER.warn(message);
						requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity(Collections.singletonMap("ERROR", message)).build());
						return;
					}
				} catch (Exception e) {
					requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build());
				}		    	
		    }			
		}
		if (accessoAllowed != null) {
			List<String> accessi = Stream.of(accessoAllowed.value()).map(x -> x.name()).collect(Collectors.toList());
			try {
				final UserContext userPrincipal = (UserContext) requestContext.getSecurityContext().getUserPrincipal();
				if (!BasicAuthentication.loginComponentSession().isUserAccessoAllowed(
						userPrincipal,
						accessi.toArray(new String[accessi.size()]))) {
					final String message = "User " + userPrincipal.getUser() + " doesn't have the following access: " + accessi;
					LOGGER.warn(message);
					requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity(Collections.singletonMap("ERROR", message)).build());
				}
			} catch (ComponentException|RemoteException|EJBException e) {
				requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build());
			}
		}
	}
    
	private boolean isUserAllowed(final UtenteBulk utente,
			final Set<String> rolesSet) throws Exception{
		try {
			return Optional.ofNullable(BasicAuthentication.getRuoli(new RESTUserContext(), utente))
					.map(x -> x.stream())
					.get()
					.filter(x -> rolesSet.contains(x.getCd_ruolo())).count() > 0;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public Response toResponse(Exception exception) {
		if (exception.getClass().getName().equalsIgnoreCase(OPTIONS_METHOD_EXCEPTION))
			return Response.ok().build();
		if (exception.getCause() instanceof RestException)
			return Response.status(((RestException)exception.getCause()).getStatus()).entity(((RestException)exception.getCause()).getErrorMap()).build();			
		LOGGER.error("ERROR for REST SERVICE", exception);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception).build();
	}

	@Override
	public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
		final List<String> allowOrigins = Optional.ofNullable(System.getProperty(CORSFilter.CORS_ALLOW_ORIGIN))
				.filter(s -> !s.isEmpty())
				.map(s -> Arrays.asList(s.split(";")))
				.orElse(Collections.emptyList());
		Optional.ofNullable(containerRequestContext.getHeaders())
				.flatMap(s -> Optional.ofNullable(s.getFirst(CORSFilter.ORIGIN)))
				.filter(allowOrigins::contains)
				.ifPresent(s -> {
					containerResponseContext.getHeaders().add(CORSFilter.ACCESS_CONTROL_ALLOW_ORIGIN, s);
					containerResponseContext.getHeaders().add(CORSFilter.ACCESS_CONTROL_ALLOW_HEADERS, CORSFilter.ORIGIN_CONTENT_TYPE_ACCEPT_AUTHORIZATION);
					containerResponseContext.getHeaders().add(CORSFilter.ACCESS_CONTROL_ALLOW_METHODS, CORSFilter.GET_POST_OPTIONS_PUT_PATCH_DELETE);
					containerResponseContext.getHeaders().add(CORSFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE);
				});
	}
}