FROM tomcat:9-jdk14-openjdk-buster

MAINTAINER geoffrey.squire@data61.csiro.au

ARG war=./target/AuScope-Portal-API-5.2.0-SNAPSHOT.war

ADD ${war} /usr/local/tomcat/webapps/VGL-Portal.war

RUN rm -rf /usr/local/tomcat/webapps/ROOT \
  && unzip /usr/local/tomcat/webapps/VGL-Portal.war -d /usr/local/tomcat/webapps/ROOT \
  && rm /usr/local/tomcat/webapps/VGL-Portal.war \
  && touch /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/application.yaml

# Don't bake-in application.yaml here. Leave it for the caller (e.g.
# docker-compose).
# 
# ADD application.yaml /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/
