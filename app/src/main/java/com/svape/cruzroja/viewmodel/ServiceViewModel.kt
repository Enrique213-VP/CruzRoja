package com.svape.cruzroja.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.svape.cruzroja.model.Service
import com.svape.cruzroja.model.Volunteer

class ServiceViewModel : ViewModel() {

    // Lista mutable de servicios
    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>>
        get() = _services

    // Lista mutable de voluntarios para el servicio actual
    private val _volunteers = MutableLiveData<List<Volunteer>>()
    val volunteers: LiveData<List<Volunteer>>
        get() = _volunteers

    init {
        // Inicializar la lista de servicios (puedes cargar datos iniciales aquí si es necesario)
        _services.value = mutableListOf()
        _volunteers.value = mutableListOf()
    }

    fun addService(service: Service) {
        val currentList = _services.value?.toMutableList() ?: mutableListOf()
        currentList.add(service)
        _services.value = currentList
    }

    // Métodos adicionales según sea necesario, como actualizar o eliminar servicios
}