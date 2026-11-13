# Matriz de trazabilidad — revisión correctiva

| Requisito | Código | Prueba / evidencia | Estado |
|---|---|---|---|
| R-AND-01 | database/Entities.kt, Daos.kt, Migrations.kt | AccountIsolationTest | Preparado, sin ejecutar |
| R-AND-02 | AuthRepository.kt, DriverRepository.kt, PrivatePhotoStore.kt | AccountIsolationTest | Parcial; baja E2E pendiente |
| R-AND-03 | BookingRepository.kt, Daos.kt | BookingSafetyTest.competingForLastSeatOnlyOneSucceeds, zeroOrNegativeSeatsNeverChangeCapacity | Sin ejecutar |
| R-AND-04 | BookingRepository.kt, DriverRepository.kt | BookingSafetyTest.cancellingTwiceReturnsSeatsOnce | Parcial; falta aceptación repetida |
| R-AND-05 | android/app/build.gradle.kts, docs/ANDROID_INTEGRATION_AND_PLAY.md | Revisión documental | DEMO, sin backend |
| R-AND-06 | AppDatabase.kt, Migrations.kt, AppModule.kt | Prueba upgrade pendiente | Parcial |
| R-AND-07 | .github/workflows/play-internal.yml | Revisión manual pendiente | Bloqueado |
| R-AND-08 | android/app/src/androidTest | Nuevas clases de test, sin ejecución | Pendiente |
