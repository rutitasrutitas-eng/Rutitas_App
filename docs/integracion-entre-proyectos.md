# Integración propuesta entre Rutitas Móvil y el proyecto hermano

Este documento es un registro informativo dentro del proyecto Móvil. No es una Constitución compartida, no modifica el otro proyecto y no constituye un contrato API aprobado.

## Límites conceptuales

- Cliente y servicio deben acordar explícitamente entradas/salidas, validaciones, errores, compatibilidad y seguridad cuando existan requisitos funcionales.
- En una integración real, el servidor será responsable de autorizar sus recursos y proteger el estado compartido; la interfaz no puede sustituir esa responsabilidad.
- La comunicación remota debe proteger los datos; HTTPS se considera un principio de diseño cuando aplique.
- Móvil considera desconexión/ciclo de vida; Backend contempla control de errores y observabilidad, cada uno según su propio ámbito.

## Pendiente entre ambos equipos

Autoridad, formato de contratos, versionado, modelo de errores, políticas de identidad, seguridad, datos, compatibilidad y pruebas de contrato. Ninguno se fija todavía. La aprobación de uno de los proyectos no ratifica automáticamente al otro.

**La fase de arquitectura base no incluye operaciones específicas, endpoints, pantallas, datos personales ni selección de proveedores.** Los anexos informativos de propuestas futuras no son contratos aprobados ni autorizan la implementación.

## Anexo informativo de futuro (registrado 2026-11-04)

El equipo Backend considera .NET 10/C#, Google Cloud Run y Firestore NoSQL, con alternativas de identidad y mapas. El [impacto exploratorio para Móvil](propuestas-futuras/impacto-propuesta-backend.md) registra preguntas futuras; **no** incorpora operaciones al contrato actual ni selecciona el stack Móvil. La integración requiere acuerdo humano entre proyectos (`MOVIL-D06` y `BACKEND-D06`).
