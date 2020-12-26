package tk.mallumo.test

import org.junit.Test
import tk.mallumo.layout.inflater.ClassBuilder
import tk.mallumo.layout.inflater.CodeWriter
import tk.mallumo.layout.inflater.DataTransform
import java.io.File

class TestGenerator {
    val layoutDir = File("/opt/android/project/layout-inflater/app/src/main/res")
    val generatedDir = File("/opt/android/project/layout-inflater/app/src/main/ksp/tk/mallumo/layout/inflater")
    val generatedKtFiles get() =  generatedDir.listFiles()?.asList()?: listOf()
    val sourceXmlFiles get() = DataTransform.readXmlLayoutFiles(layoutDir)

    val codeWriter by lazy {
        CodeWriter(
            directory = File("/opt/android/project/layout-inflater/app/src/main/ksp/"),
            rootPackage = "tk.mallumo.layout.inflater"
        )
    }
    @Test
    fun fileCompare(){

        println("............")


        val xmlHash = DataTransform.xmlHash(sourceXmlFiles)
        println(xmlHash == codeWriter.readTmpFile("hash.tmp"))

        println("............")


    }

    @Test
    fun fileStructureChanged(){
        println("............")
        val generatedFiles = generatedKtFiles
            .associate { ClassBuilder.generateLayoutFileName(it, false) to it.readLines().firstOrNull { it.startsWith("//HASH-XML ") } }

        if (generatedFiles.size != sourceXmlFiles.count() + 2) {
            println("counter -> ${generatedFiles.size} x ${sourceXmlFiles.count() + 2}")
        }

        val sourcesMap = sourceXmlFiles.associate {
            ClassBuilder.generateLayoutFileName(it) to "//HASH-XML ${
                DataTransform.readContentHash(it)
            }"
        }
        val changed =  if (generatedFiles.keys.containsAll(sourcesMap.keys)) {
            sourcesMap.map {
                generatedFiles[it.key] == it.value
            }.any { !it }
        } else {
            true
        }
        println("changed: $changed")
    }
}