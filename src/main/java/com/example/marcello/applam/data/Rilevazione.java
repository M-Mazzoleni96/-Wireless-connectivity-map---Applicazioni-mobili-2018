package com.example.marcello.applam.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Rilevazione {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String tipoConn;
    private int potenza;
    private double latitude;
    private double longitude;
    private Long orario;
    private int opacita;


    public int getOpacita() {
        return opacita;
    }

    public void setOpacita(int opacita) {
        this.opacita = opacita;
    }

    public Long getOrario() {
        return orario;
    }

    public void setOrario(Long orario) {
        this.orario = orario;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoConn() {
        return tipoConn;
    }

    public void setTipoConn(String tipoConn) {
        this.tipoConn = tipoConn;
    }

    public int getPotenza() {
        return potenza;
    }

    public void setPotenza(int potenza) {
        this.potenza = potenza;
    }


}
