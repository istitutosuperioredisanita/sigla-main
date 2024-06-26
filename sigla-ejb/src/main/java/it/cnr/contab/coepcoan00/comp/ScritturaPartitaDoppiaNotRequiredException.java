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

package it.cnr.contab.coepcoan00.comp;

import it.cnr.jada.bulk.ValidationException;

/**
 * Eccezione lanciata dalla contabilizzazione delle prime note.
 * Indica che per il documento non è prevista in assoluto alcuna contabilizzazione e quindi
 * il flag stato_coge viene settato a "X"
 */
public class ScritturaPartitaDoppiaNotRequiredException extends ValidationException {
    public ScritturaPartitaDoppiaNotRequiredException()
    {
    }

    public ScritturaPartitaDoppiaNotRequiredException(String s)
    {
        super(s);
    }
}
