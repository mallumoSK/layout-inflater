@file:Suppress("unused")

package tk.mallumo.layout.inflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent


@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class ImplLayoutInflater : LifecycleObserver {

    protected var lifecycle: Lifecycle? = null

    companion object {
        inline fun <reified T : ImplLayoutInflater> inflate(
            inflater: LayoutInflater,
            root: ViewGroup? = null,
            attachToRoot: Boolean = false,
            lifecycle: Lifecycle? = null
        ): T = ImplLayoutUtils.inflate(T::class, inflater, root, attachToRoot, lifecycle)
    }

    abstract val layoutResourceId: Int

    var root: View? = null
        protected set

    protected var layouts = arrayListOf<Any?>()

    internal fun init(content: View, lifecycle: Lifecycle?) {
        release()

        this.lifecycle = lifecycle
        this.lifecycle?.addObserver(this)

        root = content
        setup(root!!)
    }

    protected abstract fun setup(root: View)

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        layouts.forEachIndexed { index, any ->
            if (any is ImplLayoutInflater) {
                any.release()
            }
            layouts[index] = null
        }
        layouts.clear()
        root = null
        lifecycle?.removeObserver(this)
        lifecycle = null
    }
}