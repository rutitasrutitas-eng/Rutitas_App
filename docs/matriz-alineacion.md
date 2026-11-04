# Matriz de alineación documental — Rutitas Móvil

**Marco de revisión:** P01–P13 de la Constitución candidata del proyecto Móvil; no equivale a ratificación ni a implementación.

| Artefacto | Principios relacionados | Estado | Verificación documental |
|---|---|---|---|
| [AGENTS.md](../AGENTS.md) | P01, P02, P12, P13 | Propuesto | Alineación pendiente de aprobación docente; sin ejecución |
| [memoria.md](../memoria.md) | P02, P03, P04, P06, P09 | Propuesto | Alineación pendiente de aprobación docente; sin ejecución |
| [constitution.md](../.specify/memory/constitution.md) | P02, P03, P04, P06, P09 | Propuesto | Alineación pendiente de aprobación docente; sin ejecución |
| [spec.md](../specs/000-arquitectura-base/spec.md) | P02, P03, P04, P06, P09 | Propuesto | Alineación pendiente de aprobación docente; sin ejecución |
| [plan.md](../specs/000-arquitectura-base/plan.md) | P02, P03, P04, P06, P09 | Propuesto | Alineación pendiente de aprobación docente; sin ejecución |
| [task.md](../specs/000-arquitectura-base/task.md) | P02, P03, P04, P06, P09 | Propuesto | Alineación pendiente de aprobación docente; sin ejecución |
| [OpenCode](../opencode.json) | P02, P06, P12, P13 | Propuesto | Permisos de mínimo privilegio; sin ejecución |
| [Subagentes](../.opencode/agents/movil-revisor-sdd.md) | P01, P02, P12, P13 | Propuesto | Solo lectura; sin ejecución |
| [Skills](../.opencode/skills/movil-validar-coherencia-sdd/SKILL.md) | P01, P02, P12, P13 | Propuesto | Guías locales; sin ejecución |
| [integracion-entre-proyectos.md](../docs/integracion-entre-proyectos.md) | P02, P03, P04, P06, P09 | Propuesto | Alineación pendiente de aprobación docente; sin ejecución |

## Criterios

- Los documentos pertenecen al proyecto Móvil y citan únicamente su Constitución como autoridad local.
- Los requisitos ARCH-MOV-xx están destinados a arquitectura base, sin features del producto.
- `task.md` describe siete tareas futuras sin iniciar; AGENTS prohíbe ejecutarlas en esta fase.
- No se selecciona stack ni se atribuye aprobación a agentes o skills.
- La configuración OpenCode deniega edición, shell, acceso externo y consulta web; no sustituye aprobación humana.
- El documento de integración no obliga automáticamente a Backend.
