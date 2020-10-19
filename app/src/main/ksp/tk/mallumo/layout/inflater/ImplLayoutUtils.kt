@file:Suppress("unused")

package tk.mallumo.layout.inflater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import kotlin.reflect.KClass


inline fun <reified T : ImplLayoutInflater> Fragment.lazyLayout(
    root: ViewGroup? = null,
    attachToRoot: Boolean = false,
    crossinline inflater: () -> LayoutInflater? = { null },
    crossinline body: T.() -> Unit = {}
): Lazy<T> = lazy {
    ImplLayoutUtils.inflate(
        T::class,
        inflater() ?: layoutInflater,
        root,
        attachToRoot,
        lifecycle
    ).apply {
        body(this)
    }
}

inline fun <reified T : ImplLayoutInflater> AppCompatActivity.lazyLayout(
    root: ViewGroup? = null,
    attachToRoot: Boolean = false,
    crossinline inflater: () -> LayoutInflater? = { null },
    crossinline body: T.() -> Unit = {}
): Lazy<T> = lazy {
    ImplLayoutUtils.inflate(
        T::class,
        inflater() ?: layoutInflater,
        root,
        attachToRoot,
        lifecycle
    ).apply {
        body(this)
    }
}

object ImplLayoutUtils {

    @Suppress("UNCHECKED_CAST")
    fun <T : ImplLayoutInflater> inflate(
        clazz: KClass<T>,
        layoutInflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean,
        lifecycle: Lifecycle
    ): T = when (clazz) {
        LayoutSimpleLayout2::class -> LayoutSimpleLayout2.inflate(
            layoutInflater,
            root,
            attachToRoot,
            lifecycle
        )
        LayoutSimpleLayout::class -> LayoutSimpleLayout.inflate(
            layoutInflater,
            root,
            attachToRoot,
            lifecycle
        )
        LayoutInc::class -> LayoutInc.inflate(layoutInflater, root, attachToRoot, lifecycle)
        LayoutActivityMain::class -> LayoutActivityMain.inflate(
            layoutInflater,
            root,
            attachToRoot,
            lifecycle
        )
        else -> throw RuntimeException("Undefined layout => ${clazz.qualifiedName}}")
    } as T
}
    