package sk.nmh.config;


/**
 * This interface provides the abstraction for a concrete database connection.
 * 
 */
public interface ConnectionProperty {

    public enum DRIVERCLASSTYPE {

        POSTGRE("org.postgresql.Driver"),
        ORACLE("oracle.jdbc.driver.OracleDriver"),
        MYSQL("com.mysql.jdbc.Driver"),
        DB2("com.ibm.db2.jcc.DB2Driver");

        String driver;

        DRIVERCLASSTYPE(String driverc) {
            driver = driverc;
        }

        public String getDriver() {
            return driver;
        }
    }

    public enum CONECTIONTYPE {
        MASTER,
        SLAVE;
    }

    public String getConnectionId();

    public String getIp();

    public CONECTIONTYPE getConnectionType();

    public DRIVERCLASSTYPE getDriverClass();

    public String getConnectionUrl();

    public String getUsername();

    public String getPassword();

    public int getInitialPoolSize();

    public int getMaxPoolSize();


    public void setConnectionId(String connId);

    public void setIp(String Ip);

    public void setConnectionType(CONECTIONTYPE ConnType);

    public void setDriverClass(DRIVERCLASSTYPE driverClass);

    public void setConnectionUrl(String url);

    public void setUsername(String username);

    public void setPassword(String password);

    public void setInitialPoolSize(int initPS);

    public void setMaxPoolSize(int maxPS);

}
