# Reporte CI/CD-002 — Rutitas App Móvil

**Fecha:** 2026-11-11. **Estado:** revisión/CI en progreso; no fusionado ni publicado.

## 1. Auditoría inicial

- Repositorio: `Rutitas_App` (checkout local `Rutitas_Movil`); `origin` apunta al repositorio oficial.
- `main` y `origin/main`: `5b287146449ce4b9230db6556155e6f4717943d3`; sincronizados tras `git fetch origin --prune`.
- Rama auditada: `feature/ci-cd-001-bootstrap` en `309dd30b06c8836156c70ec6024f8f770b367985`, sincronizada con `origin`; worktree limpio, sin cambios indexados/no indexados ni archivos no rastreados.
- Ramas remotas observadas: `main` y `feature/ci-cd-001-bootstrap`. PR #1 está abierto hacia `main`, sin fusionar.
- Workflows en rama CI/CD-001: `ci.yml`, `package-release.yml`; no existían en main al inicio de esa fase. No se encontraron `gradlew`, `gradle-wrapper.jar`, settings/build Gradle, módulos, Kotlin/Java ni tests.
- README, AGENTS, Constitución local, `memoria.md`, spec/plan/task de arquitectura base y `docs/decisiones-pendientes.md` revisados. Constitución/espejo SHA-256 `A286B27289D7761C03199ECF114C17F2EB430B39067084462E46863785ADA040`, coinciden y siguen `PENDIENTE_RATIFICACION`. Se conservan 12 requisitos y 7 tareas (`no_iniciado`/`no_ejecutada`).
- No se encontraron archivos sensibles por nombre entre los rastreados (`.env`, llaves, keystores, credenciales); `.gitignore` excluye formatos privados habituales.
- Plataforma, stack, app, package id, signing, Play Console, CI/CD y responsables continúan pendientes. La propuesta tecnológica del Backend no elige tecnología de Móvil. La fuente primaria del profesor no está en el checkout.

## 2. Respaldo, base y archivos protegidos

- Bundle privado fuera del repo: `..\_Rutitas_CI_CD_002_Backups\2026-11-11\Rutitas_App.bundle`; SHA-256 `4A6CD3D340CF1C503F560BFFFAF5725198503279DA4FD2160297C71F68C88184`.
- Manifiesto: `Rutitas_App.manifest.txt`; SHA-256 `D2C40589C5B72E90D6A446F640E24CAC85DDA95EEA39A29A9FD0654991CC6DAB`.
- `git bundle verify` aprobó historial y refs; clon espejo de recuperación confirmó `feature/ci-cd-001-bootstrap` en `309dd30...` y `main` en `5b28714...`. Worktree estaba limpio, no había archivos no rastreados; por eso no se omitieron cambios locales.
- Se preservaron Constitución, AGENTS, README, memoria, archivos base `specs/000-arquitectura-base/*`, historial y PR #1. No se modifica `main`.

## 3. Implementación y archivos

- Base de trabajo: `feature/ci-cd-002-mobile`, creada desde `feature/ci-cd-001-bootstrap` para apilar el PR #2 sobre el PR #1 y evitar duplicar su diff.
- Cambiado: `.github/workflows/ci.yml` (el PR trigger incluye main y la rama padre CI/CD-001; nuevo job Dependency Review fijado a `a1d282b36b6f3519aa1f3fc636f609c47dddb294`, falla ante vulnerabilidades nuevas altas o críticas).
- Añadidos: `specs/002-ci-cd/{spec.md,plan.md,tasks.md,checklist.md}`, `docs/trazabilidad-ci-cd-002.md`, este reporte.
- No se crearon app/pantallas, pruebas ficticias, recursos cloud ni configuración de credenciales. `package-release.yml` permanece manual, exige wrapper y no firma/sube/publica en Play.

## 4. Arquitectura de pipeline y verificación local

El job SDD/YAML/Gitleaks se conserva. Dependency Review solo corre en PR. La detección existente busca Gradle Wrapper; el job Android solo se activa cuando existe wrapper y JAR. Sin proyecto, reporta y omite el build. El empaquetado manual de AAB no se ejecuta.

- Estado inicial CI/CD-001 (no esta rama): [run 43058860869](https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43058860869) pasó parser YAML de ambos workflows, SDD, Gitleaks y detección; Android lint/build/tests quedaron `skipped` al no existir wrapper.
- No se pudieron ejecutar pruebas de app: no hay código ni tests. No se generó AAB ni se publicó en Google Play.
- Verificación CI/CD-002, Gitleaks y Dependency Review: pendiente del PR de esta rama.
- Auditoría de dependencias de la app: NO VERIFICADA; no hay manifests ni paquete real. Dependency Review solo cubrirá cambios de manifiestos en el PR.

## 5. Resultados, PR y estados

- PR CI/CD-001: https://github.com/rutitasrutitas-eng/Rutitas_App/pull/1 (abierto, base `main`, no fusionado).
- PR CI/CD-002: pendiente de abrir hacia `feature/ci-cd-001-bootstrap`.
- SHA final y URL de Actions de CI/CD-002: se completarán tras publicar y validar el PR.
- No se generaron artefactos de app. No hay resultado de tests instrumentados o unitarios.

## 6. Requisitos cubiertos y pendientes

Cubiertos documentalmente: SDD local, escaneo de secretos, validación YAML, detección de proyecto, omisión honesta del build ausente, PR por fases y trazabilidad. Pendientes: proyecto Android, módulos, dependencias, tests unitarios/instrumentados, manifiestos para auditoría, plataforma y stack ratificados, SDKs/variantes, package id/version, signing/keystore seguro, Play Console, environments/aprobadores, dispositivo/emulador y CI/CD de pruebas. Google Play no se publicará con esta autorización.

## 7. Revisión humana y publicación

Revisar y aprobar PR #1 hacia `main` primero; después revisar PR #2 hacia `feature/ci-cd-001-bootstrap`. Tras integrar #1, retargetear #2 a `main` y volver a revisar su diff/checks antes de cualquier merge manual. La autorización actual no incluye merge ni publicación. Para CD real se requieren app real aprobada, firma/credenciales protegidas, environment y autorización de distribución independiente.

Local checks before publication: `git diff --cached --check` passed. A local YAML parser/actionlint is unavailable in this Windows environment, so YAML validation is recorded as NO VERIFICADO locally; the PR workflow will parse both YAML files with its existing Ruby/Psych step. No product tests could run because the repository contains no Android project or test source.

## Hallazgo y corrección del primer run de CI/CD-002

El primer run del PR móvil (43061238655) hizo fallar Dependency Review con el mensaje: `Dependency review is not supported on this repository. Please ensure that Dependency graph is enabled`. No fue un hallazgo de vulnerabilidad: la característica del repositorio está deshabilitada y no existen manifiestos Android. Para que el gate no ejecute análisis sin proyecto, el job se condiciona ahora a `detect-project.present == true`; si no hay wrapper, la revisión queda `skipped` y registrada como NO VERIFICADA. No se cambiaron settings del repositorio. Antes de incorporar dependencias reales, el administrador debe habilitar Dependency Graph en GitHub y el job volverá a ejecutarse.
