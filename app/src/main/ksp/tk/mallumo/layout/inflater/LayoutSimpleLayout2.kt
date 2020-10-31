@file:Suppress("unused")

package tk.mallumo.layout.inflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect


@Suppress("unused", "SpellCheckingInspection", "RemoveRedundantQualifierName", "PropertyName")
class LayoutSimpleLayout2 private constructor() : ImplLayoutInflater() {

    override val layoutResourceId: Int = tk.mallumo.layout.inflater.R.layout.simple_layout_2

    companion object {

        fun inflate(
            inflater: LayoutInflater,
            root: ViewGroup? = null,
            attachToRoot: Boolean = false,
            lifecycle: Lifecycle? = null
        ) = LayoutSimpleLayout2().apply {
            init(inflater.inflate(layoutResourceId, root, attachToRoot), lifecycle)
        }

        fun bind(root: View, lifecycle: Lifecycle? = null) = LayoutSimpleLayout2().apply {
            init(root, lifecycle)
        }
    }

    val textView get() = layouts[0] as android.widget.TextView

    override fun setup(root: View) {
        layouts.add(root.findViewById(tk.mallumo.layout.inflater.R.id.textView))
    }


    fun <T : Any> flow(flow: Flow<T>, body: LayoutSimpleLayout2.(T) -> Unit) {
        lifecycle?.coroutineScope?.launchWhenResumed {
            flow.collect {
                body(this@LayoutSimpleLayout2, it)
            }
        }
    }


}