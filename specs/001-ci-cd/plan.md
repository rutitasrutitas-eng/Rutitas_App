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
