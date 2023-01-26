# Syllabus DB

This is the implmentation of a schemaless JSON document database, written for a [blog series][blog].

## Implementation notes

This code is written with the goal of simplicity in mind. There are many inefficent operations, especially on a micro level,
in order to aid for understanding. As the main goal of this codebase is for instruction and learning, not as a serious production system.
As such, aspects such as error handling and performance will only be implemented in a way that is required for the learning of how a database system works,
and will not be as robust a true battle tested production system.

## Building

[Bazel][bazel] is the build system for SyllabusDB. You can build everything using 
`bazel build //...`, run all the tests using `bazel test //...`.

The project uses JDK 17, but Bazel will manage downloading the correct JVM for your system.

## Contributing

The code here is closely tied a to blog series, so any contributions outside of typos, missing documentation, 
clarifications or outright bugs should be discussed beforehand.

[blog]: https://blog.rockwotj.com/toy-db-1
[bazel]: https://bazel.build
