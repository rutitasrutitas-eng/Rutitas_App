# CI/CD-002 — Especificación de Móvil

**Decisión:** `propuesto`. **Ejecución:** `implementado` en rama de revisión apilada. **Verificación:** `no_ejecutada` hasta terminar Actions del PR. **Tipo:** `—` (controles CI; no funcionalidad móvil).

## Alcance y límites

Mejorar los controles de seguridad y trazabilidad del CI propuesto en CI/CD-001. Solo se ejecutan los controles documentales, análisis de cambios de dependencias y detección de proyectos que existen. No se crean pantallas, app simulada, stack móvil aprobado, artefactos ficticios, servicios cloud ni credenciales. CI/CD-001 y este incremento siguen sujetos a revisión humana; la Constitución permanece propuesta.

## Requisitos

- **MOV-CI2-01:** mantener validación SDD, parseo YAML de ambos workflows y Gitleaks con historial completo.
- **MOV-CI2-02:** revisar en cada PR dependencias introducidas; fallar si se agrega una vulnerabilidad de severidad alta o crítica.
- **MOV-CI2-03:** validar Gradle/Lint/build/tests solo si existe proyecto Android real; sin wrapper, indicar que no se verificó la app.
- **MOV-CI2-04:** conservar reportes/APK únicamente cuando existan; el workflow manual prepara AAB para revisión sin firmar ni publicar.
- **MOV-SEC2-01:** conservar permisos mínimos, acciones de seguridad fijadas a SHA y no incluir credenciales.

## Criterios

La validación remota debe identificar cada job y su estado. Dependency Review evalúa cambios de dependencias del PR, no un inventario total del software existente. Si el repositorio no contiene manifiestos/producto, el reporte lo declarará y no atribuirá auditoría de paquetes ni pruebas de app. El build/CD completo queda `no_ejecutada` mientras falten código y decisiones.
