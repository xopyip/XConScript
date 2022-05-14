# XConScript
### Basic implementation of simple concatenative programming language with compiler to .class file.
## Motivation:
> I want to learn about parsers, lexers, compilers and also about architecture of JVM.

## TODO:
 - [ ] Add support for comments
 - [ ] Add support for longs
 - [ ] Implement proper testing framework
 - [ ] Add block editor (something like Scratch)
 - [ ] Add support for Minecraft server scripting

---

# Usage

```bash
$ java -jar xconscript.jar compile examples/var.xript
$ java -cp examples ScriptVar
1
2
3
5
8
13
21
34
55
89
144
233
377
610
987
1597
2584
4181
6765
10946
17711
Hello world
```

---

# Language

### Methods
Each script must contain a method named `main` without any arguments and return value
```
method main void

end
```
You can add arguments and change return type
```
method process(key int, value string) int

end
```

### Variables
`var name type` - Define a variable named `name` with type `type`

`>test` - Assign a value from top of stack to the variable `test`

`test>` - Read a value from variable `test` and push it to the stack

### Java methods
You can use existing Java methods in your script. Just use it name or import it.

```
"Test"
java.lang.System.out.println(string)
```
```
import java.lang.System Sys
...
"Test"
Sys.out.println(string)
```

To make new instance of a class use its name as function:
```
import java.lang.System Sys
import java.util.Scanner Scan
import java.io.InputStream IS
method main void
  Sys.in> Scan(IS)
end
```

# Control flow

### IF
```
if 3 5 < then
  2
else
  3
end
log
```

### While
```
while v> 100 <= do
end
```

# Internal methods
`log` - Logs value from top of the stack on stdout

`swap` - Swap two values from top of the stack

`dup` - Duplicate value from top of the stack

`drop` - Drop value from top of the stack

`drop2` - Drop two values from top of the stack

`???` - Crashes app to force JVM to print current frame