package com.example.marcello.applam.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.marcello.applam.data.Rilevazione;

import java.util.List;

@Dao
public interface RilevazioneDao {
    @Insert
    public Long nuovaRilevazione(Rilevazione rilevazione);

    @Update
    public void aggiornaRilevazione (Rilevazione rilevazione);

    @Delete
    public  void cancellaRilevazione (Rilevazione rilevazione);

    @Query("SELECT * FROM Rilevazione WHERE tipoConn = :TipoC")
    public List<Rilevazione> trovaRilevazioneByTipo (String TipoC);
}
