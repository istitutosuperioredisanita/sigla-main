DROP ALIAS IF EXISTS ibmutl001_getLocalTransactionID;
CREATE ALIAS PUBLIC.IBMUTL001_GETLOCALTRANSACTIONID AS $$
String execute(java.sql.Connection connection, boolean createTransaction) throws Exception {
    try (java.sql.Statement stmt = connection.createStatement()) {
        java.sql.ResultSet rs = stmt.executeQuery("SELECT SESSION_ID() FROM INFORMATION_SCHEMA.SESSIONS");
        if (rs.next()) {
            return rs.getString(1);
        } else {
            return "0"; // se non trova la sessione
        }
    }
}
$$
