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

    init {
        carregarHistorico()
    }

    fun carregarHistorico() {
        viewModelScope.launch {
            _historico.value = AirTodayRepository.listarHistorico()
        }
    }

    fun limparHistorico() {
        viewModelScope.launch {
            AirTodayRepository.limparHistorico()
            carregarHistorico() // 🔹 Atualiza a UI após limpar
        }
    }
}
