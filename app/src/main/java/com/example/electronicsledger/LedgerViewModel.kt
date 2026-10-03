package com.example.electronicsledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class LedgerState(
    val products: List<Product> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null
)

class LedgerViewModel(app: Application) : AndroidViewModel(app) {
    private val database = ProductDatabase(app)
    private val mutableState = MutableStateFlow(LedgerState())
    val state = mutableState.asStateFlow()

    init { reload() }

    fun clearError() { mutableState.value = mutableState.value.copy(error = null) }

    fun reload() {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, error = null)
            runCatching { withContext(Dispatchers.IO) { database.all() } }
                .onSuccess { mutableState.value = mutableState.value.copy(products = it, loading = false) }
                .onFailure { mutableState.value = mutableState.value.copy(loading = false, error = "读取失败，请重试") }
        }
    }

    fun save(product: Product, onSuccess: () -> Unit) {
        val error = validateProduct(product.name, priceText(product.priceCents), product.purchasedOn, LocalDate.now())
        if (error != null) { mutableState.value = mutableState.value.copy(error = error); return }
        mutate({ database.save(product) }, onSuccess)
    }

    fun delete(id: Long, onSuccess: () -> Unit) = mutate({ database.delete(id) }, onSuccess)

    private fun mutate(action: () -> Unit, onSuccess: () -> Unit) {
        if (mutableState.value.saving) return
        mutableState.value = mutableState.value.copy(saving = true, error = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { database.changeAndRead(action) } }
                .onSuccess {
                    mutableState.value = LedgerState(products = it, loading = false)
                    onSuccess()
                }
                .onFailure {
                    mutableState.value = mutableState.value.copy(saving = false, error = "保存失败，请重试；原记录仍保留在本机")
                }
        }
    }
}
