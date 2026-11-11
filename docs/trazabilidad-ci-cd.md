# Trazabilidad SDD — CI/CD-001 Móvil

**Ámbito:** exclusivamente Rutitas Móvil. **Autoridad:** Constitución local candidata, `PENDIENTE_RATIFICACION`. **Decisión CI/CD:** propuesta; revisión por PR pendiente. Este anexo no cambia la arquitectura base ni aprueba plataforma, stack, proveedores o distribución.

| Requisito | Evidencia preparada | Criterio de cierre de esta fase | Límite actual |
|---|---|---|---|
| CI-01 | `.github/workflows/ci.yml` → job `sdd-and-security` | GitHub Actions valida archivos SDD y el run queda enlazado en `docs/CI_CD.md` | Validación documental, no revisión académica ni ratificación |
| CI-02 | `.github/workflows/ci.yml` → Gitleaks con historial completo | Paso Gitleaks aprobado en el run enlazado; registrar hallazgos sin secretos | No acredita ausencia de riesgos no detectados |
| CI-03 | `.github/workflows/ci.yml` → detección de wrapper y `android-build` condicional | Sin Gradle Wrapper/proyecto, build/test queda omitido y reportado como NO VERIFICADO | No hay código de app Android ni pruebas de producto; plataforma pendiente |
| CI-04 | Retención de reportes y APK debug si el build real llega a existir | Artefacto presente solo cuando el build produce resultados | No se genera evidencia de app cuando el job se omite |
| CD-01 | `.github/workflows/package-release.yml` manual, AAB de revisión | Revisión documental; ejecución manual queda fuera de alcance | No firma, sube ni publica en Google Play |
| SEG-01 | `permissions: contents: read`, checkout sin credenciales persistentes; environment en empaquetado | Revisar configuración y mantener credenciales fuera del repositorio | Environment/aprobadores deben configurarse por responsables humanos |
| P02 / P09 / P10 / P12 | Spec/plan/tareas de CI-CD-001, workflow y resultados de Actions | Evidencia SHA + URL, estados separados y aprobación humana pendiente | La Constitución local sigue propuesta; CI verde no ratifica normas |

## Evidencia de integración

- Rama: `feature/ci-cd-001-bootstrap` desde `main`.
- Commit(s) de integración: `4bef814eb48d165f0e0bc17afc955085a174d364` (integración) y `70cdf35add430b2b5c2dcb08791fceb930047d06` (validador YAML).
- Pull Request: [#1 abierto hacia main](https://github.com/rutitasrutitas-eng/Rutitas_App/pull/1); aprobación/merge pendientes.
- PR run: [43058284454](https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43058284454), éxito. Sintaxis de `ci.yml`/`package-release.yml`, SDD y Gitleaks aprobados; detección Gradle exitosa; job Android omitido por falta de wrapper.
- Plataforma/stack móvil: no seleccionado; la ruta Android solo opera si se detecta Gradle Wrapper.
- Constitución / ARCH-MOV-01..12 / 7 tareas: sin modificación; tareas permanecen `no_iniciado` y `no_ejecutada`.

| CI-05 | `.github/workflows/ci.yml` → parseo con Ruby/Psych de `ci.yml` y `package-release.yml` | Ambos archivos se parsean sin error en el run enlazado | Comprueba sintaxis YAML; no sustituye aprobación de cambios del pipeline |
