#!/bin/bash

JAVA_OPTIONS="-server \
  -XX:+UseParallelGC \
  -XX:+UseNUMA \
  -XX:+UseStringDeduplication \
  -XX:-StackTraceInThrowable \
  -Dio.netty.buffer.checkBounds=false \
  -Dio.netty.buffer.checkAccessible=false \
  -Dvertx.disableMetrics=true \
  -Dvertx.threadChecks=false \
  -Dvertx.disableContextTimings=true  \
  -Dvertx.disableTCCL=true  \
  -Dmicronaut.environments=benchmark
  $@"

export PROCESSORS="$((`grep --count ^processor /proc/cpuinfo`))"
export PROCESSORSX2="$((2*PROCESSORS))"

java $JAVA_OPTIONS -jar micronaut.jar
