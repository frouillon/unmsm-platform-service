# San Marcos Servicios

Aplicación de solicitudes de la UNMSM. El backend es Spring Boot y el frontend es React con Vite.

## Requisitos

### Con Docker (recomendado para demo)

- Docker Desktop (o Docker Engine + Compose v2)

```bash
cp .env.example .env
docker compose up --build
```

Queda en http://localhost (frontend). La API también responde en http://localhost:8080.
Swagger: http://localhost/swagger-ui/index.html (vía proxy) o http://localhost:8080/swagger-ui/index.html.

Para detener:

```bash
docker compose down
```

### Sin Docker (desarrollo local)

- Java 17
- Node.js 20 o superior
- MySQL 8 en `localhost:3306`

No hace falta instalar Maven: el backend trae su wrapper.

## Base de datos

El backend crea la base `bd_servicios_unmsm` si no existe. Por defecto entra como `root` con contraseña `root`.

Si tu MySQL usa otra contraseña, crea `backend/.env`:

```properties
DB_PASSWORD=tu_contraseña
```

## Ambientes y secretos

- **Local / Docker de desarrollo:** defaults en `application.properties` (cómodos para el equipo).
- **Cloud / demo pública:** activa el perfil `prod` con `SPRING_PROFILES_ACTIVE=prod`.
  Ahí **obligas** `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS` y `APP_PUBLIC_URL`
  (sin valores por defecto inseguros). El seed de usuarios demo queda apagado salvo que lo actives.

Plantilla de variables: [`.env.example`](.env.example). El archivo `.env` no se sube al repo.

## Documentación de la API

Con el backend en marcha:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Contrato OpenAPI (JSON): http://localhost:8080/v3/api-docs
- Contrato OpenAPI (YAML): http://localhost:8080/v3/api-docs.yaml
- Documento, especificación exportada y colección Postman: [docs/fase-3/api/README.md](docs/fase-3/api/README.md)

## Backend

En una terminal, desde la carpeta del proyecto:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

En macOS o Linux:

```bash
cd backend
./mvnw spring-boot:run
```

Queda en http://localhost:8080. La primera vez descarga dependencias y carga los catálogos y usuarios de prueba.

## Frontend

En otra terminal:

```powershell
cd frontend
npm install
npm run dev
```

Queda en http://localhost:5173. En desarrollo las llamadas a `/api` se reenvían al backend, así que levanta el backend antes.

## Pruebas

```bash
cd backend
./mvnw test        # necesita MySQL encendido; cobertura en target/site/jacoco/index.html

cd frontend
npm test           # necesita Node.js 22.12 o superior
```

En GitHub se ejecutan solas en cada push a `main` y en cada pull request (`.github/workflows/ci.yml`). El detalle de los casos y de las técnicas usadas está en [PRUEBAS.md](PRUEBAS.md).
