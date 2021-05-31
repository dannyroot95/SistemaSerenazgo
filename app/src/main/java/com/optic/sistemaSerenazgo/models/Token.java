package com.optic.sistemaSerenazgo.models;

public class Token {

    //MODELO PARA CONSUMIR EN UN PROVIDER Y GENERAR TOKENS

    String token;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
