package tk.mallumo.layout.inflater

import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    //    https://www.hameister.org/KotlinXml.html

    data class Node(val name: String, val id: String = "", val childs: List<Node> = listOf())

    @Test
    fun parseXML() {
        val xmlFile =
            File("/opt/android/project/github/layout-inflater/app/src/main/res/layout/activity_main.xml")

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(xmlFile.readText()))
        val doc = dBuilder.parse(xmlInput)

//        val xpFactory = XPathFactory.newInstance()
//        val xPath = xpFactory.newXPath()
        val root = doc.documentElement
        val nodes = Node(root.tagName, root.id, root.childs)

        println(nodes)
    }

    fun buildResourceNode(resFile: File): Node {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(resFile.readText()))
        val doc = dBuilder.parse(xmlInput)

        val root = doc.documentElement
        return Node(root.tagName, root.id, root.childs)
    }

    private val Element.childs: List<ExampleUnitTest.Node>
        get() {
            return if (!hasChildNodes()) {
                listOf()
            } else {
                (0 until childNodes.length).map { index -> childNodes.item(index) }
                    .filterIsInstance<Element>()
                    .map { Node(it.tagName, it.id, it.childs) }
            }
        }

    private val Element.id: String
        get() {
            return if (!hasAttribute("android:id")) ""
            else getAttribute("android:id").split("/").last()
        }
}