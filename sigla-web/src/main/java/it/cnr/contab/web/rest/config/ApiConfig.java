/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                title = "SIGLA REST API",
                description = "A collection of SIGLA Rest API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Marco Spasiano",
                        email = "marco.spasiano@cnr.it"
                ),
                license = @License(
                        name = "GNU AFFERO GENERAL PUBLIC LICENSE",
                        url = "https://www.gnu.org/licenses/agpl-3.0.html"
                )
        )
)
@SecurityScheme(
        name = "BASIC",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
@SecurityScheme(
        name = SIGLASecurityContext.X_SIGLA_ESERCIZIO,
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = SIGLASecurityContext.X_SIGLA_ESERCIZIO
)
@SecurityScheme(
        name = SIGLASecurityContext.X_SIGLA_CD_CDS,
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = SIGLASecurityContext.X_SIGLA_CD_CDS
)
@SecurityScheme(
        name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA,
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA
)
@SecurityScheme(
        name = SIGLASecurityContext.X_SIGLA_CD_CDR,
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = SIGLASecurityContext.X_SIGLA_CD_CDR
)
@Tag(name = "SIGLA REST API")
public interface ApiConfig {
}
