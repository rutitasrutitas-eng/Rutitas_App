# Plan 005 — Propiedad y códigos

1. Confirmar `main`, ramas y PR existentes; aislar la corrección en `fix/android-ownership-hardening`.
2. Agregar pruebas instrumentadas que reproduzcan lecturas y mutaciones cruzadas, reuso de códigos, capacidad concurrente y baja local.
3. Filtrar datos por `ownerUserId` y sesión en repositorios; conservar rutas públicas y ejemplos DEMO identificables. Los registros históricos sin dueño quedan sin atribución.
4. Comprobar propiedad y estado en cada operación; usar transacciones Room para operaciones compuestas y actualización condicional para consumir códigos.
5. Migrar Room 4→5 con `boardingValidated = 0`; verificar el recorrido 3→4→5 y los datos históricos.
6. Ejecutar lint, pruebas unitarias, compilaciones, emulador, verificación SDD y CI/Gitleaks. Registrar resultados reales y límites.
7. Publicar rama y PR sin fusionar; Ame y el equipo revisan la aceptación funcional.
