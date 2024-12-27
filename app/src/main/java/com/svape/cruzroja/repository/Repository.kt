package com.svape.cruzroja.repository

import com.svape.cruzroja.data.dao.ServiceDao
import com.svape.cruzroja.data.dao.VolunteerDao
import com.svape.cruzroja.data.model.Service
import com.svape.cruzroja.data.model.ServiceWithVolunteers
import com.svape.cruzroja.data.model.Volunteer

class Repository(
    private val serviceDao: ServiceDao,
    private val volunteerDao: VolunteerDao
) {
    fun insertService(service: Service): Long = serviceDao.insert(service)

    fun updateService(service: Service) = serviceDao.update(service)

    fun deleteService(service: Service) = serviceDao.delete(service)

    fun getAllServicesWithVolunteers(): List<ServiceWithVolunteers> =
        serviceDao.getAllWithVolunteers()

    fun getAllServices(): List<Service> = serviceDao.getAll()

    fun insertVolunteer(volunteer: Volunteer): Long =
        volunteerDao.insertVolunteer(volunteer)

    fun getVolunteersByServiceId(serviceId: Int): List<Volunteer> =
        volunteerDao.getVolunteersByServiceId(serviceId)
}