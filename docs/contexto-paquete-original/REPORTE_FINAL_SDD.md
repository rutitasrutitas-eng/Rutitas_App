> **Procedencia y alcance:** copia de contexto del paquete conjunto original (Proyecto Rutitas/docs/REPORTE_FINAL_SDD.md). No es autoridad normativa ni sustituye documentos SDD de esta raíz. Las referencias a Backend y Móvil describen el paquete original. Para reglas vigentes, consultar Constitution y AGENTS locales. Se conserva el contenido y solo se adaptan enlaces necesarios para evitar dependencias rotas.
>
> **Estado de herramientas:** las definiciones de agentes y skills son para OpenCode; esta copia no acredita instalación, descubrimiento o ejecución en Codex.
# Reporte final SDD — Rutitas Backend y Móvil

**Estado:** propuesta documental para revisión académica.
**Alcance:** arquitectura base, sin funcionalidades de producto y sin implementación.
**Autoridad:** Constituciones y AGENTS propios, aún `PENDIENTE_RATIFICACION`.

Este reporte reutiliza la documentación validada del paquete. No modifica specs, planes, tareas, constituciones, configuraciones ni código.

## 1. Resumen ejecutivo

Rutitas se entrega como dos proyectos SDD independientes: Backend y Móvil. Cada uno tiene su propia Constitución candidata, AGENTS, memoria, agentes, skills, decisiones, arquitectura base y tareas futuras. No existe una Constitución, AGENTS o memoria compartidos en la raíz.

| Proyecto | Requisitos arquitectónicos | Criterios de aceptación | Tareas futuras | Estado de tareas |
|---|---:|---:|---:|---|
| Backend | 11 | 5 | 7 | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| Móvil | 12 | 5 | 7 | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |

Las 14 tareas permanecen sin iniciar y sin ejecutar. El paquete está listo para revisión docente, no para implementación ni despliegue.

## 2. Metodología SDD aplicada

El flujo seguido es:

`fuente/necesidad → spec → revisión → plan → tareas → autorización humana → implementación → verificación → aceptación`

En esta entrega el proceso se detiene antes de la autorización y la ejecución:

- `spec.md` define alcance, actores, supuestos, requisitos y criterios documentales.
- `plan.md` organiza responsabilidades, dependencias, trazabilidad, validaciones futuras y riesgos.
- `task.md` desglosa trabajo futuro con dependencias y estados separados.
- Las decisiones abiertas se registran en `docs/decisiones-pendientes.md`.
- Las tareas no se convierten en hechos por estar documentadas.

La arquitectura es neutral respecto a funcionalidades, endpoints, entidades de negocio, pantallas y stack. Los ejemplos tecnológicos y prototipos anteriores son contexto, no decisiones aprobadas.

## 3. Estructura del paquete

### 3.1 Raíz

```text
Proyecto Rutitas/
├── Backend/
├── Movil/
├── docs/
│   └── REPORTE_FINAL_SDD.md
├── README_PARA_EL_PROFESOR.md
├── CHECKLIST_ENTREGA.md
└── MIGRACION_DESDE_PAQUETE_CONJUNTO.md
```

La raíz contiene índices y documentos de presentación. No contiene normas, agentes, skills ni memoria compartidos.

### 3.2 Backend

```text
Backend/
├── .git/
├── .specify/memory/constitution.md
├── .opencode/agents/              # 5 agentes
├── .opencode/skills/              # 5 skills
├── specs/000-arquitectura-base/
│   ├── spec.md
│   ├── plan.md
│   └── task.md
├── docs/
│   ├── decisiones-pendientes.md
│   ├── fuentes-consultadas.md
│   ├── inconsistencias-pendientes.md
│   ├── integracion-entre-proyectos.md
│   ├── matriz-alineacion.md
│   ├── trazabilidad-constitucion.md
│   └── standards/estados-y-transiciones.md
├── subagentes/README.md
├── skills/README.md
├── constitution.md                # copia de presentación
├── AGENTS.md
├── memoria.md
├── README.md
└── opencode.json
```

### 3.3 Móvil

```text
Movil/
├── .git/
├── .specify/memory/constitution.md
├── .opencode/agents/              # 5 agentes
├── .opencode/skills/              # 5 skills
├── specs/000-arquitectura-base/
│   ├── spec.md
│   ├── plan.md
│   └── task.md
├── docs/
│   ├── decisiones-pendientes.md
│   ├── fuentes-consultadas.md
│   ├── inconsistencias-pendientes.md
│   ├── integracion-entre-proyectos.md
│   ├── matriz-alineacion.md
│   ├── trazabilidad-constitucion.md
│   └── standards/estados-y-transiciones.md
├── subagentes/README.md
├── skills/README.md
├── constitution.md                # copia de presentación
├── AGENTS.md
├── memoria.md
├── README.md
└── opencode.json
```

## 4. Arquitecturas independientes

### 4.1 Backend

La arquitectura base de Backend propone:

- Adaptadores de entrada que traducen el transporte sin alojar reglas de negocio.
- Validación en límites de confianza.
- Capa de aplicación que coordina operaciones abstractas.
- Dominio futuro, sin entidades de producto en esta fase.
- Puertos y adaptadores para persistencia y sistemas externos.
- Configuración por entorno, gestión de secretos y políticas de autorización en el servidor.
- Observabilidad con correlación, sanitización y retención por definir.
- Estrategia futura de pruebas unitarias, integración, contrato y seguridad.

La seguridad se documenta como principio: mínimo privilegio, límites de confianza, autorización del servidor por recurso y operación cuando existan capacidades conectadas, y secretos fuera del repositorio. La autorización no está implementada.

### 4.2 Móvil

La arquitectura base de Móvil propone:

- Presentación con controles semánticos y sin acceso directo a almacenamiento o transporte.
- Separación entre estado de vista, estado de operación y estado persistido.
- Aplicación que coordina acciones abstractas.
- Dominio futuro, sin reglas de producto en esta fase.
- Datos mediante interfaces y adaptadores locales/remotos.
- Navegación independiente del mapa de pantallas.
- Adaptadores nativos para permisos, almacenamiento seguro y capacidades del dispositivo.
- Configuración y seguridad de cliente, sin secretos de servidor ni autoridad de permisos.

El cliente no se declara autoridad de autorización del servidor. Los riesgos arquitectónicos y sus controles se documentan en `Movil/specs/000-arquitectura-base/plan.md`, sección “Riesgos y pendientes”; la spec solo los referencia.

### 4.3 Independencia e integración

- Backend y Móvil tienen repositorios, Constituciones, AGENTS, memorias, agentes, skills y artefactos SDD propios.
- Ningún documento del proyecto hermano sustituye la autoridad local.
- Los documentos repetidos son copias independientes, no una autoridad compartida.
- La integración futura se describe en `docs/integracion-entre-proyectos.md` como propuesta.
- `BE-AB-07` y `MOV-AB-07` incorporan `BACKEND-D06` y `MOVIL-D06`, respectivamente; el acuerdo no está aprobado.

## 5. Constituciones y AGENTS

Cada proyecto tiene:

- Una Constitución canónica candidata en `.specify/memory/constitution.md`.
- Una copia de presentación en `constitution.md`, verificada como idéntica.
- Un `AGENTS.md` local que desarrolla el orden SDD, fuentes, límites, reglas de redacción, revisión, riesgos, decisiones y restricciones de agentes.

Alineación observada:

- Ambas Constituciones permanecen `PENDIENTE_RATIFICACION`.
- Los AGENTS exigen leer la Constitución y los artefactos locales antes de proponer cambios.
- Las specs, planes y tareas se refieren a fuentes y decisiones locales.
- La fase vigente es exclusivamente documental; ejecutar tareas está prohibido.
- No se modificaron estados constitucionales, decisiones ni autorizaciones.

## 6. Subagentes y skills existentes

### 6.1 Subagentes

| Proyecto | Subagente | Enfoque |
|---|---|---|
| Backend | `backend-arquitecto` | Estructura, capas conceptuales, modularidad, configuración, persistencia abstracta y observabilidad. |
| Backend | `backend-revisor-contratos` | Límites entre capas, interfaces, errores y compatibilidad futura. |
| Backend | `backend-revisor-sdd` | Coherencia Constitución, AGENTS, spec, plan y task. |
| Backend | `backend-revisor-seguridad` | Datos, secretos, autorización del servidor y riesgos de infraestructura. |
| Backend | `backend-revisor-calidad` | Requisitos verificables, mantenibilidad, logging y pruebas futuras. |
| Móvil | `movil-arquitecto` | UI, estado, navegación, ciclo de vida y adaptadores. |
| Móvil | `movil-revisor-accesibilidad` | Semántica, foco, contraste, texto escalable y tecnologías de asistencia. |
| Móvil | `movil-revisor-sdd` | Coherencia Constitución, AGENTS, spec, plan y task. |
| Móvil | `movil-revisor-seguridad` | Permisos del dispositivo, almacenamiento local y secretos de cliente. |
| Móvil | `movil-revisor-integracion` | Adaptadores remotos, conectividad, compatibilidad y separación cliente-servidor. |

### 6.2 Skills

| Proyecto | Skills |
|---|---|
| Backend | `backend-analizar-arquitectura`, `backend-crear-especificacion`, `backend-crear-plan`, `backend-desglosar-tareas`, `backend-validar-coherencia-sdd` |
| Móvil | `movil-analizar-arquitectura`, `movil-crear-especificacion`, `movil-crear-plan`, `movil-desglosar-tareas`, `movil-validar-coherencia-sdd` |

Las definiciones activas están en `.opencode/agents/` y `.opencode/skills/`; los índices de migración están en `subagentes/README.md` y `skills/README.md`. Los agentes son de solo lectura, no editan, no ejecutan shell, no acceden a fuentes externas y no aprueban decisiones. La configuración deniega `webfetch` y `websearch`; la invocación automática de subagentes está deshabilitada con `task: deny`.

## 7. Especificaciones, planes y tareas

| Proyecto | Spec | Plan | Task | Requisitos | Criterios AC | Tareas |
|---|---|---|---|---|---:|---:|
| Backend | `Backend/specs/000-arquitectura-base/spec.md` | `Backend/specs/000-arquitectura-base/plan.md` | `Backend/specs/000-arquitectura-base/task.md` | 11 | 5 | 7 |
| Móvil | `Movil/specs/000-arquitectura-base/spec.md` | `Movil/specs/000-arquitectura-base/plan.md` | `Movil/specs/000-arquitectura-base/task.md` | 12 | 5 | 7 |

### 7.1 Tareas Backend

| ID | Propósito | Dependencias | Estado |
|---|---|---|---|
| `BE-AB-01` | Confirmar alcance, fuentes, restricciones y repositorio. | — | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `BE-AB-02` | Evaluar y registrar stack y topología proporcional. | `BE-AB-01` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `BE-AB-03` | Definir límites de responsabilidades y dependencias. | `BE-AB-02` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `BE-AB-04` | Preparar configuración, validación, errores, logging, secretos y políticas de autorización. | `BE-AB-03` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `BE-AB-05` | Definir puertos de persistencia y límites de adaptadores. | `BE-AB-03` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `BE-AB-06` | Establecer tipos de pruebas y controles de calidad. | `BE-AB-03`, `BE-AB-04`, `BE-AB-05` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `BE-AB-07` | Revisar arquitectura, aceptación, autorización e integración propuesta. | `BE-AB-06`, `BACKEND-D06` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |

### 7.2 Tareas Móvil

| ID | Propósito | Dependencias | Estado |
|---|---|---|---|
| `MOV-AB-01` | Confirmar plataformas, versiones, dispositivos y restricciones. | — | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `MOV-AB-02` | Evaluar y registrar stack y herramientas móviles. | `MOV-AB-01` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `MOV-AB-03` | Definir límites de presentación, estado, aplicación y datos. | `MOV-AB-02` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `MOV-AB-04` | Definir navegación y ciclo de vida sin pantallas funcionales. | `MOV-AB-03` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `MOV-AB-05` | Definir configuración, accesibilidad, errores, seguridad y almacenamiento. | `MOV-AB-03`, `MOV-AB-04` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `MOV-AB-06` | Definir adaptadores de datos/servicios y estrategia de pruebas. | `MOV-AB-03`, `MOV-AB-05` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| `MOV-AB-07` | Revisar coherencia móvil/backend y aceptación e integración propuesta. | `MOV-AB-06`, `MOVIL-D06` | `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |

## 8. Trazabilidad requisito → tarea

### 8.1 Backend

| Requisito | Tarea(s) futura(s) |
|---|---|
| `ARCH-BE-01` Separación de responsabilidades | `BE-AB-03` |
| `ARCH-BE-02` Modularidad proporcional | `BE-AB-03` |
| `ARCH-BE-03` Organización mantenible | `BE-AB-03` |
| `ARCH-BE-04` Configuración por entorno | `BE-AB-04` |
| `ARCH-BE-05` Contratos entre capas | `BE-AB-03`, `BE-AB-05` |
| `ARCH-BE-06` Errores y validación | `BE-AB-04` |
| `ARCH-BE-07` Seguridad por diseño | `BE-AB-04`, `BE-AB-07` |
| `ARCH-BE-08` Persistencia como abstracción | `BE-AB-05` |
| `ARCH-BE-09` Observabilidad y logging | `BE-AB-04` |
| `ARCH-BE-10` Pruebas previstas | `BE-AB-06` |
| `ARCH-BE-11` Mantenibilidad | `BE-AB-07` |

`BE-AB-01` y `BE-AB-02` son tareas habilitadoras: no implementan requisitos funcionales, pero habilitan la evaluación de todos los `ARCH-BE`.

### 8.2 Móvil

| Requisito | Tarea(s) futura(s) |
|---|---|
| `ARCH-MOV-01` Separación UI/lógica/datos | `MOV-AB-03` |
| `ARCH-MOV-02` Gestión de estado | `MOV-AB-03` |
| `ARCH-MOV-03` Navegación | `MOV-AB-04` |
| `ARCH-MOV-04` Ciclo de vida | `MOV-AB-04` |
| `ARCH-MOV-05` Errores | `MOV-AB-05` |
| `ARCH-MOV-06` Configuración | `MOV-AB-05` |
| `ARCH-MOV-07` Accesibilidad | `MOV-AB-05` |
| `ARCH-MOV-08` Seguridad del cliente | `MOV-AB-05` |
| `ARCH-MOV-09` Almacenamiento local | `MOV-AB-05` |
| `ARCH-MOV-10` Comunicación remota | `MOV-AB-06` |
| `ARCH-MOV-11` Compatibilidad | `MOV-AB-01`, `MOV-AB-02` |
| `ARCH-MOV-12` Pruebas y mantenibilidad | `MOV-AB-06`, `MOV-AB-07` |

### 8.3 Cobertura de aceptación

- Backend: `AC-BE-01` a `AC-BE-05`; `AC-BE-04` incluye `BE-AB-01`, `BE-AB-02` y `BE-AB-07`.
- Móvil: `AC-MOV-01` a `AC-MOV-05`; `AC-MOV-04` incluye `MOV-AB-05` y `MOV-AB-07`.
- `ARCH-BE-07` se verifica documentalmente con la política de autorización de `BE-AB-04` y la revisión de `BE-AB-07`; no se implementa.
- `ARCH-MOV-12` y los riesgos móviles se cubren con el plan de pruebas y la revisión de `MOV-AB-06`/`MOV-AB-07`.

## 9. Validación y evidencia

La validación fue documental y estructural, no técnica.

| Verificación | Resultado |
|---|---|
| Validación global del paquete | `VALIDATION_OK` |
| JSON de configuración | Válido en Backend y Móvil |
| Frontmatter de agentes | 10/10 válidos (5 por proyecto) |
| Frontmatter de skills | 10/10 válidos (5 por proyecto) |
| Enlaces Markdown | Sin enlaces rotos |
| Requisitos | Backend 11/11 y Móvil 12/12 trazados |
| Tareas | 7 Backend + 7 Móvil; IDs preservados |
| Estados | 14/14 en `propuesto` / `no_iniciado` / `no_ejecutada` / `—` |
| Dependencias | Referencias de tareas y decisiones válidas |
| Constituciones | Copia canónica y copia de presentación idénticas por proyecto (SHA-256) |
| Autorización Backend | `ARCH-BE-07` → plan → `BE-AB-04` / `BE-AB-07` |
| Cobertura de aceptación | `AC-BE-04` y `AC-MOV-04` actualizados |
| Fecha académica | `29-10-2026` solo figura como no verificada en `BE-PEND-01` |
| Riesgos Móvil | Enlace a `plan.md#riesgos-y-pendientes` verificado |

Evidencia principal: `spec.md`, `plan.md`, `task.md`, `docs/decisiones-pendientes.md`, `docs/inconsistencias-pendientes.md`, `docs/fuentes-consultadas.md`, `docs/matriz-alineacion.md`, `docs/integracion-entre-proyectos.md` y `CHECKLIST_ENTREGA.md` de cada proyecto.

Limitaciones de la evidencia:

- No se ejecutó OpenCode en runtime.
- No se ejecutaron pruebas, builds ni código de producto.
- No se validó un backend o cliente funcional.
- No se aprobaron stack, integración, seguridad operativa ni decisiones pendientes.

## 10. Documentación validada frente a software no implementado

| Documentado y validado | No implementado |
|---|---|
| Arquitectura base Backend y Móvil | Código Backend, frontend o aplicación móvil |
| 11 + 12 requisitos arquitectónicos | Endpoints, entidades, pantallas o journeys |
| Planes de responsabilidades y dependencias | Scaffolding, servicios, componentes o migraciones |
| 7 + 7 tareas futuras | Ejecución de cualquiera de las 14 tareas |
| Políticas de autorización y seguridad | Controles de autorización activos |
| Estrategia de pruebas futura | Pruebas ejecutadas o resultados técnicos |
| Decisiones y riesgos registrados | Decisiones aprobadas o aceptación humana |
| Integración propuesta | Contrato vinculante o integración operativa |

Las tareas documentadas no son evidencia de implementación. Los estados `no_iniciado` y `no_ejecutada` son parte del resultado esperado de esta fase.

## 11. Aprobaciones humanas pendientes

- Ratificación de la Constitución y AGENTS de Backend.
- Ratificación de la Constitución y AGENTS de Móvil.
- Designación de autoridades humanas de producto, revisión técnica, seguridad, privacidad y operación.
- Resolución de las 8 decisiones Backend y las 8 decisiones Móvil.
- Prioridad inmediata: `D01` ratificación, `D05` stack, `D06` integración y `D08` aceptación de `task.md`/configuración OpenCode.
- `BE-AB-07` y `MOV-AB-07` no pueden completarse mientras `D06` siga abierta.
- `BE-PEND-01` y `MOV-PEND-01` mantienen pendiente la fuente primaria y la fecha verificable de las instrucciones académicas.

No se aprobó ninguna decisión, no se autorizó ninguna tarea y no se declaró aceptación humana.

## 12. Conclusión académica

El paquete presenta una arquitectura base SDD coherente, independiente y trazable para Backend y Móvil. Los 23 requisitos están documentados y mapeados a 14 tareas futuras; las Constituciones, AGENTS, agentes, skills y artefactos locales se mantienen separados; y la validación estructural no encontró errores de conteo, enlaces, dependencias o estados.

La entrega es adecuada para revisión académica como propuesta documental. No es software implementado ni una arquitectura aprobada. El siguiente paso requiere revisión y ratificación humana, resolución de decisiones abiertas —especialmente `D06`— y autorización explícita antes de cualquier ejecución.
