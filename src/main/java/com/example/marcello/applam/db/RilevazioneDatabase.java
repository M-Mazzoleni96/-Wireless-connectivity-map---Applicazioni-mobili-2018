package com.example.marcello.applam.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import com.example.marcello.applam.dao.RilevazioneDao;
import com.example.marcello.applam.data.Rilevazione;

@Database(
        entities = {Rilevazione.class},
        version = 1
)
public abstract class RilevazioneDatabase extends RoomDatabase {
    private static RilevazioneDatabase database;
    public abstract RilevazioneDao getRilevazioneDao();

    public static RilevazioneDatabase getInstance(Context context) {
        if(database==null){
            database = Room.databaseBuilder(
                    context,
                    RilevazioneDatabase.class,
                    "database-rilevazioni.db"
            ).build();
        }
        return database;
    }
}
