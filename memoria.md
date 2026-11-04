# Memoria de trabajo — Rutitas Móvil

**Estado:** preparación documental para revisión docente. Constitución y AGENTS propuestos, **no ratificados**.

**Origen:** separación del paquete conjunto `Proyecto Rutitas.zip` entregado para esta tarea.

**Alcance:** proyecto Móvil únicamente.

## Actividad documental registrada

1. Se utilizó como fuente la Constitución conjunta candidata de Rutitas y se creó una Constitución autónoma para Móvil, manteniendo los 13 principios generales y delimitando su ámbito.
2. Se adaptó AGENTS a este proyecto y se separó la memoria factual.
3. Se conservaron los 12 requisitos, plan y 7 tareas futuras de arquitectura base extraídos de `Movil/arquitectura-base/` de la entrega previa; esa ruta histórica no está incluida en este paquete.
4. Se prepararon definiciones **documentales** de cinco subagentes y cinco skills propias.
5. Se crearon fuentes, estados, decisiones, trazabilidad, integración propuesta y matriz de alineación para que este paquete sea autocontenido.
6. Se creó el repositorio Git local y el commit `8b119b9` como respaldo verificable anterior a esta reorganización; los cambios documentales posteriores no están confirmados en Git.
7. Se migraron los cinco subagentes y cinco skills a `.opencode/` con permisos de solo lectura; no se invocaron para ejecutar tareas.
8. Se normalizaron estados, trazabilidad requisito-plan-tarea, referencias locales y el registro de decisiones sin aprobar decisiones humanas.

## Corrección de auditoría (2026-11-03)

- Se corrigieron hallazgos documentales AUD-01..AUD-20 sin aprobar decisiones ni ejecutar tareas.
- Se denegó `websearch` en la configuración y en los cinco agentes, además de `webfetch` y `bash`.
- Se añadieron actores genéricos, supuestos y criterios `AC-MOV-01..05` a la spec; se reforzó la trazabilidad y se evitó inventar probabilidades de riesgo.
- Se precisaron las dependencias de `MOV-AB-07` con `MOVIL-D06` y el acuerdo de integración.
- Antes de editar se creó un respaldo externo; el commit `8b119b9` sigue siendo un snapshot anterior.

## Qué NO se realizó

- No se ejecutó ninguna de las siete tareas del proyecto. Todos los estados son ejecución=`no_iniciado`, verificación=`no_ejecutada`, aprobación pendiente.
- No se implementó software, ni se crearon funcionalidades, endpoints o pantallas específicas.
- No se invocaron subagentes, skills ni herramientas para ejecutar tareas; la configuración OpenCode añadida es documental y de solo lectura.
- No se ejecutaron tests, builds, instalaciones o despliegues para este paquete documental.

## Antecedentes, no resultados de esta entrega

Los informes previos reportaron 83 pruebas de una aplicación experimental RU-03 en Fase 4.1; no pertenecen a esta entrega académica. La candidata móvil React/TypeScript/Vite/Capacitor no acredita una elección oficial de stack. Los ejemplos académicos de Kotlin, .NET y AWS no se consideran decisiones adoptadas.

## Decisiones pendientes y próxima actividad

Ratificar la Constitución propia y AGENTS, confirmar autoridad y repositorio canónico, acordar restricciones del proyecto, revisar integraciones propuestas con el otro equipo y obtener retroalimentación docente. Persisten las inconsistencias en [este reporte](docs/inconsistencias-pendientes.md). Después, esperar la autorización del profesor antes de especificar funcionalidades o ejecutar tareas.

Fuentes detalladas: [documentos consultados](docs/fuentes-consultadas.md). Estado de arquitectura: [spec](specs/000-arquitectura-base/spec.md), [plan](specs/000-arquitectura-base/plan.md), [task](specs/000-arquitectura-base/task.md).

## Preparación para publicación GitHub (2026-11-04)

- Se mantiene este proyecto en su raíz local independiente `Rutitas_Movil`.
- Repositorio oficial asignado: `https://github.com/rutitasrutitas-eng/Rutitas_App`; remoto local `origin` configurado para preparar la publicación.
- Se preparó un `.gitignore` local para archivos privados, temporales, editor, dependencias y artefactos generados; no excluye documentación SDD.
- Se actualizó `README.md` con el objetivo universitario, alcance, arquitectura conceptual, estructura y decisiones pendientes. El prototipo previo no se declara oficial.
- Preparación validada documentalmente; la publicación queda pendiente de commit y confirmación del push. No afirmar publicada hasta verificar HEAD remoto.
- Constitución pendiente de ratificación; 12 requisitos conservados; 7 tareas continúan `no_iniciado` / `no_ejecutada`.
- Los cinco agentes y cinco skills permanecen como definiciones documentales de OpenCode, sin ejecución ni compatibilidad Codex acreditada.
- No se implementó una app Android, no se crearon pantallas/funcionalidades, no se seleccionó stack y no se ejecutaron tareas.
