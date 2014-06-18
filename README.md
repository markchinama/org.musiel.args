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



##More Examples

This library strives to _keep simple things simple while hard things possible_. Above example is a simple case (not the simplest). This 
chapter demonstrates the main aspects of the API with examples from the simplest use case to the most complex.


###Use Case 1: Quick Sketch

Assume you are going to implement an HTTP downloader (like wget). It accepts command line arguments like:

```sh
my-wget -o index.html --notify a@g.com --notify b@g.com -f --log-level WARN http://www.github.com
```

The simplest way to parse above arguments is the following **ONE** line:

```java
GenericResult result = new GenericParser().parse( args);
// then use the parse result
result.getArgument( "-o");          // returns "index.html"
result.getArguments( "--notify");   // returns a list containing "a@g.com" and "b@g.com"
result.isOccurred( "-f");           // returns true
result.isOccurred( "-g");           // returns false
result.getArgument( "--log-level"); // returns "WARN"
result.getArgument( "-m");          // returns null
result.getOperand();                // returns "http://www.github.com"
```

Quick, but it has its disadvantages. To name one, if `--log-level WARN` was omitted, it would treat `http://www.github.com` as an 
argument of option `-f`, because the parser has no knowledge about whether option `-f` accepts an argument, so it does its best guess. 
This is why it is often important to define the options before the parsing starts.


###Use Case 2: Bare Interfaces

Defining the options using an interface, as illustrated at the beginning of this document, is the recommended way (there is another):

```java
interface Options {
   File o();
   String[] notify();
   boolean f();
   String logLevel();
   @Operands URL url();
}
```

You might have guessed that length-1 alpha-digit method names produce POSIX short options (e.g. "-o") and longer ones produce GNU long 
names (e.g. "--log-level"). The new thing here is `@Operands` annotation, it makes the method return the operands rather than defining an 
option.

Now the problem is solved: if `--log-level WARN` in above example is omitted, the URL will not be assigned to option `-f` because the 
boolean method defines an option that does not accepts any argument.


###Use Case 3: Annotated Interfaces

####Aliases

It is a good idea to assign a short name AND a long one to frequently used options so the user can choose between readability and 
compactness. To do so, add an `@Option` annotation with any number of option names. Once this is done, auto-naming function works no more.

```java
@Option({"-o", "--output"})
File output();

@Option({"-f", "--overwrite"})
boolean overwrite();
```

####Arity

All options are by default optional, to make one required, add a `@Required` annotation.

All options that return arrays are repeatable, and others non-repeatable (`--notify` can occur more than once, but `-o` cannot). However, 
this can be changed by `@Repeatable(false)` and `@Repeatable`, respectively, in which case, the return value of an array method has at 
most one element, and the non-array method returns the first occurrence (others are dropped).

####Argument Policy

By default, option methods other than void and boolean return type require arguments, while void and boolean accept no argument. This can 
be changed by `@Argument(NONE)`, `@Argument(OPTIONAL)`, or `@Argument(REQUIRED)`.


###Use Case 4: Complex Operands

Assume you are writing a `cp` command that accepts one or more input file names, plus exactly one output directory name (i.e. 
INPUT... OUTPUT) as operands. So far we can get all the operands from one method annotated `@Operands` and differentiate input and output 
manually. But there is a better way:

```java
@OperandPattern("INPUT... OUTPUT")
interface CpOptions {

   @Operands("INPUT")
   File[] inputFiles();

   @Operands("OUTPUT")
   File outputFile();
}
```

The library checks for ambiguity of the pattern, so "INPUT... OUTPUT..." will not be accepted, since operands "A B C" has two different 
interpretations (INPUT="A B" OUTPUT="C" and INPUT="A" OUTPUT="B C").


###Use Case 5: Adding Constraints

Let's get back to the `wget` example. Assume that option `--log-level` only accepts one of following three legal arguments: INFO, WARN, 
and ERROR. This can be assured by adding a `@StringValue` annotation:

```java
@StringValue(pattern="INFO|WARN|ERROR")
String logLevel();
```

Another example of constraints could be:

```java
@ShortValue(min="0", max="100")
short volume();
```


###Use Case 6: Printing Help Messages

Descriptions of options, operands, and the command line can be added into the interface itself (by annotations):

```java
@OperandPattern("URL")
@Description("Fetches a resource and save it into a file")
interface Options {
   @Option({"-o", "--output"})
   @ArgumentName("FILE")
   @Description("Save the resource into FILE")
   File outputFile();

   @ArgumentName("EMAIL")
   @Description("Send a notification e-mail to EMAIL when done")
   String[] notify();

   @Option({"-f", "--overwrite"})
   @Description("Overwrite the file if it exists")
   boolean overwrite();

   @StringValue(pattern="INFO|WARN|ERROR")
   @ArgumentName("LEVEL")
   @Description("Print log messages with level LEVEL or higher")
   String logLevel();

   @Operands("URL")
   @Description("URL of the resource to download")
   URL url();
}
```

There is a more convenient way to provide all those descriptive information, which is to use a `@Resource` annotation to specify a 
ResourceBundle base, and add internationalized content into those resource bundles. See `@Resource` for detail.

After descriptions and argument names are given, you can use `GnuMonoTermPrinter` to print a help message for the command line interface:

```java
ReflectParser< Options> parser = new ReflectParser< Options>( Options.class);
new GnuMonoTermPrinter().print( "wget", parser, parser);
```

###Use Case 7: Custom Data Types

You may need return types other than default ones (primitive types, their wrapper classes, String, File, URL, and their arrays). To let 
a parser support them, define an annotation itself is annotated `@DecoderAnnotation` (like StringValue or ShortValue), and add that 
annotation on the method returning your custom data type.


###Use Case 8: Syntax

Both `Parser` implementations use `GnuSyntax` by default, and with its default configuration. An alternative is to use `PosixSyntax`. See 
their JavaDoc for more details, they have a few configurable properties.



##...

TODO



##Copyright and License

This project, including this README file, is written by Bagana. The copyright belongs to Bagana. It is publish under the Apache License 
Version 2.0. See LICENSE file for detail.
