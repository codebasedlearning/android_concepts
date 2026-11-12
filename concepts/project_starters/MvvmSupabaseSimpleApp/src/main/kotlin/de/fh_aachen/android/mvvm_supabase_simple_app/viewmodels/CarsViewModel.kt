// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_supabase_simple_app.data.Car
import de.fh_aachen.android.mvvm_supabase_simple_app.data.CarRepository
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

// What the screen shows per row: the car, the model in the text field, and the button state.
data class CarItem(
    val car: Car,
    val model: String,
    val canSave: Boolean,
    val isSaving: Boolean,
)

// The car list (loaded once) plus the edits in progress: one draft model name per car.
class CarsViewModel(private val repository: CarRepository) : LoadViewModel<List<Car>>() {

    // Edited, not yet saved model names by car id. In the ViewModel: survives rotation, not process death.
    private val drafts = mutableStateMapOf<Int, String>()

    var savingId by mutableStateOf<Int?>(null)          // one save at a time
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init { reload() }

    override suspend fun load() = repository.all()

    fun itemOf(car: Car): CarItem {
        val model = drafts[car.id] ?: car.model
        val changed = model.trim() != car.model
        return CarItem(
            car = car,
            model = model,
            canSave = changed && model.isNotBlank() && savingId == null,
            isSaving = savingId == car.id,
        )
    }

    fun onModelChange(car: Car, model: String) {
        drafts[car.id] = model
        error = null
    }

    // Persists the draft; on success the saved row (as the database returns it) replaces the old one.
    fun save(car: Car) {
        if (!itemOf(car).canSave) return
        val model = drafts.getValue(car.id).trim()
        viewModelScope.launch {
            savingId = car.id
            error = null
            try {
                val saved = repository.renameModel(car.id, model)
                updateData { cars -> cars.map { if (it.id == saved.id) saved else it } }
                drafts.remove(car.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: e.toString()
            } finally {
                savingId = null
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CarsViewModel(container().carRepository) }
        }
    }
}
