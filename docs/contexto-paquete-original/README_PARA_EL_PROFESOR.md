> **Procedencia y alcance:** copia de contexto del paquete conjunto original (Proyecto Rutitas/README_PARA_EL_PROFESOR.md). No es autoridad normativa ni sustituye documentos SDD de esta raíz. Las referencias a Backend y Móvil describen el paquete original. Para reglas vigentes, consultar Constitution y AGENTS locales. Se conserva el contenido y solo se adaptan enlaces necesarios para evitar dependencias rotas.
>
> **Estado de herramientas:** las definiciones de agentes y skills son para OpenCode; esta copia no acredita instalación, descubrimiento o ejecución en Codex.
# Proyecto Rutitas — Entrega SDD separada por proyecto

**Cambio solicitado:** Backend y Móvil ahora cuentan con **documentación SDD independiente**: cada uno tiene su Constitución, AGENTS, memoria, subagentes, skills y `spec.md`, `plan.md`, `task.md` de arquitectura base.

Esta es una **propuesta documental para revisión**, no una aprobación ni un repositorio listo para programar. Las tareas continúan sin ejecutarse, conforme a la indicación del profesor.

## Selecciona un proyecto

- Backend tiene su propia raíz independiente y no está incluida aquí.
- [README local de Móvil](../../README.md)

## Distinción esencial

- **Backend** es el candidato de proyecto SDD para arquitectura de servidor y su organización futura.
- **Móvil** es el candidato de proyecto SDD para arquitectura cliente, navegación, estado y adaptadores futuros.
- **Ninguno hereda normativamente** archivos del otro. Cada Constitución se ratifica por separado, con autoridad humana.
- Los documentos de raíz son este índice, el checklist de entrega y la nota de migración; no hay Constitución, AGENTS o memoria compartidos.
- Los documentos que se repiten entre proyectos (matriz, fuentes, integración) son copias independientes; cualquier cambio debe revisarse en ambos, sin crear autoridad compartida.

## Qué mostrar en clase

1. Abrir Backend y mostrar `constitution.md`, `AGENTS.md`, `memoria.md`, los índices `subagentes/README.md` y `skills/README.md`, y las definiciones activas en `.opencode/agents/` y `.opencode/skills/`.
2. Mostrar Backend `spec.md`, `plan.md`, `task.md` en `specs/000-arquitectura-base/`.
3. Abrir Móvil y repetir mostrando **archivos propios**, no enlaces al Backend.
4. Verificar que los 14 tasks (7+7) están sin iniciar y que ambos documentos constitucionales siguen pendientes de ratificación.

## Alcance académico

El profesor indicó elaborar sólo arquitectura base, **sin funcionalidades específicas del producto y sin ejecutar tareas**. Los prototipos anteriores y sus 83 pruebas históricas no forman parte de esta entrega.

[Checklist de revisión](CHECKLIST_ENTREGA.md) · [Nota de migración](MIGRACION_DESDE_PAQUETE_CONJUNTO.md)
