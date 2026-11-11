# Checklist — CI/CD-002 Móvil

- [x] Auditoría y respaldo verificable.
- [x] Rama nueva apilada sin cambios a main ni PR #1.
- [x] Workflow YAML revisado; CI valida sintaxis localmente en runner.
- [x] CI SDD/YAML y Gitleaks aprobados en el PR; Dependency Review y build quedaron omitidos por ausencia de proyecto.
- [ ] Dependency Review ejecutado con graph habilitado y manifiestos reales (no aplica/NO VERIFICADO mientras no exista proyecto).
- [ ] Lint, build y tests de app real (bloqueados: no hay Gradle Wrapper/proyecto).
- [ ] Aprobación y merge humano del PR.
- [ ] Plataforma/stack, package id, versionCode/versionName, firma y environment aprobados.
- [ ] Publicación Google Play (fuera de esta autorización; requiere aprobación separada).
