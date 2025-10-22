DROP ALIAS IF EXISTS ibmutl001_trace_user_connection;
CREATE ALIAS ibmutl001_trace_user_connection AS $$
void execute(java.sql.Connection connection, String aUser, java.sql.Date aTSNow, String aHTTPSID) throws Exception {

}
$$;
