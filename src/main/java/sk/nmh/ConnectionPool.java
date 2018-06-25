package sk.nmh;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.nmh.config.ConnectionProperty;

public abstract class ConnectionPool extends Observable implements Runnable {

    /** The Constant log. */
    static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    static final int RECONNECT_TRY_INTERVAL = 5000;

    protected ConnectionProperty prop;
    protected int delay = 5000; // ms

    protected boolean isConnected = false;


    protected LinkedList<Connection> freeConnections =
            new LinkedList<Connection>(new ArrayList<Connection>());
    protected LinkedList<Connection> busyConnections =
            new LinkedList<Connection>(new ArrayList<Connection>());

    protected Connection refConnection = null;


    /** The counter. */
    protected int counter;


    /** The thread. */
    protected Thread thread;


    protected boolean running = true;


    public ConnectionPool(ConnectionProperty prop) {
        this.prop = prop;
    }


    protected boolean checkServer(String ip) {
        InetAddress inet;

        try {
            inet = InetAddress.getByAddress(InetAddress.getByName(ip).getAddress());
            return (inet.isReachable(3000) ? true : false);

        } catch (IOException e) {
            log.error(String.format("ConnectionPool#checkServer {%s}", ip), e.getMessage());
            return false;
        }

    }


    protected void connect() throws SQLException {
        for (int i = 0; i < prop.getInitialPoolSize(); i++) {
            freeConnections.add(createConnection());
        }
    }

    /**
     * Start.
     */
    public synchronized final void start() {
        if (!isAlive()) {

            log.info("Starting service: " + prop.getConnectionId());
            thread = new Thread(this);
            thread.setName(this.prop.getConnectionId());
            thread.setDaemon(true);
            thread.start();
        } else {
            log.info("ConnectionPool " + prop.getConnectionId() + " is already running.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        try {
            setRunning(true);
            initialize();

            for (counter = 1; isRunning(); counter++) { // main loop
                doService();
                delay();
            }

        } finally {
            stoppingService();
        }
    }

    /**
     * Initialize.
     *
     * @throws 
     */
    protected void initialize() {
        // override if needed
    }


    /**
     * Do service.
     *
     * @throws
     */
    protected abstract void doService();


    protected void delay() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if is alive.
     *
     * @return true, if is alive
     */
    boolean isAlive() {
        if (thread != null) {
            return this.thread.isAlive();
        } else
            return false;
    }


    /**
     * Stop service.
     */
    public synchronized void stopService() {
        if (isAlive()) {
            log.info("Waiting for service stop...");
            this.setRunning(false);
        }
    }

    /**
     * Stopping service.
     */
    private void stoppingService() {
        log.info("Service stopped");
        this.setRunning(false);
        this.finished();
        this.thread = null;
        synchronized (this) {
            ConnectionPool.this.notify();
        }
    }

    protected void finished() {
        closeAll();
    }

    public boolean isRunning() {
        return running;
    }

    protected void setRunning(boolean running) {
        this.running = running;
    }


    public synchronized void closeAll() {
        try {
            close(freeConnections);
            freeConnections.clear();
            close(busyConnections);
            busyConnections.clear();
            if (refConnection != null) {
                refConnection.close();
            }
        } catch (SQLException e) {
            log.error("ConnectionPool#closeAll", e.getMessage());
        }
    }

    private void close(LinkedList<Connection> connections) throws SQLException {
        for (Connection conn : connections) {
            if (!conn.isClosed()) {
                conn.close();
            }
        }
    }

    public synchronized void freeConnection(Connection connection) {
        busyConnections.remove(connection);
        freeConnections.addLast(connection);
    }

    protected Connection createConnection() throws SQLException {
        try {

            Class.forName(prop.getDriverClass().getDriver());
            Connection connection = DriverManager.getConnection(prop.getConnectionUrl(), prop.getUsername(),
                    prop.getPassword());

            return (connection);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException(
                    "ConnectionPool#createConnection SQLException encountered:"
                            + e.getMessage());
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (!freeConnections.isEmpty()) {
            Connection existingConnection = freeConnections.getFirst();
            freeConnections.removeFirst();
            if (existingConnection.isClosed()) {
                return (getConnection());
            } else {
                busyConnections.add(existingConnection);
                return (existingConnection);
            }
        } else {

            if ((totalConnections() < prop.getMaxPoolSize())) {
                Connection newConn = createConnection();
                busyConnections.add(newConn);
                return newConn;
            } else {
                throw new SQLException("Connection limit was exceeded");
            }
        }
    }

    public synchronized int totalConnections() {
        return (freeConnections.size() + busyConnections.size());
    }

    public void reconnect() throws UnknownHostException, SQLException {
        log.info("ConnectionPool#reconnect");
        if (isConnected()) {
            try {
                closeAll();
            } catch (Exception ee) {
                // do nothing if not successful
            }
        }
        // loop waiting for opening connection
        while (!this.isConnected()) {
            connect();
            try {
                Thread.sleep(RECONNECT_TRY_INTERVAL);
            } catch (InterruptedException ie) {
                // do nothing if interrupted
            }
        } // end while loop
    }

    /**
     * Checks if is connected.
     *
     * @return true, if is connected
     */
    public synchronized boolean isConnected() {
        return isConnected;
    }

    public synchronized void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }



}
