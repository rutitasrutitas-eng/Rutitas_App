---
name: movil-validar-coherencia-sdd
description: Valida coherencia SDD y enlaces de Movil sin modificar artefactos.
---

# Skill: validar-coherencia-sdd — Rutitas Móvil

**Estado:** skill nativa de OpenCode para revisión documental; no autoriza ejecución.

**Propósito:** Comprobar alineación Constitución/AGENTS/spec/plan/task y enlaces internos.

## Cuándo utilizarla

Para redactar o revisar únicamente la arquitectura base del proyecto Móvil, bajo la [Constitución propia](../../../.specify/memory/constitution.md) y [AGENTS propio](../../../AGENTS.md).

## Cuándo NO usarla

No utilizar para ejecutar tareas, construir software, describir funcionalidades específicas, seleccionar un stack, desplegar, decidir contratos particulares o sustituir aprobación humana.

## Entradas

- Constitución y AGENTS locales.
- [Spec](../../../specs/000-arquitectura-base/spec.md), [plan](../../../specs/000-arquitectura-base/plan.md) y [task](../../../specs/000-arquitectura-base/task.md) según corresponda.
- [Memoria](../../../memoria.md), fuentes identificables y [decisiones pendientes](../../../docs/decisiones-pendientes.md).

## Proceso paso a paso

1. Comprobar los cuatro archivos principales.
2. Verificar enlaces, estados y nombres.
3. Buscar requisitos de producto prematuros.
4. Detectar decisiones técnicas disfrazadas de hechos.
5. Emitir matriz de hallazgos sin autoaprobar.

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

Una matriz Móvil con referencias verificadas, enlaces, estados, requisito-plan-tarea, decisiones abiertas y hallazgos con evidencia; sin cambiar los originales. La evidencia debe separar hechos, propuestas, incógnitas y validaciones futuras; no presentar contenido propuesto como ejecución.

## Condiciones para detenerse

Detener la conclusión afectada si falta una fuente autorizada, aprobación humana o decisión necesaria; si existe conflicto entre documentos; o si el pedido excede arquitectura base. Registrar archivo/sección, impacto y pregunta pendiente, sin inferir respuesta ni continuar hacia implementación.

## Criterio de terminación

La salida incluye el artefacto o revisión documental esperado, sus referencias locales, alcance y exclusiones, evidencia por criterio aplicable, decisiones abiertas y estado correcto. Para validación, se documentan enlaces y hallazgos reproducibles. La skill finaliza sin invocar agentes ni ejecutar tareas.
