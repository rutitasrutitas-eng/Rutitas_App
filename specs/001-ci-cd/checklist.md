# Checklist — CI/CD-001

- [x] YAML preparado para revisión.
- [x] Documentación SDD escrita.
- [x] YAML de ambos workflows parseado por el job CI-05 de GitHub Actions.
- [ ] PR aprobado por el equipo.
- [x] Escaneo Gitleaks ejecutado en GitHub sin hallazgos (run enlazado).
- [ ] Compilación y pruebas de aplicación/API ejecutadas sobre código real.
- [ ] Empaquetado verificado cuando exista código.
- [ ] CD productivo aprobado y verificado (fuera del alcance actual).


## Auditoría de entorno Android Play (2026-11-12)
- [x] android-play-internal restringido solo a main y verificado tras recarga.
- [x] Revisor requerido confirmado por el usuario; autoaprobación y bypass administrativo desactivados.
- [x] Revisada la presencia de secretos sin abrir ni exponer valores: ninguno en repositorio ni en el entorno consultado.
- [x] Comparados workflows de main con la propuesta aún abierta del PR #4.
- [ ] Crear y proteger android-release antes de usar el empaquetado de AAB.
- [ ] Configurar de forma segura y comprobar los secretos operativos necesarios para el flujo aprobado.
- [ ] Fijar a SHA completo las acciones externas aún referenciadas por etiquetas mutables.
- [ ] Revisar y fusionar PR #4; el workflow de Play todavía no está en main.
- [ ] Ejecutar despliegue de pruebas internas solo con autorización explícita futura y evidencia verificable.

No se ejecutó CI/CD ni distribución durante esta auditoría. Evidencia: [reporte](../../docs/AUDITORIA_ANDROID_PLAY_INTERNAL.md), PR documental #5.
