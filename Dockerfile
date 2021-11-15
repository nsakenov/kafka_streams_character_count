FROM maven:3-openjdk-8-slim AS BUILD
WORKDIR /tmp/
COPY /src/main/java/com/nurbolsakenov/resources/log4j.properties /app/
COPY /scripts/wait-for-it.sh /app/
COPY pom.xml /tmp/
COPY src /tmp/src/
RUN chmod +x /app/wait-for-it.sh
RUN mvn package
WORKDIR /app/
