package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.views;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.App;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.Message;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.MessageType;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.Messenger;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.ResponseCode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class ChatController extends Messenger {

    public TextArea msgTextField;
    public Button sendMsgButton;
    public ListView<String> chatContent;
    public TextField loginField;
    public TextField passwordField;
    public Button connectButton;
    public Label passwordLabel;
    private static final ChatHistoryManager chatHistoryManager = new ChatHistoryManager();

    Logger logger = Logger.getGlobal();
    private boolean isClosedConnection = false;

    {
        try {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setEncoding(StandardCharsets.UTF_8.toString());
            logger.addHandler(consoleHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    synchronized public static ChatHistoryManager getChatHistoryManager() {
        return chatHistoryManager;
    }

    private final ObservableList<String> msgList = FXCollections.observableArrayList();

    private Socket socket;

    public void sendMessage() {
        String text = msgTextField.getText();
        if (text.length() > 0) {
            Message msg = new Message();
            if (text.startsWith("\\w")) {
                String[] strings = text.split("\\s+");
                String username = strings[1];
                String _msg = String.join(" ", Arrays.copyOfRange(strings, 2, strings.length));
                msg.setToUsername(username).setMessage(_msg);
            } else {
                msg.setMessage(text);
            }

            msg.setFromUserName(loginField.getText()).setMessageType(MessageType.MESSAGE);
            try {
                super.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        msgTextField.clear();
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 5000);

            Message msg = new Message()
                    .setFromUserName(loginField.getText())
                    .setMessageType(MessageType.AUTH)
                    .setMessage(passwordField.getText());

            super.sendMessage(msg);

            msg = getMessage();

            printMessage(msg);

            chatContent.scrollTo(msgList.size() - 1);

            setClosedConnection(false);

            if (msg.getResponseCode() == ResponseCode.ERROR) {
                //TODO Сообщение об ошибке подключения и зачистка пароля
            } else {
                hideLoginPanel();
                startReader();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startReader() {
        Thread reader = new Thread(() -> {
            while (!isClosedConnection()) {
                //TODO Обработка MessageType.*
                try {
                    Message msg = getMessage();

                    printMessage(msg);
                } catch (Exception e) {
                    String errMsg = e.getLocalizedMessage();

                    disconnect();
                    //TODO Сообщение об ошибке и переход к экрану ввода логина и пароля

                    logger.info(errMsg);
                    break;
                }
            }
        });
        reader.start();

        new Thread(() -> {
            while (!App.isClosed()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!reader.isInterrupted()) {
                reader.interrupt();
            }
        }).start();
    }

    private void hideLoginPanel() {
        Platform.runLater(() -> {
            loginField.setEditable(false);
            passwordField.setVisible(false);
            passwordLabel.setVisible(false);
            connectButton.setOnAction(event -> disconnect());
            connectButton.setText("Выход");
        });
    }

    private void showLoginPanel() {
        Platform.runLater(() -> {
            loginField.setEditable(true);
            passwordField.clear();
            passwordField.setVisible(true);
            passwordLabel.setVisible(true);
            connectButton.setOnAction(event -> connect());
            connectButton.setText("Вход");
        });
    }

    private void disconnect() {
        try {
            getOutputStream().close();
            getInputStream().close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setClosedConnection(true);
        showLoginPanel();
    }

    synchronized public boolean isClosedConnection() {
        return isClosedConnection;
    }

    synchronized public void setClosedConnection(boolean closedConnection) {
        isClosedConnection = closedConnection;
    }

    private void printMessage(Message msg) {

        chatHistoryManager.writeMessageToFile(msg);

        Platform.runLater(() -> {

            msgList.add(convertMessage(msg));

            chatContent.scrollTo(msgList.size() - 1);
        });
    }

    private String convertMessage(Message msg) {
        String message;

        if (msg.getMessageType() == MessageType.INFO) {
            message = "Инфо! : " + msg.getMessage();
        } else if (msg.getMessageType() == MessageType.MESSAGE) {
            String fromUser = msg.getFromUserName().toLowerCase().equals(loginField.getText().toLowerCase())
                    ? "            Я"
                    : msg.getFromUserName();
            message = "<" + fromUser + ">: " + msg.getMessage();
        } else {
            message = msg.getMessageType() + " " + msg.getResponseCode() + " :" + msg.getMessage();
        }

        return message;
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            chatContent.setItems(msgList);

            chatContent.scrollTo(msgList.size() - 1);
        });

        Message[] hist = chatHistoryManager.readeLastMessagesFromFile();
        for (Message message : hist) {
            if (message != null) {
                msgList.add(convertMessage(message));
            }
        }

        sendMsgButton.setOnAction(event -> sendMessage());
        sendMsgButton.setDefaultButton(true);

        msgTextField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        sendMessage();

                        ((TextArea) event.getTarget()).clear();
                    }
                }
        );

        connectButton.setOnAction(event -> connect());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
}
