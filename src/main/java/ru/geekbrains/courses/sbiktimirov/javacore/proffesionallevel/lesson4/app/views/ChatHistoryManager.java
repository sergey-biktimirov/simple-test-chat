package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.views;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatHistoryManager {

    private final String HIST_PATH = "hist/messages/";
    private BufferedWriter bwr = null;
    private int msgCount = 0;
    private final int MAX_MESSAGES_IN_FILE = 101;

    {
        if (!new File(HIST_PATH).exists()) new File(HIST_PATH).mkdirs();
    }

    private FileWriter getFileWriter() throws IOException {
        return new FileWriter(HIST_PATH + System.currentTimeMillis() + ".txt", StandardCharsets.UTF_8);
    }

    public void writeMessageToFile(Message message) {
        try {
            if (bwr == null || ++msgCount == MAX_MESSAGES_IN_FILE) {
                msgCount = 0;
                if (bwr != null) {
                    bwr.write("]");
                    bwr.flush();
                    bwr.close();
                }

                bwr = new BufferedWriter(getFileWriter());
                bwr.write("[");
            }


            bwr.write(message.toString());
            if (msgCount != MAX_MESSAGES_IN_FILE - 1) {
                bwr.write(",");
                bwr.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeMessageFileWriter() {
        try {
            bwr.write("]");
            bwr.flush();
            bwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message[] readeLastMessagesFromFile() {
        Message[] msgList = new Message[MAX_MESSAGES_IN_FILE];
        int maxMsgCnt = MAX_MESSAGES_IN_FILE;

        File[] files = new File(HIST_PATH).listFiles();
        if (files != null) {
            for (int i = files.length - 1; i >= 0; i--) {
                try {
                    FileReader fileReader = new FileReader(files[i], StandardCharsets.UTF_8);
                    JsonReader jsonReader = new JsonReader(fileReader);
                    List<Message> messages = new Gson().fromJson(jsonReader, new TypeToken<List<Message>>() {
                    }.getType());


                    maxMsgCnt -= messages.size();
                    int start = maxMsgCnt;

                    for (Message message : messages) {
                        if (start >= 0) {
                            msgList[start] = message;
                        }

                        start++;
                    }
                    if (maxMsgCnt == 0) break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return msgList;
    }
}
