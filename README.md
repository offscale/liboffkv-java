liboffkv-java
===========
[![License](https://img.shields.io/badge/license-MIT%2FApache--2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/offscale/liboffkv-java.svg?branch=master)](https://travis-ci.org/offscale/liboffkv-java)

#### liboffkv-java is a wrapper around [liboffkv](https://github.com/offscale/liboffkv).

#### The library is designed to provide a uniform interface for three distributed KV storages: etcd, ZooKeeper and Consul.


## Supported platforms

The library is currently tested on

- Ubuntu 18.04

  Full support.

- MacOS

  Full support.


## Dependencies

  - C++ compiler

    Currently tested compilers are

    - VS 2019
    - g++ 7.4.0
    - clang

    VS 2017 is known to fail.

  - [vcpkg](https://docs.microsoft.com/en-us/cpp/build/vcpkg)

  - Maven 3.6.1 or higher

  - JDK 11

    The library targets Java 1.8, but one of the maven plugins is compiled for Java 11.

## Configure build system

- Install [vcpkg](https://docs.microsoft.com/en-us/cpp/build/vcpkg)
- Install Java 11 or higher
- Install [maven](https://maven.apache.org/)
- Install vcpkg packages `offscale-libetcd-cpp`, `zkpp`, `ppconsul`
- Download submodules: `git submodule init && git submodule update`

## Build and test

```sh
mvn test -Pvcpkg-cmake -Dcmake.toolchain="vcpkg-root/scripts/buildsystems/vcpkg.cmake"
# where vcpkg-root is path to your vcpkg directory
```

## License

Licensed under either of

- Apache License, Version 2.0 ([LICENSE-APACHE](LICENSE-APACHE) or <https://www.apache.org/licenses/LICENSE-2.0>)
- MIT license ([LICENSE-MIT](LICENSE-MIT) or <https://opensource.org/licenses/MIT>)

at your option.

