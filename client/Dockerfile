FROM openjdk:8

WORKDIR /app

ADD /target/scala-2.12/client-assembly**.jar /app/client.jar

ADD application.conf /app/application.conf

CMD ["/usr/bin/java", "-Dconfig.file=application.conf", "-jar", "client.jar"]