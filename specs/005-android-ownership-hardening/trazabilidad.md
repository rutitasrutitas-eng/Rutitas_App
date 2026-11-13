# Trazabilidad 005

| Requisito | Implementación | Regresión | Estado |
|---|---|---|---|
| APP-01 | BookingRepository, RouteRepository, DriverRepository, ReviewRepository | OwnershipHardeningTest.sessionChangeHidesPrivateBookingsAndOwnRoutes; completedRouteIsPrivateExceptToItsPassenger; legacyBookingIsNotAssignedByGuessAndDemoSeedRemainsVisible | Local aprobada; revisión humana pendiente |
| APP-02 | BookingRepository, RouteRepository, DriverRepository, ReviewRepository, IncidentRepository, Daos | OwnershipHardeningTest.anotherUserCannotMutateRouteOrBooking; anotherUserCannotAcceptRejectOrValidateOwnersRequest; forgedReviewReferenceCannotRateAnotherUsersBooking; concurrentRequestsCannotExceedCapacityAndCancellationIsIdempotent | Local aprobada; revisión humana pendiente |
| APP-03 | Entities, Daos, Migrations, AppDatabase, AppModule, DriverRepository | OwnershipHardeningTest.bookingCodeIsRouteBoundAndSingleUseAcrossRepositoryInstances; acceptedRequestCodeCannotBeReused; RoomMigrationTest.migration3To5PreservesExistingRows | Local aprobada; revisión humana pendiente |
| APP-04 | AuthRepository, Daos | OwnershipHardeningTest.deletingAccountDoesNotRemoveOtherUsersRows; AccountIsolationTest | Local aprobada; revisión humana pendiente |

Pruebas instrumentadas: `android/app/src/androidTest/java/mx/edu/uttt/rutitas/`. Resultado local: 19/19, 0 fallidas, 0 omitidas, emulador Rutitas_Audit_API_37. El CI usa `.github/workflows/ci.yml`; resultado remoto pendiente de comprobación.
