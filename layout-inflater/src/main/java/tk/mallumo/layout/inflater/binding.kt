package tk.mallumo.layout.inflater

data class XmlNode(
    val name: String,
    val id: String = "",
    val layout: String = "",
    val childs: List<XmlNode> = listOf()
)

data class XmlInfo(
    val name: String,
    val path: String,
    val node: XmlNode = XmlNode("")
)

data class LayoutInflaterField(
    val id: String,
    val viewQualifiedName: String = "",
    val inflaterName: String = "",
    var propertyName: String = id,
)

data class ClassDef(
    val fileName: String,
    val imports: List<String>,
    val source: String,
    val xmlInfo: XmlInfo = XmlInfo("", ""),
    val contentOrigin:String = ""
)