org.musiel.args

#A Command Line Argument Parser

Bagana, π day, 2014



##Example in 15 Seconds

Define a Java interface:

```java
interface Options {
   File output();
   Integer logLevel();
   boolean version();
   boolean help();
}
```

Then in your main function,

```java
Options options = ReflectParser.parse( Options.class, args).check();
```

gives you the parse result in the interface **YOU** defined!

`options.help()` indicates whether `--help` option is given, while `options.output()` returns a `File` object whose path name was given 
in `--output` option.



##Example without an Interface

For real simple use cases, even the pre-defining of an interface can be skipped. For example, for command line:

```shell
apply-template -o output.html -f --log-level WARN input.xml
```

following **ONE** line will parse it into a `GenericResult` model:

```java
GenericResult result = new GenericParser().parse( args);
// then use the parse result
result.getArgument( "-o");          // returns "output.html"
result.isOccurred( "-f");           // returns true
result.isOccurred( "-g");           // returns false
result.getArgument( "--log-level"); // returns "WARN"
result.getOperand();                // returns "input.xml"
```



##Learn More

TODO



##Copyright and License

This project, including this README file, is written by Bagana. The copyright belongs to Bagana. It is publish under the Apache License 
Version 2.0. See LICENSE file for detail.
