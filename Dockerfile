FROM openjdk:19-jdk
RUN mkdir /filehub
VOLUME /tmp
EXPOSE 8088
COPY target/FileHub-1.0.0.jar app.jar
ENTRYPOINT exec java -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar /app.jar