# Skill: validar-coherencia-sdd — Rutitas Móvil

**Estado:** guía documental local; no activada ni instalada.

**Propósito:** Comprobar alineación Constitución/AGENTS/spec/plan/task y enlaces internos.

## Cuándo utilizarla

Para redactar o revisar únicamente la arquitectura base del proyecto Móvil, bajo la [Constitución propia](../../.specify/memory/constitution.md) y [AGENTS propio](../../AGENTS.md).

## Cuándo NO usarla

No utilizar para ejecutar tareas, construir software, describir funcionalidades específicas, seleccionar un stack, desplegar, decidir contratos particulares o sustituir aprobación humana.

## Entradas

- Constitución y AGENTS locales.
- [Spec](../../specs/000-arquitectura-base/spec.md), [plan](../../specs/000-arquitectura-base/plan.md) y [task](../../specs/000-arquitectura-base/task.md) según corresponda.
- [Memoria](../../memoria.md), fuentes identificables y [decisiones pendientes](../../docs/decisiones-pendientes.md).

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
