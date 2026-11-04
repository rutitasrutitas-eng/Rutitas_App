# Dependencia futura del Backend propuesto — impacto para Rutitas Móvil

> **Estado:** `BORRADOR_EXPLORATORIO` / `PENDIENTE_APROBACION` (2026-11-04). **No** cambia la Constitución, AGENTS ni la especificación de arquitectura base de Móvil.
> **Ámbito:** este archivo informa sobre una **propuesta del proyecto hermano**; no selecciona stack, biblioteca, navegación, pantallas o proveedor propio de Rutitas_App.
> **Autoridad:** información del equipo reportada por el usuario, sin acuerdo de integración ratificado (`MOVIL-D05`/`MOVIL-D06`).

## 1. Propuesta que el equipo Backend está evaluando

| Componente ajeno a Móvil | Alternativa candidata | Significado para la app |
|---|---|---|
| Servicio | ASP.NET Core 10 / C# | Cliente HTTP interoperable; la app **no necesita C#** por este hecho. |
| Hospedaje | Google Cloud Run | El cliente consume HTTPS; la región y URL se definirán después. |
| Datos del servidor | Google Cloud Firestore (NoSQL) | La aplicación no debe conocer la estructura interna ni modificar reservas directamente sin un contrato de seguridad aprobado. |
| Identidad | Firebase Authentication, todavía en evaluación | Si se adopta, la app obtiene token y el Backend lo valida; las reglas institucionales se verifican también en servidor. |
| Mapas | Google Maps Platform, uso selectivo | Uso potencial de mapa/puntos/rutas; SDK/plataforma móvil y costos pendientes. |

**Restricciones docentes reportadas:** usar datos NoSQL y elegir alojamiento entre Azure, AWS y Google Cloud. La preferencia por Google Cloud no ha sido ratificada ni impide evaluar otras nubes. El stack del cliente Móvil continúa **sin decidir**; React/TypeScript/Vite/Capacitor describe un prototipo previo, no una decisión de este repositorio.

## 2. Integración conceptual a futuro

```text
Rutitas_App (stack por decidir)
   └── HTTPS + JSON + autenticación aprobada
        └── API REST ASP.NET Core (Backend candidato)
             ├── Autorización y lógica de reservas
             ├── Firestore NoSQL
             └── Maps/identidad según decisiones futuras
```

El contrato de versión, rutas, estados, errores, modelos DTO, expiración de token, paginación, desconexión, reintentos, protección de datos y pruebas se acordará **entre ambos equipos**. Ningún esquema Firestore es contrato público del cliente.

## 3. Posibles escenarios de autenticación

- **Firebase Authentication administrada:** la app integra el flujo de identidad elegido, solicita credenciales a Firebase y aporta un token a la API; el Backend valida firma/expiración/audiencia y permisos. No contar endpoints externos como endpoints implementados en ASP.NET.
- **Identidad propia Backend:** la app consumiría endpoints REST de registro/login/verificación desarrollados por el equipo Backend; cambian el conjunto de siete endpoints. No se elige esta alternativa por inferencia.

Por eso no deben fijarse pantallas definitivas, SDKs ni flujos de sesión hasta resolver `MOVIL-D06` / `BACKEND-D06` y la decisión de identidad.

## 4. Requisitos no funcionales del cliente que habrá que evaluar

1. Presentar estados de carga, vacío, sin conexión, sesión vencida y conflictos (`409`) sin perder claridad.
2. No asumir cupos por datos en caché: el Backend da la respuesta definitiva a reservar/cancelar.
3. Evitar reservas duplicadas por reintentos, doble toque o reconexión; definir idempotencia con Backend.
4. Mostrar solo puntos públicos/permitidos; no exponer domicilios personales ni claves secretas.
5. Proteger tokens en almacenamiento adecuado a plataforma, nunca incluir secretos del servidor en el APK/paquete.
6. Respetar permisos de localización y minimización de rastreo; Google Maps tiene SKUs y cuotas diferenciados.
7. Ejecutar pruebas de contrato, errores, accesibilidad y compatibilidad **solo tras aprobación y elección del stack móvil**.

## 5. Próximas validaciones conjuntas (no ejecutadas)

- Confirmar fuente primaria de las restricciones docentes (NoSQL y proveedores cloud), fechas y aprobación.
- Cerrar stack/identidad de Backend y **luego** decidir el stack del cliente de manera autónoma.
- Acordar documento OpenAPI, respuestas, modelo de error, autorizaciones, fechas y límites de exposición.
- Evaluar quién llama a Google Maps (cliente / servidor) y cómo se protegen claves y cuotas.
- Revisar estrategias de sincronización, desconexión y datos personales antes de una implementación.

### Referencias informativas del proyecto hermano

- Propuesta tecnológica (rama oficial Backend; **enlace válido solo después de publicar el cambio**): https://github.com/rutitasrutitas-eng/Rutitas_BackEnd/blob/main/docs/propuestas-futuras/stack-backend-y-cloud.md
- Inventario exploratorio de siete endpoints (**no contrato aprobado**): https://github.com/rutitasrutitas-eng/Rutitas_BackEnd/blob/main/docs/propuestas-futuras/inventario-7-endpoints.md

**Nota:** estos enlaces apuntan a futuros archivos del proyecto Backend; la actualización local del ZIP **no los publica** automáticamente en GitHub.
