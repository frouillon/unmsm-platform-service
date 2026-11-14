# Pruebas automatizadas

Este documento describe las pruebas que se agregaron al proyecto: qué verifica cada una, con qué técnica se diseñó y qué requisito del entregable respalda.

Son **60 casos de prueba**: 30 en el backend y 30 en el frontend. Se suman a las pruebas que ya existían (`SolicitudControllerTest`, `AuthControllerTest`, `TransicionSolicitudTest`, `SlaCalculatorTest`, `DataSeederTest`).

## Cómo ejecutarlas

**Backend** (necesita MySQL encendido, igual que la aplicación):

```bash
cd backend
./mvnw test
```

El reporte de cobertura queda en `backend/target/site/jacoco/index.html`.

**Frontend** (necesita Node.js 22.12 o superior):

```bash
cd frontend
npm install
npm test
```

**En GitHub** no hay que hacer nada: el flujo `.github/workflows/ci.yml` ejecuta todo en cada *push* a `main` y en cada *pull request*. Levanta un MySQL temporal para el backend, publica el porcentaje de cobertura en el resumen de la ejecución y deja el reporte completo como artefacto descargable.

## Técnicas de diseño usadas

| Sigla | Técnica | Tipo | En qué consiste |
|---|---|---|---|
| PE | Partición de equivalencia | Caja negra | Se agrupan las entradas en clases que el sistema trata igual (válidas e inválidas) y se prueba un representante de cada clase. |
| AVL | Análisis de valores límite | Caja negra | Se prueban los valores justo en el borde de cada clase y a cada lado de él, que es donde se concentran los errores. |
| TD | Tabla de decisión | Caja negra | Se enumeran las combinaciones de condiciones y el resultado esperado para cada una. |
| TE | Transición de estados | Caja negra | Se verifica qué cambios de estado están permitidos y cuáles no. |
| CB | Cobertura de ramas | Caja blanca | Se lee el código y se escribe una prueba por cada camino de salida del método. |
| CE | Conjetura de errores | Basada en la experiencia | Se prueba un abuso o un descuido concreto que se sabe que suele ocurrir, por ejemplo un ataque conocido. |

### Resumen por técnica

| Técnica | Backend | Frontend | Total |
|---|---:|---:|---:|
| Partición de equivalencia | 6 | 8 | 14 |
| Análisis de valores límite | 3 | 15 | 18 |
| Tabla de decisión | 4 | 5 | 9 |
| Transición de estados | 2 | 1 | 3 |
| Cobertura de ramas | 10 | 0 | 10 |
| Conjetura de errores | 5 | 1 | 6 |
| **Total** | **30** | **30** | **60** |

### Resumen por nivel

| Nivel | Backend | Frontend |
|---|---:|---:|
| Unitarias (lógica pura, sin dependencias) | 17 | 26 |
| Unitarias con dobles de prueba (Mockito) | 10 | — |
| De componente (React Testing Library) | — | 3 |
| De integración | 3 (API + MySQL) | 1 (sesión completa) |

## Backend — 30 casos

Ubicación: `backend/src/test/java/gs/unmsm/platform/`.

### Máquina de estados — `modules/solicitud/service/TransicionSolicitudReglasTest`

RF-19, HU-09 a HU-13.

| N.º | Caso | Técnica |
|---|---|---|
| B-01 | «Cerrado» es el estado final: ningún rol puede mover una solicitud cerrada. | TE |
| B-02 | Un rol que no existe en el sistema no puede avanzar desde ningún estado. | PE |
| B-03 | El único paso que exige elegir técnico es el que lleva a «Asignado». | TE |

### Reglas de autorización — `modules/solicitud/service/SolicitudAccesoTest`

HU-08, RNF-14. Tabla de decisión: rol del usuario × su relación con la solicitud.

| N.º | Caso | Técnica |
|---|---|---|
| B-04 | El estudiante ve su solicitud y no la de otro estudiante. | TD |
| B-05 | El técnico ve la solicitud que tiene asignada; no ve la asignada a otro ni una sin asignar. | TD |
| B-06 | El supervisor ve las solicitudes de su área y no las de otra. | TD |
| B-07 | El administrador ve cualquier solicitud, tenga o no un área. | TD |

### Motor de SLA — `modules/solicitud/service/SlaCalculatorLimitesTest`

RF-27. El límite interesante es el fin de semana, donde cambia el resultado.

| N.º | Caso | Técnica |
|---|---|---|
| B-08 | Tres días hábiles desde un viernes vencen el miércoles. | AVL |
| B-09 | Un día hábil desde un domingo vence el lunes. | AVL |
| B-10 | Los días calendario (prioridad Urgente) sí cuentan sábado y domingo. | AVL |

### Token JWT — `common/security/JwtServiceTest`

RF-02, HU-02.

| N.º | Caso | Técnica |
|---|---|---|
| B-11 | Un token recién emitido devuelve los mismos datos del usuario (con y sin área). | PE |
| B-12 | Un token expirado es rechazado. | PE |
| B-13 | Un token al que se le cambió el rol de ESTUDIANTE a ADMINISTRADOR, conservando la firma, es rechazado. | CE |
| B-14 | Un token firmado con otra clave es rechazado. | CE |
| B-15 | Un texto que no es un token, o un texto vacío, es rechazado. | PE |

### Inicio de sesión — `modules/auth/service/AuthServiceTest`

HU-01, RF-01 a RF-03. Con Mockito se sustituyen el repositorio de usuarios y el emisor de tokens.

| N.º | Caso | Técnica |
|---|---|---|
| B-16 | El correo se normaliza (espacios y mayúsculas) antes de buscar al usuario. | CB |
| B-17 | Un correo inexistente y una contraseña incorrecta dan exactamente el mismo error, y no se emite token. | CB |
| B-18 | Una cuenta inactiva es rechazada aunque la contraseña sea correcta, y no se emite token. | CB |

### Flujo de atención — `modules/solicitud/service/SolicitudFlujoServiceTest`

HU-09, HU-10, HU-17. Con Mockito se sustituyen los cuatro repositorios; cada prueba recorre una salida distinta del método `avanzar`.

| N.º | Caso | Técnica |
|---|---|---|
| B-19 | Una transición fuera del flujo (Pendiente → Asignado) se rechaza y no deja registro en el historial. | CB |
| B-20 | No se puede asignar un técnico de otra área. | CB |
| B-21 | No se puede asignar un técnico inactivo. | CB |
| B-22 | No se puede asignar a un usuario que no es técnico. | CB |
| B-23 | Asignar sin elegir técnico es un error del campo «técnico». | CB |
| B-24 | Si la cuenta de quien actúa fue desactivada, su token ya no le permite avanzar (respuesta de sesión inválida, no un error interno). | CB |
| B-25 | Un avance válido cambia el estado y guarda en el historial: solicitud, estado de origen, estado de destino, usuario, rol, nota y fecha. | CB |

### Evidencias en disco — `modules/solicitud/service/EvidenciaStorageTest`

RF-13, RF-14.

| N.º | Caso | Técnica |
|---|---|---|
| B-26 | *Path traversal*: no se entrega un archivo que existe fuera de la carpeta de evidencias, ni con `../` ni con una ruta absoluta. | CE |
| B-27 | Una evidencia guardada en su carpeta sí se encuentra (caso de control). | PE |

### Seguridad de la API — `modules/solicitud/SeguridadSolicitudesApiTest`

HU-02, HU-08, HU-11. Peticiones HTTP contra la aplicación completa y MySQL. Cada prueba incluye el caso legítimo, para confirmar que el rechazo se debe a la regla y no a otra causa.

| N.º | Caso | Técnica |
|---|---|---|
| B-28 | Un estudiante no puede descargar la evidencia de otro combinando el código de su propia solicitud con el identificador de una evidencia ajena. | CE |
| B-29 | Un técnico no puede iniciar la atención de una solicitud asignada a otro técnico de su misma área; el técnico asignado sí puede. | CE |
| B-30 | Un token vencido no da acceso a la API; el mismo usuario con un token vigente sí entra. | PE |

## Frontend — 30 casos

Ubicación: `frontend/src/`, cada archivo de prueba junto al código que verifica.

### Validación del formulario — `features/solicitudes/validacion.test.ts`

HU-04, RF-11.

| N.º | Caso | Técnica |
|---|---|---|
| F-01 | Un formulario completo no produce errores. | PE |
| F-02 | Falta la categoría. | PE |
| F-03 | Asunto vacío o formado solo por espacios. | PE |
| F-04 | Falta la prioridad. | PE |
| F-05 | Asunto: 100 caracteres pasa, 101 falla. | AVL |
| F-06 | Descripción: 19 caracteres falla, 20 pasa. | AVL |
| F-07 | Descripción: 2000 caracteres pasa, 2001 falla. | AVL |

### Adjuntar evidencias — `features/solicitudes/validacion.test.ts`

HU-05, RF-12.

| N.º | Caso | Técnica |
|---|---|---|
| F-08 | Se aceptan PNG, JPG, JPEG y PDF, sin importar las mayúsculas de la extensión. | PE |
| F-09 | Se rechaza una extensión no permitida. | PE |
| F-10 | Cantidad: el quinto archivo entra y el sexto se rechaza. | AVL |
| F-11 | Tamaño: se rechaza un archivo de 5 MB más un byte. | AVL |
| F-12 | Tamaño: se acepta un archivo de exactamente 5 MB. | AVL |
| F-13 | Tamaño: se rechaza un archivo vacío (0 bytes). | AVL |
| F-14 | Si un archivo de un lote falla, se conservan los anteriores y se descarta el resto. | CE |

### Estado del plazo — `lib/plazo.test.ts`

HU-16, RF-28. Los días se cuentan por fecha calendario, no por bloques de 24 horas.

| N.º | Caso | Técnica |
|---|---|---|
| F-15 | Una solicitud Resuelta o Cerrada figura como «Atendida» aunque su plazo haya vencido. | PE |
| F-16 | 0 días: «Vence hoy», en tono de advertencia. | AVL |
| F-17 | 1 día: «Vence mañana», aunque falten menos de 24 horas. | AVL |
| F-18 | 2 días todavía es advertencia; desde 3 días el tono es neutro. | AVL |
| F-19 | Venció hace unas horas: «Vencida hoy». | AVL |
| F-20 | Venció el día anterior: «Vencida ayer». | AVL |
| F-21 | Venció hace 2 días o más: «Vencida hace N días». | AVL |

### Textos de apoyo — `lib/format.test.ts`, `lib/domain.test.ts`

| N.º | Caso | Técnica |
|---|---|---|
| F-22 | `formatBytes` cambia de unidad en 1 KB y en 1 MB. | AVL |
| F-23 | `plazoTexto` combina singular o plural con días hábiles o calendario. | TD |
| F-24 | `haceTiempo` pasa de «hace un momento» a minutos, horas y días y, tras 30 días, muestra la fecha. | AVL |
| F-25 | `iniciales` toma la primera letra de los dos primeros nombres. | PE |

### Rutas y sesión — `features/auth/`

HU-01, HU-02, RF-05 a RF-07.

| N.º | Caso | Archivo | Técnica |
|---|---|---|---|
| F-26 | Cada rol llega a su propia página de inicio. | `homePath.test.ts` | TD |
| F-27 | Sin sesión, una ruta protegida lleva al inicio de sesión. | `guards.test.tsx` | TD |
| F-28 | Con sesión, solo el rol autorizado entra; otro rol ve «acceso denegado». | `guards.test.tsx` | TD |
| F-29 | Con sesión, abrir el inicio de sesión lleva al inicio del propio rol. | `guards.test.tsx` | TD |
| F-30 | Con un token vencido: se envía el token, el servidor responde 401, se borra el token y la sesión queda marcada como expirada. | `AuthProvider.test.tsx` | TE |

## Fuera del alcance de esta entrega

- Pruebas de extremo a extremo con navegador (Playwright o Cypress) y escenarios en formato BDD, que el entregable menciona en la estrategia de pruebas.
- Prueba de concurrencia del correlativo de códigos `SOL-AAAA-NNNN`.
- Limpieza de archivos en disco cuando se revierte la transacción de registro.
- Módulos sin lógica implementada todavía: notificaciones, comentarios y configuración.
