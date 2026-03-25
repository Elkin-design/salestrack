# 🎯 GUÍA RÁPIDA - SalesTrack

## ¿Qué es SalesTrack?

Una **aplicación multiplatforma** (Android, iOS, Desktop) para gestión de ventas con:
- 📊 Dashboard de KPIs
- 🛒 Gestión de ventas
- 📦 Control de inventario
- 📈 Generación de reportes
- 👥 Gestión del equipo
- ⚙️ Configuración y preferencias

---

## 🏗️ Arquitectura en 30 segundos

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                   │
│              (UI - Composables + ViewModel)             │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                    DOMAIN LAYER                         │
│          (Use Cases + Repositories Interface)           │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                     DATA LAYER                          │
│              (Repository Impl + Data Sources)           │
└─────────────────────────────────────────────────────────┘

 Direction: Presentation → Domain ← Data (Dependency Inversion)
```

---

## 📱 Estructura Visual de la App

### Pantalla Principal (con Navegación)

```
┌─────────────────────────────────────┐
│          SCREEN CONTENT             │
│   (Varía según pestaña seleccionada)│
│                                     │
│                                     │
│                                     │
│                                     │
│                                     │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│  📊    🛒    📦    📈    👥    ⚙️   │
│ Selected icon is highlighted        │
│ Labels only show when selected      │
└─────────────────────────────────────┘
```

### Estados de la Navegación

```
HOVER/SELECTED:
┌──────────┐
│   📊    │
│Dashboard│
└──────────┘

NOT SELECTED:
┌──────────┐
│   🛒    │
└──────────┘
```

---

## 🗂️ Dónde está cada cosa

### Quiero entender la arquitectura
→ Leer **DEVELOPMENT.md**

### Quiero ver el plan de desarrollo
→ Leer **PROJECT_ROADMAP.md**

### Quiero ver qué se hizo esta sesión
→ Leer **SESSION_SUMMARY.md**

### Quiero compilar la app
```bash
cd SalesTrack
./gradlew compileDebugKotlinAndroid
```

### Quiero agregar una nueva pantalla
→ Seguir template en **PROJECT_ROADMAP.md** → "Quick Start para Próxima Fase"

---

## 📂 Estructura de Carpetas Importante

```
SalesTrack/
├── composeApp/src/
│   ├── commonMain/           ← Código compartido (Android + iOS + Desktop)
│   │   └── kotlin/org/salestrack/app/
│   │       ├── core/         ← Design system, utilities
│   │       ├── domain/       ← Lógica de negocio
│   │       ├── data/         ← Fuentes de datos
│   │       └── presentation/ ← UI
│   ├── androidMain/          ← Código específico Android
│   ├── iosMain/              ← Código específico iOS
│   └── jvmMain/              ← Código específico Desktop
├── DEVELOPMENT.md            ← Documentación técnica
├── PROJECT_ROADMAP.md        ← Plan visual
└── SESSION_SUMMARY.md        ← Resumen de esta sesión
```

---

## 🧩 Componentes Principales

### 1. **App.kt** - Punto de entrada
```kotlin
@Composable
fun App() {
    SalesTrackTheme {
        AppNavHost()  // Navegación
    }
}
```

### 2. **NavGraph.kt** - Navegación
- Define 6 destinos
- Renderiza PlaceholderScreen según destino
- Usa emojis para iconos

### 3. **PlaceholderScreen.kt** - Pantalla temporal
- UI elegante y profesional
- Emoji centrado
- Jerarquía visual clara

### 4. **Theme.kt** - Colores y estilos
- Modo claro/oscuro
- Colores Material 3
- Tipografía centralizada

---

## 🧪 Próximas Features (Roadmap)

```
Semana 1: ✅ Fundación          [COMPLETADO]
Semana 2: 🔜 Autenticación (mock)
Semana 3: 📊 Dashboard
Semana 4: 🛒 Gestión de Ventas
Semana 5: 📦 Inventario
Semana 6: 📈 Reportes
Semana 7: 👥 Equipo
Semana 8: ⚙️  Configuración
Semana 9-10: Tests + Polish
```

---

## 💻 Para Desarrolladores

### Agregar nueva pantalla (Fase 2 - Auth)

1. **Crear carpeta**
   ```
   presentation/auth/
   ├── screen/
   ├── viewmodel/
   └── state/
   ```

2. **Crear archivos**
   ```
   LoginScreen.kt          → Composable UI
   AuthViewModel.kt        → Lógica y estado
   AuthUiState.kt          → Estados posibles
   AuthUiEvent.kt          → Eventos del usuario
   ```

3. **Agregar a NavGraph**
   ```kotlin
   AppDestination.Auth -> LoginScreen()
   ```

4. **Escribir tests**
   ```
   commonTest/presentation/auth/
   └── AuthViewModelTest.kt
   ```

### Template ViewModel

```kotlin
class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.Login -> login(event.email, event.password)
        }
    }
}
```

---

## 🎨 Colores y Tipografía

### Usar desde código

```kotlin
// Colores
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.error
MaterialTheme.colorScheme.background

// Tipografía
MaterialTheme.typography.headlineMedium  // Títulos
MaterialTheme.typography.bodyMedium      // Cuerpo
MaterialTheme.typography.labelSmall      // Etiquetas
```

### Paleta de colores

```
PRIMARY BLUE:     #2563EB (light) / #60A5FA (dark)
SECONDARY CYAN:   #0EA5E9 (light) / #38BDF8 (dark)
ERROR RED:        #DC2626 (light) / #F87171 (dark)
SUCCESS GREEN:    #10B981 (light) / #34D399 (dark)
```

---

## 🔍 Debugging

### Ver logs de compilación
```bash
./gradlew compileDebugKotlinAndroid --info
```

### Limpiar build
```bash
./gradlew clean
```

### Ver estructura de proyecto
```bash
./gradlew projects
```

---

## 📚 Recursos Útiles

| Recurso | URL |
|---------|-----|
| Kotlin Multiplatform | https://kotlinlang.org/docs/multiplatform.html |
| Compose | https://www.jetbrains.com/help/compose-multiplatform/ |
| MVVM Pattern | https://wikipedia.org/wiki/MVVM |
| Clean Architecture | https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html |

---

## ⚡ Quick Checklist para Nueva Feature

- [ ] Crear carpeta `feature/`
- [ ] Crear `Screen.kt` (UI)
- [ ] Crear `ViewModel.kt` (Lógica)
- [ ] Crear `UiState.kt`, `UiEvent.kt`, `UiEffect.kt` (Estados)
- [ ] Crear Repository interface en `domain/`
- [ ] Crear RepositoryImpl en `data/`
- [ ] Crear UseCase en `domain/usecase/`
- [ ] Agregar a `NavGraph.kt`
- [ ] Crear `_Test.kt` para tests
- [ ] Verificar compilación: `./gradlew build`
- [ ] Hacer commit con mensaje clear
- [ ] Hacer push a main

---

## 🆘 Problemas Comunes

### Q: ¿Dónde está el icono de X?
**A:** Se usan emojis en lugar de iconos. Busca en `NavGraph.kt` los emojis usados.

### Q: ¿Por qué la pantalla se ve igual?
**A:** Todas las pantallas usan `PlaceholderScreen` temporalmente. Reemplázalas con tu propia Screen.

### Q: ¿Cómo cambio los colores?
**A:** Edita `core/designsystem/Theme.kt` - LightColors y DarkColors.

### Q: ¿Cómo agrego dependencias?
**A:** Usa `gradle/libs.versions.toml` y `composeApp/build.gradle.kts`

### Q: ¿Por qué no compila en iOS?
**A:** Requiere AGP 9.0+. Ver `DEVELOPMENT.md` para detalles.

---

## 📞 Próximas Acciones

1. ✅ Leer **SESSION_SUMMARY.md** - Lo que se hizo
2. ✅ Leer **DEVELOPMENT.md** - Arquitectura
3. ✅ Leer **PROJECT_ROADMAP.md** - Plan completo
4. 🔜 Implementar Fase 1 - Autenticación
5. 🔜 Agregar tests
6. 🔜 Crear nuevas features

---

## 📊 Progress

```
████████░░░░░░░░░░░ Fase 0 (Fundación)     100% ✅
░░░░░░░░░░░░░░░░░░░ Fase 1 (Auth)         0%
░░░░░░░░░░░░░░░░░░░ Fase 2 (Dashboard)    0%
░░░░░░░░░░░░░░░░░░░ Fase 3-7 (Features)   0%
░░░░░░░░░░░░░░░░░░░ Fase 8 (Testing)      0%
░░░░░░░░░░░░░░░░░░░ Fase 9 (Deploy)       0%

Overall: ▓▓▓▓▓░░░░░ 10% (Fase 0 de 10 completada)
```

---

**Última actualización**: 24 de Marzo 2026  
**Nivel**: Beginner-Friendly Guide  
**Estado**: Activo ✅

Para dudas técnicas, consulta **DEVELOPMENT.md**  
Para detalles del plan, consulta **PROJECT_ROADMAP.md**  

