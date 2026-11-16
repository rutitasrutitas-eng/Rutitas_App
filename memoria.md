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


## Publicación inicial verificada en GitHub (2026-11-04)

- Se confirmó el push normal de la rama main a https://github.com/rutitasrutitas-eng/Rutitas_App y se verificó que el HEAD remoto coincide con el commit local.
- El repositorio es público; la publicación de la autoría histórica fue autorizada por el usuario.
- Esta constancia registra únicamente la publicación documental. La Constitución sigue pendiente de ratificación, las siete tareas permanecen sin ejecutar y no se realizó implementación ni selección tecnológica.

## Registro exploratorio de dependencia Backend (2026-11-04)

- El usuario reportó la obligación académica de NoSQL y la restricción de nube a Azure, AWS o Google Cloud; no se aportó todavía fuente docente primaria.
- La preferencia por ASP.NET Core 10/C#, Cloud Run y Firestore corresponde al **proyecto Backend**, no al stack de esta app. Maps y Firebase Authentication son alternativas de integración, no decisiones aprobadas.
- Se añadió [anexo informativo de impacto](docs/propuestas-futuras/impacto-propuesta-backend.md) sin alterar especificaciones funcionales, 12 requisitos, 7 tareas ni la Constitución/espejo. No hubo código, despliegue ni ejecución de pruebas de producto; la actualización documental se publicó mediante push normal en main y se verificó el HEAD remoto.

## Publicación de anexos documentales verificada (2026-11-04)

- Se confirmó el push normal de esta actualización documental a la rama main del repositorio propio.
- El HEAD remoto se comprobó coincidente con el commit local; la revisión directa de GitHub confirmó los documentos del paquete.
- No se implementó software ni se ejecutaron tareas; propuestas tecnológicas y el inventario de endpoints y las alternativas tecnológicas permanecen exploratorios.

## Integración CI/CD-001 (2026-11-11)

- Fuente de archivos: `Rutitas_Pipelines_CI_CD_Preparados.zip`; se integró únicamente la carpeta Móvil a la rama `feature/ci-cd-001-bootstrap`, creada desde `main` tras volver a comparar `origin/main`.
- Se añadieron workflows SDD/Gitleaks, detección de Gradle Wrapper, compilación condicional y preparación manual de AAB; junto con `docs/CI_CD.md` y `specs/001-ci-cd/`.
- Se añadió `docs/trazabilidad-ci-cd.md` para relacionar CI-01..04, CD-01 y SEG-01 con controles, evidencia y límites. El empaquetado manual no se ejecutó.
- No existe Gradle Wrapper/proyecto Android ni código de app en este repositorio; no se afirma build/test de app. La ruta Android es condicional y no ratifica plataforma ni stack móvil.
- La Constitución y su espejo no se modificaron y siguen `PENDIENTE_RATIFICACION`. Se conservaron ARCH-MOV-01..12 y las siete tareas de arquitectura base (`no_iniciado` / `no_ejecutada`).
- El escaneo local de patrones de secretos encontró cero coincidencias. El PR #1 está abierto y el run PR 43058284454 pasó validación YAML, SDD y Gitleaks; detección Gradle aprobada y build/lint/tests omitidos por falta de proyecto. No se implementaron pantallas o funcionalidades, ni se publicó en Google Play.

## CI/CD-003 — Auditoría de integración y preparación (2026-11-11)

- Se comprobó en GitHub que los PR #1 y #2 de CI/CD de Rutitas Móvil aparecen **fusionados** en sus respectivas ramas destino; esto no prueba que CI/CD-002 se encuentre en `main` (el PR #2 estaba dirigido a la rama CI/CD-001).
- En el árbol revisado de `main` continúan únicamente el SDD y los workflows; **no se halló código de aplicación ni proyectos compilables**. No se generan proyectos ficticios ni se afirman pruebas de producto.
- Sigue sin Gradle Wrapper ni código Android en main; queda pendiente integrar el proyecto real y comprobar lint, tests y APK.
- La nueva rama de trabajo CI/CD-003 no autoriza automáticamente un merge a `main`, publicación en Google Play ni despliegue a nube. La Constitución permanece `PENDIENTE_RATIFICACION`; se conservan requisitos/tareas de arquitectura base.
- Antes de editar, el worktree estaba limpio. Se respaldaron todas las refs en ../_Rutitas_CI_CD_003_Backups/2026-11-11_ci003/Rutitas_App.bundle (SHA-256 3463E5CB3F103D6412D8C5A85267239D40864252C90E06C9B31FC3394DDE8467) y se verificó el bundle. Las versiones originales de .github/workflows/ci.yml y memoria.md quedaron fuera del repo en ../_Rutitas_CI_CD_003_Backup_local/Rutitas_Movil/ con SHA-256 4D34A3CD72B246F8B79226B0476A15933495DC0B23D3BE7531902F2E468E1717 y 853B8CCB114381EFF5F9965E0449D074155A035757293455DCCC0D45A6E35CEF.

### Publicación y verificación remota

- Rama: feature/ci-cd-003-mobile-integration; commit verificado: d30aa2a51f2449e0927cfda54a7c2975c9a55db9.
- PR #3: https://github.com/rutitasrutitas-eng/Rutitas_App/pull/3, abierto hacia main y sin fusionar.
- Actions PR run: https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43081378215. Validación YAML, SDD y Gitleaks: success; detección Gradle: success; build/lint/tests: skipped por ausencia de Gradle Wrapper/proyecto Android.
- No se afirma que pruebas de producto hayan pasado. Pendientes: integrar proyecto Android real, aprobar SDK/stack, habilitar Dependency Graph y revisar CI con evidencia por SHA. No se cambió main, Constitución, requisitos ni estados de arquitectura base; no hubo publicación a Google Play.

## 2026-11-12 — Preparación de correcciones técnicas Android (PR #4, SIN merge)

- Responsable de integración local: Ame; revisión humana final: pendiente.
- Rama solicitada: `feature/integracion-android-ci-cd`. PR: https://github.com/rutitasrutitas-eng/Rutitas_App/pull/4.
- SHA base revisado: `264d4f3c73682900b41ce7c39174ee93dbd2d451`.
- Evidencia CI **anterior a correcciones**: https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43287112859 (aprobó pipeline inicial; no demuestra pruebas de negocio).
- Entrada de trabajo preparada desde un ZIP proporcionado por la persona responsable. Respaldo privado local reportado y comprobación de presencia de `AuthRepository.kt` y `memoria.md`; no se verificó aquí restaurabilidad integral del respaldo.
- P0-A: se agregó `ownerUserId` a entidades, eliminación acotada por ID en `AuthRepository`, fotos de conductor por cuenta, y limpieza específica; los datos legados quedan con propietario `NULL` y no se atribuyen sin evidencia. Las rutas propias se marcan canceladas preservando reservas ajenas.
- P0-B: reserva transaccional Room, actualización condicional de asientos, validación de cantidad positiva y código basado en UUID; el servidor todavía no aplica estas garantías.
- P0-C: cancelación condicional e idempotente y aceptación transaccional de solicitudes pendientes. Prueba completa de aceptación repetida aún pendiente.
- P1-A/B: modo DEMO conservado, contraseñas SHA-256 locales no aptas para usuarios reales; Room sube a versión 4 con migración explícita desde versión 3. No se configuraron migraciones antiguas (1/2), requiere revisión.
- P1-C: no se tocaron ni ejecutaron workflows de firma o Play; `upload-google-play@v1` aún requiere fijación por SHA y aprobación de administradores.
- P1-D/P2: actualizados README y documentación de SDD/CI; agregados `BookingSafetyTest` y `AccountIsolationTest` instrumentados.
- **Estado de validación:** análisis y modificaciones locales preparados. **NO se ejecutó** compilación Android, lint, suite de pruebas, emulador, Gitleaks ni CI en un nuevo SHA en este entorno. Todo resultado nuevo queda PENDIENTE; no reportar como aprobado.
- **Estado Git:** no se creó commit nuevo, no hay SHA final ni run de GitHub Actions. Este paquete se entrega para aplicar/revisar en la rama existente, no sobrescribe `.git`.
- **Pendientes:** verificar migración y pruebas, validar la restauración funcional de DEMO sin sobrescribir registros existentes, filtros `ownedByMe`, operaciones heredadas y permisos del resto de rutas; remediar autorización backend; validar Play Environment y pin SHA; aprobación humana del equipo.
- Constitución y decisiones tecnológicas: **PENDIENTE_RATIFICACION**. No cambiar estado ni alterar la historia CI/CD-001, 002, 003.
- Merge `main`, Google Play y despliegue productivo: **no realizados** durante esta preparación.

### 2026-11-12 — Validación local de correcciones Android (PR #4)

- Responsable: Ame.
- Rama: feature/integracion-android-ci-cd.
- SHA base: 264d4f3c73682900b41ce7c39174ee93dbd2d451.
- Respaldo: Rutitas_Backup_PreCorrecciones_2026-11-12, almacenado fuera del repositorio; se comprobó la presencia de AuthRepository.kt y memoria.md.
- Compilación assembleDebug: BUILD SUCCESSFUL.
- Análisis lintDebug: BUILD SUCCESSFUL.
- Pruebas testDebugUnitTest: BUILD SUCCESSFUL.
- Pruebas connectedDebugAndroidTest: BUILD SUCCESSFUL; 7 pruebas ejecutadas, 0 fallidas y 0 omitidas, en Medium_Phone_API_37.0.
- Corrección de compilación: se completaron los cierres de las entidades en Entities.kt.
- Migración Room 3 a 4: se agregó una comprobación de existencia de columnas en Migrations.kt tras detectar ownerUserId duplicado. Las pruebas instrumentadas pasaron después de la modificación; aún falta validar explícitamente la preservación de datos en actualizaciones.
- Pruebas agregadas: BookingSafetyTest.kt y AccountIsolationTest.kt.
- Alcance: validación local de los casos cubiertos por las pruebas; no demuestra seguridad completa ni integración con backend.
- Gitleaks y GitHub Actions para un nuevo commit: pendientes.
- Persisten pendientes de revisión: autenticación productiva, migraciones antiguas, operaciones DEMO, permisos de rutas, seguridad del pipeline Play y aprobación humana.
- Constitución SDD: PENDIENTE_RATIFICACION.
- Nuevo commit SHA: pendiente.
- Merge a main, publicación Google Play y despliegue: no realizados.


### 2026-11-12 — Auditoría del entorno android-play-internal

- Evidencia del equipo: https://github.com/rutitasrutitas-eng/Rutitas_App/pull/5
- El líder confirmó que android-play-internal tiene como revisor obligatorio a DxRxXgSo.
- Prevención de autoaprobación: configurada.
- Bypass administrativo: desactivado.
- Ramas autorizadas: únicamente main.
- Estado de la revisión: configuración del entorno reportada como verificada por el equipo.
- Pendientes: configurar android-release y los secretos para una futura distribución.
- No se ejecutó publicación, firma de distribución ni merge a main.
- Esta evidencia no sustituye la revisión técnica ni la aprobación final del PR #4.


### 2026-11-12 — Prueba instrumentada de migración Room

- Prueba: RoomMigrationTest.kt.
- Objetivo: verificar la actualización de Room versión 3 a 4.
- Entorno: emulador Medium_Phone_API_37.0.
- Resultado: BUILD SUCCESSFUL; 1 prueba ejecutada.
- Duración: 1 minuto y 45 segundos.
- Alcance: escenario de migración cubierto por la prueba; no acredita todas las actualizaciones posibles.
- Pendientes: revisar migraciones desde versiones 1 y 2, restauración DEMO y pruebas adicionales.
- No se realizó commit, push, merge ni publicación.


### 2026-11-12 — Validación de inserciones DEMO

- Se agregaron métodos insertDemoIfAbsent() con OnConflictStrategy.IGNORE en los cuatro DAO.
- DemoSeeder utiliza estos métodos para evitar reemplazar registros con identificadores existentes.
- Compilación assembleDebug: BUILD SUCCESSFUL.
- Pruebas connectedDebugAndroidTest: 8/8 aprobadas, 0 fallidas.
- Emulador: Medium_Phone_API_37.0.
- Pendiente: prueba específica de restauración DEMO, aislamiento entre usuarios y revisión de registros faltantes.
- Sin commit, push, merge ni publicación.

### 2026-11-13 — Sprint de endurecimiento local Android APP-01/02/03

- Solicitud humana actual: corregir aislamiento y autorización de repositorios Android, código de abordaje, regresiones y SDD; no modifica el estado `PENDIENTE_RATIFICACION` de la Constitución.
- Base `main` verificada antes de trabajar: `8112b5bacd276bb0b9c3cf326bbf84946d2d5f73`; árbol limpio. PR #5 documental abierto y separado. Rama nueva `fix/android-ownership-hardening`.
- Estado anterior: listas globales de reservas/solicitudes/reseñas, ruta propia guiada por `ownedByMe`, operaciones directas sin comprobación de dueño y código de reserva reutilizable o aceptado en otra ruta.
- Primero se añadió `OwnershipHardeningTest.kt` (commit `bf3156b`). Luego se corrigieron repositorios, DAO, migración Room 4→5 y pruebas de migración (commit `b6802150ae74f3b4a8ba228025c3f24f3e22c739`). La matriz y archivos exactos constan en `docs/REPORTE_OWNERSHIP_HARDENING.md` y `specs/005-android-ownership-hardening/`.
- Resultado local final: `lintDebug`, `testDebugUnitTest` (6/6), `assembleDebug`, `assembleDebugAndroidTest`, `connectedDebugAndroidTest` (19/19 en Rutitas_Audit_API_37), y `scripts/verify_project.py` aprobados. Primera corrida instrumentada: 17/18 por comparación de código UUID con diferente uso de mayúsculas; se corrigió y repitió. Una corrida se interrumpió por un fallo nativo de Java 17; la repetición con `-XX:TieredStopAtLevel=1` aprobó.
- Room v5 conserva datos anteriores mediante migración explícita; datos históricos con propietario NULL no se atribuyen por nombre. DEMO y rutas públicas se mantienen. Las pruebas no acreditan autenticación ni borrado remoto, ni migraciones Room 1/2.
- GitHub Actions, Gitleaks, SHA final y URL del PR: pendientes de verificación tras publicar la rama. Revisión funcional a cargo de Ame y el equipo. Sin merge, distribución Play, cambios de secretos ni entornos.
