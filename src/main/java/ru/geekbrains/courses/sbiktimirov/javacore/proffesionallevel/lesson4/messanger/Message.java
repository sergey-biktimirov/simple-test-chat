package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.messanger;

import com.google.gson.Gson;

import java.io.Serializable;

public class Message implements Serializable {
    private String fromUserName;
    private String toUsername;
    private String message;
    private MessageType messageType;
    private ResponseCode responseCode;

    public String getFromUserName() {
        return fromUserName;
    }

    public Message setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
        return this;
    }

    public String getToUsername() {
        return toUsername;
    }

    public Message setToUsername(String toUsername) {
        this.toUsername = toUsername;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Message setMessage(String message) {
        this.message = message;
        return this;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Message setMessageType(MessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public Message setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
