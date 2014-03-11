# clojure.llvm

A LLVM bytecode compiler for Clojure

## Prerequisites

Mac OS X with LLVM 3.5 built from source. If you use Homebrew, you can build and install LLVM with the following command:

    $ brew install llvm35 --HEAD --with-clang --with-libcxx --with-asan --all-targets --rtti

## Usage

To automatically rebuild the Java JNA bindings with JNAerator:

    $ java -jar resources/jnaerator.jar resources/config.jnerator

This will populate the java source paths at src/java with the bindings JNA requires to interface with the LLVM-C library.

Documentation for the LLVM-C interface to the C++ LLVM backend can be found at
http://llvm.org/doxygen.

General documentation for LLVM can be found at http://llvm.org/docs.

## TODO

1. Make this library actually work :)

## License

Copyright © 2014 Adrian Medina

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
