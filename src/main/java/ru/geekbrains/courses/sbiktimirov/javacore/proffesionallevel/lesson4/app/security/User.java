package ru.geekbrains.courses.sbiktimirov.javacore.proffesionallevel.lesson4.app.security;

public class User implements UserInfo {
    private String username;
    private String password;
    private String nickName;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    @Override
    public String getNickName() {
        return nickName == null ? username : nickName;
    }

    @Override
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return "User {username:\"" + getUsername() + "," +
                "nickName:\"" + getNickName() + "" +
                "}";
    }
}
