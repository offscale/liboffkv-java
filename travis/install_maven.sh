#!/usr/bin/env bash

set -e

curl -L http://apache-mirror.rbc.ru/pub/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz -o "$HOME"/maven.tar.gz

mkdir -p "$HOME/maven"
tar xzf "$HOME"/maven.tar.gz -C "$HOME/maven" --strip-components=1

rm -f "$HOME"/maven.tar.gz
