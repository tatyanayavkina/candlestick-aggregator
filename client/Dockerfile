FROM openjdk:8

WORKDIR /app

ADD /target/scala-2.12/client_**.jar /app/client.jar

CMD ["/usr/bin/java", "-jar", "server.jar"]