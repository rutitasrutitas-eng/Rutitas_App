# Reporte de endurecimiento Android APP-01/02/03

Fecha: 13 de noviembre de 2026. Base comprobada: `main` `8112b5bacd276bb0b9c3cf326bbf84946d2d5f73` (sin cambios remotos nuevos antes del commit). Rama: `fix/android-ownership-hardening`. PR #5 documental ajeno permanece separado. Constitución: `PENDIENTE_RATIFICACION`.

## Hallazgos y regresiones

| Hallazgo | Antes | Corrección y prueba | Estado |
|---|---|---|---|
| APP-01: lectura entre cuentas | Reservas, rutas propias, solicitudes y reseñas se leían globalmente; `ownedByMe` era compartido entre sesiones. | Filtrado por `ownerUserId` y sesión, ruta pública separada, ruta terminada limitada a dueño/pasajero; `OwnershipHardeningTest` de A→B→A, datos DEMO y ruta terminada. | 19/19 pruebas instrumentadas locales aprobadas; revisión humana pendiente. |
| APP-02: operaciones sin propiedad | Edición/estado de ruta, progreso de reserva, rechazo de solicitud, incidencia y reseña admitían llamadas directas sin contexto suficiente. | Comprobaciones en repositorios; aceptación y cancelación transaccional; referencia de reseña ligada al recurso. Pruebas de mutación ajena, capacidad concurrente, cancelación repetida y baja local. | Pruebas locales aprobadas; revisión humana pendiente. |
| APP-03: abordaje | Reserva confirmada podía validarse para otra ruta y reutilizarse; el código UUID creado en minúsculas fallaba al buscarse en mayúsculas. | Validación de ruta vigente y propiedad, actualización condicional persistida, búsqueda sin distinción de mayúsculas, Room 4→5; pruebas de ruta equivocada, reuso, cancelación y migración 3→4→5. | Pruebas locales aprobadas; revisión humana pendiente. |

La primera corrida de 18 instrumentadas dio 17 aprobadas y 1 fallida por el código UUID en minúsculas. Tras corregirlo, 18/18 pasaron. Una corrida posterior se interrumpió por `EXCEPTION_ACCESS_VIOLATION` del JDK 17 local durante compilación, sin fallo de pruebas; la repetición con `-XX:TieredStopAtLevel=1` pasó 19/19.

## Archivos y comprobación

Pruebas: `OwnershipHardeningTest.kt`, `RoomMigrationTest.kt`. Datos: `Entities.kt`, `Daos.kt`, `Migrations.kt`, `AppDatabase.kt`, `AppModule.kt`. Repositorios: `AuthRepository.kt`, `BookingRepository.kt`, `DriverRepository.kt`, `RouteRepository.kt`, `ReviewRepository.kt`, `IncidentRepository.kt`. Presentación: `ViewModels.kt`. Especificación y trazabilidad: `specs/005-android-ownership-hardening/`.

Commits de código: pruebas primero `bf3156b`; corrección `b6802150ae74f3b4a8ba228025c3f24f3e22c739`.

| Control local | Resultado real |
|---|---|
| `lintDebug` | Aprobado |
| `testDebugUnitTest` | 6/6, sin fallos ni omitidas |
| `assembleDebug` | Aprobado |
| `assembleDebugAndroidTest` | Aprobado |
| `connectedDebugAndroidTest` | 19/19, sin fallos ni omitidas; emulador Rutitas_Audit_API_37, API 37 |
| `python scripts/verify_project.py` | Aprobado |
| GitHub Actions y Gitleaks | Pendiente de verificación tras publicar el PR |

Alcance de pruebas: la migración verifica datos v3→v5 de una muestra reproducible; no cubre versiones 1/2 ni todos los historiales reales. Las reseñas DEMO sembradas son públicas; las privadas de usuario se limitan a su sesión, mientras las calificaciones de conductores se exponen como dato público. El modelo actual carece de identificador fiable del destinatario para reseñas históricas: no se reasignan por coincidencia de nombre. La autenticación productiva, la autorización del backend, el borrado remoto y la distribución Google Play siguen pendientes de diseño y aprobación humana.

No se fusionó `main`, no se ejecutó Play ni se modificaron secretos o entornos de publicación. La aprobación funcional corresponde a Ame y al equipo.
