FROM openjdk:8

WORKDIR /app

ADD /target/scala-2.12/server-assembly-**.jar /app/server.jar

ADD application.conf /app/application.conf

EXPOSE 5558

CMD ["/usr/bin/java", "-Dconfig.file=application.conf", "-jar", "server.jar"]