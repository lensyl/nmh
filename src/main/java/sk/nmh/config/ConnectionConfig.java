package sk.nmh.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import flexjson.JSON;
import flexjson.JSONDeserializer;

/**
 * Trieda na cita databazove konfiguracie z konfiguracneho suboru
 *  
 * @author Sylvia
 *
 */
public class ConnectionConfig {

    private static ConnectionConfig instance;
    private static final Logger log = LoggerFactory.getLogger(ConnectionConfig.class);
    public static final String REGISTER_PATH = "register.config";

    @JSON(include = false)
    private List<ConnectionProperty> connections = new ArrayList<ConnectionProperty>();

    private ConnectionConfig() {
        /* singleton class */
    }

    public static ConnectionConfig getInstance() {
        if (instance != null)
            return instance;
        ClassLoader classLoader = ConnectionConfig.class.getClassLoader();
        File file = new File(classLoader.getResource(REGISTER_PATH).getFile());

        return instance = new ConnectionConfig().init(file);
    }

    public static ConnectionConfig getInstance(String configFile) {
        if (instance != null)
            return instance;
        File f = new File(configFile);
        return instance = new ConnectionConfig().init(f);
    }

    public ConnectionConfig init(File configFile) {
        log.info("ConnectionConfig#init()");


        if (configFile.exists()) {
            log.info("ConnectionConfig#init() :: configuration found on disk, loading from file.");

            ConnectionConfig config = ConnectionConfig.load(configFile);
            return config;
        } else {
            log.error("Register file (" + configFile.getPath() + ") not found...");
        }

        return this;
    }

    private static ConnectionConfig load(File configFile) {
        JSONDeserializer<ConnectionConfig> serializer = new JSONDeserializer<ConnectionConfig>();
        ConnectionConfig result = null;
        try {
            result = serializer.use("connections", ArrayList.class)
                    .use("connections.values", ConnectionPropertyImpl.class)
                    .deserialize(
                            new String(Files.readAllBytes(java.nio.file.Paths.get(configFile.getPath()))));
        } catch (IOException e) {
            log.error("ConnectionConfig#load()", e);
        }

        return result;
    }

    public List<ConnectionProperty> getConnections() {
        return connections;
    }

    public void setConnections(List<ConnectionProperty> connections) {
        this.connections = connections;
    }

    public ConnectionProperty findByDefaultConnection() {
        return connections.stream()
                .filter(conn -> conn.getConnectionType() == ConnectionProperty.CONECTIONTYPE.MASTER)
                .findFirst()
                .orElse(null);
    }

    public ConnectionProperty findByConnectionId(String connId) {
        return connections.stream()
                .filter(conn -> conn.getConnectionId().equals(connId))
                .findFirst()
                .orElse(null);
    }

}
