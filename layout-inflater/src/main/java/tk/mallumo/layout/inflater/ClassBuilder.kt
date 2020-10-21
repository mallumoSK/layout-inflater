package tk.mallumo.layout.inflater


object ClassBuilder {

    fun getLazyInflaters(items: Sequence<ClassDef>) = ClassDef(
        "ImplLayoutUtils.kt",
        imports = listOf(
            "android.view.LayoutInflater",
            "android.view.ViewGroup",
            "androidx.lifecycle.Lifecycle",
            "androidx.fragment.app.Fragment",
            "androidx.appcompat.app.AppCompatActivity",
            "kotlin.reflect.KClass"
        ),
        """
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

object ImplLayoutUtils{
    
    @Suppress("UNCHECKED_CAST")
    fun <T : ImplLayoutInflater> inflate(
        clazz: KClass<T>,
        layoutInflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean,
        lifecycle: Lifecycle): T = when (clazz) {
${items.joinToString("\n") { it.inflationRow }}
        else -> throw RuntimeException("Undefined layout => ${"$"}{clazz.qualifiedName}}")
    } as T    
}
    """
    )

    private val ClassDef.inflationRow: String
        get() = buildString {
            val name = xmlInfo.name.layoutFileName
            append("\t\t")
            append("$name::class -> $name.inflate(layoutInflater,root,attachToRoot,lifecycle)")
        }

    fun getImplLayoutInflaterDef() = ClassDef(
        "ImplLayoutInflater.kt",
        imports = listOf(
            "android.view.View",
            "androidx.lifecycle.Lifecycle",
            "androidx.lifecycle.LifecycleObserver",
            "androidx.lifecycle.OnLifecycleEvent",
            "androidx.lifecycle.Lifecycle"
        ),
        """
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
    """
    )


    fun buildDef(packageName: String, xmlInfo: XmlInfo, isFlowEnabled: Boolean): ClassDef {

        val fields = xmlInfo.inflaterFields

        val name = xmlInfo.name.layoutFileName
        return ClassDef(
            "$name.kt",
            xmlInfo = xmlInfo,
            imports = listOf(
                "android.view.LayoutInflater",
                "android.view.View",
                "android.view.ViewGroup",
                "androidx.lifecycle.Lifecycle"
            ).let {
                if (isFlowEnabled) {
                    it.plus(
                        listOf(
                            "androidx.lifecycle.coroutineScope",
                            "kotlinx.coroutines.flow.Flow",
                            "kotlinx.coroutines.flow.collect"
                        )
                    )
                } else it
            },
            source = """
@Suppress("unused","SpellCheckingInspection", "RemoveRedundantQualifierName", "PropertyName")
class $name private constructor(lifecycle: Lifecycle?) :
    ImplLayoutInflater(lifecycle) {

    companion object {

        fun inflate(
            inflater: LayoutInflater,
            root: ViewGroup? = null,
            attachToRoot: Boolean = false,
            lifecycle: Lifecycle? = null
        ) = $name(lifecycle).apply {
            init(inflater.inflate(${packageName}.R.layout.${xmlInfo.name}, root, attachToRoot))
        }

        fun bind(root: View, lifecycle: Lifecycle? = null) = $name(lifecycle).apply {
            init(root)
        }
    }
    
${fields.mapIndexed { index, field -> field.fieldAccessor(index) }.joinToString("\n")}

    override fun setup(root: View) {
${fields.joinToString("\n") { it.fieldCreator(packageName) }}
    }

${
                if (isFlowEnabled) """
    fun <T : Any> flow(flow: Flow<T>, body: $name.(T) -> Unit) {
       lifecycle?.coroutineScope?.launchWhenResumed {
           flow.collect {
                body(this@$name, it)
           }
       }
    }
""" else ""
            }

}"""
        )
    }


    private val XmlInfo.inflaterFields: List<LayoutInflaterField>
        get() = inflaterFieldsTree(node).also { origin ->
            val usedIds = arrayListOf("layouts", "root", "lifecycle")
            val allPropertyNames = origin.map { it.propertyName }
                .plus(usedIds)
                .let { arrayListOf<String>().apply { addAll(it) } }

            origin.filter { o -> allPropertyNames.count { it == o.propertyName } > 1 }
                .forEach {
                    it.propertyName = genUniquePropertyName(it.propertyName, allPropertyNames)
                    allPropertyNames.add(it.propertyName)
                }


        }

    private fun genUniquePropertyName(
        propertyName: String,
        allPropertyNames: List<String>
    ): String {
        val newName = "${propertyName}_"

        return if (!allPropertyNames.contains(newName)) newName
        else genUniquePropertyName(newName, allPropertyNames)
    }

    private fun inflaterFieldsTree(node: XmlNode): List<LayoutInflaterField> {
        val result = arrayListOf<LayoutInflaterField>()
        node.asLayoutInflaterField()?.also { result.add(it) }
        node.childs.forEach {
            result.addAll(inflaterFieldsTree(it))
        }
        return result
    }

    private fun LayoutInflaterField.fieldAccessor(index: Int) = if (inflaterName.isNotEmpty()) {
        "\tval $propertyName get() = layouts[$index] as $inflaterName"
    } else {
        "\tval $propertyName get() = layouts[$index] as $viewQualifiedName"
    }

    private fun LayoutInflaterField.fieldCreator(packageName: String) =
        if (inflaterName.isNotEmpty()) {
            "\t\tlayouts.add(${inflaterName}.bind(root.findViewById(${packageName}.R.id.$id)))"
        } else {
            "\t\tlayouts.add(root.findViewById(${packageName}.R.id.$id))"
        }


    private val String.layoutFileName
        get() = split("_")
            .joinToString("") {
                when {
                    it.isEmpty() -> ""
                    it.length == 1 -> it[0].toUpperCase().toString()
                    else -> "${it[0].toUpperCase()}${it.substring(1)}"
                }
            }.let { "Layout$it" }


    private fun XmlNode.asLayoutInflaterField(): LayoutInflaterField? {
        return if (id.isNotEmpty()) {
            when {
                name == "include" -> {
                    LayoutInflaterField(
                        id = id,
                        inflaterName = layout.layoutFileName
                    )
                }
                name == "WebView" -> {
                    LayoutInflaterField(
                        id = id,
                        viewQualifiedName = "android.webkit.WebView"
                    )
                }
                name in arrayOf(
                    "ViewStub",
                    "TextureView",
                    "View",
                    "SurfaceView",
                    "ViewDebug",
                    "ViewGroup",
                    "ViewOverlay",
                    "ViewGroupOverlay"
                ) -> {
                    LayoutInflaterField(
                        id = id,
                        viewQualifiedName = "android.view.$name"
                    )
                }
                name.contains(".") -> {
                    LayoutInflaterField(
                        id = id,
                        viewQualifiedName = name
                    )
                }
                name[0].isUpperCase() -> {
                    LayoutInflaterField(
                        id = id,
                        viewQualifiedName = "android.widget.$name"
                    )
                }
                else -> null
            }
        } else null
    }
}
