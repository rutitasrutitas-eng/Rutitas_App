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
