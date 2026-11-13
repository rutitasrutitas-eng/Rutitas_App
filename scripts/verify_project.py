#!/usr/bin/env python3
"""Validaciones estáticas rápidas; NO sustituyen Gradle ni pruebas en emulador."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

repo = Path(__file__).resolve().parents[1]
app = repo / "android"
build = (app / "app/build.gradle.kts").read_text()
root_build = (app / "build.gradle.kts").read_text()
manifest = ET.parse(app / "app/src/main/AndroidManifest.xml").getroot()
android = "{http://schemas.android.com/apk/res/android}"
errors = []

def require(ok, message):
    if not ok: errors.append(message)

require((app / "gradlew").is_file(), "Falta Gradle wrapper")
require((app / "gradle/wrapper/gradle-wrapper.jar").is_file(), "Falta JAR del wrapper")
require('gradle-8.13-bin.zip' in (app / 'gradle/wrapper/gradle-wrapper.properties').read_text(), 'Gradle 8.13 esperado')
require('8.13.2' in root_build, 'AGP 8.13.2 esperado')
require(re.search(r'targetSdk\s*=\s*36\b',build), 'Google Play requiere target API 36 en noviembre 2026')
require(re.search(r'compileSdk\s*=\s*36\b',build), 'SDK de compilación 36 esperado')
require('applicationId = "mx.edu.uttt.rutitas"' in build, 'Application ID inesperado')
application = manifest.find('application')
require(application is not None, 'Falta <application>')
if application is not None:
    require(application.get(android+'usesCleartextTraffic') == 'false', 'HTTP inseguro en manifest release')
perms = {e.attrib.get(android+'name') for e in manifest.findall('uses-permission')}
require('android.permission.READ_MEDIA_IMAGES' not in perms,'Permiso amplio de galería innecesario')
require('android.permission.READ_EXTERNAL_STORAGE' not in perms,'Permiso amplio de archivos innecesario')
require('android.permission.POST_NOTIFICATIONS' not in perms, 'Permiso de notificaciones sin funcionalidad')
require((app / 'app/src/debug/AndroidManifest.xml').exists(),'Falta el override HTTP de debug')
require(len(list((app / 'app/src/test').rglob('*Test.kt'))) >= 3,'Faltan pruebas unitarias')
require(len(list((app / 'app/src/androidTest').rglob('*Test.kt'))) >= 1,'Falta prueba instrumentada')
for item in app.rglob('*'):
    if item.is_file() and (item.suffix in {'.jks','.keystore','.pem','.p12'} or item.name == 'local.properties'):
        errors.append('Archivo privado accidental: '+str(item.relative_to(repo)))
if errors:
    for e in errors: print('ERROR:',e,file=sys.stderr)
    sys.exit(1)
print('Verificación estática OK: estructura, API 36, permisos, HTTPS release y pruebas declaradas.')
