# =============================================================================
# Taller Evaluativo 2 - Fase 2: Empaquetamiento Inmutable Multi-Stage
# Frontend: studytrack-frontend
#
# - Stage 1 compila la SPA con Node 20 (tsc + vite build).
# - Stage 2 solo sirve los estáticos de dist/ con Nginx (sin Node ni fuentes).
# - Nginx corre con el usuario sin privilegios "nginx" en el puerto 3000.
# =============================================================================

# ---------- Stage 1: Builder ----------
FROM node:20-alpine AS builder
WORKDIR /app

# URL del backend que usará el navegador (Vite la incrusta en tiempo de build)
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=${VITE_API_URL}

# Dependencias primero para aprovechar la cache de capas
COPY package*.json ./
RUN npm install --no-audit --no-fund

COPY . .
RUN npm run build

# ---------- Stage 2: Runtime ----------
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

# Permisos para ejecutar Nginx sin root
RUN chown -R nginx:nginx /usr/share/nginx/html /var/cache/nginx /var/log/nginx /etc/nginx/conf.d \
 && touch /var/run/nginx.pid \
 && chown nginx:nginx /var/run/nginx.pid

USER nginx
EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
