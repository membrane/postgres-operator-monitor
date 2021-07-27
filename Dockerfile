FROM maven:3.8.1-jdk-11

ENV HOME=/root
ADD settings.xml /root/.m2/

ADD pom.xml /app/
WORKDIR /app/
RUN mvn dependency:go-offline -B -Dmaven.artifact.threads=8
ADD . /app/
RUN mvn package -DskipTests


FROM openjdk:16.0.2
MAINTAINER Valentin Brückel <brueckel@predic8.de>

COPY --from=0 /app/target/*.jar /app/foo.jar

CMD [ "/opt/jdk/bin/java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/app/foo.jar" ]