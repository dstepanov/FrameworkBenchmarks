FROM gradle:8.1-jdk18 as build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle micronaut4-jdbc:build -x test --no-daemon

FROM openjdk:19
WORKDIR /micronaut
COPY --from=build /home/gradle/src/micronaut4-jdbc/build/libs/micronaut4-jdbc-all.jar micronaut.jar
COPY run_benchmark.sh run_benchmark.sh

EXPOSE 8080
ENTRYPOINT "./run_benchmark.sh"
