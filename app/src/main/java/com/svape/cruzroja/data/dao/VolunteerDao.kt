package com.svape.cruzroja.data.dao

import androidx.room.*
import com.svape.cruzroja.data.model.Volunteer
import kotlinx.coroutines.flow.Flow

@Dao
interface VolunteerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVolunteer(volunteer: Volunteer): Long  // Método sin corutina

    @Query("SELECT * FROM volunteers WHERE serviceId = :serviceId")
    fun getVolunteersByServiceId(serviceId: Int): List<Volunteer>
}