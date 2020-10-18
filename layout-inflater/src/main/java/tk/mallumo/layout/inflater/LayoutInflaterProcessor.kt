package tk.mallumo.layout.inflater

import org.jetbrains.kotlin.ksp.processing.CodeGenerator
import org.jetbrains.kotlin.ksp.processing.KSPLogger
import org.jetbrains.kotlin.ksp.processing.Resolver
import org.jetbrains.kotlin.ksp.processing.SymbolProcessor
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory


class LayoutInflaterProcessor : SymbolProcessor {

    /**
     * helper of file/class management
     */
    private lateinit var codeWriter: CodeWriter

    private lateinit var options: Map<String, String>

    private lateinit var packageName: String

    companion object {

//        /**
//         * root package for extensions, annotation
//         */
//        private const val bundledPackageName = "tk.mallumo.bundled"


        /**
         * error info, if is gradle file modified
         */
        private const val errProjectOutDir =
            "Inside yours gradle.build must be defined constant (output): 'ksp.arg(\"LayoutInflaterSrcOut\", \"\${projectDir.absolutePath}/src/main/ksp\")'"

        private const val errProjectInDir =
            "Inside yours gradle.build must be defined constant (input): 'ksp.arg(\"LayoutInflaterResIn\", \"\${projectDir.absolutePath}/src/main/res\")'"

        private const val errProjectPackageName =
            "Inside yours gradle.build must be defined constant (app package name): etc: 'ksp.arg(\"LayoutInflaterAppPackage\", \"com.example.sampleapplication\")'"

    }

    override fun init(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.options = options
        this.packageName =
            options["LayoutInflaterAppPackage"] ?: throw RuntimeException(errProjectPackageName)
        this.codeWriter = CodeWriter(
            directory = File(
                options["LayoutInflaterSrcOut"] ?: throw RuntimeException(
                    errProjectOutDir
                )
            ),
            rootPackage = "tk.mallumo.layout.inflater"
        )
    }

    // https://developer.android.com/training/improving-layouts/loading-ondemand
    override fun process(resolver: Resolver) {
        val resourceDirectory =
            File(options["LayoutInflaterResIn"] ?: throw RuntimeException(errProjectInDir))
        if (!resourceDirectory.exists()) throw RuntimeException("project resources directory not exists (${resourceDirectory.absolutePath})")
        val isFlowEnabled = (options["LayoutInflaterFlow"] ?: "true").toBoolean()
        val items = resourceDirectory.listFiles()
            ?.asSequence()
            ?.filter { it.isDirectory && it.name.startsWith("layout") }
            ?.map { file -> file.listFiles()?.filter { it.isFile && it.name.endsWith(".xml") } }
            ?.filterNotNull()
            ?.flatten()
            ?.map { ClassBuilder.buildDef(packageName, it.xmlInfo, isFlowEnabled) }
            ?.distinctBy { it.fileName }
            ?: sequenceOf()

        ClassBuilder.getImplLayoutInflaterDef().also {
            codeWriter.add(
                "tk.mallumo.layout.inflater",
                it.fileName,
                it.imports
            ) { append(it.source) }
        }

        ClassBuilder.getLazyInflaters(items).also {
            codeWriter.add(
                "tk.mallumo.layout.inflater",
                it.fileName,
                it.imports
            ) { append(it.source) }
        }

        items.forEach {
            codeWriter.add(
                "tk.mallumo.layout.inflater",
                it.fileName,
                it.imports
            ) { append(it.source) }
        }
    }


    override fun finish() {
        codeWriter.write(true)
    }

    private val File.xmlInfo: XmlInfo
        get() = XmlInfo(
            name.split(".").first(),
            absolutePath,
            xmlNode
        )

    private val File.xmlNode: XmlNode
        get() = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(readText())))
            .documentElement
            .xmlNode


    private val Element.xmlNode
        get() = XmlNode(
            tagName,
            id,
            layout,
            childes
        )

    private val Element.childes: List<XmlNode>
        get() {
            return if (!hasChildNodes()) {
                listOf()
            } else {
                (0 until childNodes.length)
                    .map { index -> childNodes.item(index) }
                    .filterIsInstance<Element>()
                    .map { it.xmlNode }
            }
        }

    private val Element.id: String get() = attr("android:id")

    private val Element.layout: String get() = attr("layout")

    private fun Element.attr(name: String): String {
        return if (!hasAttribute(name)) ""
        else getAttribute(name).split("/").last()
    }
}