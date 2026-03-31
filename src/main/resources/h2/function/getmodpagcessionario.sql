DROP ALIAS IF EXISTS getModPagCessionario;
CREATE ALIAS getModPagCessionario AS $$
String execute(java.sql.Connection conn, int aCdTerzo, String aTiPagamento) throws Exception {
    String result = null;
    String sql =
        "SELECT DISTINCT A.cd_modalita_pag " +
        "FROM RIF_MODALITA_PAGAMENTO B, MODALITA_PAGAMENTO A " +
        "WHERE B.ti_pagamento = ? " +
        "AND A.cd_modalita_pag = B.cd_modalita_pag " +
        "AND A.cd_terzo = ? " +
        "AND A.cd_terzo_delegato IS NULL " +
        "AND A.dacr = ( " +
            "SELECT MAX(A1.dacr) " +
            "FROM RIF_MODALITA_PAGAMENTO B1, MODALITA_PAGAMENTO A1 " +
            "WHERE B1.ti_pagamento = ? " +
            "AND A1.cd_modalita_pag = B1.cd_modalita_pag " +
            "AND A1.cd_terzo = ? " +
            "AND A1.cd_terzo_delegato IS NULL) " +
        "ORDER BY A.cd_modalita_pag ASC";

    try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, aTiPagamento);
        ps.setInt(2, aCdTerzo);
        ps.setString(3, aTiPagamento);
        ps.setInt(4, aCdTerzo);

        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                result = rs.getString(1);
            }
        }
    }
    return result;
}
$$;