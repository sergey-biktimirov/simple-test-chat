package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.statement;

import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.DBService;

import java.sql.*;

public class UpdateStatement {

    private UpdateStatement() {

    }

    public static UpdateStatement create() {
        return new UpdateStatement();
    }

    public void executeUpdate(Connection connection, String query) throws SQLException {
        Statement stmt = null;
        try {
            if (connection != null) {
                stmt = connection.createStatement();
                stmt.executeUpdate(query);
            }
        } finally {
            DBService.getInstance().releaseConnection(connection);
        }
    }

    public void executeUpdate(Connection connection, String query, String... args) throws SQLException {
        PreparedStatement stmt;
        try {
            if (connection != null) {
                stmt = connection.prepareStatement(query);
                for (int i = 0; i < args.length; i++) {
                    stmt.setString(i + 1, args[i]);
                }
                stmt.addBatch();
                stmt.executeBatch();
            }
        } finally {
            DBService.getInstance().releaseConnection(connection);
        }
    }
}
