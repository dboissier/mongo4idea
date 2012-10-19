package org.codinjutsu.tools.mongo;

public class MongoConfiguration {

    private String host;

    private int port;

    private String username;

    private String password;

    private String defaultDatabase;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDefaultDatabase() {
        return defaultDatabase;
    }

    public void setDefaultDatabase(String defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
    }
}
