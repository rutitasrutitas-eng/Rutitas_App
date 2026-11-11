# CI/CD — Rutitas Móvil (preparación)

**Estado**: la rama `main` revisada el 11 de noviembre de 2026 es documental; no contiene una app Android. Esta configuración no ratifica una tecnología ni asegura un build real.

- `.github/workflows/ci.yml`: comprueba documentos SDD y ejecuta Gitleaks. Detecta Gradle Wrapper y solo entonces ejecuta `lintDebug`, `testDebugUnitTest`, `assembleDebug`. El trabajo omitido significa **Android NO verificado**, incluso si los controles documentales son verdes.
- `.github/workflows/package-release.yml`: prepara manualmente un AAB; exige un proyecto real, y **no firma, sube ni publica** en Google Play. Configurar `android-release` con revisores antes de usarlo.

**Para activar CI móvil de verdad**: incorporar `gradlew`, su JAR de wrapper, `settings.gradle(.kts)`, módulos, dependencias y pruebas; ajustar tareas para variantes/flavors reales. Posteriormente agregar pruebas instrumentadas/emulador. La selección tecnológica está pendiente en la Constitución/AGENTS actuales: deben revisarse conforme a la autorización del equipo. No insertar keystores ni credenciales Play en Git.

**Para completar CD**: obtener cuenta Play Console, configuración de firma, versión e identificador de paquete definitivos; acordar environment, revisores y un despliegue primero a pista interna. La subida real a tienda requiere una autorización independiente. `main` no se altera con este paquete; los workflows deben integrarse mediante PR.

## Trazabilidad SDD de CI/CD-001

La correspondencia requisito → control → evidencia y sus límites está en [trazabilidad CI/CD-001](trazabilidad-ci-cd.md). Esta automatización es una propuesta local de Móvil y queda sujeta a revisión humana; no altera ARCH-MOV-01..12 ni sus siete tareas futuras.

El job Android es una ruta condicional para un futuro proyecto Gradle: si no hay wrapper, no compila ni ejecuta pruebas de app. Esto no ratifica Android como plataforma del producto; la plataforma y el stack móvil siguen pendientes. El empaquetado manual no se ejecuta durante esta revisión.
