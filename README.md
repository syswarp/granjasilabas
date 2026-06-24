# 🦫 Granja de Sílabas

App Android educativa para niños con síndrome de Down.
Desarrollada con amor, siguiendo premisas de psicopedagogía especial.

## Características
- Drag & drop de sílabas sobre líneas
- 15 animales de granja con emoji
- Celebración con confetti y melodía al acertar
- Solo refuerzo positivo: ¡NUNCA se indica error!
- Pantalla de bienvenida con el carpincho mascota 🦫
- Orientación vertical, optimizada para celular y tablet
- Sin login, sin publicidad, arranca directo al juego

## Cómo compilar

### Requisitos
- Android Studio Hedgehog (2023.1) o superior
- JDK 11+
- Android SDK 34

### Pasos
1. Abrí Android Studio
2. File → Open → seleccioná la carpeta `granjasilabas`
3. Esperá que Gradle sincronice las dependencias
4. Conectá un celular Android o iniciá un emulador
5. Presioná ▶ Run

### Compilar APK para instalar manualmente
Build → Build Bundle(s)/APK(s) → Build APK(s)
El APK queda en: `app/build/outputs/apk/debug/app-debug.apk`

## Dependencias principales
- Kotlin 1.9
- AndroidX AppCompat, ConstraintLayout
- Material Components
- Konfetti (lluvia de confetti): nl.dionsegijn:konfetti-xml:2.0.4

## Agregar más animales
Editá el archivo:
`app/src/main/java/com/granjasilabas/app/AnimalData.kt`

Formato:
```kotlin
Animal("NOMBRE", "🐾", listOf("SÍ", "LA", "BA"))
```

## Próximas ideas
- Módulo "ropa en el ropero"
- Sonido de cada animal al aparecer
- Fotos reales en lugar de emoji
- Modo sin internet garantizado

Hecho con ❤️ para chicos especiales.
