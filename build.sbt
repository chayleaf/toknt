import Dependencies._

import java.net.URI
import java.net.URL
import java.nio.file.StandardCopyOption
import sbt.io.IO

val baseUrl =
  "https://raw.githubusercontent.com/antlr/grammars-v4/6b517735620223475eefaa85c92f8d6bce15f360"
val lexers = Seq(
  "cpp/CPP14Lexer.g4",
  "python/python3_13/PythonLexer.g4",
  "python/python3_13/Java/PythonLexerBase.java",
  // used by PythonLexer...
  "python/python3_13/PythonParser.g4",
  "haskell/HaskellLexer.g4",
  "haskell/Java/HaskellBaseLexer.java",
  "rust/RustLexer.g4",
  "rust/Java/RustLexerBase.java",
  "csharp/CSharpLexer.g4",
  "csharp/Java/CSharpLexerBase.java",
  "kotlin/kotlin/KotlinLexer.g4",
  "kotlin/kotlin/UnicodeClasses.g4"
)

ThisBuild / scalaVersion := "3.7.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "org.chayleaf"
ThisBuild / organizationName := "chayleaf"

enablePlugins(Antlr4Plugin)
Antlr4 / antlr4PackageName := Some((ThisBuild / organization).value)
Antlr4 / antlr4Version := "4.13.2"

lazy val root = (project in file("."))
  .settings(
    name := "toknt",
    scalacOptions ++= Seq(
      "-java-output-version",
      "8",
      "-deprecation",
      "-feature"
    ),
    javacOptions ++= Seq("-source", "8", "-target", "8"),
    libraryDependencies ++= Seq(
      munit % Test,
      "org.antlr" % "antlr4-runtime" % (Antlr4 / antlr4Version).value
    )
  )

val downloadLexers = taskKey[Seq[File]]("Download lexers")

downloadLexers := {
  val prefix = (Compile / sourceDirectory).value

  lexers.map(path => {
    val name = path.split('/').last
    val outDir = if (name.endsWith(".g4")) {
      prefix / "antlr4" / "fetched"
    } else {
      prefix / "java" / "fetched"
    }
    IO.createDirectory(outDir)
    val outFile = outDir / name

    if (!outFile.exists()) {
      val is = URI.create(baseUrl + "/" + path).toURL.openStream()
      try {
        java.nio.file.Files.copy(is, outFile.toPath)
      } finally is.close()
      if (name.endsWith(".java")) {
        // HACK: put java files into this package
        // (Scala code can't access classes from the root namespace)
        val text = IO.read(outFile)
        IO.write(
          outFile,
          "package " + (ThisBuild / organization).value + ";\n" + text
        )
      } else if (path.equals("cpp/CPP14Lexer.g4")) {
        // HACK: tokenize preprocessor directives as if they were normal code
        // (# is 1 token, \ is 0 tokens)
        val text = IO.read(outFile)
        IO.write(
          outFile,
          text
            .replace(
              """MultiLineMacro: '#' (~[\n]*? '\\' '\r'? '\n')+ ~ [\n]+ -> channel (HIDDEN);""",
              """Backslash: '\\' -> channel (HIDDEN);"""
            )
            .replace(
              """Directive: '#' ~ [\n]* -> channel (HIDDEN);""",
              "Hash: '#';"
            )
        )
      }
    }

    outFile
  })
}

(Compile / compile) := ((Compile / compile) dependsOn downloadLexers).value

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
