package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.statement;

import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.DBService;

import java.sql.*;

public class QueryStatement {

    private QueryStatement() {

    }

    public static QueryStatement create() {
        return new QueryStatement();
    }

    public ResultSet executeQuery(Connection connection, String query, String... args) throws SQLException {
        PreparedStatement stmt = null;
        try {
            if (connection != null) {
                stmt = connection.prepareStatement(query);
                for (int i = 0; i < args.length; i++) {
                    stmt.setString(i + 1, args[i]);

                }
            }
        } finally {
            DBService.getInstance().releaseConnection(connection);
        }
        assert stmt != null;
        return stmt.executeQuery();
    }

}
