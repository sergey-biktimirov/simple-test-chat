package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.server;

import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.security.AuthService;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.security.User;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.Message;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class ConsoleServer extends Thread {

    private final ServerSocket serverSocket;
    private final String serverName;
    private boolean isClosed = false;
    private ExecutorService executorService = Executors.newFixedThreadPool(30);
    private final HashMap<String, ServerClient> clients = new HashMap<>();
    public Logger logger = Logger.getLogger(ConsoleServer.class.getName());

    {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setEncoding(StandardCharsets.UTF_8.toString());
        logger.addHandler(consoleHandler);
    }

    public ConsoleServer(String serverName, int port) throws IOException {
        serverSocket = new ServerSocket(port);
        this.serverName = serverName;
        start();
    }

    public ConsoleServer(int port) throws IOException {
        this("serverName", port);
    }

    public void start() {
        super.start();
        //TODO Обработка команд с консоли
    }

    public String getServerName() {
        return serverName;
    }

    public ServerClient getClientByUsername(String username) {
        return clients.get(username.toLowerCase());
    }

    public void sendMessage(Message msg) {
//        new Thread(() -> {
        String fromUserName = msg.getFromUserName();
        String toUsername = msg.getToUsername() == null ? "all" : msg.getToUsername();

        try {
            if (toUsername.equals("all")) {
                clients.forEach((s, serverClient) -> {
                    try {
                        serverClient.sendMessage(msg);

                        logger.info("Сообщение для всех " + msg.toString());
                    } catch (IOException e) {
                        logger.info("Не удалось отправить сообщение пользователю " + serverClient.getUserName());
                        removeClient(serverClient.getUserName());
                    }
                });
            } else {
                ServerClient fromClient = getClientByUsername(fromUserName);
                ServerClient toClient = getClientByUsername(toUsername);

                Message clbckMsg = new Message()
                        .setFromUserName(fromUserName)
                        .setToUsername(fromUserName)
                        .setMessage(msg.getMessage())
                        .setMessageType(MessageType.MESSAGE);

                fromClient.sendMessage(clbckMsg);

                if (toClient == null) {
                    String _msg = "Пользователь <" + toUsername + "> не найден среди клиентов";
                    Message infMsg = new Message()
                            .setFromUserName(getServerName())
                            .setToUsername(fromUserName)
                            .setMessage(_msg)
                            .setMessageType(MessageType.INFO);

                    fromClient.sendMessage(infMsg);

                    logger.warning(_msg);
                } else {
                    toClient.sendMessage(msg);
                    logger.info("Сообщение для <" + msg.getToUsername() + "> -> " + msg.toString());
                }
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
//        }).start();
    }

    private void userConnected(ServerClient serverClient) {
        String _msg = "<" + serverClient.getUserName() + "> подключился к чату";

        Message msg = new Message()
                .setMessageType(MessageType.INFO)
                .setMessage(_msg);

        sendMessage(msg);

        logger.info(_msg);
    }

    private void userDisconnected(String username) {
        String _msg = "<" + username + "> вышел из чата";

        Message msg = new Message()
                .setMessageType(MessageType.INFO)
                .setMessage(_msg);

        sendMessage(msg);

        logger.info(_msg);
    }

    synchronized public void addClient(User user, ServerClient serverClient) {
        String username = user.getUsername();
        clients.put(username.toLowerCase(), serverClient);

        userConnected(serverClient);

        logger.info("Пользователь <" + username + "> добавлен в список");
    }

    synchronized public void removeClient(String username) {
        clients.remove(username.toLowerCase());

        userDisconnected(username);

        logger.info("Пользователь <" + username + "> удален из списка");
    }

    synchronized public HashMap<String, ServerClient> getClients() {
        return clients;
    }

    @Override
    public void run() {
        logger.info("Сервер запущен.");
        while (!isClosed) {
            try {
//                new Thread(new AuthService(serverSocket.accept(), this)).start();
                executorService.execute(new AuthService(serverSocket.accept(), this));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }
}
