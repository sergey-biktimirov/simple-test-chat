package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db;

import com.gluonhq.charm.down.common.PlatformFactory;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.exception.NoAvailableFreeConnectionPoolException;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DBService {
    private static DBService instance = null;
    private static final String DB_NAME = "chat";
    private static Connection connection;
    private static String dbUrl = "jdbc:sqlite:";
    private static int connectionPoolSize = 10;
    private static List<Connection> freeConnectionList = new ArrayList<>();
    private static List<Connection> usedConnectionList = new ArrayList<>();
    private static AtomicBoolean availableFreeConnections = new AtomicBoolean(true);

    private DBService() {
        try {
            File dir = PlatformFactory.getPlatform().getPrivateStorage();
            File db = new File(dir, DB_NAME);
            dbUrl = dbUrl + db.getAbsolutePath();
        } catch (IOException ex) {
            System.out.println("Error " + ex);
        }
    }

    public static DBService getInstance() {
        return Objects.requireNonNullElseGet(instance, () -> instance = new DBService());
    }

    public Connection createConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl);
        } catch (SQLException ex) {
            System.out.println("Error establishing connection " + ex);
        }
        return connection;
    }

    synchronized public Connection getConnection() throws SQLException {
        if (!availableFreeConnections.get()) {
            throw new NoAvailableFreeConnectionPoolException(connectionPoolSize);
        }

        Connection connection = null;

        try {
            connection = freeConnectionList.remove(freeConnectionList.size() - 1);
        } catch (Exception ignored) {
        }

        if (connection == null) {
            if (usedConnectionList.size() < connectionPoolSize)
                connection = createConnection();
            else {
                availableFreeConnections.set(false);
                throw new NoAvailableFreeConnectionPoolException(connectionPoolSize);
            }
        } else if (connection.isClosed()) {
            connection = createConnection();
        }
        usedConnectionList.add(connection);
        return connection;
    }

    synchronized public boolean releaseConnection(Connection connection) {
        freeConnectionList.add(connection);
        boolean b = usedConnectionList.remove(connection);
        availableFreeConnections.set(true);
        return b;
    }

    public void executeUpdate(Connection connection, String query) throws SQLException {
        Statement stmt = null;
        try {
            if (connection != null) {
                stmt = connection.createStatement();
                stmt.executeUpdate(query);
            }
        } finally {
            try {
                assert stmt != null;
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void executeUpdate(Connection connection, String query, String... args) throws SQLException {
        PreparedStatement stmt = null;
        try {
            if (getConnection() != null) {
                stmt = getConnection().prepareStatement(query);
                for (int i = 0; i < args.length; i++) {
                    stmt.setString(i + 1, args[i]);
                }
                stmt.addBatch();
                stmt.executeBatch();
            }
        } finally {
            try {
                assert stmt != null;
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public ResultSet executeQuery(Connection connection, String query, String... args) throws SQLException {
        PreparedStatement stmt = getConnection().prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            stmt.setString(i + 1, args[i]);
        }
        return stmt.executeQuery();
    }
}
