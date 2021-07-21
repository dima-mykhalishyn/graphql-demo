FROM openjdk:11.0.4-jre-slim

ADD ./*app/target/*.jar /app/demo-api.jar

ENV JAVA_XMX_VALUE "512m"

#Java execution
CMD ["sh", "-c", "java -jar -Xmx${JAVA_XMX_VALUE} /app/demo-api.jar"]
EXPOSE 8080
