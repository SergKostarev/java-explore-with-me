FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG JAR_CLIENT_FILE=ewm-stats-client/target/*.jar
COPY ${JAR_CLIENT_FILE} ewm-stats-client-app.jar
ARG JAR_DTO_FILE=ewm-stats-dto/target/*.jar
COPY ${JAR_DTO_FILE} ewm-stats-dto-app.jar
ARG JAR_SERVICE_FILE=ewm-stats-serv/target/*.jar
COPY ${JAR_SERVICE_FILE} ewm-stats-serv-app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /ewm-stats-client-app.jar"]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /ewm-stats-dto-app.jar"]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /ewm-stats-serv-app.jar"]