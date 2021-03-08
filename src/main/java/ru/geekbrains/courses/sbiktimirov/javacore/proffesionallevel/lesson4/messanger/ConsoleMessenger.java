package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleMessenger extends Messenger {
    private final Socket socket;
    private final String uuid;
    private final Scanner scanner;
    private boolean isClosed = false;

    public ConsoleMessenger(String uuid, Socket socket, Scanner scanner) {
        this.socket = socket;
        this.uuid = uuid;
        this.scanner = scanner;
    }

    public ConsoleMessenger(Socket socket) {
        this(UUID.randomUUID().toString(), socket, new Scanner(System.in));
    }

    public void startReader() {
        Thread reader = new Thread(() -> {
            while (!isClosed) {
                try {
                    Message msg = getMessage();
                    System.out.println(msg);
                } catch (Exception e) {
                    String errMsg = e.getLocalizedMessage();
                    System.out.println(errMsg == null ? "Connection is closed" : errMsg);
                    System.exit(0);
                }
            }
        });
        reader.start();
    }

    public void startWriter() {
        Thread writer = new Thread(() -> {
            while (!isClosed) {
                String line = scanner.nextLine();
                if (line.equals("/q")) {
                    close();
                } else {
                    try {
                        sendMessage(
                                new Message()
                                        .setFromUserName(uuid)
                                        .setMessage(line)
                        );
                    } catch (Exception e) {
                        System.out.println(e.getLocalizedMessage());
                        System.exit(0);
                    }
                }
            }
            System.exit(0);
        });
        writer.start();
    }

    private void close() {
        isClosed = true;
        scanner.close();
        try {
            if (getInputStream() != null) getInputStream().close();
            if (getOutputStream() != null) getOutputStream().close();
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public String getUuid() {
        return uuid;
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
