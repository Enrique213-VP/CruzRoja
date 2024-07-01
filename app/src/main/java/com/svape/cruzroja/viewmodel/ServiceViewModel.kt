package com.svape.cruzroja.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.svape.cruzroja.model.Service
import com.svape.cruzroja.model.Volunteer

class ServiceViewModel : ViewModel() {

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> get() = _services

    init {
        _services.value = mutableListOf()
    }

    fun addService(service: Service) {
        val currentList = _services.value as MutableList
        currentList.add(service)
        _services.value = currentList
    }

    fun addVolunteer(serviceName: String, volunteer: Volunteer) {
        val currentList = _services.value as MutableList
        val service = currentList.find { it.serviceName == serviceName }
        service?.let {
            val updatedService = it.copy(volunteers = it.volunteers + volunteer)
            currentList[currentList.indexOf(it)] = updatedService
            _services.value = currentList
        }
    }
}