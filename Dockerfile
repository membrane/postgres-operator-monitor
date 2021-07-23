FROM maven:3.5.4-jdk-8-alpine

ENV HOME=/root
ADD settings.xml /root/.m2/

ADD pom.xml /app/
WORKDIR /app/
RUN mvn dependency:go-offline -B
ADD . /app/
RUN mvn package -DskipTests


FROM hub.predic8.de/p8/java10:1
MAINTAINER Valentin Brückel <brueckel@predic8.de>

COPY --from=0 /app/target/*.jar /app/foo.jar

CMD [ "/opt/jdk/bin/java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/app/foo.jar" ]