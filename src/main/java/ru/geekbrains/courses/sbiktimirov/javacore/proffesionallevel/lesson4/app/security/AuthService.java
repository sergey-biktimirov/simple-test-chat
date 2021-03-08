package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.security;

import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.DBService;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.statement.QueryStatement;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.db.statement.UpdateStatement;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.Message;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.MessageType;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.Messenger;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.ResponseCode;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.server.ConsoleServer;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.server.ServerClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService extends Messenger implements Runnable {
    private final Socket socket;
    private final ConsoleServer server;
    private Connection connection;

    public AuthService(Socket socket, ConsoleServer server) {
        this.socket = socket;
        this.server = server;
        try {
            this.connection = DBService.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initUserEntity();

        //TODO Для тестов, после реализации регистрации удалить!!!
        for (int i = 1; i < 9; i++) {
            registerUser("User" + i, "User" + i);
        }
    }

    @Override
    public void run() {
        try {
            Message msg = getMessage();

            if (msg.getMessageType() == MessageType.AUTH) {
                server.logger.info("Попытка авторизации " + msg.toString());
                if (server.getClientByUsername(msg.getFromUserName()) == null) {
                    //TODO ActionType.SIGNOUT/SIGNIN
                    User user = signIn(msg.getFromUserName(), msg.getMessage());
                    if (user != null) {
                        sendSuccessSignIn(msg.getFromUserName());

                        server.addClient(
                                user,
                                new ServerClient(socket, server, user));

                        server.logger.info("Пользователь авторизовался " + msg.toString());
                    } else {
                        sendErrorSignIn(msg.getFromUserName());

                        server.logger.warning("Пользователь не авторизован " + msg.toString());
                    }
                } else {
                    sendSuccessSignIn(msg.getFromUserName());

                    server.logger.info("Пользователь <" + msg.getFromUserName() + "> уже авторизован");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            server.logger.severe(e.getMessage());
        } finally {
            DBService.getInstance().releaseConnection(connection);
        }
    }

    private User signIn(String userName, String password) {
        User user = null;
        try {
            User _user = getUser(userName.toLowerCase());
            if (_user.checkPassword(password)) user = _user;
        } catch (Exception e) {
            server.logger.info("Пользователь <" + userName + "> не найден");
        }
        return user;
    }

    //TODO SIGNOUT

    private void sendSuccessSignIn(String username) throws IOException {
        sendMessage(new Message()
                .setMessageType(MessageType.AUTH)
                .setFromUserName(server.getServerName())
                .setResponseCode(ResponseCode.OK)
                .setToUsername(username)
                .setMessage("Вы авторизованы.\n Введите \\w !имя пользователя! !сообщение! для отправки личного сообщения."));
    }

    private void sendErrorSignIn(String username) throws IOException {
        sendMessage(new Message()
                .setMessageType(MessageType.AUTH)
                .setFromUserName(server.getServerName())
                .setResponseCode(ResponseCode.ERROR)
                .setToUsername(username)
                .setMessage("Ошибка логина или пароля"));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    private void initUserEntity() {
        try {
            UpdateStatement.create().executeUpdate(connection, "create table if not exists users (id integer not null primary key autoincrement , username string unique, password string, nickName string)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registerUser(String username, String password, String nickname) {
        try {
            UpdateStatement.create().executeUpdate(connection, "insert into users (username, password, nickName) values (?, ?, ?)", username, password, nickname);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registerUser(String username, String password) {
        registerUser(username, password, username);
    }

    private User getUser(String username) {
        User user = new User();
        ResultSet rs = null;
        try {
            rs = QueryStatement.create().executeQuery(connection, "select * from users where lower(username) = ?", username);
            rs.next();
            for (Field field : User.class.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(user, rs.getString(field.getName()));
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            assert rs != null;
            try {
                rs.getStatement().close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    //TODO Для смены ника придется много изменить, поэтому задание 2 не реализовано.
    public void changeNickName(String username, String nickName) {
        try {
            UpdateStatement
                    .create()
                    .executeUpdate(connection, "update users set nicName = ? where username = ?", nickName, username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
