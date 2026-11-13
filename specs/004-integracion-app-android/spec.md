# 004 — Especificación correctiva Android, PR #4

**Propuesta para revisión, no ratificada.** No modifica la Constitución.

- R-AND-01 (P0-A): cada registro nuevo de rutas, reservas y reseñas debe almacenar `ownerUserId`; los registros históricos NULL no se atribuyen por nombre ni por `ownedByMe`.
- R-AND-02 (P0-A): baja local de A no borra datos de B; fotografías de conductor separadas por identificador de usuario; no `deleteAll` en baja de cuenta.
- R-AND-03 (P0-B): cantidades de asientos estrictamente positivas; validar ruta activa; reservar con transacción Room y descuento condicional.
- R-AND-04 (P0-C): cancelar y aceptar cambios de estado solo una vez; sin devoluciones/descuentos duplicados.
- R-AND-05 (P1): no presentar DEMO como backend o autenticación productiva.
- R-AND-06 (P1): preservar datos al migrar Room 3->4; bloquear upgrades no especificados.
- R-AND-07 (P1): publicación manual exclusivamente bajo autorización humana.
- R-AND-08 (P2): agregar regresiones Android verificables en CI.

**No alcance:** distribución Google Play, credenciales reales, despliegue backend, migraciones históricas 1/2 no documentadas y ratificación SDD.
