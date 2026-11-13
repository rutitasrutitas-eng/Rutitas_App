# Trazabilidad SDD — CI/CD-001 Móvil

**Ámbito:** exclusivamente Rutitas Móvil. **Autoridad:** Constitución local candidata, `PENDIENTE_RATIFICACION`. **Decisión CI/CD:** propuesta; revisión humana del PR pendiente. Este anexo no cambia la arquitectura base ni aprueba plataforma, stack, proveedores o distribución.

| Requisito | Evidencia preparada | Criterio de cierre de esta fase | Límite actual |
|---|---|---|---|
| CI-01 | `.github/workflows/ci.yml` → job `sdd-and-security` | GitHub Actions valida archivos SDD y el run queda enlazado en `docs/CI_CD.md` | Validación documental, no revisión académica ni ratificación |
| CI-02 | `.github/workflows/ci.yml` → Gitleaks con historial completo | Paso Gitleaks aprobado en el run enlazado; registrar hallazgos sin secretos | No acredita ausencia de riesgos no detectados |
| CI-03 | `.github/workflows/ci.yml` → detección de wrapper y `android-build` condicional | Sin Gradle Wrapper/proyecto, build/test queda omitido y reportado como NO VERIFICADO | No hay código de app Android ni pruebas de producto; plataforma pendiente |
| CI-04 | Retención de reportes y APK debug si el build real llega a existir | Artefacto presente solo cuando el build produce resultados | No se genera evidencia de app cuando el job se omite |
| CI-05 | `.github/workflows/ci.yml` → parseo con Ruby/Psych de `ci.yml` y `package-release.yml` | Ambos archivos se parsean sin error en el run enlazado | Comprueba sintaxis YAML; no sustituye aprobación de cambios del pipeline |
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


## Evidencia de controles de entorno — 2026-11-12

| Requisito / control | Estado observado | Evidencia y límite |
|---|---|---|
| SEG-01: revisión humana del environment | Verificado para android-play-internal | Reviewer requerido DxRxXgSo confirmado por el usuario; prevent self-review activado; bypass administrativo desactivado. Revisado después de recargar Settings. |
| SEG-01: limitar origen de publicación | Verificado | Única rama permitida: main; 0 etiquetas. No se ejecutó el workflow. |
| SEG-01: secretos fuera de Git | Pendiente para el flujo Android del PR #4 | Settings mostró cero secretos de repositorio y cero secretos del entorno; ningún valor fue leído. Configuración operativa pendiente. |
| CD-01: distribución interna | No verificada / no ejecutada | play-internal.yml está solo en el PR #4 abierto, no fusionado; es workflow_dispatch y requiere confirmación exacta. |
| CI/CD: environment de firma | Pendiente | android-release no aparece en la lista de entornos; package-release.yml de main lo referencia. |
| SEG-02: acciones inmutables | Parcial | checkout, upload-artifact y Gitleaks están fijados a SHA/digest; hay otras acciones con etiquetas mutables. |

El informe detallado está en [AUDITORIA_ANDROID_PLAY_INTERNAL.md](AUDITORIA_ANDROID_PLAY_INTERNAL.md). Esta evidencia es de configuración visual y revisión estática de workflows; no sustituye logs de ejecución, aprobación humana de un deployment ni ratificación constitucional. El PR documental #5 es la propuesta que incorpora este registro; no está fusionado.


## Trazabilidad de protecciones de main y entornos — 2026-11-12

| Control | Estado verificado | Evidencia | Pendiente |
|---|---|---|---|
| Revisión de `main` | PR requerido; 1 aprobación; aprobación del push más reciente por otra persona; conversaciones resueltas | Ruleset activo `Rutitas main: PR review y protección`, limitado a `main` | Revisión humana de PRs #4/#5 |
| Integridad de `main` | Eliminación y force-push bloqueados; bypass vacío | Ruleset persistió tras verificación | — |
| Status checks | No obligatorios | Nombres de CI difieren entre main y #4 | Uniformar y probar antes de requerir checks |
| `android-release` | DxRxXgSo requerido; no self-review ni bypass; solo main, cero tags | Guardado y recargado; sin secretos/variables | Firma/Play en fase autorizada |
| `android-play-internal` | Sin cambios; DxRxXgSo, no self-review/bypass, main solamente, cero tags | Revisión visual tras actualizar | — |

PR #4 y #5 permanecen abiertos y sin fusionar. No se ejecutó publicación ni se accedió a valores secretos. El detalle está en [auditoría](AUDITORIA_ANDROID_PLAY_INTERNAL.md).
