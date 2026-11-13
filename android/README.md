# Rutitas UTTT (Android nativo)

App de rutas compartidas universitarias: **Kotlin + Jetpack Compose + Material 3**, Navigation Compose, MVVM,
Repository, Coroutines/StateFlow, Room, Retrofit y Hilt. Funciona **sin servidor** en modo DEMO.

## 1. Abrir en Android Studio
1. Descomprime el ZIP.
2. `File > Open` y elige la carpeta `RutitasUTTT` (Android Studio Koala o más nuevo, JDK 17 incluido y Android SDK 36).

## 2. Sincronizar Gradle
Android Studio sincroniza solo al abrir. Si no: `File > Sync Project with Gradle Files`.
Necesitas conexión a internet la primera vez (descarga Gradle 8.9 y dependencias) y el SDK Android 36.

## 3. Ejecutar en emulador
`Device Manager > Create device` (Pixel, API 26 o superior) y pulsa **Run ▶**.

## 4. Ejecutar en celular
Activa *Opciones de desarrollador > Depuración USB*, conecta el cable, acepta la autorización y pulsa **Run ▶**.

## 5. Credenciales demo
| Cuenta | Correo | Contraseña |
|---|---|---|
| Pasajero demo | `demo@rutitas.com` | `123456` |
| Cuenta inhabilitada (motivo: Documento vencido) | `bloqueado@rutitas.com` | `123456` |

También puedes **crear cuenta** (se guarda en Room) y recorrer Ubicación → Horario → Privacidad.

**Dominios institucionales:** el registro solo acepta correos de estas instituciones (y sus subdominios, ej. `alumni.uaeh.edu.mx`): `uaeh.edu.mx`, `itpachuca.edu.mx`, `upp.edu.mx`, `upmetropolitana.edu.mx`, `uttt.edu.mx`, `utvm.edu.mx`, `unideh.edu.mx`, `upiih.ipn.mx`. La lista y los nombres de institución están en `utils/AppConfig.kt` (`INSTITUTIONS`); el perfil muestra la institución según el dominio del correo. Las cuentas demo pueden iniciar sesión sin restricción.

- **Reseñas del conductor para el pasajero:** en el detalle de ruta se ve el promedio, cuántas reseñas tiene y las últimas dos; *Ver todas las reseñas del conductor* abre la lista completa.

### Novedades (ronda 4)
- **Tiempo de espera real:** el conductor pulsa *Llegué a [punto]* y corre una cuenta regresiva con el tiempo configurado (5–20 min); puede continuar antes o cancelar la espera. El pasajero ve el mismo contador en el seguimiento.
- **Puntos de encuentro editables:** al publicar/editar una ruta el conductor elige los puntos intermedios; el mapa los muestra numerados.
- **Horario del conductor:** se muestra en el inicio y en su perfil (turno, entrada y salida) y se edita con reloj.
- **Historial de reseñas** (*Perfil > Reseñas y calificaciones*), en ambos sentidos: pasajero → conductor (al terminar el viaje) y conductor → pasajero (al finalizar la ruta, *Calificar pasajero*). Pestañas *Recibidas* y *Enviadas*, con promedio.
- **Perfil rediseñado** (pasajero y conductor): foto de perfil (tomar, elegir o quitar), calificación, viajes y reseñas. **Editar perfil**: foto, nombre, correo (con dominio institucional), tipo de usuario, zona y horario. El correo de las cuentas demo no se puede cambiar.
- La foto de perfil se guarda en almacenamiento privado, sin metadatos, y se borra al eliminar la cuenta.
- Base de datos Room versión 5: migraciones explícitas 3→4→5 conservan los registros, mantienen sin propietario los históricos no verificables y agregan validación persistente de abordaje. Las versiones 1/2 aún no tienen migración aprobada; no se aplica borrado destructivo automático.

### Novedades de la ronda anterior
- **Eliminar cuenta:** borra de verdad (perfil, reservas, rutas, solicitudes, registro de conductor y fotos privadas). Las cuentas demo también se eliminan; se recuperan con **Restaurar cuentas demo** en la pantalla de inicio de sesión.
- **Conductor:** *Iniciar ruta* (inicio y lista de rutas), detalle completo de cada ruta (ver pasajero, aceptar/rechazar, **validar código**, avanzar puntos, finalizar, **cancelar viaje**, reportar problema) y **editar rutas** que no han iniciado.
- **Publicar ruta:** reloj para elegir la hora, **tiempo de espera** en el punto de encuentro (5–20 min) y mapa con los puntos de encuentro numerados. Con *Agregar mi ubicación* (GPS o zona) se dibuja "Tú" y el punto más cercano.
- **Solicitudes:** cada una indica el horario (Matutino/Vespertino/Mixto) y si el viaje es de **Entrada** (voy a la escuela) o **Salida** (regreso a casa). El pasajero lo elige al reservar.
- **Pasajero:** *Ver mi código* en el seguimiento (se resalta cuando el conductor está por llegar) y en *Viajes*.
- **Seguridad de fotos (credencial y documentos):** consentimiento previo, pantalla con captura bloqueada (FLAG_SECURE), foto a resolución completa guardada solo en almacenamiento privado de la app, sin metadatos EXIF/GPS, opción de eliminarla y borrado al eliminar la cuenta.
- Códigos demo para validar como conductor (después de aceptar al pasajero): `RT-3101` (Ana), `RT-3102` (Luis), `RT-3103` (Sofía). También sirven los códigos de reservas hechas desde el modo pasajero (ej. `RT-2481`).
- La base de datos subió a la versión 2: al actualizar sobre una instalación anterior los datos locales se reinician (usa «Restaurar cuentas demo» o reinstala).

**Eliminar cuenta:** *Perfil > Eliminar cuenta* (también en el perfil de conductor). Pide escribir `ELIMINAR` y borra perfil, reservas, rutas publicadas, solicitudes y registro de conductor, libera los lugares reservados y cierra la sesión. En la cuenta demo restablece sus datos (se puede volver a entrar).
En *Perfil > Opciones demo* puedes simular una cuenta inhabilitada y restablecer los datos.

### Qué probar
- Mapa sin iniciar sesión, buscar, detalle de ruta, reservar → código `RT-2481` → seguimiento → viaje en curso → calificación.
- *Reportar problema > Vehículo descompuesto*: la ruta pasa a **Interrumpida**, se bloquean reservas y se muestran alternativas.
- Perfil > *Quiero ser conductor*: permisos reales → identidad → vehículo → documentos → revisión → aprobado → panel (publicar ruta, solicitudes, ganancias).
- Atajos demo: *Autocompletar datos demo*, *Autocompletar documentos*, *Saltar espera*.

## 6. Configurar Google Maps después
1. Crea una API key (Maps SDK for Android) en Google Cloud Console.
2. En `local.properties` agrega: `MAPS_API_KEY=TU_CLAVE`.
3. Sincroniza y ejecuta. Sin clave se muestra el mapa DEMO (la app nunca se bloquea).
4. Las coordenadas de `GeoPlaces` (`ui/components/Maps.kt`) son **aproximadas**: reemplázalas por las reales.

## 7. Conectar un backend
- `network/ApiService.kt` define el contrato Retrofit; la URL base está en `app/build.gradle.kts` (`BASE_URL`, por defecto `http://10.0.2.2:8080/api/` para el emulador).
- Cambia `DEMO_MODE` a `false` en `app/build.gradle.kts`.
- Implementa las llamadas dentro de los repositorios (`repository/`): `RouteRepository.refreshFromServer()` ya muestra el patrón (descarga y guarda en Room). Las pantallas y ViewModels no cambian.
- La release ya rechaza HTTP sin cifrar (`usesCleartextTraffic=false`); debug permite la dirección HTTP del emulador. El backend productivo y su integración siguen pendientes.

## Logo y animación de inicio
Al abrir la app se reproduce una vez el GIF `res/raw/animacion_rutitas.gif` (≈6.4 s; tocar la pantalla lo omite). Se reproduce con Coil GIF en `ui/screens/SplashScreen.kt`; la duración se ajusta con `SPLASH_DURATION_MS`. Para cambiar la animación reemplaza el GIF conservando el nombre. El ícono de la app usa el emblema del logo (`res/drawable-nodpi/ic_launcher_foreground.png`).

## Estructura
```
mx.edu.uttt.rutitas
├── MainActivity / RutitasApp   (solo inicia la navegación y siembra datos demo)
├── navigation   (Routes, AppNavHost)
├── ui           (theme, components, screens)
├── viewmodel    (ViewModels con StateFlow)
├── repository   (Auth, Route, Booking, Driver, Incident)
├── domain       (Recommender: orden por horario y zona)
├── data         (DemoData, DemoSeeder, SessionManager)
├── database     (Room: entidades, DAOs, mappers)
├── network      (Retrofit ApiService + DTOs)
├── model / utils / di (Hilt)
```

## Notas
- La sesión vive en memoria (se pierde al cerrar la app); usuarios registrados, rutas, reservas y solicitudes persisten en Room.
- Contraseñas de cuentas registradas se guardan con hash SHA-256 (solo para demo; en producción usa el backend).

## CI/CD y Google Play

La carpeta Android se integra al repositorio documental como `android/`. Consulta `../docs/ANDROID_INTEGRATION_AND_PLAY.md`. Los workflows que generan AAB requieren llave de carga y aprobaciones; el modo DEMO todavía no sirve para movilidad multiusuario real.
