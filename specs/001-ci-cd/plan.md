# Plan — CI/CD-001 Móvil

1. Conservar la documentación SDD existente sin ratificarla automáticamente.
2. Incorporar CI de documentación y secretos desde rama de trabajo mediante PR.
3. Detectar código compilable y activar pruebas reales solo cuando exista.
4. Configurar environment de revisión con aprobadores humanos.
5. Construir paquete de entrega manual, sin despliegue.
6. Tras ratificar el stack, completar CD real con gestión de secretos, control de costos, smoke tests y rollback.

**Riesgos:** decisiones pendientes, tests aún no disponibles, permisos insuficientes del conector GitHub, ejecución documental mal interpretada como software probado.


## Estado de seguridad — 2026-11-12
El punto 4 queda verificado parcialmente para android-play-internal: revisor confirmado, sin autoaprobación ni bypass y solo main. El entorno android-release sigue ausente y debe protegerse antes del empaquetado firmado. Ningún workflow de distribución fue ejecutado.


### Actualización administrativa — 2026-11-12

- [x] Proteger `main` con PR y una aprobación humana, invalidación de revisiones antiguas, aprobación del último push por otra persona, conversaciones resueltas, force-push/eliminación bloqueados y sin bypass.
- [x] Crear `android-release`: reviewer DxRxXgSo, auto-revisión y bypass desactivados, solo `main`, sin tags.
- [x] Revalidar que `android-play-internal` mantiene su configuración.
- [ ] Unificar nombres de jobs antes de exigir checks obligatorios.
- [ ] Firma y distribución Google Play permanecen fuera del alcance y requieren autorización futura.

PR #4 y #5 siguen abiertos y sin merge; no se ejecutó distribución ni se consultaron secretos. Ver [reporte de seguridad](../../docs/AUDITORIA_ANDROID_PLAY_INTERNAL.md).
