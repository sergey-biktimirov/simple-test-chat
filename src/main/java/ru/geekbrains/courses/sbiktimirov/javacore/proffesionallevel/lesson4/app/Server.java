package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app;

import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.server.ConsoleServer;

import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException {
        ConsoleServer server = new ConsoleServer(5000);
    }
}
