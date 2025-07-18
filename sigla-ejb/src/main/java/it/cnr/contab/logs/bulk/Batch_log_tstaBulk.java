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

package it.cnr.contab.logs.bulk;

import java.math.BigDecimal;

// Referenced classes of package it.cnr.contab.logs.bulk:
//            Batch_log_tstaBase

public class Batch_log_tstaBulk extends Batch_log_tstaBase
{

    public static String  LOG_TIPO_INTERF_CASS00 = "INTERF_CASS00";

    public static String  LOG_TIPO_CONTAB_COGECOAN00 = "CONTAB_COGECOAN00";

    public static String  LOG_TIPO_OBBL_PLURIENNALE = "CONTAB_OBBPLU00";

    public static String  LOG_TIPO_ACC_PLURIENNALE = "CONTAB_ACCPLU00";

    public static String  LOG_TIPO_AMMORT_BENE = "INVENT_AMMORBENE";
    public static String  LOG_TIPO_CONS_SOSTITUIVA = "CONS_SOSTIT00";

    public static String STATO_JOB_RUNNING = "R";
    public static String STATO_JOB_COMPLETE = "C";
    public static String STATO_JOB_ERROR = "E";

    public Batch_log_tstaBulk()
    {
    }

    public Batch_log_tstaBulk(BigDecimal bigdecimal)
    {
        super(bigdecimal);
    }
}