package org.ucombinator.jade.decompile

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.util.TraceClassVisitor
import org.ucombinator.jade.util.AtomicWriteFile
import org.ucombinator.jade.util.Log
import org.ucombinator.jade.util.ReadFiles
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

// TODO: nested class?
// TODO: error message
// TODO: load flag
// TODO: support stdin for files to decompile
// TODO: skip over ct.jar as it is just signatures.  Maybe don't skip second load if it is better.

/** Handles processing files and decomposing them into classes and methods.  Processing of class-level constructs and
 * method bodies are delegated to `DecompileClass` and `DecompileMethodBody` respectively.
 */
object Decompile {
  private val log = Log {}

  /** The main entry point for the decompiler. It takes a list of files as input and attempts to decompile them.
   *
   * @param files The list of files to decompile.
   * @param outputDir TODO
   */
  fun main(files: List<File>, outputDir: File) {
    val readFiles = ReadFiles()
    for (file in files) readFiles.dir(file)
    readFiles.result.entries.forEachIndexed { i, (filePath, bytes) ->
      val file = filePath.last() // TODO: temporary code: multiple file paths due to nested archives
      val classReader = ClassReader(bytes)

      log.info { "Decompiling [${i + 1} of ${readFiles.result.size}] ${classReader.className} from ${filePath}" }
      val compilationUnit = decompileClassReader(classReader)

      log.debug { "stubCompilationUnit\n${compilationUnit}" }

      // TODO: use class name to create directory hierarchy (may need CLI options to control this)
      // Write to .java file of the same name as .class file (e.g. SampleClass.class -> SampleClass.java)
      val suffix = Regex("\\.class$")
      val classFileName = file.getName()

      if (!classFileName.contains(suffix)) {
        throw Exception("Invalid file name: file $classFileName does not end with .class")
      }

      // TODO: options for handling whether to override the existing file
      AtomicWriteFile.write(File(outputDir, classFileName.replace(suffix, ".java")), "${compilationUnit}", false)

      for (type in compilationUnit.types) {
        log.debug { "type: ${type.javaClass}" }
        if (type is ClassOrInterfaceDeclaration) {
          val classNode = type.getData(DecompileClass.CLASS_NODE)!!
          // TODO: for (callable in type.members.iterator().filterIsInstance<CallableDeclaration<*>>()) {
          for (callable in type.constructors + type.methods) {
            val methodNode = callable.getData(DecompileClass.METHOD_NODE)!!
            DecompileMethodBody.decompileBody(classNode, methodNode, callable)
            log.debug { "method: $callable" }
          }
        } else {
          TODO()
        }
      }

      log.debug { "compilationUnit\n${compilationUnit}" }
    }

    // for (((name, readers), classIndex) <- VFS.classes.zipWithIndex) {
    //   for ((path, classReader) <- readers) { // TODO: pick "best" classReader
    //     // TODO: why don't we combine the class and method passes?
    //     // Decompile class structure
    //     val compilationUnit = decompileClassFile(name, path.toString, classReader, classIndex)

    //     // Decompile method bodies
    //     for (typ <- compilationUnit.types.iterator().asScala) {
    //       val members = typ.members.iterator().asScala.flatMap(x => Decompile.methods[x].map((_, x))).toList
    //       for ((((classNode, methodNode), bodyDeclaration), methodIndex) <- members.zipWithIndex) {
    //         log.debug { "!!!!!!!!!!!!" }
    //         log.info {
    //           "Decompiling [${classIndex + 1} of ${VFS.classes.size}] ${classNode.name} [${methodIndex + 1} of " +
    //           "${members.size}] ${methodNode.name} (signature = ${methodNode.signature}, " +
    //           "descriptor = ${methodNode.desc})"
    //         }
    //         DecompileMethodBody.decompileBody(classNode, methodNode, bodyDeclaration)
    //       }
    //     }

    //     log.debug { f"compilationUnit\n${compilationUnit}" }
    //   }
    // }
  }

  /** Decompiles a class file and returns the corresponding CompilationUnit.
   *
   * @param classReader The ClassReader object for the class file
   * @return The decompiled CompilationUnit
   */
  fun decompileClassReader(classReader: ClassReader): CompilationUnit {
    val classNode = ClassNode(Opcodes.ASM9)
    classReader.accept(classNode, ClassReader.EXPAND_FRAMES) // TODO: Do we actually need ClassReader.EXPAND_FRAMES?

    log.debug { "class name: ${classNode.name}" }
    log.debug {
      val stringWriter = StringWriter()
      classNode.accept(TraceClassVisitor(null, Textifier(), PrintWriter(stringWriter)))
      "++++ asm ++++\n${stringWriter}"
    }

    return DecompileClass.decompileClass(classNode)
  }
}
