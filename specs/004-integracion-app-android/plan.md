# Plan de corrección Android — propuesta

1. Inventariar propietario por entidad y conservar nulos históricos.
2. Migrar Room 3->4 sin pérdida destructiva.
3. Acotar limpieza de cuenta a identificador y archivos propios; mantener rutas canceladas para conservar reservas ajenas.
4. Reservar, cancelar y aceptar en transacciones locales y cambios de estado condicionados.
5. Agregar pruebas instrumentadas de concurrencia y aislamiento.
6. Ejecutar emulador, lint, unitarias, Gitleaks y CI del nuevo SHA; registrar los resultados reales.
7. Revisar pendientes de seguridad, ratificación y publicación antes del merge.
