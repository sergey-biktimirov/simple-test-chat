package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

public class ChatView {

    public static BorderPane getView() {
        try {
            FXMLLoader loader = new FXMLLoader(ChatView.class.getResource("chat.fxml"));

            BorderPane view = loader.load();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            BorderPane view = new BorderPane();
            return view;
        }
    }


}
