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

data class XmlNode(
    val name: String,
    val id: String = "",
    val inflatedId: String = "",
    val layout: String = "",
    val childs: List<XmlNode> = listOf()
)

data class XmlInfo(
    val name: String,
    val path: String,
    val node: XmlNode
)

class LayoutInflaterProcessor : SymbolProcessor {

    /**
     * helper of file/class management
     */
    private lateinit var codeWriter: CodeWriter

    private lateinit var options: Map<String, String>

    companion object {

//        /**
//         * root package for extensions, annotation
//         */
//        private const val bundledPackageName = "tk.mallumo.bundled"


        /**
         * error info, if is gradle file modified
         */
        private const val errProjectOutDir =
            "Inside yours gradle.build must be defined constant (output): 'ksp.arg(\"out\", \"\${projectDir.absolutePath}/src/main/ksp\")'"
        private const val errProjectInDir =
            "Inside yours gradle.build must be defined constant (input): 'ksp.arg(\"in\", \"\${projectDir.absolutePath}/src/main/res\")'"

    }

    override fun init(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.options = options
        this.codeWriter = CodeWriter(
            directory = File(options["out"] ?: throw RuntimeException(errProjectOutDir)),
            rootPackage = "tk.mallumo.layout.inflater"
        )
    }

    // https://developer.android.com/training/improving-layouts/loading-ondemand
    override fun process(resolver: Resolver) {
        val resourceDirectory = File(options["in"] ?: throw RuntimeException(errProjectInDir))
        if (!resourceDirectory.exists()) throw RuntimeException("project resources directory not exists (${resourceDirectory.absolutePath})")
        val items = resourceDirectory.listFiles()
            ?.asSequence()
            ?.filter { it.isDirectory && it.name.startsWith("layout") }
            ?.map { file -> file.listFiles()?.filter { it.isFile && it.name.endsWith(".xml") } }
            ?.filterNotNull()
            ?.flatten()
            ?.map { generateSource(it) }
            ?: sequenceOf()
        File("/tmp/___/test").writeText("\nNEW\n${items.joinToString("\n")}")
    }

    private fun generateSource(it: File): XmlInfo {
        return buildXmlInfo(it)
    }

    override fun finish() {

    }


    private fun buildXmlInfo(resFile: File): XmlInfo {
        return XmlInfo(
            resFile.name.split(".").first(),
            resFile.absolutePath,
            buildXmlNode(resFile)
        )
    }

    private fun buildXmlNode(resFile: File): XmlNode {
        val doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(resFile.readText())))

        return doc.documentElement.xmlNode
    }

    private val Element.xmlNode
        get() = XmlNode(
            tagName,
            id,
            inflatedId,
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
    private val Element.inflatedId: String get() = attr("android:inflatedId")

    private val Element.layout: String get() = attr("layout")

    private fun Element.attr(name: String): String {
        return if (!hasAttribute(name)) ""
        else getAttribute(name).split("/").last()
    }
}