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

package it.cnr.contab.web.rest.local.config00;

public interface DefaultContrattoLocal {

    public enum ApiVersion {
        V1("v1.0"),
        V2("v2.0");

        private final String version;

        private ApiVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }

        public static ApiVersion getValueFrom(String version) {
            for (ApiVersion apiVersion : ApiVersion.values()) {
                if (apiVersion.version.equals(version))
                    return apiVersion;
            }
            throw new IllegalArgumentException("No found for version: " + version);
        }

    }


}
