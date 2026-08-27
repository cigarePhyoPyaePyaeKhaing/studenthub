FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
COPY database ./database
RUN mvn -B clean package

FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/studenthub.war /usr/local/tomcat/webapps/ROOT.war
ENV PORT=8080
EXPOSE 8080
CMD ["sh", "-c", "case \"${PORT}\" in ''|*[!0-9]*) echo 'PORT must be numeric' >&2; exit 1;; esac; sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT}\\\"/\" /usr/local/tomcat/conf/server.xml && exec catalina.sh run"]
