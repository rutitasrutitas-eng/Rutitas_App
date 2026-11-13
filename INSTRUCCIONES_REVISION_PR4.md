# Cómo aplicar la revisión del PR #4 sin tocar main

**No se ejecutaron ni certificaron pruebas Android en esta entrega.** Este proyecto no incluye `.git`, caches, secretos ni archivos de compilación.

## Método recomendado (rama existente)

1. Verifica la rama local: `git branch --show-current` debe ser `feature/integracion-android-ci-cd` y `git status --short` vacío.
2. Conserva tu respaldo privado; no reemplaces ni borres tu carpeta `.git`.
3. Extrae `Rutitas_PR4_Archivos_Modificados.zip` fuera del repositorio; copia los archivos sobre las mismas rutas de `Rutitas_GitHub` sin borrar carpetas. **Si tienes cambios locales de compañeros, compara diferencias antes de sobrescribir.**
4. Revisa `git diff --check`, `git diff --stat` y `git diff` antes de compilar.
5. Con JDK 17, Android SDK API 36 y emulador listo, en `android/`: `./gradlew lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest connectedDebugAndroidTest`.
6. Revisa especialmente `BookingSafetyTest`, `AccountIsolationTest`, migración 3->4 y los asuntos pendientes en `docs/ANDROID_INTEGRATION_AND_PLAY.md`.
7. **No hagas push ni merge sin pruebas y revisión del responsable.** Al tener resultados, actualiza `memoria.md` con SHA, tests, run y decisiones verificadas. No inventes SHA.

## Advertencias

- No usar usuarios ni documentación reales: DEMO y contraseñas locales no productivas.
- `DemoSeeder.reset()` dejó de borrar registros de otras cuentas; la función «Restaurar cuentas demo» requiere validación funcional, porque no restaura datos de ejemplo cuando existen rutas.
- La visibilidad de rutas propias se basa parcialmente en `ownedByMe`, aún no se filtra por propietario en toda la UI; no está listo para multiusuario.
- No existe prueba de migración desde todas las versiones históricas; solo se añadió migración 3->4.
- No se ejecutaron Gitleaks, GitHub Actions, Play, firma ni backend en esta entrega.
