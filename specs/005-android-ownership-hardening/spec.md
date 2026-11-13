# 005 — Endurecimiento de propiedad y abordaje Android

Estado: implementación propuesta en `fix/android-ownership-hardening`; aceptación funcional pendiente de Ame y el equipo. La Constitución sigue `PENDIENTE_RATIFICACION`.

Fuente: solicitud del sprint DevSecOps Android del 13 de noviembre de 2026 y auditoría del código en `main` SHA `8112b5bacd276bb0b9c3cf326bbf84946d2d5f73`.

| ID | Requisito verificable |
|---|---|
| APP-01 | Una sesión solo lee sus reservas, rutas propias, solicitudes asignadas y reseñas privadas. Las rutas publicadas conservan visibilidad pública; los ejemplos DEMO se distinguen de registros históricos sin propietario. Cambiar A→B→A no mezcla datos. |
| APP-02 | Cada método de repositorio que cambia reservas, rutas, solicitudes o reseñas verifica usuario, propietario y estado antes de escribir; una llamada directa no elude la comprobación. La capacidad y los reintentos se protegen con transacciones y actualizaciones condicionales. |
| APP-03 | Solo el dueño de la ruta vigente valida códigos de solicitudes aceptadas o reservas confirmadas de esa ruta. El estado se guarda en Room, se consume una vez y sobrevive a nuevas instancias del repositorio. La migración 4→5 mantiene los datos anteriores. |
| APP-04 | La baja local de A no elimina filas de B ni asigna registros históricos `NULL` por inferencia. |

Exclusiones: autenticación remota, autorización del backend, borrado remoto, Google Play, credenciales y ratificación técnica o legal.
