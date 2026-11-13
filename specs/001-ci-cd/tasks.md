# Tareas — CI/CD-001 Móvil

- [x] Preparar pipeline YAML documental y Gitleaks.
- [x] Preparar pipeline de compilación condicional.
- [x] Preparar empaquetado manual sin despliegue.
- [x] Abrir PR hacia `main` (PR #1 abierto; revisión humana pendiente).
- [x] Ejecutar pipeline remoto y registrar SHA, URL y evidencias en `docs/CI_CD.md`.
- [ ] Agregar software, tests y contratos reales al repositorio.
- [ ] Confirmar y aprobar stack, proveedores, environments y secretos.
- [ ] Ejecutar y verificar CD real, con rollback.


## Auditoría de environment y distribución interna — 2026-11-12
- [x] Proteger android-play-internal: revisor confirmado, no autoaprobación, sin bypass y solo main.
- [x] Registrar hallazgos en el reporte y trazabilidad documental, en rama separada.
- [ ] Definir y proteger android-release.
- [ ] Confirmar secretos requeridos de manera segura y ejecutar solo tras autorización específica.
- [ ] Fijar acciones por SHA revisado.
- [ ] Revisar PR #4 y decidir su integración mediante aprobación humana.


## Cierre de controles administrativos — 2026-11-12

- [x] Definir y proteger `android-release`: reviewer DxRxXgSo, auto-revisión y bypass administrativo desactivados, solo main y cero tags. Esta actualización resuelve el pendiente marcado arriba.
- [x] Proteger `main` con PR, una aprobación, aprobación del último push por otra persona, conversaciones resueltas, force-push y eliminación bloqueados, sin bypass.
- [x] Revalidar `android-play-internal` intacto.
- [ ] Unificar jobs antes de hacer obligatorios los checks.
- [ ] Mantener pendientes secrets de firma/Play y distribución; no se consultaron ni modificaron.

PR #4 y #5 continúan abiertos y sin merge.
