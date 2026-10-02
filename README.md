# StudyTrack — Código Base (Taller Evaluativo 2)

Ingeniería de Software V · Universidad Icesi · Período 202620

Este repositorio es el punto de partida del **Taller Evaluativo 2: Orquestación
CI/CD con Jenkins, Empaquetamiento Inmutable en Nexus y Webhooks con Smee.io**.
Varios archivos están **incompletos a propósito** (marcados con `TODO`): su
tarea es completarlos siguiendo las instrucciones del enunciado (PDF).

## Estructura del repositorio

```
codigo_base/
├── backend/                   # Spring Boot 3 (Java 17) + JPA + H2
│   ├── pom.xml                 # TODO: distributionManagement (Fase 2)
│   ├── src/main/...             # Código funcional (no requiere cambios)
│   ├── src/test/...             # Pruebas ya completas (Fase 1)
│   └── Dockerfile               # TODO: multi-stage (Fase 2)
├── frontend/                  # React 18 + Vite + TypeScript
│   ├── src/...                  # Código funcional (no requiere cambios)
│   ├── Dockerfile               # TODO: multi-stage (Fase 2)
│   └── nginx.conf               # TODO: fallback SPA (Fase 2)
├── infra/
│   ├── nexus_config/
│   │   ├── docker-compose.yml   # TODO: puertos y healthcheck (Fase 3)
│   │   └── .env.example
│   └── jenkins_config/
│       ├── Dockerfile           # TODO: Docker CLI (Fase 3)
│       ├── docker-compose.yml   # TODO: red, volumen, socket, smee (Fase 3)
│       ├── plugins.txt
│       ├── .env.example
│       └── smee/
│           └── Dockerfile
├── deploy/
│   └── docker-compose.yml     # TODO: imágenes desde Nexus (Fase 3/4)
└── Jenkinsfile                 # TODO: las 4 etapas del pipeline (Fase 4)
```

## Orden sugerido de trabajo

1. **Fase 1** — `backend/` y `frontend/`: ejecute las pruebas y el build tal
   como están (no requieren cambios) para validar que el código base
   funciona antes de contenerizarlo.
2. **Fase 2** — Complete `backend/Dockerfile`, `frontend/Dockerfile`,
   `frontend/nginx.conf` y el bloque `distributionManagement` de
   `backend/pom.xml`.
3. **Fase 3** — Complete `infra/nexus_config/docker-compose.yml` e
   `infra/jenkins_config/docker-compose.yml`, levante ambos stacks, cree su
   canal en [smee.io](https://smee.io) y regístrelo como webhook en su
   repositorio de GitHub.
4. **Fase 4** — Complete el `Jenkinsfile` (raíz del repositorio) y
   `deploy/docker-compose.yml`, haga `git push` y verifique que el pipeline
   se dispare automáticamente y finalice en verde.

Consulte el documento del taller (PDF) para el detalle de cada fase, el
cuestionario técnico, la autorreflexión y la rúbrica de evaluación.

## Nota sobre puertos

Todo el ecosistema (Jenkins + Nexus + Smee + la propia aplicación) corre en
contenedores en **su misma máquina**. Los puertos sugeridos en el enunciado
evitan colisiones entre sí, pero si usted ya tiene algo corriendo en alguno
de ellos, ajústelo en los archivos `.env` correspondientes — lo importante es
la consistencia entre lo que declara en `docker-compose.yml` y lo que
referencia en el `Jenkinsfile`.
