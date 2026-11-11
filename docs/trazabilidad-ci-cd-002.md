# Trazabilidad SDD — CI/CD-002 Móvil

| Necesidad / requisito | Spec | Tarea | Workflow | Prueba / evidencia prevista | Estado real actual |
|---|---|---|---|---|---|
| Controles documentales y secretos solicitados (MOV-CI2-01) | `specs/002-ci-cd/spec.md` | Mantener SDD/YAML/Gitleaks | `.github/workflows/ci.yml` → `sdd-and-security` | Run PR; parser YAML, documentos y Gitleaks | CI/CD-001 pasó en run `43058860869`; la rama 002 aún debe ejecutarse |
| Revisar vulnerabilidades nuevas (MOV-CI2-02) | `spec.md` | Agregar Dependency Review | `.github/workflows/ci.yml` → `dependency-review` | Run PR para cambios de manifiestos | Pendiente run; no hay manifiestos de producto |
| No afirmar build sin app (MOV-CI2-03) | `spec.md` | Detectar wrapper y ejecutar condicional | `.github/workflows/ci.yml` → `detect-project`, `android-build` | Estado del job y resumen | Detección anterior confirmó ausencia; build omitido; app no verificada |
| Artefacto manual sin Play (MOV-CI2-04) | `spec.md` | Mantener despacho manual | `.github/workflows/package-release.yml` | Revisión de YAML; no ejecutar durante esta tarea | No se generó AAB ni se publicó |
| Permisos mínimos / acciones inmutables (MOV-SEC2-01) | `spec.md` | Revisar permisos y SHA | Ambos workflows | Revisión diff + ejecución CI | Pendiente revisión final y run 002 |
| Reglas SDD candidatas | Constitución local P02/P06/P09/P10/P11/P12 | Trazar y registrar límites | `ci.yml`, `package-release.yml` | `docs/REPORTE_CI_CD_002.md`, run URL y SHA | Constitución pendiente; no se modifica |

**Límite:** Dependency Review detecta vulnerabilidades introducidas por cambios de dependencias en un PR; no demuestra un inventario actualizado ni seguridad completa de la aplicación. La fuente primaria de requisitos docentes no está anexada a este checkout; las restricciones comunicadas en la memoria quedan pendientes de verificación documental.
