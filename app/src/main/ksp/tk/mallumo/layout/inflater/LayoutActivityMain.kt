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
class LayoutActivityMain private constructor(lifecycle: Lifecycle?) :
    ImplLayoutInflater(lifecycle) {

    companion object {

        fun inflate(
            inflater: LayoutInflater,
            root: ViewGroup? = null,
            attachToRoot: Boolean = false,
            lifecycle: Lifecycle? = null
        ) = LayoutActivityMain(lifecycle).apply {
            init(
                inflater.inflate(
                    tk.mallumo.layout.inflater.R.layout.activity_main,
                    root,
                    attachToRoot
                )
            )
        }

        fun bind(root: View, lifecycle: Lifecycle? = null) = LayoutActivityMain(lifecycle).apply {
            init(root)
        }
    }

    val veverka get() = layouts[0] as android.widget.TextView
    val incLayout get() = layouts[1] as LayoutInc

    override fun setup(root: View) {
        layouts.add(root.findViewById(tk.mallumo.layout.inflater.R.id.veverka))
        layouts.add(LayoutInc.bind(root.findViewById(tk.mallumo.layout.inflater.R.id.incLayout)))
    }


    fun <T : Any> flow(flow: Flow<T>, body: LayoutActivityMain.(T) -> Unit) {
        lifecycle?.coroutineScope?.launchWhenResumed {
            flow.collect {
                body(this@LayoutActivityMain, it)
            }
        }
    }


}