package com.svape.cruzroja.model

import java.util.Date

data class Service(
    val serviceName: String,
    val date: Date,
    val volunteers: List<Volunteer>,
    val imageUri: String
)