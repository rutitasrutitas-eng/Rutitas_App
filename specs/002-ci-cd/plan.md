# Plan — CI/CD-002 Móvil

1. Auditar la rama CI/CD-001, main remoto, cambios locales, archivos, código, tests, workflows y decisiones.
2. Respaldar refs/historial en bundle externo y verificar su restauración.
3. Crear `feature/ci-cd-002-mobile` desde `feature/ci-cd-001-bootstrap`, sin alterar el PR previo.
4. Agregar Dependency Review con umbral alto, manteniendo SDD/Gitleaks y compilación condicional.
5. Probar workflows y registrar evidencia en PR apilado hacia la rama CI/CD-001.
6. Dejar build real/CD pendientes hasta que existan app, pruebas, plataforma/stack y aprobaciones.

**Riesgos:** PR base todavía sin merge; Dependency Review cubre cambios de dependencias del PR y no el inventario total; GitHub Actions omitirá build al no existir `gradlew` ni proyecto Android.
