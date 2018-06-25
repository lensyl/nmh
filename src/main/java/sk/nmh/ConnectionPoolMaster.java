package sk.nmh;

import java.net.UnknownHostException;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.nmh.config.ConnectionProperty;

public class ConnectionPoolMaster extends ConnectionPool {

    /** The Constant log. */
    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolMaster.class);

    public ConnectionPoolMaster(ConnectionProperty prop) {
        super(prop);
    }

    protected void doService() {

        if (!checkServer(prop.getIp())) {
            setChanged();
            notifyObservers("DOWN");
            return;
        }
        try {
            if (!checkDbConnection()) {
                setChanged();
                notifyObservers("DOWN");
                reconnect();
            } else {
                setChanged();
                notifyObservers("UP");
                log.info("Connection pool MASTER running !!! ");
            }
        } catch (SQLException | UnknownHostException e) {
            setChanged();
            notifyObservers("DOWN");
        }

    }

    private boolean checkDbConnection() throws SQLException {
        if (refConnection != null) {
            return refConnection.isValid(2);
        } else {
            refConnection = createConnection();
            setConnected(true);
            return true;
        }
    }

}
