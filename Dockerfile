FROM openjdk:17
COPY target/Sistema-de-Gestion-de-Biblioteca-2.2-1.jar app.jar
EXPOSE 8100
ENTRYPOINT [ "java","-jar", "app.jar" ]