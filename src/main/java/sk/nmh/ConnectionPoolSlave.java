package sk.nmh;

import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.nmh.config.ConnectionProperty;

public class ConnectionPoolSlave extends ConnectionPool implements Observer {

    /** The Constant log. */
    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolMaster.class);
    private boolean wakeUp = false;

    public ConnectionPoolSlave(ConnectionProperty prop) {
        super(prop);
    }

    protected void doService() {

        if (wakeUp) {

            if (!checkServer(prop.getIp())) {
                setChanged();
                notifyObservers("DOWN");
                return;
            }

            if (!isConnected) {
                try {
                    connect();
                    setChanged();
                    notifyObservers("UP");
                    log.info("Connection pool SLAVE running !!! ");
                } catch (SQLException e) {
                    log.error("SLAVE Couldn't obtain a connection.");
                    setChanged();
                    notifyObservers("DOWN");
                }
            }

        }

        try {
        } catch (Exception ex) {
            log.error("doService#error:" + ex.getMessage());
            ex.printStackTrace();
        }

    }

    protected void finished() {
        super.finished();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;

    }


    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ConnectionPoolMaster) {
            log.debug("SLAVE info from  MASTER: " + arg);
            if ("DOWN".equals(arg.toString()) && !wakeUp) {
                wakeUp = true;
            } else if ("UP".equals(arg.toString()) && wakeUp) {
                wakeUp = false;
                closeAll();

            }
        }

    }


}
