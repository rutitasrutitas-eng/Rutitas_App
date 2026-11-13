# CI/CD-001 — Especificación de Móvil

**Decisión:** `propuesto`. **Ejecución:** `implementado` en rama de revisión. **Verificación:** `no_ejecutada` hasta documentar el run de Actions. **Tipo:** `—` (automatización documental, no funcionalidad de app).

- **CI-01:** todo PR a main valida documentos SDD obligatorios y reporta fallos.
- **CI-02:** ejecutar Gitleaks con historial Git completo, sin imprimir secretos.
- **CI-03:** compilar y probar software real cuando esté versionado; en ausencia de código, el job se omite e indica NO VERIFICADO.
- **CI-04:** guardar evidencias mediante artefactos con caducidad.
- **CI-05:** validar la sintaxis YAML de ambos workflows en CI antes de los controles documentales.
- **CD-01:** preparar artefactos solo bajo despacho manual; nunca desplegar a producción con esta versión.
- **SEG-01:** permisos mínimos, no credenciales en Git, revisión humana de entornos.

Los criterios de aceptación de compilación y despliegue requieren ejecución real y referencia a SHA + URL de GitHub Actions. Las plataformas y proveedores siguen sujetos a decisión humana.


**Evidencia de CI parcial (2026-11-11):** GitHub Actions parseó ambos YAML, validó SDD y aprobó Gitleaks. Detección no encontró Gradle Wrapper y omitió el build. Por eso la verificación del alcance completo CI/CD-001 permanece `no_ejecutada`; los requisitos de app real y CD siguen abiertos.


## Verificación posterior de seguridad — 2026-11-12

La protección del environment android-play-internal se configuró y verificó visualmente: revisor requerido DxRxXgSo (cuenta confirmada por el usuario), autoaprobación impedida, bypass administrativo desactivado y rama permitida exclusivamente main. Este control está listo como configuración, pero no se ha ejecutado un deployment y no acredita el funcionamiento end-to-end del CD.

Pendientes de cierre de seguridad: crear/proteger android-release antes de empaquetado firmado; aprovisionar secretos cuando corresponda por canal seguro; fijar las acciones externas todavía referenciadas por etiquetas mutables. play-internal.yml solo está en el PR #4 abierto, no en main. Sin ejecutar workflows ni modificar la arquitectura base.


## Actualización de controles del repositorio — 2026-11-12

`main` está protegido por PR y una revisión humana, invalidación de revisiones antiguas, aprobación del último push por otra persona, resolución de conversaciones, bloqueo de force-push/eliminación y lista de bypass vacía. Status checks no obligatorios hasta unificar los nombres de jobs entre main y PR #4.

`android-release` requiere aprobación de `DxRxXgSo`, impide auto-revisión, no permite bypass y limita despliegues a `main` (sin tags). `android-play-internal` se conservó sin cambios. Configuración verificada tras recargar; no se ejecutó distribución, no se accedió a secretos. PR #4 y #5 permanecen abiertos/sin merge. Detalle: [reporte de seguridad](../../docs/AUDITORIA_ANDROID_PLAY_INTERNAL.md).
