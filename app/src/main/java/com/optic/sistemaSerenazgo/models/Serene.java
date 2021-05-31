package com.optic.sistemaSerenazgo.models;

public class Serene {

    // MODELO PARA CONSUMIR DATOS DEL SERENO

    String id;
    String name;
    String email;
    String image;

    public Serene() {

    }

    public Serene(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Serene(String id, String name, String email, String image) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
