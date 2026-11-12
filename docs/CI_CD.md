# CI/CD — Rutitas Móvil (preparación)

**Estado**: la rama `main` revisada el 11 de noviembre de 2026 es documental; no contiene una app Android. Esta configuración no ratifica una tecnología ni asegura un build real.

- `.github/workflows/ci.yml`: comprueba documentos SDD y ejecuta Gitleaks. Detecta Gradle Wrapper y solo entonces ejecuta `lintDebug`, `testDebugUnitTest`, `assembleDebug`. El trabajo omitido significa **Android NO verificado**, incluso si los controles documentales son verdes.
- `.github/workflows/package-release.yml`: prepara manualmente un AAB; exige un proyecto real, y **no firma, sube ni publica** en Google Play. Configurar `android-release` con revisores antes de usarlo.

**Para activar CI móvil de verdad**: incorporar `gradlew`, su JAR de wrapper, `settings.gradle(.kts)`, módulos, dependencias y pruebas; ajustar tareas para variantes/flavors reales. Posteriormente agregar pruebas instrumentadas/emulador. La selección tecnológica está pendiente en la Constitución/AGENTS actuales: deben revisarse conforme a la autorización del equipo. No insertar keystores ni credenciales Play en Git.

**Para completar CD**: obtener cuenta Play Console, configuración de firma, versión e identificador de paquete definitivos; acordar environment, revisores y un despliegue primero a pista interna. La subida real a tienda requiere una autorización independiente. `main` no se altera con este paquete; los workflows deben integrarse mediante PR.

## Trazabilidad SDD de CI/CD-001

La correspondencia requisito → control → evidencia y sus límites está en [trazabilidad CI/CD-001](trazabilidad-ci-cd.md). Esta automatización es una propuesta local de Móvil y queda sujeta a revisión humana; no altera ARCH-MOV-01..12 ni sus siete tareas futuras.

El job Android es una ruta condicional para un futuro proyecto Gradle: si no hay wrapper, no compila ni ejecuta pruebas de app. Esto no ratifica Android como plataforma del producto; la plataforma y el stack móvil siguen pendientes. El empaquetado manual no se ejecuta durante esta revisión.

La etapa inicial también parsea los dos archivos YAML con la biblioteca estándar YAML de Ruby; los resultados se registran en la matriz de trazabilidad.

## Resultados remotos verificados — 2026-11-11

- Rama: `feature/ci-cd-001-bootstrap`; HEAD al validar: `70cdf35add430b2b5c2dcb08791fceb930047d06`.
- [PR #1 hacia main](https://github.com/rutitasrutitas-eng/Rutitas_App/pull/1), abierto y sin fusionar.
- [GitHub Actions, run PR 43058284454](https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43058284454): job SDD/YAML/Gitleaks **success**; detección Gradle **success**; build/lint/tests Android **skipped** porque no hay `gradlew`/proyecto.
- Gitleaks: **success**, sin hallazgos reportados. La validación CI-05 parseó ambos workflows YAML sin error.
- App y AAB no verificados; no se ejecutó el workflow manual ni se publicó en Google Play.


## Auditoría del entorno de Play interno — 2026-11-12

Configuración visualmente verificada después de recargar GitHub: android-play-internal conserva revisores requeridos (DxRxXgSo, identidad confirmada por el usuario), Prevent self-review activado, bypass administrativo desactivado y única rama permitida main. No se detectaron secretos de repositorio ni para el entorno en la vista de GitHub Settings; no se consultaron valores.

Distinguir el estado por ref: play-internal.yml no existe en main; el PR #4 propone un workflow manual, limitado a main y con confirmación explícita, que carga a internal con status draft. El PR #4 sigue abierto, sin fusionar, y este workflow no se ejecutó. package-release.yml en main no firma ni publica; sí referencia android-release, que no aparece en la lista de entornos. Configurarlo y protegerlo es pendiente.

En las acciones externas aún fijadas a etiquetas mutables (setup-java@v6, setup-android@v4, setup-gradle@v6, emulator-runner@v2, upload-google-play@v1), se recomienda fijar SHAs completos revisados antes de operaciones sensibles. checkout, upload-artifact y Gitleaks se observan fijados en los archivos revisados. GitHub muestra main sin protección de rama; no se cambió.

Detalle y límites en [Auditoría Android Play](AUDITORIA_ANDROID_PLAY_INTERNAL.md). Esta verificación no ejecutó Actions ni distribución y no modifica la Constitución, que sigue pendiente de ratificación.


### CI automático del PR documental #5 — estado observado
La apertura del PR #5 activó automáticamente [run 43332604438](https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43332604438) para SHA 7ff6c63125a59c904f1c6fc88fde6ea0dc9ed6e5: SDD/YAML/Gitleaks y detección de Gradle **success**; build Android **skipped** por ausencia de wrapper en main. El run no ejecutó distribución. Este run corresponde al SHA indicado, no se presenta como evidencia de ejecución de un workflow de Play.
