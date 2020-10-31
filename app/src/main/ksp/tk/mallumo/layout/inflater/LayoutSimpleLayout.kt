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
class LayoutSimpleLayout private constructor() : ImplLayoutInflater() {

    override val layoutResourceId: Int = tk.mallumo.layout.inflater.R.layout.simple_layout

    companion object {

        fun inflate(
            inflater: LayoutInflater,
            root: ViewGroup? = null,
            attachToRoot: Boolean = false,
            lifecycle: Lifecycle? = null
        ) = LayoutSimpleLayout().apply {
            init(inflater.inflate(layoutResourceId, root, attachToRoot), lifecycle)
        }

        fun bind(root: View, lifecycle: Lifecycle? = null) = LayoutSimpleLayout().apply {
            init(root, lifecycle)
        }
    }

    val veverka get() = layouts[0] as android.widget.TextView
    val textTemplate0 get() = layouts[1] as LayoutSimpleLayout2
    val textTemplate1 get() = layouts[2] as LayoutSimpleLayout2

    override fun setup(root: View) {
        layouts.add(root.findViewById(tk.mallumo.layout.inflater.R.id.veverka))
        layouts.add(LayoutSimpleLayout2.bind(root.findViewById(tk.mallumo.layout.inflater.R.id.textTemplate0)))
        layouts.add(LayoutSimpleLayout2.bind(root.findViewById(tk.mallumo.layout.inflater.R.id.textTemplate1)))
    }


    fun <T : Any> flow(flow: Flow<T>, body: LayoutSimpleLayout.(T) -> Unit) {
        lifecycle?.coroutineScope?.launchWhenResumed {
            flow.collect {
                body(this@LayoutSimpleLayout, it)
            }
        }
    }


}