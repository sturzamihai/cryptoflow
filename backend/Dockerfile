# syntax=docker/dockerfile:experimental
FROM atools/jdk-maven-node:jdk17-nodelts as build
WORKDIR /workspace/app

RUN adduser --disabled-password spring-user

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN mvn install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM atools/jdk-maven-node:jdk17-nodelts
VOLUME /tmp

COPY --from=build /etc/passwd /etc/passwd

ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

USER spring-user

ENTRYPOINT ["java","-cp","app:app/lib/*","ro.mihaisturza.cryptoflow.CryptoflowApplication"]