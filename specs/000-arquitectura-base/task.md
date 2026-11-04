# Tareas futuras — Arquitectura base Móvil

> Todas son propuestas futuras. **No ejecutar en esta entrega.** Se usa `task.md` singular según la instrucción del profesor.

| ID | Descripción | Dependencias | Prioridad | Entregable esperado | Validación futura (no ejecutada) | Decisión | Ejecución | Verificación | Tipo |
|---|---|---|---|---|---|---|---|---|---|
| MOV-AB-01 | Confirmar plataformas, versiones, dispositivos objetivo y restricciones | — | P0 | Matriz de plataforma/requisitos | Revisión por responsables y fuente identificable | propuesto | no_iniciado | no_ejecutada | — |
| MOV-AB-02 | Evaluar y registrar stack móvil y herramientas con criterios de equipo/soporte | MOV-AB-01 | P0 | ADR comparativo | Evidencia de restricciones, capacidades, mantenimiento y costo | propuesto | no_iniciado | no_ejecutada | — |
| MOV-AB-03 | Definir límites de presentación, estado, aplicación y datos | MOV-AB-02 | P1 | Diagrama y convención de dependencias | Revisión de separación y ausencia de ciclos | propuesto | no_iniciado | no_ejecutada | — |
| MOV-AB-04 | Definir navegación y tratamiento general del ciclo de vida, sin pantallas funcionales | MOV-AB-03 | P1 | Convenciones arquitectónicas documentadas | Revisión de restauración, retorno y permisos aplicables | propuesto | no_iniciado | no_ejecutada | — |
| MOV-AB-05 | Definir configuración, accesibilidad, errores, seguridad y almacenamiento como políticas base | MOV-AB-03, MOV-AB-04 | P1 | Decisiones/guías transversales | Checklist futuro por criterios y plataformas aprobadas | propuesto | no_iniciado | no_ejecutada | — |
| MOV-AB-06 | Definir adaptadores de datos/servicios y estrategia de pruebas sin contratos funcionales | MOV-AB-03, MOV-AB-05 | P1 | Interfaces propuestas y plan de pruebas | Revisión de independencia, compatibilidad y cobertura de riesgo | propuesto | no_iniciado | no_ejecutada | — |
| MOV-AB-07 | Revisar coherencia móvil/backend, aceptación de arquitectura base y acuerdo de integración propuesto | MOV-AB-06, MOVIL-D06 | P0 | Registro de revisión, versión y acuerdo de integración | Revisión humana, matriz de decisiones cerradas/abiertas y acuerdo de integración revisado | propuesto | no_iniciado | no_ejecutada | — |

**Estado del paquete:** decisión=`propuesto`, ejecución=`no_iniciado`, verificación=`no_ejecutada`, tipo=`—`. Las siete tareas continúan sin ejecutar; no se construyeron componentes ni se ejecutó comprobación técnica. La prioridad expresa criticidad y la secuencia la determinan las dependencias. Las dependencias con ID `MOVIL-Dxx` no están aprobadas y bloquean la tarea hasta su resolución.
