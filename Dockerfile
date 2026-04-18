FROM eclipse-temurin:21-jre

WORKDIR /app

ARG SPRING_PROFILE
RUN test -n "${SPRING_PROFILE}" || { echo "ERROR: SPRING_PROFILE build arg is required (dev or prod)"; exit 1; }
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}
ENV JAVA_OPTS=""

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
