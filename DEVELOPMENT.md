# SalesTrack - Aplicación Multiplatforma de Gestión de Ventas

## Estado Actual del Proyecto

### ✅ Completado - Fase 0: Fundación

#### 1. **Estructuras Base**
- ✅ Paquetes organizados en `commonMain` con arquitectura limpia:
  - `core/` - Utilidades compartidas (designsystem, result, utils)
  - `domain/` - Contratos y modelos del negocio
  - `data/` - Implementaciones de repositorios
  - `presentation/` - UI con MVVM

#### 2. **Design System Mejorado**
- ✅ `Theme.kt` - Colores personalizados para modo claro/oscuro
- ✅ `Typography.kt` - Tipografía profesional
- ✅ `Spacing.kt` - Sistema de espaciado consistente
- ✅ `Shapes.kt` - Formas y bordes redondeados
- ✅ `StringResources.kt` - Cadenas centralizadas para navegación y mensajes

#### 3. **Navegación Elegante y Responsiva**
- ✅ `NavGraph.kt` - Navegación multiplatforma con:
  - 6 destinos principales (Dashboard, Ventas, Inventario, Reportes, Equipo, Configuración)
  - Emojis representativos (📊, 🛒, 📦, 📈, 👥, ⚙️)
  - Ocultamiento de etiquetas cuando no están seleccionadas (`alwaysShowLabel = false`)
  - Perfecto para móvil y desktop

#### 4. **UI Mejorada y Profesional**
- ✅ `PlaceholderScreen.kt` - Pantalla temporal elegante con:
  - Emoji grande y representativo
  - Jerarquía visual clara (título, descripción, indicación)
  - Uso completo de colores del tema (Material 3)
  - Responsive layout que funciona en todas las plataformas

#### 5. **Multiplataforma y Testeable**
- ✅ `TimeProvider.kt` (expect/actual)
  - Interfaz común en `commonMain`
  - Implementación Android en `androidMain`
  - Implementación iOS en `iosMain`
  - Implementación JVM en `jvmMain`
  - Permite tests deterministas

#### 6. **Compilación**
- ✅ Compilación exitosa para Android
- ⚠️ iOS requiere ajustes en configuración del proyecto (AGP 9.0)
- ⚠️ JVM/Desktop compilación está en progreso

---

## Arquitectura Implementada

```
SalesTrack/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── core/
│   │   │   │   ├── designsystem/    (Theme, Typography, Shapes, Spacing, StringResources)
│   │   │   │   ├── utils/           (TimeProvider interfaz)
│   │   │   │   └── result/          (Tipos de resultado)
│   │   │   ├── domain/
│   │   │   │   ├── model/           (Entidades del negocio)
│   │   │   │   ├── repository/      (Interfaces de repositorios)
│   │   │   │   └── usecase/         (Casos de uso)
│   │   │   ├── data/
│   │   │   │   ├── source/          (Fuentes de datos)
│   │   │   │   ├── repository/      (Implementaciones)
│   │   │   │   ├── mapper/          (Mapeos de DTO a Domain)
│   │   │   │   └── mock/            (Datos mock para pruebas)
│   │   │   └── presentation/
│   │   │       └── app/
│   │   │           ├── NavGraph.kt  (Navegación principal)
│   │   │           └── PlaceholderScreen.kt (UI temporal)
│   │   ├── androidMain/
│   │   │   └── utils/TimeProvider.kt (Implementación Android)
│   │   ├── iosMain/
│   │   │   └── utils/TimeProvider.kt (Implementación iOS)
│   │   └── jvmMain/
│   │       └── utils/TimeProvider.kt (Implementación JVM)
│   └── build.gradle.kts
└── gradle/
    └── libs.versions.toml
```

---

## Principios Aplicados

### MVVM (Model-View-ViewModel)
- ✅ UI (Composables) → ViewModel → Domain
- ✅ Separación clara de responsabilidades

### Clean Architecture
- ✅ Dependencias apuntan hacia adentro (Presentation → Domain ← Data)
- ✅ Dominio independiente de frameworks
- ✅ Repositorios definidos en dominio, implementados en data

### SOLID
- ✅ **S**ingle Responsibility: Cada composable y clase tiene una responsabilidad
- ✅ **O**pen/Closed: Extensible sin modificar código existente
- ✅ **L**iskov Substitution: Interfaces como TimeProvider
- ✅ **I**nterface Segregation: StringResources agrupados por contexto
- ✅ **D**ependency Inversion: Injección de dependencias preparada

### Clean Code
- ✅ Nombres claros y descriptivos
- ✅ Funciones pequeñas y focalizadas
- ✅ Documentación donde es necesaria
- ✅ Sin duplicación de código

---

## Diseño UI/UX

### Navegación
- **Móvil**: Barra de navegación inferior con emojis, etiquetas ocultas cuando no está seleccionado
- **Desktop**: Misma barra de navegación (adaptable a drawer lateral en futuro)
- **Accesibilidad**: ContentDescription en todos los elementos

### Colores (Material 3)
- **Primary**: Azul moderno (#2563EB claro, #60A5FA oscuro)
- **Secondary**: Cian (#0EA5E9 claro, #38BDF8 oscuro)
- **Error**: Rojo (#DC2626 claro, #F87171 oscuro)
- **Background**: Blanco/Negro según tema
- **Surface**: Superficies prominentes

### Tipografía
- **Títulos**: Bold, 28sp
- **Descripción**: Regular, 16sp, alpha 70%
- **Etiquetas**: Regular, 10sp
- **Instrucciones**: Regular, 13sp, alpha 50%

---

## Próximos Pasos

### Fase 1 - Autenticación (Mock)
1. Crear `auth/` feature con:
   - LoginScreen (email/password)
   - RegisterScreen (registro simple)
   - AuthViewModel + AuthUseCase
   - FakeAuthRepository

2. Añadir navegación con estado autenticado/no autenticado

3. Tests unitarios de ViewModel y UseCase

### Fase 2 - Dashboard
1. KPIs: Ventas del día, Clientes nuevos, Órdenes pendientes
2. Gráficas simples (mock data)
3. Últimas transacciones

### Fase 3 - Ventas
1. ListaVentasScreen con FlatList
2. CrearVentaScreen con formulario
3. DetalleVentaScreen

### Fase 4 - Inventario
1. ListaProductosScreen
2. EstockBajoScreen (alerta visual)
3. AjusteInventarioScreen

### Fase 5 - Reportes
1. Filtros por fecha/rango
2. Gráficas por categoría
3. Exportar reportes

### Fase 6 - Equipo
1. ListaMiembrosScreen
2. GestionarRolesScreen
3. HistorialActividadScreen

### Fase 7 - Configuración
1. PerfilUsuarioScreen
2. PreferenciasScreen
3. AboutScreen

---

## Comandos Útiles

### Compilar Android
```bash
./gradlew compileDebugKotlinAndroid
```

### Compilar Desktop (JVM)
```bash
./gradlew compileKotlinJvm
```

### Compilar iOS
```bash
./gradlew compileKotlinIosSimulatorArm64
```

### Build completo (con advertencias de AGP)
```bash
./gradlew build
```

### Ejecutar linter y verificar
```bash
./gradlew check
```

---

## Dependencias Clave

```toml
# Compose Multiplatform
compose = "1.10.0"

# Kotlin & Coroutines
kotlin = "2.3.0"
kotlinx-coroutines = "1.10.2"
kotlinx-datetime = "0.6.0"

# Android
androidx-lifecycle = "2.9.6"
androidx-activity = "1.12.2"

# Materiales
material3 = "1.10.0-alpha05"
```

---

## Notas Importantes

1. **Iconos**: Se usan emojis en lugar de la librería de iconos extendidos de Material porque no está disponible en Compose Multiplatform 1.10.0
2. **AGP**: El proyecto actual usa AGP 8.11.2, y será necesario migrar a AGP 9.0+ para iOS
3. **Tests**: Framework preparado para Kotlin Test + JUnit4
4. **Mockito**: Integración lista para mockear repositorios

---

## Equipo y Licencia

- **Desarrollador**: IA Assistant (GitHub Copilot)
- **Cliente**: SENA
- **Proyecto**: SalesTrack v1.0
- **Fecha**: 2026

---

## Estado de Build

```
✅ Android: SUCCESS
⚠️ iOS: PENDING (AGP migration)
⚠️ JVM: PENDING
```

Última actualización: marzo 24, 2026

