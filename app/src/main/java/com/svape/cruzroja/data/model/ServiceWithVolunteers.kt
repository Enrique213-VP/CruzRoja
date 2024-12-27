package com.svape.cruzroja.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ServiceWithVolunteers(
    @Embedded
    val service: Service,

    @Relation(
        parentColumn = "id",
        entityColumn = "serviceId"
    )
    val volunteers: List<Volunteer>
)