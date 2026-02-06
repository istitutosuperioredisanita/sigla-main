# DOCKER-VERSION 17.10.0-ce
FROM eclipse-temurin:21-jdk-alpine
LABEL maintainer="Marco Spasiano <marco.spasiano@cnr.it>"

COPY sigla-web/target/sigla-bootable.jar /opt/sigla-bootable.jar

ENV ESERCIZIO=2024
ENV JBOSS_BIND_ADDRESS=0.0.0.0
ENV ALLOW_ORIGIN=http://localhost:9000

EXPOSE 8080

CMD java -Dliquibase.bootstrap.esercizio=$ESERCIZIO -Dcors.allow-origin=$ALLOW_ORIGIN -Djava.security.egd=file:/dev/./urandom -jar /opt/sigla-bootable.jar
