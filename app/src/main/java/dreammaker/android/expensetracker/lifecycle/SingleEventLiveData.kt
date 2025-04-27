package dreammaker.android.expensetracker.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleEventLiveData<T> : MutableLiveData<T>() {

    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) {
            if (pending.get()) {
                observer.onChanged(it)
                pending.set(false)
            }
        }
    }

    override fun setValue(value: T) {
        pending.set(true)
        super.setValue(value)
    }
}