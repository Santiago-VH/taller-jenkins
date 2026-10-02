# Guía de ejecución — Taller Evaluativo 2 (Jenkins + Nexus + Smee)

Pasos para correr la solución en un equipo con Docker (sala de cómputo).
La raíz del proyecto es `codigo_base/`. Todos los comandos sirven en PowerShell y en bash.

| Servicio | URL en el host | Dentro de `cicd_network` |
|---|---|---|
| Jenkins UI | http://localhost:9082 | http://jenkins:8080 |
| Nexus UI / API | http://localhost:9081 | http://nexus:8081 |
| Nexus Docker registry | `localhost:9080` | — |
| Backend | http://localhost:8080/api/tasks | http://studytrack-api:8080 |
| Frontend | http://localhost:3000 | — |

> ¿Por qué hay dos direcciones para Nexus? `mvn deploy` corre **dentro** del contenedor de Jenkins, así que usa `http://nexus:8081`. En cambio, `docker push/pull` lo ejecuta el **daemon del host** (Jenkins usa el socket montado), así que usa `localhost:9080`. Docker acepta HTTP sin TLS para `localhost`, por eso no hay que configurar *insecure-registries*.

---

## Fase 1: pruebas y build (sin cambios en el código)

```bash
cd codigo_base/backend
mvn -B clean test            # TaskServiceTest (6) + TaskControllerTest (5) en verde

cd ../frontend
npm install
npx tsc --noEmit             # consistencia de tipos
npm run build                # genera dist/
```

Si el equipo no tiene Maven o Node instalados, puede correr lo mismo con Docker (PowerShell):

```powershell
docker run --rm -v "${PWD}:/app" -w /app maven:3.9.6-eclipse-temurin-17-alpine mvn -B clean test   # en backend/
docker run --rm -v "${PWD}:/app" -w /app node:20-alpine sh -c "npm install && npx tsc --noEmit && npm run build"   # en frontend/
```

## Fase 2: imágenes multi-stage (prueba local)

```bash
cd codigo_base/backend
docker build -t studytrack-api:1.0.0-manual --build-arg REVISION=1.0.0-manual .
cd ../frontend
docker build -t studytrack-frontend:1.0.0-manual .
docker images "studytrack*"         # backend ~220 MB (JRE alpine), frontend ~50 MB
```

La **publicación manual** que pide la Fase 2 se hace cuando Nexus ya está corriendo (ver Fase 3, paso 4).

## Fase 3: levantar el ecosistema

> **Orden importante:** primero Jenkins (crea la red `cicd_network`) y después Nexus.

### 1. Canal de Smee
Entre a https://smee.io, haga clic en **Start a new channel** y copie la URL.

### 2. Jenkins + smee-client
```bash
cd codigo_base/infra/jenkins_config
cp .env.example .env          # edite SMEE_CHANNEL_URL con su canal
docker compose up -d --build  # la primera vez tarda (Docker CLI, Maven, plugins)
docker compose ps             # jenkins debe quedar "healthy"
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 3. Nexus
```bash
cd ../nexus_config
cp .env.example .env
docker compose up -d
docker compose ps             # esperar a "healthy" (3–5 min la primera vez)
docker exec nexus cat /nexus-data/admin.password
```

Configuración en http://localhost:9081:

1. **Sign in** con `admin` y la clave del archivo anterior. El asistente le pide definir una clave nueva. En la pregunta de acceso anónimo, elija *Disable anonymous access*.
2. **Settings (engranaje) → Security → Realms**: active **Docker Bearer Token Realm** y guarde. Sin esto, `docker login` falla.
3. **Repositories → Create repository → docker (hosted)**:
   - Name: `docker-hosted`
   - **HTTP: 9080** (debe coincidir con el puerto publicado en el compose)
   - Deployment policy: **Disable redeploy**, para que un tag publicado no se pueda sobrescribir.
4. **Repositories → maven-releases** (ya existe): verifique *Version policy: Release* y *Deployment policy: Disable redeploy*.

### 4. Publicación manual (validación de credenciales, Fase 2)
PowerShell, desde `codigo_base/backend`:
```powershell
$env:NEXUS_USER="admin"; $env:NEXUS_PASS="<su-clave-nexus>"
mvn -B deploy -DskipTests -s ci-settings.xml -Drevision=1.0.0-manual   # usa http://localhost:9081

docker login localhost:9080 -u admin
docker tag studytrack-api:1.0.0-manual localhost:9080/studytrack-api:1.0.0-manual
docker push localhost:9080/studytrack-api:1.0.0-manual
```
(En bash use `export NEXUS_USER=admin NEXUS_PASS=...`.)

### 5. Configurar Jenkins (http://localhost:9082)
1. Pegue la clave inicial. Elija **Install suggested plugins**: los plugins de `plugins.txt` ya vienen preinstalados. Cree su usuario administrador.
2. **Manage Jenkins → Credentials → System → Global credentials → Add Credentials**:
   - Kind *Username with password*, ID **`nexus-credentials`**, con el usuario y la clave de Nexus.
   - Kind *Username with password*, ID **`github-credentials`**, con su usuario de GitHub y un Personal Access Token (*Contents: Read-only*). Solo es obligatoria si el repo es privado.
3. **New Item →** nombre `studytrack-pipeline`, tipo **Pipeline**:
   - *GitHub project*: `https://github.com/Santiago-VH/taller-jenkins/`
   - *Triggers*: marque **GitHub hook trigger for GITScm polling**
   - *Definition*: **Pipeline script from SCM** → Git
     - Repository URL: `https://github.com/Santiago-VH/taller-jenkins.git`
     - Credentials: `github-credentials` (o *none* si el repo es público)
     - Branch: `*/main`
     - **Script Path: `codigo_base/Jenkinsfile`**
4. Guarde y ejecute **Build Now** una vez. Esa primera ejecución registra el trigger `githubPush()` definido en el Jenkinsfile.

### 6. Webhook en GitHub
En el repo, vaya a **Settings → Webhooks → Add webhook**:
- Payload URL: **la misma URL de smee.io**
- Content type: `application/json`
- Evento: *Just the push event*

Verifique que el relay funciona:
```bash
docker logs -f smee-client    # "Forwarding https://smee.io/... to http://jenkins:8080/github-webhook/"
```

## Fase 4: disparo automático

```bash
git add .
git commit -m "Taller 2: pipeline CI/CD con Jenkins, Nexus y Smee"
git push
```
El push llega a smee.io, el smee-client lo reenvía a `/github-webhook/` y Jenkins inicia el build sin intervención. El build queda con el nombre `#N - 1.0.N-<commit>`.

Verificación final:
- Jenkins: las 4 etapas en verde (*Stage View*) y el reporte de pruebas JUnit.
- Nexus → Browse → `maven-releases` → `com/icesi/studytrack-api/1.0.N-<commit>/`.
- Nexus → Browse → `docker-hosted` → `studytrack-api` y `studytrack-frontend` con el tag `1.0.N-<commit>`.
- http://localhost:3000 muestra la app y http://localhost:8080/api/tasks responde.

## Evidencias para el PDF de resultados
- [ ] Enlace al repositorio (privado, con acceso al docente)
- [ ] Captura del Stage View con las 4 etapas en verde
- [ ] Evidencia del disparo automático: *"Started by GitHub push by …"* en el build, más la entrega en smee.io o en los logs de `smee-client`
- [ ] Capturas de `maven-releases` y `docker-hosted` en Nexus con el artefacto publicado
- [ ] Respuestas al cuestionario, autorreflexión (≥150 palabras) y bitácora de prompts IAG

## Solución de problemas
| Síntoma | Causa / solución |
|---|---|
| `network cicd_network declared as external, but could not be found` | Levante primero el stack de Jenkins o ejecute `docker network create cicd_network`. |
| `jenkins-plugin-cli` falla por versiones de dependencias | Quite los números de versión en `plugins.txt` (deje solo los nombres) y reconstruya con `docker compose build --no-cache`. |
| `permission denied ... docker.sock` | El servicio jenkins debe tener `user: root` (ya está en el compose). |
| `docker login localhost:9080` → 401 o *connection refused* | Active *Docker Bearer Token Realm* y revise que `docker-hosted` tenga el conector HTTP en el puerto 9080. |
| `mvn deploy` → 400 Bad Request | La versión ya existe (Disable redeploy) o se intentó publicar una `-SNAPSHOT` en `maven-releases`. |
| El push no dispara el build | Revise `docker logs smee-client`, la casilla *GitHub hook trigger* y que el job haya corrido al menos una vez. |
| Puerto ocupado | Cámbielo en el `.env`. Si cambia 9080 o 9081, actualice también `NEXUS_REGISTRY` en el Jenkinsfile y `nexus.url` en el `pom.xml`. |
