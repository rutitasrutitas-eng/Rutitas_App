> **Procedencia y alcance:** copia de contexto del paquete conjunto original (Proyecto Rutitas/MIGRACION_DESDE_PAQUETE_CONJUNTO.md). No es autoridad normativa ni sustituye documentos SDD de esta raíz. Las referencias a Backend y Móvil describen el paquete original. Para reglas vigentes, consultar Constitution y AGENTS locales. Se conserva el contenido y solo se adaptan enlaces necesarios para evitar dependencias rotas.
>
> **Estado de herramientas:** las definiciones de agentes y skills son para OpenCode; esta copia no acredita instalación, descubrimiento o ejecución en Codex.
# Migración del paquete conjunto a dos proyectos SDD autónomos

## Antes

Una sola Constitución, AGENTS, memoria, cinco subagentes y cinco skills, mientras Backend y Móvil sólo tenían sus propios archivos de arquitectura base.

## Ahora

Se separaron en dos proyectos completos: `Backend/` y `Movil/`. Cada uno tiene autoridad constitucional **propia y todavía candidata**, agentes/skills propios, una memoria exclusiva, documentación de fuentes/decisiones y arquitectura base independiente.

## Continuidad preservada

- Backend conserva **ARCH-BE-01–11** y siete tareas futuras.
- Móvil conserva **ARCH-MOV-01–12** y siete tareas futuras.
- Se conservan los trece principios P01–P13 como base común de procedencia, pero se han adaptado a dos constituciones autónomas.
- El convenio académico actual es `memoria.md` y `task.md` singular en ambos proyectos.

## Importante

El hecho de que algunos principios coincidan no significa que los dos proyectos tengan una Constitución compartida. Sus archivos y eventuales ratificaciones son independientes. Las normas de integración requieren acuerdo humano entre los equipos.

El paquete ZIP original se cita como fuente histórica, pero no está incluido en este paquete ni es verificable aquí. La nueva carpeta es una propuesta derivada, no una sustitución automática de los documentos en el escritorio.
