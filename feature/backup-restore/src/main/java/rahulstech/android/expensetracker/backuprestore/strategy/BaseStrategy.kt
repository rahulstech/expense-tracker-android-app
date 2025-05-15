package rahulstech.android.expensetracker.backuprestore.strategy

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

abstract class BaseStrategy(context: Context): Strategy {

    private val TAG = BaseStrategy::class.simpleName

    override val applicationContext: Context = context.applicationContext

    private val mainHandler = Handler(Looper.getMainLooper())

    private var progressListenerReference = WeakReference<Strategy.ProgressListener>(null)

    private val _state = AtomicReference(Strategy.State.CREATED)

    override fun getState(): Strategy.State = _state.get()

    override fun updateProgress(progress: Strategy.Progress) {
        mainHandler.post {
            progressListenerReference.get()?.onProgressUpdated(this@BaseStrategy, progress)
        }
    }

    override fun registerProgressListener(listener: Strategy.ProgressListener) {
        mainHandler.post {
            progressListenerReference = WeakReference(listener)
        }
    }

    override fun unregisterProgressListener(listener: Strategy.ProgressListener) {
        mainHandler.post {
            progressListenerReference = WeakReference(null)
        }
    }

    final override fun perform(parameter: Strategy.Parameter): Strategy.Output {
        try {
            if (_state.compareAndSet(Strategy.State.CREATED, Strategy.State.RUNNING)) {
                val result = doPerform(parameter)
                if (isCanceled()) {
                    return Strategy.Output(Strategy.State.CANCELED, parameter, null, null)
                }
                _state.set(Strategy.State.SUCCEEDED)
                val output = Strategy.Output(Strategy.State.SUCCEEDED, parameter, result, null)
                return output
            }
            throw IllegalStateException("calling perform(WorkParameter) is allowed when state is ${Strategy.State.CREATED}; " +
                    "but current state is ${_state.get()}")
        }
        catch (error: Throwable) {
            _state.set(Strategy.State.FAILED)
            return Strategy.Output(Strategy.State.FAILED, parameter, null, error)
        }
    }

    protected abstract fun doPerform(params: Strategy.Parameter): Any?

    final override fun cancel() {
        val state = getState()
        if (state.isFinished()) {
            throw IllegalStateException("already finished, can not cancel")
        }
        _state.set(Strategy.State.CANCELED)
    }

    override fun isCanceled(): Boolean = _state.get() == Strategy.State.CANCELED

    override fun clean() {}
}