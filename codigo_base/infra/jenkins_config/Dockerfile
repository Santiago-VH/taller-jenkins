# Imagen de Jenkins con el CLI de Docker instalado, para que el propio
# controlador (montando /var/run/docker.sock) pueda construir y publicar
# las imágenes del backend y del frontend durante el pipeline.
FROM jenkins/jenkins:lts-jdk17

USER root

# TODO

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

USER jenkins
