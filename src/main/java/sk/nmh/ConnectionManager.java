package sk.nmh;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.nmh.config.ConnectionConfig;
import sk.nmh.config.ConnectionProperty;

public class ConnectionManager implements Observer {
    /** The Constant log. */
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    /** The instance. */
    private static volatile ConnectionManager instance = null;

    private boolean running = false;

    private boolean finished = false;

    /** The Constant MANAGER_SLEEP_INTERVAL. */
    private static final int MANAGER_SLEEP_INTERVAL = 10000;

    private ConnectionPool activePool = null;
    private ConnectionPool masterPool = null;
    private ConnectionPool slavePool = null;

    /** Instantiates a new ConnectionManager. */
    public ConnectionManager() {

    }


    public static void close() {
        ConnectionManager instance = getInstance();

        if (instance.finished == false) {

            log.info("Exitting ConnectionManager...");

            if (instance.masterPool != null)
                instance.masterPool.stopService();

            if (instance.slavePool != null)
                instance.slavePool.stopService();
        }
        instance.running = false;
        instance.finished = true;
    }


    public synchronized void startAll() {
        masterPool.start();
        slavePool.start();
        running = true;

    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public void initialize(String[] args) {

        log.debug("ConnectionManager initializing...");
        if (args.length == 1) {
            // register cez parameter
            ConnectionConfig.getInstance(args[0]);
        } else {
            ConnectionConfig.getInstance();
        }

        for (ConnectionProperty prop : ConnectionConfig.getInstance().getConnections()) {

            switch (prop.getConnectionType()) {
                case MASTER:
                    masterPool = new ConnectionPoolMaster(prop);
                    masterPool.addObserver((Observer) this);
                    break;
                case SLAVE:
                    slavePool = new ConnectionPoolSlave(prop);
                    slavePool.addObserver((Observer) this);
                    break;
            }
        }
        if (slavePool != null)
            masterPool.addObserver((Observer) slavePool);

    }

    public synchronized Connection getConnection() throws SQLException {
        if (activePool == null)
            throw new SQLException("Couldn't obtain a connection.");
        return activePool.getConnection();
    }

    public synchronized void freeConnection(Connection conn) throws SQLException {
        if (activePool != null)
            activePool.freeConnection(conn);
    }


    public synchronized void setActivePool(ConnectionPool activePool) {
        this.activePool = activePool;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ConnectionPoolMaster) {
            log.debug("Conection pool MASTER: " + arg);
            if ("UP".equals(arg.toString()))
                setActivePool(masterPool);
            else
                setActivePool(null);
        } else if (o instanceof ConnectionPoolSlave) {
            log.debug("Conection pool SLAVE: " + arg);
            if ("UP".equals(arg.toString()))
                setActivePool(slavePool);
            else
                setActivePool(null);
        }

    }

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() { // shutdown thread
                this.setName("ConnectionManager shutdown");
                close();
            }
        });

        try {
            log.info("Starting ConnectionManager...");
            ConnectionManager cm = getInstance();
            cm.initialize(args);
            cm.startAll();

            while (cm.running) {
                try {
                    Thread.sleep(MANAGER_SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    //
                }
            }


        } finally {
            close();
            log.info("ConnectionManager stopped");
        }
    }

}
