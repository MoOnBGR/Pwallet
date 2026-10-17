package com.example.pwallet.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pwallet.domain.model.Session
import com.example.pwallet.domain.usecase.ClientField
import com.example.pwallet.domain.usecase.ClientLoanForm
import com.example.pwallet.domain.usecase.CreateClientResult
import com.example.pwallet.domain.usecase.CreateClientWithLoanUseCase
import com.example.pwallet.domain.usecase.LoanPlan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateClientUiState(
    val form: ClientLoanForm = ClientLoanForm(),
    val errors: Map<ClientField, String> = emptyMap(),
    val preview: LoanPlan? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
)

class CreateClientViewModel(
    private val createClient: CreateClientWithLoanUseCase,
    private val session: Session,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateClientUiState())
    val state: StateFlow<CreateClientUiState> = _state.asStateFlow()

    fun onChange(form: ClientLoanForm) = _state.update {
        it.copy(form = form, errors = emptyMap(), saveError = null, preview = createClient.preview(form))
    }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        val owner = session.user ?: return
        if (current.isSaving) return
        _state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                when (val result = createClient(owner.id, current.form)) {
                    is CreateClientResult.Success -> {
                        _state.value = CreateClientUiState() // formulario limpio para el siguiente cliente
                        onSaved()
                    }
                    is CreateClientResult.Invalid ->
                        _state.update { it.copy(isSaving = false, errors = result.errors) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, saveError = "No se pudo guardar. Intenta de nuevo.") }
            }
        }
    }
}