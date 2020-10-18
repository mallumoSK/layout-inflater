@file:Suppress("unused")

package tk.mallumo.layout.inflater

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent


@Suppress("unused", "MemberVisibilityCanBePrivate", "LeakingThis")
abstract class ImplLayoutInflater(protected var lifecycle: Lifecycle?) : LifecycleObserver {

    init {
        lifecycle?.addObserver(this)
    }

    var root: View? = null
        protected set

    protected var layouts = arrayListOf<Any>()

    protected fun init(content: View) {
        root = content
        setup(root!!)
    }

    protected abstract fun setup(root: View)

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        layouts.clear()
        root = null
        lifecycle?.removeObserver(this)
        lifecycle = null
    }
}
    