package com.svape.cruzroja.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.svape.cruzroja.data.dao.ServiceDao
import com.svape.cruzroja.data.dao.VolunteerDao
import com.svape.cruzroja.data.model.DateConverter
import com.svape.cruzroja.data.model.Service
import com.svape.cruzroja.data.model.Volunteer

@Database(
    entities = [Service::class, Volunteer::class],
    version = 2, // Cambiado de 1 a 2
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun volunteerDao(): VolunteerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cruz_roja_database"
                )
                    .addMigrations(MIGRATION_1_2) // Añadida la migración
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla temporal con la nueva estructura
                database.execSQL(
                    """
                    CREATE TABLE services_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        serviceName TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        date INTEGER NOT NULL,
                        imageUri TEXT NOT NULL
                    )
                    """
                )

                // Copiar datos existentes
                database.execSQL(
                    """
                    INSERT INTO services_new (id, serviceName, date, imageUri)
                    SELECT id, serviceName, date, imageUri FROM services
                    """
                )

                // Eliminar tabla antigua
                database.execSQL("DROP TABLE services")

                // Renombrar tabla nueva
                database.execSQL("ALTER TABLE services_new RENAME TO services")
            }
        }
    }
}