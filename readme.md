# Backend de Aprobaciones (API)

Servicio REST para gestionar solicitudes de aprobación genérica dentro del Banco de Bogotá. Está construido con Spring Boot 3 (Java 21), protegido con OAuth2/JWT (Azure AD), persiste en PostgreSQL (RDS) y se despliega en AWS ECS Fargate.

## Objetivo

Exponer un backend simple, consistente y seguro para crear, consultar y resolver solicitudes de aprobación con trazabilidad (comentarios, estado, auditoría básica).

## Arquitectura (vista rápida)

-   **Aplicación**: Spring Boot 3.3.x, Tomcat embebido (HTTP :8080), perfiles `postgres` + `aad`.
-   **Seguridad**: Resource Server OAuth2 con Azure AD v2.0.
    -   Validación por `issuer-uri` y `audience` (claim `aud`) específica del API.
-   **Base de datos**: PostgreSQL (AWS RDS). Migraciones con Flyway (idempotentes).
-   **Despliegue**: AWS ECS Fargate (awsvpc), 1–N réplicas, logs en CloudWatch Logs.
-   **Conectividad operativa**: Acceso puntual vía AWS SSM Port Forwarding (bastión) para pruebas internas.
-   **Observabilidad**: `/actuator/health`, logs estructurados (nivel INFO/ERROR).

```
[Cliente/Bots] --OAuth2--> [Azure AD] --JWT-->
[bdb-approvals-api] --JPA--> [PostgreSQL RDS]
      |
      +--> [CloudWatch Logs]

(despliegue en ECS Fargate; acceso operativo por SSM port-forward)
```

## Modelo de dominio (simplificado)

### Request

-   `id` (UUID)
-   `title` (string)
-   `description` (string)
-   `type` (enum: `GENERAL`, …)
-   `status` (enum: `PENDING` | `APPROVED` | `REJECTED`)
-   `requesterUpn` (UPN/ID del solicitante tomado del token)
-   `approverUpn` (UPN del aprobador)
-   `createdAt` / `updatedAt` (Instant)
-   `comments[]` → `{ authorUpn, comment, createdAt }`

### Transiciones

-   `PENDING` → `APPROVED` (endpoint de `approve`)
-   `PENDING` → `REJECTED` (endpoint de `reject`)

## Endpoints principales

-   **Prefijo**: `/api`
-   **Autenticación**: `Bearer JWT` (Azure AD).
-   **Content-Type**: `application/json`.

| Método | Ruta | Descripción | Códigos |
| :--- | :--- | :--- | :--- |
| GET | `/requests/inbox` | Bandeja para el aprobador autenticado | 200 |
| GET | `/requests/outbox` | Solicitudes creadas por el solicitante | 200 |
| GET | `/requests/{id}` | Detalle de una solicitud | 200 / 404 |
| POST | `/requests` | Crear nueva solicitud | 201 |
| POST | `/requests/{id}/approve` | Aprobar (opcional: `{"comment": "..."}`) | 200 / 409 |
| POST | `/requests/{id}/reject` | Rechazar (requiere: `{"comment": "..."}`) | 200 / 409 |
| GET | `/actuator/health` | Health check (sin auth si se habilita así) | 200 |

## Ejemplos

### Crear

```sh
curl -s -X POST http://HOST:8080/api/requests \
-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
-d '{
"title":"Demo",
"description":"prueba",
"approverUpn":"aprobador@empresa.com",
"type":"GENERAL"
}'
```

### Aprobar

```sh
curl -s -X POST http://HOST:8080/api/requests/<id>/approve \
-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
-d '{"comment":"aprobado"}'
```

> **Nota**: para aprobar/rechazar, el token debe pertenecer al aprobador (`approverUpn`). Un token “de aplicación” (client credentials) sirve para flujos técnicos, pero no sustituye la identidad del aprobador humano.

## Seguridad (Azure AD)

La API actúa como **Resource Server**. Se configuraron:

-   `issuer-uri`: `https://login.microsoftonline.com/<TENANT_ID>/v2.0`
-   `audiences`: GUID del API (sin el prefijo `api://`)

Importante: en `application.yml` usamos:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${OAUTH_ISSUER}
          audiences: ${OAUTH_AUDIENCE} # GUID sin prefijo
```

### Obtener un token (client credentials)

```sh
TENANT_ID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
CLIENT_ID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
CLIENT_SECRET='<secreto>'
API_AUDIENCE='api://<GUID-DEL-API>'

curl -s -X POST "https://login.microsoftonline.com/${TENANT_ID}/oauth2/v2.0/token" \
-H "Content-Type: application/x-www-form-urlencoded" \
--data-urlencode "client_id=${CLIENT_ID}" \
--data-urlencode "client_secret=${CLIENT_SECRET}" \
--data-urlencode "scope=${API_AUDIENCE}/.default" \
--data-urlencode "grant_type=client_credentials"
```

> **Pitfall común**: si el backend responde `401 invalid_token` y menciona “The aud claim is not valid”, verifique que la env var `OAUTH_AUDIENCE` sea **SOLO** el GUID del API (sin el prefijo `api://`).

## Configuración & variables

La aplicación lee variables de entorno (ejemplo de `.env`/ECS):

```sh
# Spring Profiles
SPRING_PROFILES_ACTIVE=postgres,aad

# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://<rds-endpoint>:5432/<db>
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<password>

# OAuth2 (Azure AD)
OAUTH_ISSUER=https://login.microsoftonline.com/<TENANT_ID>/v2.0
OAUTH_AUDIENCE=<GUID-DEL-API> # sin api://

# CORS
CORS_ALLOWED_HEADERS=Authorization,Content-Type

# (Opcional) correo saliente
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT=10000
SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT=10000
```

**Persistencia**: Flyway migra el esquema al iniciar. Versionado inicial `V1__*.sql`.

## Ejecución local

**Requisitos**: Java 21, Maven/Gradle, acceso a PostgreSQL y credenciales AAD.

```sh
# 1) Exportar variables (ver sección anterior)
export SPRING_PROFILES_ACTIVE=postgres,aad
# ... exportar el resto de variables

# 2) Build & run (Maven)
./mvnw clean package
java -jar target/*-SNAPSHOT.jar

# o si ya tienes el JAR empacado:
java -jar app.jar
```

-   **Puerto por defecto**: `8080`
-   **Health**: `GET /actuator/health`

## Despliegue en AWS (resumen)

-   **ECS Fargate**
    -   **Cluster**: `bdb-approvals`
    -   **Service**: `bdb-approvals-svc`
    -   **Task Definition**: `bdb-approvals-api-td`
    -   **Network Mode**: `awsvpc` (subnets privadas/públicas según ambiente)
    -   **Logs**: CloudWatch → `/ecs/bdb-approvals-api` (`ecs/<container>/<taskId>`)
-   **RDS PostgreSQL** (seguridad por SG/VPC).
-   **Azure AD** como IdP (issuer & audience via env vars).

### Acceso operativo (túnel SSM)

Para pruebas internas sin exponer puertos:

```sh
# 1) Obtener IP privada del task
TASK_ARN=$(aws ecs list-tasks --cluster bdb-approvals --service-name bdb-approvals-svc \
--desired-status RUNNING --query 'taskArns[-1]' --output text)

TASK_IP=$(aws ecs describe-tasks --cluster bdb-approvals --tasks "$TASK_ARN" \
--query "tasks[0].containers[?name=='bdb-approvals-api'].networkInterfaces[0].privateIpv4Address | [0]" \
--output text)

# 2) Abrir túnel 127.0.0.1:8081 -> TASK_IP:8080 a través del bastión (SSM)
aws ssm start-session \
--target <instance-id-bastion> \
--document-name AWS-StartPortForwardingSessionToRemoteHost \
--parameters "host=${TASK_IP},portNumber=8080,localPortNumber=8081"

# 3) Probar API
curl -i -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8081/api/requests/inbox
```

## Logs & Observabilidad

-   **CloudWatch Logs**: `/ecs/bdb-approvals-api`
    -   **Formato**: `ecs/<containerName>/<taskId>`
-   **Actuator**: `/actuator/health` (viva/ready según configuración).
-   **Métricas/trazas**: se pueden habilitar (Micrometer/OpenTelemetry) si el banco lo requiere.

## Estándares de calidad

-   Java 21, Spring Boot 3.x, Hibernate 6.x.
-   Validación de parámetros y códigos HTTP consistentes.
-   Migraciones versionadas con Flyway (0-downtime friendly).
-   Seguridad “secure by default”: sólo `Authorization` y `Content-Type` en CORS.
-   Contenedor inmutable; configuración por env vars.

## Roadmap (sugerido)

-   [ ] Roles/claims finos (autorización por App Roles/Groups AAD).
-   [ ] Paginación/filtrado en inbox/outbox.
-   [ ] Notificaciones de correo configurables por ambiente.
-   [ ] Observabilidad ampliada (tracing, métricas de negocio).

## Licencia & contacto

-   **Licencia**: según lineamientos internos del Banco de Bogotá.
-   **Contacto técnico**: Equipo de Arquitectura / Plataforma Digital.

## TL;DR

API de aprobaciones lista para producción: segura (AAD), portable (Docker/ECS), con persistencia (RDS/Flyway) y endpoints claros para crear, listar y resolver solicitudes. Se conecta vía JWT Bearer; en AWS se opera con port-forward SSM para pruebas internas.