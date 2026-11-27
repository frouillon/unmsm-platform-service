# Registro de uso de IA

Registro de las herramientas de IA generativa usadas durante el desarrollo de San Marcos Servicios: para qué se usaron,
qué produjeron y cómo verificó el equipo el resultado. Toda salida de IA se revisa antes de integrarse a `main`; el
responsable de cada entrada responde por el código o documento que se integró.

## Criterios del equipo

- La IA se usa como apoyo para redactar, programar y revisar; las decisiones de diseño y de alcance las toma el equipo.
- No se comparten con la herramienta contraseñas, tokens, el contenido de `.env` ni datos personales reales; solo se usan
  los datos de demostración del `DataSeeder`.
- Ningún cambio generado se integra sin pasar por el CI (pruebas del backend y lint, pruebas y build del frontend) y sin
  revisión en un pull request.
- Lo que la herramienta no pudo verificar por sí misma se deja indicado en la entrada.

## Entradas

| Fecha | Herramienta | Responsable | Tarea | Resultado | Verificación humana |
|---|---|---|---|---|---|
| 2026-11-26 | Claude (Anthropic) | Ronald Munayco | Implementar las alertas de SLA (HU-21): proceso programado cada 15 min, consolidado diario y endpoints `/api/admin/sla/*`. | Módulo `modules.sla`, consultas en repositorios, ajuste de `notificacion.solicitud_id`, pruebas `SlaAlertaServiceTest` y `SlaAlertaControllerTest`. | Revisión del código y ejecución de `./mvnw test` en local y en CI (la herramienta no pudo compilar el backend en su entorno). |
| 2026-11-26 | Claude (Anthropic) | Ronald Munayco | Observabilidad: Actuator, Prometheus, OpenTelemetry, id de correlación y logs estructurados. | `CorrelacionIdFilter`, MDC con usuario, reglas de seguridad para `/actuator/**`, propiedades, `ObservabilidadTest`. | Revisar `/actuator/health`, `/actuator/metrics` con token de administrador y los logs con `X-Correlation-Id`. |
| 2026-11-26 | Claude (Anthropic) | Ronald Munayco | Reportes con filtros temporales y exportación CSV / vista PDF (HU-23, HU-24). | Módulo `modules.reporte`, página `ReportesPage`, estilos de impresión, pruebas de backend y `filtros.test.ts`. | Frontend: `npm run lint`, `npm test` y `npm run build` sin errores. Backend: `./mvnw test`. Revisión manual del CSV en Excel y del PDF impreso. |
| 2026-11-26 | Claude (Anthropic) | Ronald Munayco | Actualizar el Manual de Usuario y redactar el Manual Técnico. | Secciones «Generar y exportar reportes» y «Recibir alertas de vencimiento», capturas nuevas y Manual Técnico. | Lectura completa de ambos manuales por el equipo y contraste de las capturas con el sistema desplegado. |

> Para agregar una entrada, copia una fila y complétala. Si la herramienta propuso algo que el equipo descartó, anótalo
> también en «Resultado»: sirve para la retrospectiva.
