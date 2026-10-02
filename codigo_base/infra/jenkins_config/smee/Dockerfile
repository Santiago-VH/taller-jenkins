# Contenedor liviano que ejecuta smee-client: reenvía los eventos del
# webhook de GitHub (recibidos en un canal público de smee.io) hacia el
# endpoint /github-webhook/ de Jenkins, sin necesidad de exponer Jenkins
# a Internet.
FROM node:20-alpine

RUN npm install -g smee-client

# Valores por defecto (docker-compose los sobrescribe desde .env)
ENV SMEE_URL="" \
    SMEE_TARGET="http://jenkins:8080/github-webhook/"

# No requiere privilegios: solo hace conexiones salientes
USER node

ENTRYPOINT ["sh", "-c", "smee -u \"$SMEE_URL\" -t \"$SMEE_TARGET\""]
