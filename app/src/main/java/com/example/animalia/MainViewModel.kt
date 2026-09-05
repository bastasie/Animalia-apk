package com.example.animalia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AnimalRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = AnimalRepository(database.animalDao())
        
        viewModelScope.launch {
            try {
                repository.populateInitialData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val speciesList: StateFlow<List<Species>> = repository.allSpecies.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val speciesCount: StateFlow<Int> = repository.speciesCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val vocalizationCount: StateFlow<Int> = repository.vocalizationCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val avgPiComm: StateFlow<Double?> = repository.avgPiComm.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    private val _selectedSpeciesId = MutableStateFlow<Int?>(null)
    val selectedSpeciesId = _selectedSpeciesId.asStateFlow()

    private val _vocalizations = MutableStateFlow<List<Vocalization>>(emptyList())
    val vocalizations = _vocalizations.asStateFlow()

    fun selectSpecies(speciesId: Int?) {
        _selectedSpeciesId.value = speciesId
        if (speciesId != null) {
            viewModelScope.launch {
                repository.getVocalizations(speciesId).collect {
                    _vocalizations.value = it
                }
            }
        } else {
            _vocalizations.value = emptyList()
        }
    }
}
