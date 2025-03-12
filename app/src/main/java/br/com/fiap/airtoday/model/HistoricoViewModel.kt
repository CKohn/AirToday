package br.com.fiap.airtoday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.airtoday.model.AirToday
import br.com.fiap.airtoday.repository.AirTodayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoricoViewModel : ViewModel() {

    private val _historico = MutableStateFlow<List<AirToday>>(emptyList())
    val historico: StateFlow<List<AirToday>> = _historico

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError

    init {
        carregarHistorico()
    }

    fun carregarHistorico() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _showError.value = false

                _historico.value = AirTodayRepository.listarHistorico()
            } catch (e: Exception) {
                _showError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limparHistorico() {
        viewModelScope.launch {
            try {
                AirTodayRepository.limparHistorico()
                carregarHistorico()
            } catch (e: Exception) {
                _showError.value = true
            }
        }
    }
}
