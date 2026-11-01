# Estados canónicos y transiciones

Cada artefacto separa cuatro ejes. Este catálogo es propuesta pendiente de ratificación.

| Eje | Valores |
|---|---|
| Decisión | `propuesto` → `en_revision` → (`aprobado` / `rechazado`); `aprobado` → `supersedido` sólo con sustituto trazable |
| Ejecución | `no_iniciado` → `en_curso` → (`bloqueado` / `implementado`); bloqueado puede volver a `en_curso`; `implementado` → `liberado` sólo tras gates humanos |
| Verificación | `no_ejecutada` → (`aprobada` / `fallida` / `bloqueada` / `no_aplica`); una corrección requiere nueva ejecución |
| Tipo | `real`, `experimental`, `simulada`; `—` para documento sin comportamiento |

Aliases históricos: `PENDIENTE` → decisión `propuesto` sólo si el campo era aprobación, o ejecución `no_iniciado` si era trabajo; `HECHA` → ejecución `implementado`; `IMPLEMENTADO-NO-VERIFICADO` → ejecución `implementado` + verificación `no_ejecutada`. Conversión requiere conservar la fuente original y justificar casos ambiguos. Estado `liberado` no se deriva de build/test exitoso: requiere aprobación de release.
