# CI/CD-001 — Especificación de Móvil

**Decisión:** `propuesto`. **Ejecución:** `implementado` en rama de revisión. **Verificación:** `no_ejecutada` hasta documentar el run de Actions. **Tipo:** `—` (automatización documental, no funcionalidad de app).

- **CI-01:** todo PR a main valida documentos SDD obligatorios y reporta fallos.
- **CI-02:** ejecutar Gitleaks con historial Git completo, sin imprimir secretos.
- **CI-03:** compilar y probar software real cuando esté versionado; en ausencia de código, el job se omite e indica NO VERIFICADO.
- **CI-04:** guardar evidencias mediante artefactos con caducidad.
- **CD-01:** preparar artefactos solo bajo despacho manual; nunca desplegar a producción con esta versión.
- **SEG-01:** permisos mínimos, no credenciales en Git, revisión humana de entornos.

Los criterios de aceptación de compilación y despliegue requieren ejecución real y referencia a SHA + URL de GitHub Actions. Las plataformas y proveedores siguen sujetos a decisión humana.
