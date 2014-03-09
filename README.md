# clojure.llvm

A LLVM bytecode compiler for Clojure

## Prerequisites

Mac OS X with LLVM 3.4 built from source & installed to /usr/local/opt/llvm

## Usage

To automatically rebuild the Java JNA bindings with JNAerator:

    $ java -jar resources/jnaerator.jar resources/config.jnerator

This will populate the java source paths at src/java with the bindings JNA requires to interface with the LLVM-C library.

Documentation for the LLVM-C interface to the C++ LLVM backend can be found at
http://llvm.org/doxygen.

General documentation for LLVM 3.4 can be found at http://llvm.org/releases/3.4/docs.

## TODO

1. Make this library actually work :)
2. Make JNA bindings & build process cross platform


## License

Copyright © 2014 Adrian Medina

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
