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

## Adenda 2026-11-12 — Correcciones PR #4

La fase documental descrita arriba es histórica. En `feature/integracion-android-ci-cd` ya existe proyecto Android y la CI original aprobó en SHA `264d4f3` (run 43287112859); **ningún check del run antiguo valida esta revisión posterior**. La ejecución del nuevo HEAD, Gitleaks, lint, pruebas instrumentadas y APK aún están pendientes. Las nuevas pruebas instrumentadas abarcan concurrencia local de reservas, doble cancelación y aislamiento por cuenta; no sustituyen aceptación ni operaciones remotas. Distribución Play y credenciales siguen bloqueadas por autorización manual. Consultar [revisión Android](ANDROID_INTEGRATION_AND_PLAY.md).
