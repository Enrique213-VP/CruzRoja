package com.svape.cruzroja.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.cruzroja.data.model.Service
import com.svape.cruzroja.data.model.ServiceWithVolunteers
import com.svape.cruzroja.data.model.Volunteer
import com.svape.cruzroja.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class ServiceViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _services = MutableLiveData<List<ServiceWithVolunteers>>()
    val services: LiveData<List<ServiceWithVolunteers>> = _services
    var tempServiceName: String = ""
    var tempDescription: String = ""
    var tempSelectedDate: Date? = null
    var tempPhotoUri: Uri? = null
    var tempVolunteers: MutableList<Volunteer> = mutableListOf()

    fun loadServices() {
        viewModelScope.launch {
            val servicesWithVolunteersList = withContext(Dispatchers.IO) {
                repository.getAllServicesWithVolunteers()
            }
            _services.value = servicesWithVolunteersList
        }
    }

    fun addService(service: Service) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val serviceId = repository.insertService(service)
                service.volunteers.forEach { volunteer ->
                    volunteer.serviceId = serviceId.toInt()
                    insertVolunteer(volunteer)
                }
            }
            loadServices()
        }
    }

    fun updateService(service: Service) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateService(service)
            }
            loadServices()
        }
    }

    fun deleteService(service: Service) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteService(service)
            }
            loadServices()
        }
    }

    private suspend fun insertVolunteer(volunteer: Volunteer) {
        withContext(Dispatchers.IO) {
            repository.insertVolunteer(volunteer)
        }
    }

    suspend fun getVolunteersForService(serviceId: Int): List<Volunteer> {
        return withContext(Dispatchers.IO) {
            repository.getVolunteersByServiceId(serviceId)
        }
    }
}