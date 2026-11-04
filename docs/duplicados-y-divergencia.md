# Copias de origen y política de divergencia — Rutitas Móvil

Este registro pertenece solo a Móvil; no establece una autoridad compartida con Backend ni dispara sincronización automática.

## Identidad que sí debe conservarse

- `constitution.md` y `.specify/memory/constitution.md` de Móvil son el único espejo que debe permanecer byte por byte idéntico. Si se cambia la Constitución, actualizar ambos como una operación local única y comprobar SHA-256. Los espejos de Backend son otro par, con contenido y autoridad separados.
- Los IDs y valores de estados comunes de `docs/standards/estados-y-transiciones.md` deberían conservar la misma semántica que su copia de Backend mientras se mantenga el catálogo común. Cualquier divergencia requiere una decisión documentada en ambos ámbitos afectados.

## Copias idénticas por origen, pero administradas localmente

- `opencode.json` existe en Móvil y Backend con contenido idéntico. Mantener permisos equivalentes cuando el alcance sea igual; cada archivo es configuración local OpenCode. Puede evolucionar por necesidades de agentes o permisos propios, con motivo y revisión local. No es configuración de Codex.
- `docs/standards/estados-y-transiciones.md` existe en ambos con el mismo contenido. Preservar la semántica compartida; una especialización local requiere justificación y revisión del impacto cruzado, sin reemplazar la copia del otro proyecto.

## Documentos que deben poder evolucionar distinto

Las Constituciones de Backend y Móvil comparten principios generales de origen, pero deben conservar fronteras y decisiones propias y pueden divergir mediante aprobación local; no se exige identidad entre proyectos. `AGENTS.md`, memorias, spec/plan/task, agentes, skills, decisiones, fuentes, matrices, trazabilidad, inconsistencias e integración son artefactos de cada proyecto. No copiarlos ni sincronizarlos automáticamente. El documento de integración de Móvil es una propuesta local, no autoridad contractual compartida.

## Revisión de cambios

Antes de copiar entre proyectos, clasificar el cambio como principio común, regla local o propuesta de integración; citar origen, impacto y aprobación. Actualizar el inventario de duplicados y recalcular hashes. No sobrescribir cambios propios del otro proyecto.
