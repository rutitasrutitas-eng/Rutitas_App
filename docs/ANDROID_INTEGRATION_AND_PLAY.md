# Rutitas Android — Integración y distribución (PR #4)

**Fecha:** 2026-11-12. **Estado:** implementación en rama de revisión, NO ratificada, NO publicada.

- Plataforma implementada de facto: Kotlin, Jetpack Compose, Room (versión 4), Hilt, Retrofit, SDK 36.
- **DEMO_MODE=true**, autenticación local de demostración (SHA-256 simple, **no apto para cuentas reales**), sin servicios remotos funcionales. La URL de release `example.invalid` es un marcador, no endpoint operativo.
- Almacenamiento de fotos: claves diferenciadas por usuario y borrado individual; las fotos de claves antiguas sin propietario no se atribuyen automáticamente a nuevas cuentas.
- Room: migración explícita `3 -> 4` agrega `ownerUserId` NULL a registros históricos; **no hay migraciones 1->3 verificadas**. `fallbackToDestructiveMigration()` se eliminó. Las operaciones de borrado solo abarcan registros de propietario conocido; los históricos requieren revisión de migración/retención.
- Reserva: actualización condicional de lugares y creación de reserva dentro de transacción local; evita sobreventa en el mismo dispositivo. La concurrencia entre dispositivos requiere operación atómica en backend.
- Cancelación y aceptación: transiciones condicionales dentro de transacciones locales; siguen pendientes pruebas completas y autorización del backend para datos reales.
- Baja de cuenta: afecta solo registros con propiedad explícita; rutas propias se marcan `CANCELLED` para conservar referencias ajenas. No se realiza baja de datos remotos.

## CI/CD

En el commit base `264d4f3c73682900b41ce7c39174ee93dbd2d451` se observó CI aprobada (run [43287112859](https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43287112859)). **La CI previa no valida estos cambios**; requieren nuevos checks en el próximo commit.

## Antes de solicitar merge

1. Ejecutar `cd android; ./gradlew lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest` con SDK 36, JDK 17 y emulador activo.
2. Comprobar las pruebas instrumentadas `BookingSafetyTest` y `AccountIsolationTest` y ampliar las pruebas para aceptación repetida, migraciones y baja completa de cuentas.
3. Inspeccionar resultados del CI y Gitleaks del nuevo SHA y actualizar la memoria con **evidencias reales**, sin declarar pruebas no ejecutadas.
4. Revisar la semántica final de `DemoSeeder.reset()` y la visibilidad de rutas/reseñas: ahora evita limpieza global, pero no repone registros DEMO si ya existen rutas. Persisten filtros históricos sin propietario.
5. Revisar el workflow `play-internal.yml`: `r0adkll/upload-google-play@v1` no está fijada por SHA; requiere auditoría y configuración de Environment con revisores. No ejecutar workflows manuales de AAB/Play.
6. Backend: acordar contrato API, autenticación de servidor, autorización por recurso, transacciones y protección de datos personales. No establecer `DEMO_MODE=false` sin esa integración.
7. Ratificación humana de Constitución, tecnología, protección de datos y distribución sigue **pendiente**.

## Seguridad y límites

No subir claves de Play/Maps, `local.properties`, APK/AAB firmados ni cuentas de servicio. No modificar `main` ni iniciar distribución durante esta revisión.
