FROM openjdk:11-jre-slim
VOLUME /tmp
ADD target/*.jar pdfsigner.jar
ADD src/main/resources/ca_certs/cuba_root_ca.pem cuba_root_ca.pem
ADD src/main/resources/ca_certs/ca_xetid.pem ca_xetid.pem
ENV TZ="America/Havana"
ENTRYPOINT [ "java", "-jar", "pdfsigner.jar" ]