FROM openjdk:8-jre-alpine
COPY app/SimpleServer.jar app/SimpleServer.jar
COPY app/SimpleServer.properties app/SimpleServer.properties
COPY /data/ /data/
CMD ["java", "-cp", "app/SimpleServer.jar:app/.", "Main.AppEntryPoint"]