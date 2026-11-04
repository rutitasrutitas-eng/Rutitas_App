---
name: movil-analizar-arquitectura
description: Analiza arquitectura base movil sin seleccionar tecnologia ni implementar.
---

# Skill: analizar-arquitectura — Rutitas Móvil

**Estado:** skill nativa de OpenCode para análisis documental; no autoriza ejecución.

**Propósito:** Examinar responsabilidades, límites y riesgos sin decidir tecnologías ni funciones concretas.

## Cuándo utilizarla

Para redactar o revisar únicamente la arquitectura base del proyecto Móvil, bajo la [Constitución propia](../../../.specify/memory/constitution.md) y [AGENTS propio](../../../AGENTS.md).

## Cuándo NO usarla

No utilizar para ejecutar tareas, construir software, describir funcionalidades específicas, seleccionar un stack, desplegar, decidir contratos particulares o sustituir aprobación humana.

## Entradas

- Constitución y AGENTS locales.
- [Spec](../../../specs/000-arquitectura-base/spec.md), [plan](../../../specs/000-arquitectura-base/plan.md) y [task](../../../specs/000-arquitectura-base/task.md) según corresponda.
- [Memoria](../../../memoria.md), fuentes identificables y [decisiones pendientes](../../../docs/decisiones-pendientes.md).

## Proceso paso a paso

1. Leer Constitución, AGENTS y restricciones.
2. Distinguir evidencia, opciones y decisiones aprobadas.
3. Identificar capas y fronteras abstractas.
4. Evaluar modularidad, seguridad, errores, calidad y riesgos.
5. Registrar preguntas y criterios de aceptación documental.

## Formato de salida

Texto Markdown con ID, fuente, fecha si se conoce, evidencias, criterio verificable, estado y pendientes. Para una discrepancia: sección, motivo, riesgo y cambio propuesto, sin aprobarlo automáticamente.

## Validaciones y restricciones

Confirmar que el contenido es pertinente a Móvil, no contiene funciones de Rutitas ni código, respeta `task.md` singular y mantiene toda tarea ejecución=`no_iniciado`, verificación=`no_ejecutada`. El rol de agente que use esta guía se subordina a Constitución/AGENTS locales y a revisión humana.

## Ejemplo genérico

Especificar cómo se separa el estado de la presentación, sin definir pantallas de producto.

## Relación con autoridad y subagentes

Esta skill se subordina a la [Constitución local](../../../.specify/memory/constitution.md) y al [AGENTS local](../../../AGENTS.md) del proyecto que la contiene. No crea normas ni aprueba decisiones.

**Subagentes autorizados:** ninguno. La skill no invoca ni autoriza agentes por sí sola; cualquier futura asociación requiere aprobación explícita en el proyecto local.

## Restricciones de ejecución

Uso exclusivamente documental dentro de arquitectura base. No escribir código, ejecutar tareas, comandos, pruebas, builds, instalaciones o despliegues; no definir funciones, endpoints, pantallas, stack o decisiones aprobadas. No acceder a fuentes externas ni elevar permisos. Estos límites son instrucciones de alcance y no prueban controles de runtime.

## Evidencia documental esperada

Una síntesis o matriz móvil de presentación, estado, aplicación, datos, navegación, ciclo de vida y adaptadores, enlazada a ARCH-MOV-xx y decisiones locales. La evidencia debe separar hechos, propuestas, incógnitas y validaciones futuras; no presentar contenido propuesto como ejecución.

## Condiciones para detenerse

Detener la conclusión afectada si falta una fuente autorizada, aprobación humana o decisión necesaria; si existe conflicto entre documentos; o si el pedido excede arquitectura base. Registrar archivo/sección, impacto y pregunta pendiente, sin inferir respuesta ni continuar hacia implementación.

## Criterio de terminación

La salida incluye el artefacto o revisión documental esperado, sus referencias locales, alcance y exclusiones, evidencia por criterio aplicable, decisiones abiertas y estado correcto. Para validación, se documentan enlaces y hallazgos reproducibles. La skill finaliza sin invocar agentes ni ejecutar tareas.
