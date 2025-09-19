# toknt

A source code token counter using antlr.
Currently supports C, C++, C#, Haskell, Python, Rust.

## Adding a language

1. See which files from [antlr/grammars-v4](https://github.com/antlr/grammars-v4) the lexer needs
2. Add them to `build.sbt`'s `lexers`
3. Extend `src/main/scala/toknt/Application.scala` to support the language's file extension
4. Add the language to this README.md.

If grammars-v4 doesn't already have your language, you can still add it by creating an antlr lexer in
`toknt`'s tree, but the process will naturally be a bit more involved.
