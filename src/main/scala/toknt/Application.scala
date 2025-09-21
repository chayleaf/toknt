package org.chayleaf.toknt

import scala.io.Source
import scala.util.Using
import org.antlr.v4.runtime.{CharStreams, Lexer}
import org.chayleaf._

def process(filename: String, input: String): Option[Long] =
  val charStream = CharStreams.fromString(input)

  filename
    .split("\\.")
    .lastOption
    .map(_.toLowerCase)
    .collect {
      case "c" | "cc" | "cpp" | "cxx" | "c++" | "h" | "hh" | "hpp" | "hxx" |
          "h++" =>
        new CPP14Lexer(charStream).getAllTokens.stream
      case "rs" => new RustLexer(charStream).getAllTokens.stream
      case "cs" => new CSharpLexer(charStream).getAllTokens.stream
      case "py" =>
        new PythonLexer(charStream).getAllTokens.stream.filter(_.getType match
          case PythonLexer.INDENT | PythonLexer.DEDENT | PythonLexer.NEWLINE =>
            false
          case _ => true)
      case "hs" =>
        new HaskellLexer(charStream).getAllTokens.stream.filter(_.getType match
          case HaskellLexer.VOCURLY | HaskellLexer.VCCURLY |
              HaskellLexer.SEMI =>
            false
          case _ => true)
      case "kt" =>
        new KotlinLexer(charStream).getAllTokens.stream.filter(_.getType match
          case KotlinLexer.NL => false
          case _              => true)
    }
    .map(_.filter(_.getChannel != Lexer.HIDDEN).count)

def main(args: Array[String]): Unit =
  args foreach (arg =>
    println(
      process(arg, Using.resource(Source.fromFile(arg))(_.mkString))
        .map(_.toString)
        .getOrElse("?")
    )
  )
