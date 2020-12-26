package tk.mallumo.layout.inflater


import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import tk.mallumo.layout.inflater.DataTransform.xmlInfo
import java.io.File


class LayoutInflaterProcessor : SymbolProcessor {

    private lateinit var logger: KSPLogger

    /**
     * helper of file/class management
     */
    private lateinit var codeWriter: CodeWriter

    private lateinit var options: Map<String, String>

    private lateinit var packageName: String

    companion object {
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
        this.logger = logger
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
    override fun process(resolver: Resolver){
        val resourceDirectory =
            File(options["LayoutInflaterResIn"] ?: throw RuntimeException(errProjectInDir))
        if (!resourceDirectory.exists()) throw RuntimeException("project resources directory not exists (${resourceDirectory.absolutePath})")
        val isFlowEnabled = (options["LayoutInflaterFlow"] ?: "true").toBoolean()

        val sourceFiles = DataTransform.readXmlLayoutFiles(resourceDirectory)
        val xmlHash = DataTransform.xmlHash(sourceFiles)


        if (xmlHash != codeWriter.readTmpFile("hash.tmp")) {
            val items = sourceFiles
                .map { ClassBuilder.buildDef(it, packageName, it.xmlInfo, isFlowEnabled) }
                .distinctBy { it.fileName }

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
                ) {
                    appendLine()
                    append(it.source)
                }
            }
            codeWriter.write(deleteOld = true)
            codeWriter.writeTmpFile("hash.tmp", xmlHash)
        }
    }

    override fun finish() {
    }
}