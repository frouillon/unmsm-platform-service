# San Marcos Servicios

Aplicación de solicitudes de la UNMSM. El backend es Spring Boot y el frontend es React con Vite.

## Requisitos

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
