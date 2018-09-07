FROM openjdk:8

WORKDIR /app

ADD /target/scala-2.12/server-assembly-**.jar /app/server.jar

EXPOSE 5558

CMD ["/usr/bin/java", "-jar", "server.jar"]