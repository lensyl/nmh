package sk.nmh.config;

import java.io.Serializable;

public class ConnectionPropertyImpl implements ConnectionProperty, Serializable {

    private static final long serialVersionUID = 7462299013208883734L;

    public static long getSerialversionUID() {
        return serialVersionUID;
    }

    private String connectionId;
    private String Ip;
    private CONECTIONTYPE connectionType;
    private DRIVERCLASSTYPE driverClass;
    private String connectionUrl;
    private String username;
    private String password;
    private int initialPoolSize;
    private int maxPoolSize;

    public ConnectionPropertyImpl() {}

    public ConnectionPropertyImpl(String connectionId, CONECTIONTYPE connectionType,
            DRIVERCLASSTYPE driverClass, String connectionUrl, String username, String password,
            int initialPoolSize) {
        this.connectionId = connectionId;
        this.connectionType = connectionType;
        this.driverClass = driverClass;
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;
        this.initialPoolSize = initialPoolSize;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConnectionPropertyImpl other = (ConnectionPropertyImpl) obj;
        if (connectionUrl == null) {
            if (other.connectionUrl != null)
                return false;
        } else if (!connectionUrl.equals(other.connectionUrl))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((connectionUrl == null) ? 0 : connectionUrl.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public CONECTIONTYPE getConnectionType() {
        return connectionType;
    }

    @Override
    public DRIVERCLASSTYPE getDriverClass() {
        return driverClass;
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    @Override
    public void setConnectionId(String connId) {
        this.connectionId = connId;

    }

    @Override
    public void setConnectionType(CONECTIONTYPE connType) {
        this.connectionType = connType;

    }

    @Override
    public void setDriverClass(DRIVERCLASSTYPE driverClass) {
        this.driverClass = driverClass;

    }

    @Override
    public void setConnectionUrl(String url) {
        this.connectionUrl = url;

    }

    @Override
    public void setUsername(String username) {
        this.username = username;

    }

    @Override
    public void setPassword(String password) {
        this.password = password;

    }

    @Override
    public void setInitialPoolSize(int initPS) {
        this.initialPoolSize = initPS;

    }

    @Override
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    @Override
    public void setMaxPoolSize(int maxPS) {
        this.maxPoolSize = maxPS;

    }

    @Override
    public String getIp() {
        return Ip;
    }

    @Override
    public void setIp(String Ip) {
        this.Ip = Ip;
    }

}
