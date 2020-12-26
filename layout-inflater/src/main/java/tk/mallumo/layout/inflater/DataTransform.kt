package tk.mallumo.layout.inflater

import org.w3c.dom.Element
import org.xml.sax.InputSource
import tk.mallumo.layout.inflater.HashUtils.sha1
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object DataTransform {

    fun readXmlLayoutFiles(dir: File) = dir.listFiles()
        ?.asSequence()
        ?.filter { it.isDirectory && it.name.startsWith("layout") }
        ?.map { file -> file.listFiles()?.filter { it.isFile && it.name.endsWith(".xml") } }
        ?.filterNotNull()
        ?.flatten()
        ?.filterNotNull()
        ?: sequenceOf()

    fun readContentHash(file: File): String =
        file.readText().sha1()

     val File.xmlInfo: XmlInfo
        get() = XmlInfo(
            name.split(".").first(),
            absolutePath,
            xmlNode
        )

     val File.xmlNode: XmlNode
        get() = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(readText())))
            .documentElement
            .xmlNode


     val Element.xmlNode
        get() = XmlNode(
            tagName,
            id,
            layout,
            childes
        )

     val Element.childes: List<XmlNode>
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

     val Element.id: String get() = attr("android:id")

     val Element.layout: String get() = attr("layout")

     fun Element.attr(name: String): String {
        return if (!hasAttribute(name)) ""
        else getAttribute(name).split("/").last()
    }

    fun xmlHash(sourceFiles: Sequence<File>): String =sourceFiles.sortedBy { it.name }
        .joinToString { it.readText().sha1() }
}