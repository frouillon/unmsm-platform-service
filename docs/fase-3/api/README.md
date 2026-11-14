# API REST — San Marcos Servicios

Documentación de la API del backend **Spring Boot** de San Marcos Servicios.

| Recurso | Ubicación |
|---|---|
| Swagger UI (interactivo) | `http://localhost:8080/swagger-ui.html` |
| Especificación OpenAPI (JSON) | `http://localhost:8080/v3/api-docs` |
| Especificación OpenAPI (YAML) | `http://localhost:8080/v3/api-docs.yaml` |
| Archivos versionados | [`openapi.yaml`](./openapi.yaml) · [`openapi.json`](./openapi.json) |
| Colección Postman | [`postman-collection.json`](./postman-collection.json) |

- **Versión:** 1.0.0
- **Base URL local:** `http://localhost:8080`
- **Formato:** JSON (`application/json`), excepto el registro de solicitudes (`multipart/form-data`) y la descarga de evidencias (binario).
- **Fechas:** ISO-8601 (`2026-11-14T09:30:00`).
- **Zona horaria:** `America/Lima`.

---

## 1. Autenticación

La API usa **JWT** con esquema `Bearer`. Todos los endpoints, salvo el login, requieren el token.

### Flujo

1. `POST /api/auth/login` con correo institucional y contraseña.
2. La respuesta incluye `token`, `tipo` (`Bearer`), `expiraEn` y los datos del usuario.
3. Enviar el token en cada petición:

```http
Authorization: Bearer <token>
```

4. El token expira a los **480 minutos** por defecto (configurable con `JWT_EXPIRATION_MINUTES`).

### `POST /api/auth/login`

**Request**

```json
{
  "correo": "andrea.mendoza@unmsm.edu.pe",
  "password": "SanMarcos2026"
}
```

**Response `200`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEn": "2026-11-15T06:30:00Z",
  "usuario": {
    "id": 1,
    "nombres": "Andrea Mendoza",
    "correo": "andrea.mendoza@unmsm.edu.pe",
    "rol": "ESTUDIANTE",
    "areaId": null,
    "areaNombre": null
  }
}
```

**Errores**

| Código | Cuándo |
|---|---|
| `400` | Correo sin formato `@unmsm.edu.pe` o contraseña vacía |
| `401` | Credenciales incorrectas |
| `403` | Cuenta inactiva |

### `GET /api/auth/me`

Devuelve el usuario dueño del token.

```json
{
  "id": 6,
  "nombres": "Carlos Quispe",
  "correo": "carlos.quispe@unmsm.edu.pe",
  "rol": "TECNICO",
  "areaId": 3,
  "areaNombre": "Tecnologías de la información"
}
```

---

## 2. Roles y permisos

| Rol | Alcance |
|---|---|
| `ESTUDIANTE` | Registra solicitudes, consulta las propias, cierra las resueltas y descarga sus evidencias. |
| `TECNICO` | Consulta sus solicitudes asignadas, las pasa a **En proceso** y **Resuelto**, y descarga evidencias. |
| `SUPERVISOR` | Consulta las solicitudes de su área, lista técnicos, asigna responsable y revisa. |
| `ADMINISTRADOR` | Consulta todas las solicitudes, asigna responsables y accede a cualquier detalle. |

> El detalle y las evidencias aplican reglas de acceso por propietario, técnico asignado o área, además del rol.

---

## 3. Catálogo de endpoints

| # | Método | Ruta | Rol | Éxito | Descripción |
|---|---|---|---|---|---|
| 1 | `POST` | `/api/auth/login` | Público | `200` | Iniciar sesión y obtener JWT |
| 2 | `GET` | `/api/auth/me` | Autenticado | `200` | Usuario dueño del token |
| 3 | `GET` | `/api/catalogos/categorias` | Autenticado | `200` | Categorías activas |
| 4 | `GET` | `/api/catalogos/prioridades` | Autenticado | `200` | Prioridades y SLA |
| 5 | `POST` | `/api/solicitudes` | `ESTUDIANTE` | `201` | Registrar solicitud con evidencias |
| 6 | `GET` | `/api/solicitudes/mias` | `ESTUDIANTE` | `200` | Solicitudes propias |
| 7 | `GET` | `/api/solicitudes/asignadas` | `TECNICO` | `200` | Solicitudes asignadas |
| 8 | `GET` | `/api/solicitudes/area` | `SUPERVISOR` | `200` | Solicitudes del área |
| 9 | `GET` | `/api/solicitudes/tecnicos` | `SUPERVISOR` | `200` | Técnicos activos del área |
| 10 | `GET` | `/api/solicitudes/todas` | `ADMINISTRADOR` | `200` | Todas las solicitudes |
| 11 | `GET` | `/api/solicitudes/{codigo}/tecnicos` | `SUPERVISOR`, `ADMINISTRADOR` | `200` | Técnicos candidatos para una solicitud |
| 12 | `POST` | `/api/solicitudes/{codigo}/estado` | Según transición | `200` | Avanzar el estado |
| 13 | `GET` | `/api/solicitudes/{codigo}` | Autenticado con acceso | `200` | Detalle, evidencias e historial |
| 14 | `GET` | `/api/solicitudes/{codigo}/evidencias/{evidenciaId}` | Autenticado con acceso | `200` | Descargar evidencia |

Los listados devuelven las solicitudes más recientes primero.

---

## 4. Catálogos

### `GET /api/catalogos/categorias`

```json
[
  {
    "id": 3,
    "nombre": "Soporte informático",
    "area": "Tecnologías de la información",
    "requiereUbicacion": false
  },
  {
    "id": 5,
    "nombre": "Reserva de ambientes",
    "area": "Servicios generales",
    "requiereUbicacion": true
  }
]
```

### `GET /api/catalogos/prioridades`

```json
[
  { "id": 4, "nombre": "Urgente", "slaDias": 1, "diasHabiles": false },
  { "id": 3, "nombre": "Alta", "slaDias": 3, "diasHabiles": true },
  { "id": 2, "nombre": "Media", "slaDias": 7, "diasHabiles": true },
  { "id": 1, "nombre": "Baja", "slaDias": 14, "diasHabiles": true }
]
```

---

## 5. Solicitudes

### `POST /api/solicitudes` — Registrar

Solo `ESTUDIANTE`. Se envía como `multipart/form-data` con dos partes:

| Parte | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `solicitud` | JSON (`application/json`) | Sí | Datos de la solicitud |
| `evidencias` | Archivo (repetible) | No | Hasta 5 archivos PNG, JPG o PDF de 5 MB cada uno |

**Campos de `solicitud`**

| Campo | Tipo | Obligatorio | Regla |
|---|---|---|---|
| `categoriaId` | número | Sí | Categoría activa |
| `prioridadId` | número | Sí | Prioridad existente |
| `asunto` | texto | Sí | Máximo 100 caracteres |
| `descripcion` | texto | Sí | Entre 20 y 2000 caracteres |
| `pabellon` | texto | Depende | Obligatorio si la categoría exige ubicación |
| `nivel` | texto | Depende | Obligatorio si la categoría exige ubicación |
| `ambiente` | texto | Depende | Obligatorio si la categoría exige ubicación |

**Ejemplo con `curl`**

```bash
curl -X POST http://localhost:8080/api/solicitudes \
  -H "Authorization: Bearer $TOKEN" \
  -F 'solicitud={"categoriaId":3,"prioridadId":2,"asunto":"Computadora del laboratorio no enciende","descripcion":"La computadora 12 del laboratorio 204 no enciende desde esta mañana.","pabellon":"Pabellón de Ingeniería de Sistemas","nivel":"2","ambiente":"Laboratorio 204"};type=application/json' \
  -F "evidencias=@foto-equipo.jpg"
```

**Response `201 Created`** (cabecera `Location: /api/solicitudes/SOL-2026-0001`)

```json
{
  "id": 15,
  "codigo": "SOL-2026-0001",
  "asunto": "Computadora del laboratorio no enciende",
  "categoria": "Soporte informático",
  "area": "Tecnologías de la información",
  "prioridad": "Media",
  "estado": "Pendiente",
  "fechaCreacion": "2026-11-14T09:30:00",
  "fechaVencimiento": "2026-11-21T09:30:00",
  "evidencias": 1
}
```

La fecha de vencimiento se calcula con el SLA de la prioridad.

### `GET /api/solicitudes/mias`

Lista las solicitudes del estudiante autenticado.

```json
[
  {
    "id": 15,
    "codigo": "SOL-2026-0001",
    "asunto": "Computadora del laboratorio no enciende",
    "categoria": "Soporte informático",
    "area": "Tecnologías de la información",
    "prioridad": "Media",
    "estado": "Asignado",
    "tecnico": "Carlos Quispe",
    "fechaCreacion": "2026-11-14T09:30:00",
    "fechaVencimiento": "2026-11-21T09:30:00",
    "ultimaActualizacion": "2026-11-14T10:00:00"
  }
]
```

### `GET /api/solicitudes/{codigo}` — Detalle

```json
{
  "id": 15,
  "codigo": "SOL-2026-0001",
  "asunto": "Computadora del laboratorio no enciende",
  "descripcion": "La computadora 12 del laboratorio 204 no enciende desde esta mañana.",
  "categoria": "Soporte informático",
  "area": "Tecnologías de la información",
  "prioridad": "Media",
  "estado": "Asignado",
  "solicitante": "Andrea Mendoza",
  "tecnico": "Carlos Quispe",
  "pabellon": "Pabellón de Ingeniería de Sistemas",
  "nivel": "2",
  "ambiente": "Laboratorio 204",
  "fechaCreacion": "2026-11-14T09:30:00",
  "fechaVencimiento": "2026-11-21T09:30:00",
  "evidencias": [
    { "id": 1, "nombre": "foto-equipo.jpg", "tipoMime": "image/jpeg", "tamanoBytes": 245760 }
  ],
  "historial": [
    {
      "estadoOrigen": "Pendiente",
      "estadoDestino": "En revisión",
      "usuario": "Lucía Ramos",
      "rol": "SUPERVISOR",
      "nota": "Se revisa el caso.",
      "fecha": "2026-11-14T10:00:00"
    }
  ],
  "accion": { "estado": "En proceso", "requiereTecnico": false }
}
```

`accion` indica la siguiente transición disponible para el usuario autenticado; es `null` cuando no le corresponde mover la solicitud.

### `POST /api/solicitudes/{codigo}/estado` — Avanzar estado

**Request**

```json
{
  "estado": "Asignado",
  "tecnicoId": 6,
  "nota": "Se asigna al técnico de turno."
}
```

**Transiciones permitidas**

| Rol | Estado actual | Estado destino | `tecnicoId` |
|---|---|---|---|
| `SUPERVISOR` / `ADMINISTRADOR` | `Pendiente` | `En revisión` | No |
| `SUPERVISOR` / `ADMINISTRADOR` | `En revisión` | `Asignado` | **Sí** |
| `TECNICO` | `Asignado` | `En proceso` | No |
| `TECNICO` | `En proceso` | `Resuelto` | No |
| `ESTUDIANTE` | `Resuelto` | `Cerrado` | No |

- `nota` es opcional (máximo 500 caracteres) y queda en el historial.
- Al asignar, `tecnicoId` debe ser un técnico activo del área de la solicitud.
- La respuesta es el detalle completo actualizado.

### `GET /api/solicitudes/{codigo}/evidencias/{evidenciaId}`

Devuelve el archivo binario con su `Content-Type` real (`image/png`, `image/jpeg` o `application/pdf`), `Content-Disposition: inline` y `Cache-Control: private, no-store`. Aplica las mismas reglas de acceso que el detalle.

---

## 6. Errores

Todos los errores usan este formato:

```json
{
  "timestamp": "2026-11-14T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Hay campos con errores. Revisa el formulario.",
  "path": "/api/solicitudes",
  "fieldErrors": {
    "asunto": "Escribe el asunto de la solicitud."
  }
}
```

| Código | Significado |
|---|---|
| `400` | Datos inválidos, archivo no permitido o formato incorrecto |
| `401` | Token ausente, inválido o expirado; credenciales incorrectas |
| `403` | Sin permisos para la operación o cuenta inactiva |
| `404` | Solicitud, evidencia o recurso inexistente |
| `405` | Método HTTP no permitido |
| `409` | Transición de estado no permitida |
| `413` | Archivo mayor a 5 MB |
| `500` | Error inesperado del servidor |

---

## 7. Flujo de estados

```text
Pendiente ──(supervisor/admin)──> En revisión ──(supervisor/admin + técnico)──> Asignado
                                                                                  │
                                                                        (técnico) │
                                                                                  ▼
                                                                            En proceso
                                                                                  │
                                                                        (técnico) ▼
                                                                             Resuelto
                                                                                  │
                                                                     (estudiante) ▼
                                                                              Cerrado
```

---

## 8. Artefactos de documentación

| Archivo | Uso |
|---|---|
| [`openapi.yaml`](./openapi.yaml) | Contrato OpenAPI 3.1 (fuente para Postman, CI y revisión) |
| [`openapi.json`](./openapi.json) | Mismo contrato en JSON |
| [`postman-collection.json`](./postman-collection.json) | Colección ejecutable con variable `token` automática |

### Regenerar la especificación

Con el backend corriendo:

```bash
curl -s http://localhost:8080/v3/api-docs.yaml -o docs/fase-3/api/openapi.yaml
curl -s http://localhost:8080/v3/api-docs -o docs/fase-3/api/openapi.json
```

### Usar la colección Postman

1. Importar `postman-collection.json`.
2. Ejecutar **Autenticación → Login** con un usuario del seeder.
3. El script de la petición guarda el `token` en las variables de la colección.
4. Ejecutar el resto de peticiones; ya envían `Authorization: Bearer {{token}}`.

### Configuración relacionada

| Variable / propiedad | Valor por defecto | Uso |
|---|---|---|
| `APP_PUBLIC_URL` | `http://localhost:8080` | URL que aparece en la especificación |
| `JWT_EXPIRATION_MINUTES` | `480` | Vigencia del token |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Ruta de Swagger UI |
| `springdoc.api-docs.path` | `/v3/api-docs` | Ruta del contrato OpenAPI |

---

## 9. Evidencias para la entrega

- Captura de Swagger UI con los tags y el botón **Authorize**.
- `openapi.yaml` y `openapi.json` versionados.
- Colección Postman con una corrida exitosa (login + registro + consulta + cambio de estado).
- Este documento en Markdown o exportado a PDF.
- URL pública de Swagger UI cuando la aplicación esté desplegada (`APP_PUBLIC_URL`).
