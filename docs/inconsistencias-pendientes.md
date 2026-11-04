# Inconsistencias pendientes - Rutitas Móvil

**Estado:** `PENDIENTE_RATIFICACION` / decisión=`propuesto`. Este reporte no autoriza implementación ni modifica el estado de las tareas.

| ID | Pendiente | Impacto | Acción y autoridad requerida |
|---|---|---|---|
| MOV-PEND-01 | Las instrucciones docentes no tienen versión, fecha verificable ni identificador de fuente primaria en el registro. | No permite demostrar completamente la procedencia del alcance. | Registrar fuente primaria y validarla con el docente. |
| MOV-PEND-02 | Constitución, AGENTS y responsables siguen sin ratificación ni designación humana. | Bloquea aprobación normativa y ejecución futura. | Resolver MOVIL-D01 y MOVIL-D03. |
| MOV-PEND-03 | Plataformas, restricciones, stack y controles técnicos permanecen abiertos. | Bloquea scaffold, pruebas y decisiones tecnológicas. | Resolver MOVIL-D02, MOVIL-D04, MOVIL-D05 y MOVIL-D07. |
| MOV-PEND-04 | El acuerdo de integración con Backend es propuesto, no vinculante. | No se pueden definir contratos específicos. | Resolver MOVIL-D06 mediante acuerdo explícito. |
| MOV-PEND-05 | La configuración OpenCode está restringida y no fue validada mediante una ejecución de OpenCode. | La compatibilidad declarada debe comprobarse antes de uso operativo. | Revisar configuración cargada en un entorno autorizado, sin habilitar edición ni shell. |

No hay tareas ejecutadas, decisiones aprobadas ni afirmaciones de controles técnicos ejecutados.

## Corrección de auditoría (2026-11-03)

Correcciones documentales aplicadas; no se aprobaron decisiones ni se ejecutaron tareas. Estados: corregidos AUD-01, AUD-04..AUD-11, AUD-14..AUD-16 y AUD-20; documentados/mitigados AUD-12, AUD-13 y AUD-17..AUD-19. AUD-02 y AUD-03 se corrigieron en Backend (auditoría cruzada).

- AUD-01/AUD-20: `websearch` denegado en `opencode.json` y en los cinco agentes.
- AUD-04: actores genéricos y supuestos en `spec.md` §3.1–3.2.
- AUD-05: `Proyecto Rutitas/Móvil/` corregido a `Movil/` en ambas copias de la Constitución.
- AUD-06: `MOV-AB-07` depende de `MOVIL-D06` y del acuerdo de integración.
- AUD-07/AUD-08: README raíz corregido (este paquete).
- AUD-09: índices de subagentes y skills sin la etiqueta “histórico”.
- AUD-10: criterio de matriz corregido para no mencionar “Backend externo”.
- AUD-11: título de este reporte corregido.
- AUD-12: ruta histórica identificada como no incluida en `memoria.md`.
- AUD-13: commit anterior frente a cambios actuales documentado en `memoria.md`.
- AUD-14: prioridad frente a secuencia aclarada en `task.md`.
- AUD-15: `AC-MOV-01..05` y riesgos sin estimación inventada en `spec.md` y `plan.md`.
- AUD-16: este reporte actualizado.
- AUD-17: política `task: deny` documentada en `AGENTS.md`; cambio de política pendiente de aprobación.
- AUD-18: control de copias duplicadas añadido a `CHECKLIST_ENTREGA.md`.
- AUD-19: ZIP original marcado como no incluido/no verificable en `fuentes-consultadas.md` y nota de migración.
- AUD-02/AUD-03: corregidos en el paquete Backend (auditoría cruzada).
