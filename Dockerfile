FROM registry-1.docker.io/library/maven:3.8.1-jdk-11

ENV HOME=/root
ADD settings.xml /root/.m2/

ADD pom.xml /app/
WORKDIR /app/
RUN mvn dependency:go-offline -B -Dmaven.artifact.threads=8
ADD . /app/
RUN mvn package -DskipTests


FROM registry-1.docker.io/library/ubuntu
RUN apt-get update && apt-get install -y openjdk-16-jre-headless

COPY --from=0 /app/target/*.jar /app/foo.jar

CMD [ "/usr/bin/java", "-jar", "/app/foo.jar" ]
