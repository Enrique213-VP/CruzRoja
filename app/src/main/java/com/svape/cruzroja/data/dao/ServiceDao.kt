package com.svape.cruzroja.data.dao

import androidx.room.*
import com.svape.cruzroja.data.model.Service
import com.svape.cruzroja.data.model.ServiceWithVolunteers

@Dao
interface ServiceDao {
    @Transaction
    @Query("SELECT * FROM services")
    fun getAllWithVolunteers(): List<ServiceWithVolunteers>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(service: Service): Long

    @Update
    fun update(service: Service) // Nuevo método para actualizar

    @Delete
    fun delete(service: Service) // Nuevo método para eliminar

    @Query("SELECT * FROM services")
    fun getAll(): List<Service>
}