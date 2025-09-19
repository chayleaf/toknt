package toknt

import scala.io.Source
import scala.util.Using
import org.antlr.v4.runtime.{CharStreams, Lexer}
import org.chayleaf.{
  CPP14Lexer,
  CSharpLexer,
  HaskellLexer,
  Python3Lexer,
  RustLexer
}

object Application {
  def process(filename: String, input: String): Option[Long] = {
    val charStream = CharStreams.fromString(input)

    filename
      .split("\\.")
      .lastOption
      .map(_.toLowerCase)
      .flatMap(ext =>
        ext match {
          case "c" | "cc" | "cpp" | "cxx" | "c++" | "h" | "hh" | "hpp" | "hxx" |
              "h++" =>
            Some(new CPP14Lexer(charStream).getAllTokens.stream)
          case "rs" =>
            Some(new RustLexer(charStream).getAllTokens.stream)
          case "cs" =>
            Some(new CSharpLexer(charStream).getAllTokens.stream)
          case "py" =>
            Some(
              new Python3Lexer(charStream).getAllTokens.stream.filter(x =>
                x.getType match {
                  case Python3Lexer.INDENT | Python3Lexer.DEDENT |
                      Python3Lexer.NEWLINE =>
                    false
                  case _ => true
                }
              )
            )
          case "hs" =>
            Some(
              new HaskellLexer(charStream).getAllTokens.stream.filter(x =>
                x.getType match {
                  case HaskellLexer.VOCURLY | HaskellLexer.VCCURLY |
                      HaskellLexer.SEMI =>
                    false
                  case _ => true
                }
              )
            )
          case _ => None
        }
      )
      .map(_.filter(x => x.getChannel != Lexer.HIDDEN).count)
  }
  def main(args: Array[String]): Unit =
    args foreach (arg =>
      println(
        process(arg, Using.resource(Source.fromFile(arg))(_.mkString))
          .map(_.toString)
          .getOrElse("?")
      )
    )
}
