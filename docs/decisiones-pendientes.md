# Decisiones pendientes — Rutitas Móvil

Todas estas decisiones siguen **sin aprobación humana**. No impiden preparar la arquitectura base documental; sí pueden bloquear especificación detallada o ejecución futura.

| ID | Pregunta pendiente | Opciones sin seleccionar | Evidencia/autoridad requerida | Recomendación no vinculante | Consecuencia si permanece abierta | Aprobador | Estado |
|---|---|---|---|---|---|---|---|
| MOVIL-D01 | ¿Se ratifican Constitución y AGENTS propios? | Ratificar, ajustar o rechazar | Versión, fecha, aprobadores y acta/registro | No asumir ratificación | Bloquea obligatoriedad normativa | PENDIENTE_DE_DESIGNAR | propuesto |
| MOVIL-D02 | ¿Cuál es el repositorio canónico? | Seleccionar ruta y flujo aprobados | Ruta, propietarios, rama, flujo de revisión | No fijar repositorio por inferencia | Bloquea ejecución futura | PENDIENTE_DE_DESIGNAR | propuesto |
| MOVIL-D03 | ¿Quién cubre arquitectura, seguridad y release? | Designar roles humanos | Asignación humana expresa | No asignar roles a agentes | Bloquea aprobaciones y aceptación | PENDIENTE_DE_DESIGNAR | propuesto |
| MOVIL-D04 | ¿Qué restricciones no funcionales aplican? | Definir capacidad, privacidad y presupuesto | Necesidades y fuente identificable | Esperar alcance confirmado | Limita diseño proporcional | PENDIENTE_DE_DESIGNAR | propuesto |
| MOVIL-D05 | ¿Qué stack y herramientas se aprueban? | Comparar alternativas pertinentes | Comparativa y decisión aprobada | No adoptar ejemplos de clase | Bloquea scaffold y controles técnicos | PENDIENTE_DE_DESIGNAR | propuesto |
| MOVIL-D06 | ¿Qué límites rigen la integración? | Acordar contrato documental | Acuerdo entre equipos | No definir endpoints | Bloquea contratos específicos | PENDIENTE_DE_DESIGNAR | propuesto |
| MOVIL-D07 | ¿Qué controles de seguridad, datos y calidad aplican? | Definir controles proporcionales | Criterios, controles y responsables | No declarar controles implementados | Bloquea validación técnica futura | PENDIENTE_DE_DESIGNAR | propuesto |
| MOVIL-D08 | ¿Se acepta `task.md` y la configuración OpenCode? | Mantener, ajustar o rechazar convención | Revisión docente y convención aprobada | Mantener tareas sin ejecutar y agentes de solo lectura | Bloquea cambio de fase | PENDIENTE_DE_DESIGNAR | propuesto |

Esta entrega **no** decide endpoints, modelos de producto, pantallas, proveedor cloud, base de datos ni operaciones funcionales.

## Registro de propuesta externa (2026-11-04)

Se documentó una **preferencia no aprobada del equipo Backend** por ASP.NET Core 10/C#, Google Cloud Run, Firestore NoSQL, Google Maps Platform y posible Firebase Authentication. El usuario comunicó que el profesor exige **NoSQL** y limita la nube a Azure/AWS/Google Cloud; fuente primaria pendiente. Ninguna de estas preferencias selecciona automáticamente el stack de Móvil. Permanecen abiertos `MOVIL-D05` y `MOVIL-D06`.

[Impacto exploratorio de la propuesta Backend](propuestas-futuras/impacto-propuesta-backend.md).

## Registro de implementación de facto — 2026-11-12 (NO ratificado)

En la rama `feature/integracion-android-ci-cd` se observan Kotlin, Jetpack Compose, Room, Retrofit, Hilt, SDK 36 y workflows GitHub Actions. Son tecnologías **presentes en código**, no decisiones aprobadas por la Constitución. La Constitución sigue `PENDIENTE_RATIFICACION`. Pendientes de aprobación humana: plataforma móvil definitiva, datos y retención, estrategia de backend, migraciones antiguas, método de autenticación de usuarios reales y política Play. Ver `docs/ANDROID_INTEGRATION_AND_PLAY.md`.
