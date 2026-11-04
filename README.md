# Rutitas — Aplicación Móvil

## Descripción y objetivo

Rutitas es un proyecto universitario de software. Este repositorio prepara la arquitectura base documental de una futura aplicación móvil universitaria; no constituye una app Android implementada ni acredita funcionalidades operativas.

## Alcance y metodología SDD

El alcance actual define responsabilidades arquitectónicas para presentación, estado, aplicación, navegación, ciclo de vida, datos, adaptadores, accesibilidad, seguridad del cliente y validación futura. La especificación incluye **12 requisitos arquitectónicos**; plan y tareas los relacionan con evidencia prevista.

El flujo SDD es: fuente y necesidad autorizadas → especificación y criterios → revisión → plan → tareas → autorización humana → ejecución y verificación futuras. `task.md` contiene **7 tareas** propuestas; todas permanecen `no_iniciado` y `no_ejecutada`.

No se especifican pantallas, recorridos ni funcionalidades de Rutitas. El prototipo experimental React/TypeScript/Vite/Capacitor mencionado en antecedentes no es una implementación oficial ni una decisión de este repositorio. La selección tecnológica definitiva sigue pendiente y debe respetar las decisiones documentadas.

## Arquitectura base y estructura

El plan describe una separación conceptual entre UI, estado, lógica de aplicación, datos, navegación y adaptadores locales/remotos, sin elegir framework, plataforma, mecanismo de almacenamiento o servicios concretos.

```text
Rutitas_Movil/
├── .opencode/
│   ├── agents/                 # cinco perfiles documentales
│   └── skills/                 # cinco guías documentales
├── .specify/memory/constitution.md
├── docs/                       # decisiones, integración y contexto
├── skills/README.md
├── specs/000-arquitectura-base/
│   ├── spec.md
│   ├── plan.md
│   └── task.md
├── subagentes/README.md
├── AGENTS.md
├── constitution.md             # espejo de la canónica
├── memoria.md
├── opencode.json
└── README.md
```

## Gobierno y documentación

- Constitución canónica propuesta: [`.specify/memory/constitution.md`](.specify/memory/constitution.md); `constitution.md` es su espejo exacto. **Estado: PENDIENTE_RATIFICACION**.
- Procedimiento local: [`AGENTS.md`](AGENTS.md). Registro factual: [`memoria.md`](memoria.md).
- Arquitectura: [`spec.md`](specs/000-arquitectura-base/spec.md), [`plan.md`](specs/000-arquitectura-base/plan.md) y [`task.md`](specs/000-arquitectura-base/task.md).
- Cinco perfiles en [`.opencode/agents/`](.opencode/agents/) y cinco skills en [`.opencode/skills/`](.opencode/skills/). Son definiciones documentales para **OpenCode**; su instalación, ejecución o compatibilidad con Codex no está acreditada.

## Estado académico y restricciones

Constitución y AGENTS propuestos, pendientes de ratificación humana. No ejecutar las siete tareas, implementar pantallas o funcionalidades, elegir stack, desplegar ni ratificar normas. Plataformas, restricciones, responsables, datos, seguridad, calidad y distribución continúan como decisiones pendientes.

## Relación futura con Backend

La integración con Backend se describe como propuesta en [`docs/integracion-entre-proyectos.md`](docs/integracion-entre-proyectos.md), no como contrato vinculante. Cuando haya alcance funcional, ambos equipos deberán acordar interfaces y políticas; la UI no es autoridad de autorización del servidor.

## Repositorio

Repositorio oficial asignado: [Rutitas_App](https://github.com/rutitasrutitas-eng/Rutitas_App). La rama `main` está publicada en GitHub; el HEAD remoto se verificó después del push.

## Dependencias tecnológicas futuras del Backend (2026-11-04)

El equipo Backend estudia **ASP.NET Core 10/C# + Google Cloud Run + Firestore NoSQL**, con Google Maps Platform y eventual Firebase Authentication. El profesor exige NoSQL según lo comunicado por el usuario y restringe la nube a Azure/AWS/Google Cloud; las fuentes docentes primarias están pendientes. Estas **preferencias de Backend no eligen tecnología para la aplicación móvil**.

- [Impacto informativo del Backend en la futura integración](docs/propuestas-futuras/impacto-propuesta-backend.md).
- [Integración todavía pendiente de acuerdo](docs/integracion-entre-proyectos.md).

El anexo no integra endpoints en la especificación SDD móvil ni cambia los 12 requisitos o las 7 tareas pendientes.
