package com.optic.sistemaSerenazgo.models;

import java.io.Serializable;

//MODELO PARA UTILIZAR EN LA LISTA DE INCIDENTE

public class Incidents implements Serializable {

    String fecha;
    String hora;
    Double lat;
    Double lng;
    String lugarDeOcurrencia;
    String modalidad;
    String numFicha;
    String afectado;
    String sereno;
    String placa;
    String resumen;

    public Incidents(){
    }

    public Incidents(String fecha, String hora, Double lat, Double lng, String lugarDeOcurrencia,
                     String modalidad, String numFicha, String afectado, String sereno, String placa,
                     String resumen) {
        this.fecha = fecha;
        this.hora = hora;
        this.lat = lat;
        this.lng = lng;
        this.lugarDeOcurrencia = lugarDeOcurrencia;
        this.modalidad = modalidad;
        this.numFicha = numFicha;
        this.afectado = afectado;
        this.sereno = sereno;
        this.placa = placa;
        this.resumen = resumen;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getLugarDeOcurrencia() {
        return lugarDeOcurrencia;
    }

    public String getModalidad() {
        return modalidad;
    }

    public String getNumFicha() {
        return numFicha;
    }

    public String getAfectado() {
        return afectado;
    }

    public String getSereno() {
        return sereno;
    }

    public String getPlaca() {
        return placa;
    }

    public String getResumen() {
        return resumen;
    }
}
