# Especificación — Arquitectura base Móvil

**ID:** ARCH-MOV-BASE  
**Decisión:** `propuesto` · **Aprobación:** pendiente  
**Ejecución:** `no_iniciado` · **Verificación:** `no_ejecutada`

## 1. Objetivo

Definir requisitos arquitectónicos generales para una aplicación móvil modular, accesible, mantenible y segura, con límites claros entre presentación, lógica y datos. No define funcionalidades del producto.

## 2. Alcance y exclusiones

Incluye capas, estado, navegación como capacidad, ciclo de vida, errores, configuración, accesibilidad, seguridad del cliente, almacenamiento local y comunicación con servicios como abstracciones, compatibilidad y pruebas previstas. Excluye pantallas, contenido, flujos, entidades, funcionalidades, endpoints, mapas, notificaciones de producto y código nuevo.

## 3. Fuentes y contexto tecnológico

Fuentes: Constitución y AGENTS propios del proyecto, instrucciones académicas y análisis de ejemplos SDD móviles. La copia experimental inspeccionada usa React/TypeScript/Vite/Capacitor; se registra únicamente como observación de una copia candidata, no como decisión o requisito. No se presume plataforma objetivo ni backend aprobado.

## 4. Requisitos arquitectónicos

| ID | Requisito | Criterio documental de aceptación |
|---|---|---|
| ARCH-MOV-01 | Separación UI/lógica/datos | Presentación no contiene acceso directo a almacenamiento o transporte; las responsabilidades se describen. |
| ARCH-MOV-02 | Gestión de estado | Estado de vista, estado de operación y estado persistido se distinguen; la política evita datos obsoletos presentados como confirmados. |
| ARCH-MOV-03 | Navegación | Se define una abstracción que respeta ciclo de vida, retorno y estado recuperable sin fijar pantallas/rutas de producto. |
| ARCH-MOV-04 | Ciclo de vida | El plan considera suspensión/reanudación, pérdida de proceso, permisos y conectividad según plataforma seleccionada. |
| ARCH-MOV-05 | Errores | Las capas traducen resultados y fallos a estados previsibles y recuperables sin filtrar información sensible. |
| ARCH-MOV-06 | Configuración | Valores por entorno no incluyen secretos de servidor ni defaults de producción inseguros; capacidad de compilación se decidirá. |
| ARCH-MOV-07 | Accesibilidad | El diseño futuro considera semántica, foco/orden, texto escalable, contraste y tecnologías de asistencia; criterios se acuerdan antes de aceptación. |
| ARCH-MOV-08 | Seguridad del cliente | Almacenamiento y permisos de plataforma se seleccionan por amenaza y capacidad; el cliente no es autoridad de permisos del servidor. |
| ARCH-MOV-09 | Almacenamiento local | Se define alcance, clasificación, caducidad y borrado para cada uso futuro; no se presupone persistencia específica. |
| ARCH-MOV-10 | Comunicación remota | Adaptadores dependen de contratos explícitos; se prevén timeout, desconexión, compatibilidad y respuestas de error, sin rutas ni payloads concretos. |
| ARCH-MOV-11 | Compatibilidad | Se acuerdan plataformas, versiones mínimas, capacidades nativas y matriz de dispositivos antes de seleccionar framework. |
| ARCH-MOV-12 | Pruebas y mantenibilidad | Se planifican pruebas unitarias de lógica, UI/estado, adaptadores y dispositivos de acuerdo con el stack y riesgo; no se reportan como ejecutadas. |

## 5. Criterios de aceptación de la especificación

- Describe capacidades arquitectónicas móviles generales y verificables documentalmente.
- No incorpora pantallas, journeys, historias ni identificadores funcionales de Rutitas.
- No impone React, Kotlin, Flutter, Capacitor, proveedor, almacenamiento ni librerías.
- Alinea seguridad, accesibilidad, configuración y errores con la Constitución propuesta.
- Separa evidencia documental de implementación/pruebas futuras.

## 6. Decisiones pendientes

Plataformas y versiones soportadas; stack; arquitectura de estado/navegación; capacidades nativas; estrategia de almacenamiento; comunicación; controles de seguridad móvil; accesibilidad; estrategia de pruebas; distribución y soporte. Ver [decisiones de este proyecto](../../docs/decisiones-pendientes.md).
