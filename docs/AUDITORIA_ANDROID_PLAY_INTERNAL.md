# Auditoría del entorno android-play-internal

**Fecha:** 2026-11-12  
**Repositorio:** [rutitasrutitas-eng/Rutitas_App](https://github.com/rutitasrutitas-eng/Rutitas_App)  
**main revisada:** 82eafdc908c8669bffe29d673e5367571761f178  
**PR revisado:** [#4 — integración Android y CI/CD](https://github.com/rutitasrutitas-eng/Rutitas_App/pull/4), abierto, sin fusionar; head 264d4f3c73682900b41ce7c39174ee93dbd2d451.

## Alcance y método

Se inspeccionó GitHub Settings y se recargó la configuración del entorno para verificar persistencia. La página Actions mostró cero secretos de repositorio y cero secretos para el entorno consultado; no se abrió ningún valor. Se leyeron ci.yml, package-release.yml y play-internal.yml en main y en el head del PR #4. No se ejecutaron Actions, workflows de distribución, pruebas ni builds.

## Configuración antes y después

| Control | Antes | Guardado y verificado |
|---|---|---|
| Entorno | android-play-internal ya existía | Conservado |
| Revisores requeridos | Activado; el buscador mostraba “dx”, sin selección confirmada | Activado; se seleccionó DxRxXgSo después de que el usuario confirmara que es el revisor autorizado |
| Impedir autoaprobación | Desactivado | Activado |
| Bypass administrativo | Permitido | Desactivado |
| Ramas y etiquetas | Sin restricción | Solo main (1 rama, 0 etiquetas) |

GitHub ofreció DxRxXgSo como opción seleccionable en el campo de revisores. El usuario confirmó explícitamente esa cuenta. El endpoint de permisos de colaborador de la integración devolvió HTTP 403, por lo que no se afirma que ese endpoint verificara los permisos. La configuración guardada se volvió a abrir y confirmó las reglas descritas.

## Workflows

### main

- ci.yml valida YAML/SDD y ejecuta Gitleaks; el build Android es condicional a la existencia de Gradle Wrapper.
- package-release.yml permite despacho manual, referencia android-release y genera un AAB para revisión. El contenido de main declara que no firma, distribuye ni publica en Play.
- play-internal.yml no existe en main.

### PR #4 (propuesta no fusionada)

- play-internal.yml es manual, exige la cadena SUBIR_A_PRUEBAS_INTERNAS y limita el job a refs/heads/main; usa android-play-internal y declara pista internal con status draft. No se ejecutó.
- package-release.yml propone firmar un AAB y requiere secretos de firma y Maps. No se ejecutó.
- Los workflows del PR son una propuesta mientras el PR permanezca abierto.

## Entornos, secretos y acciones externas

La lista de entornos solo muestra android-play-internal; android-release no aparece, aunque package-release.yml de main y del PR lo referencia. Debe configurarse explícitamente con sus protecciones humanas antes de usar el empaquetado.

La página de secretos informa que no hay secretos de repositorio ni secretos en el entorno consultado. No se revisaron valores. Los secretos requeridos por los workflows del PR #4 no están configurados según esta vista; no se intentó ejecutar la distribución.

En los workflows revisados, checkout, upload-artifact y la imagen de Gitleaks están fijados por SHA/digest. Se mantienen etiquetas mutables en actions/setup-java@v6, android-actions/setup-android@v4, gradle/actions/setup-gradle@v6, reactivecircus/android-emulator-runner@v2 y r0adkll/upload-google-play@v1. Conviene fijar las acciones externas a un SHA completo revisado antes de habilitar una operación sensible.

## Hallazgos y pendientes

1. Protección de Play interna configurada y verificada: revisor confirmado, sin autoaprobación ni bypass, únicamente main.
2. Crear y proteger android-release antes de cualquier uso del workflow de empaquetado.
3. Proveer y validar secretos solo por el mecanismo seguro de GitHub; no incorporarlos a Git.
4. Revisar el PR #4 y sus workflows antes de integrarlos; sigue abierto y sin fusionar.
5. Fijar las acciones externas que aún usan etiquetas mutables.
6. GitHub indica que main no tiene protección de rama. Se registra como observación; esta regla no se modificó.
7. La configuración se comprobó visualmente tras recargar; no se exportó captura a archivo ni se incluyen imágenes.

## Resultado

No se ejecutaron pruebas, builds, Actions ni workflows de distribución. No se aprobó ningún deployment, no se hizo merge y no se publicó en Google Play. Esta auditoría verifica configuración documental/visual; no acredita una ejecución de CI/CD.


## Actualización de CI automática del PR documental — 2026-11-12

La frase anterior “No se ejecutaron Actions” se refiere a que esta auditoría no despachó workflows manuales ni de distribución. La creación/actualización del PR #5 activó automáticamente CI de documentación:

- Run [43332604438](https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43332604438): concluido con éxito para el SHA 7ff6c63125a59c904f1c6fc88fde6ea0dc9ed6e5.
- Documentos, validación YAML y Gitleaks: success.
- Detección de Gradle Wrapper: success.
- Build/lint/tests Android: skipped; main no contiene wrapper/proyecto Android.
- No hubo ejecución de play-internal ni package-release, publicación a Play, aprobación de deployment o merge.


## Actualización de seguridad del repositorio — 2026-11-12

Se completó la protección administrativa de `main` y se configuró `android-release` desde la sesión autorizada de `rutitasrutitas-eng`. La configuración se comprobó después de guardar y recargar GitHub.

- Ruleset activo `Rutitas main: PR review y protección`, aplicable únicamente a `main`; la lista de bypass está vacía.
- Requiere PR, una aprobación, descarta aprobaciones antiguas ante nuevos commits, exige aprobación de una persona distinta de quien hizo el push más reciente y resolución de conversaciones. Bloquea force-push y eliminación.
- Los checks obligatorios quedan desactivados porque los nombres de jobs de `main` y PR #4 no coinciden. Se uniformarán y comprobarán antes de exigirlos.
- `android-release`: revisor requerido `DxRxXgSo`, auto-revisión impedida, bypass administrativo desactivado, una sola rama permitida (`main`) y cero etiquetas. No tiene secretos ni variables configurados.
- `android-play-internal` se volvió a revisar sin editar: conserva a `DxRxXgSo`, impide auto-revisión, no permite bypass y limita a `main` (cero etiquetas).
- La lista de colaboradores confirmó a `DxRxXgSo` como colaborador y persona distinta de la autora de #4 (`america123create`).
- PR [#4](https://github.com/rutitasrutitas-eng/Rutitas_App/pull/4) y [#5](https://github.com/rutitasrutitas-eng/Rutitas_App/pull/5) siguen abiertos y sin fusionar.
- No se ejecutaron workflows de publicación, no se leyeron ni modificaron secretos, y no se cambiaron permisos de colaboradores.

### Jobs y verificación de Actions

En `main` los jobs son `sdd-and-security`, `detect-project` y `android-build` (condicional). El run de PR #4 muestra `Documentos, configuración y escaneo de secretos`, `Kotlin / Android - lint, unit tests, APK y androidTest APK` y `Emulador Android - prueba real de inicio`; sus nombres difieren. En el run documental [43333140707](https://github.com/rutitasrutitas-eng/Rutitas_App/actions/runs/43333140707), SDD/YAML/Gitleaks y detección de Gradle pasaron; el build Android se omitió porque `main` no contiene el proyecto Android.

### Pendientes

Integrar #4 solo después de revisión humana; unificar nombres y condiciones de checks y luego exigirlos; configurar credenciales de firma/Play únicamente en fase autorizada. El cuerpo del PR #5 aún contiene una frase anterior que dice que no se ejecutó Actions; esta actualización aclara que no hubo despacho manual ni distribución, aunque el PR activó CI automático.
