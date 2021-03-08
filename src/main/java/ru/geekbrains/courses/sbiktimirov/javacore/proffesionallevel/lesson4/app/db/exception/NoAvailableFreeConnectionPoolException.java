package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.exception;

import java.sql.SQLException;

public class NoAvailableFreeConnectionPoolException extends SQLException {
    public NoAvailableFreeConnectionPoolException(Integer pullSize) {
        super("No available free connections in connection pool, pool size is " + pullSize);
    }
}
