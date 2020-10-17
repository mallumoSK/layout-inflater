package tk.mallumo.layout.inflater

import org.jetbrains.kotlin.ksp.processing.CodeGenerator
import org.jetbrains.kotlin.ksp.processing.KSPLogger
import org.jetbrains.kotlin.ksp.processing.Resolver
import org.jetbrains.kotlin.ksp.processing.SymbolProcessor
import tk.mallumo.bundled.CodeWriter
import java.io.File

class LayoutInflaterProcessor : SymbolProcessor {

    /**
     * helper of file/class management
     */
    private lateinit var codeWriter: CodeWriter

    private lateinit var options: Map<String, String>

    companion object {

        /**
         * root package for extensions, annotation
         */
        private const val bundledPackageName = "tk.mallumo.bundled"

        /**
         * qualifiedName name of generated annotation
         */
        private const val bundledAnnotationPath = "$bundledPackageName.Bundled"

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

    override fun process(resolver: Resolver) {
        val resourceDirectory = File(options["in"] ?: throw RuntimeException(errProjectInDir))
        if (!resourceDirectory.exists()) throw RuntimeException("project resources directory not exists (${resourceDirectory.absolutePath})")
        val items = resourceDirectory.listFiles()
            ?.asSequence()
            ?.filter { it.isDirectory && it.name.startsWith("layout") }
            ?.map { it.listFiles()?.filter { it.isFile && it.name.endsWith(".xml") } }
            ?.filterNotNull()
            ?.flatten()
            ?.map { generateSource(it) }
            ?: sequenceOf()
        File("/tmp/___/test").appendText("\nNEW\n${items.joinToString("\n")}")
    }

    private fun generateSource(it: File): String {
        return it.absolutePath
    }

    override fun finish() {

    }
}