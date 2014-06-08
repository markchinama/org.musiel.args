org.musiel.args

#A Command Line Argument Parser



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

Then in your main class:

```java
Options options = ReflectParser.parse( Options.class, args);
```

will give you the parse result in the interface **YOU** defined!

`options.help()` indicates whether `--help` option is specified, while `options.output()` gives you a `File` object whose path name was 
given in `--output` option.



##Learn More

TODO



##Copyright and License

This project, including this README file, is written by Bagana. The copyright belongs to Bagana. It is publish under the Apache License 
Version 2.0. See LICENSE file for detail.
