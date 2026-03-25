# Plan Visual de Implementación - SalesTrack

## 🎯 Objetivo General
Construir una aplicación multiplatforma (Android, iOS, Desktop) de gestión de ventas con arquitectura MVVM + Clean Architecture + SOLID + Clean Code, siguiendo un enfoque UI-First con datos mock.

---

## 📋 Fases de Implementación

### Fase 0 - ✅ COMPLETADA: Fundación (Semana 1)

```
┌─────────────────────────────────────────┐
│         FUNDACIÓN COMPLETADA            │
├─────────────────────────────────────────┤
│ ✅ Design System completo                │
│ ✅ Navegación multiplatforma             │
│ ✅ Estructura de paquetes                │
│ ✅ TimeProvider multiplataforma          │
│ ✅ Compilación Android exitosa           │
│ ✅ StringResources centralizados         │
└─────────────────────────────────────────┘
```

---

### Fase 1 - 🔜 PRÓXIMA: Autenticación (Semana 2)

#### Funcionalidad
```
┌──────────────────────────────────────────────────────┐
│             AUTH FEATURE (Mock)                      │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌─────────────┐    ┌─────────────┐                │
│  │  Login      │───▶│  Dashboard  │                │
│  │  Screen     │    │  (si OK)    │                │
│  └─────────────┘    └─────────────┘                │
│       │                                             │
│       └──▶ Register (opcional)                      │
│                                                     │
│  Validaciones:                                      │
│  • Email válido (regex)                            │
│  • Password mínimo 6 caracteres                    │
│  • Confirmación de password (Register)             │
│                                                     │
└──────────────────────────────────────────────────────┘
```

#### Estructura
```kotlin
presentation/auth/
  ├── screen/
  │   ├── LoginScreen.kt
  │   ├── RegisterScreen.kt
  │   └── ForgotPasswordScreen.kt
  ├── viewmodel/
  │   └── AuthViewModel.kt
  └── state/
      ├── AuthUiState.kt
      ├── AuthUiEvent.kt
      └── AuthUiEffect.kt

domain/usecase/
  └── auth/
      ├── LoginUseCase.kt
      ├── RegisterUseCase.kt
      └── ValidateEmailUseCase.kt

domain/repository/
  └── AuthRepository.kt (interfaz)

data/repository/
  └── AuthRepositoryImpl.kt (fake)

data/mock/
  └── FakeAuthDataSource.kt
```

#### Criterios de Aceptación
- ✓ Login funcional con credenciales fake (admin/admin123)
- ✓ ViewModel expone UiState observable
- ✓ Manejo de errores con UiEffect
- ✓ Tests unitarios 80%+ coverage
- ✓ Preview de estados (loading, error, success)

---

### Fase 2 - 📊 Dashboard (Semana 3)

#### Vista
```
┌─────────────────────────────────────┐
│   Dashboard                    🏠    │
├─────────────────────────────────────┤
│                                     │
│  ┌──────────┐ ┌──────────────────┐ │
│  │ Ventas   │ │ Clientes Nuevos  │ │
│  │ Hoy:     │ │ Hoy:             │ │
│  │ $5,234   │ │ 12               │ │
│  └──────────┘ └──────────────────┘ │
│                                     │
│  ┌──────────┐ ┌──────────────────┐ │
│  │Órdenes   │ │ Revenue (Gráf)   │ │
│  │Pendientes│ │ [Gráfico Mock]   │ │
│  │ 8        │ │                  │ │
│  └──────────┘ └──────────────────┘ │
│                                     │
│  Últimas Transacciones:             │
│  ┌─────────────────────────────────┐│
│  │ • Venta #1234 - $500  09:30     ││
│  │ • Venta #1235 - $250  10:15     ││
│  │ • Venta #1236 - $1200 11:45     ││
│  └─────────────────────────────────┘│
│                                     │
└─────────────────────────────────────┘
```

#### Componentes a crear
```
• KpiCard.kt (reutilizable)
• LineChart.kt (mock con canvas)
• TransactionItem.kt
• DashboardScreen.kt
• DashboardViewModel.kt
```

---

### Fase 3 - 🛒 Ventas (Semana 4)

#### Vista - Lista de Ventas
```
┌─────────────────────────────────────┐
│   Ventas                       🛒    │
├─────────────────────────────────────┤
│ [Filtros] [+ Nueva]                 │
├─────────────────────────────────────┤
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ #1234 | Cliente X    | $500     │ │
│ │ 2026-03-24 | Completada        │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ #1235 | Cliente Y    | $250     │ │
│ │ 2026-03-24 | Pendiente         │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ #1236 | Cliente Z    | $1200    │ │
│ │ 2026-03-23 | Completada        │ │
│ └─────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

#### Funcionalidades
- ListaVentasScreen + ViewModel
- CrearVentaScreen (formulario)
- DetalleVentaScreen
- Filtros por estado y fecha
- Búsqueda por cliente

---

### Fase 4 - 📦 Inventario (Semana 5)

```
INVENTARIO SCREEN
├── ListaProductos
│   ├── Cantidad en Stock
│   ├── Stock Mínimo (alerta roja si ≤)
│   └── Categoría
├── EstockBajoScreen (alerta destacada)
│   └── Trigger automático si cantidad < mínimo
└── AjusteInventario (+ / - cantidad)
```

---

### Fase 5 - 📈 Reportes (Semana 6)

```
REPORTES SCREEN
├── Filtros
│   ├── Fecha Inicio / Fin
│   ├── Categoría
│   └── Estado
├── Gráficas
│   ├── Ventas por Día (línea)
│   ├── Categorías (pie)
│   └── Top Clientes (barras)
└── Exportar (preparado para PDF/CSV)
```

---

### Fase 6 - 👥 Equipo (Semana 7)

```
EQUIPO SCREEN
├── ListaMiembros
│   ├── Avatar
│   ├── Nombre
│   ├── Rol (Admin, Vendedor, Gerente)
│   └── Estado Online/Offline
├── GestionarRoles
│   └── Cambiar permisos
└── HistorialActividad (quién hizo qué y cuándo)
```

---

### Fase 7 - ⚙️ Configuración (Semana 8)

```
CONFIGURACIÓN SCREEN
├── PerfilUsuario
│   ├── Avatar
│   ├── Nombre y Email
│   └── Teléfono
├── Preferencias
│   ├── Tema (Claro/Oscuro)
│   ├── Idioma (ES/EN)
│   └── Notificaciones
└── Acerca De
    ├── Versión
    └── Créditos
```

---

## 🏗️ Estructura de Carpetas Completa

```
SalesTrack/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/org/salestrack/app/
│   │   │   │   ├── core/
│   │   │   │   │   ├── designsystem/          ✅
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   ├── Typography.kt
│   │   │   │   │   │   ├── Spacing.kt
│   │   │   │   │   │   ├── Shapes.kt
│   │   │   │   │   │   └── StringResources.kt
│   │   │   │   │   ├── utils/
│   │   │   │   │   │   └── TimeProvider.kt   ✅
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── NavGraph.kt       ✅
│   │   │   │   │   ├── result/
│   │   │   │   │   │   └── Result.kt
│   │   │   │   │   └── component/
│   │   │   │   │       ├── KpiCard.kt
│   │   │   │   │       ├── ErrorDialog.kt
│   │   │   │   │       └── LoadingDialog.kt
│   │   │   │   │
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── User.kt
│   │   │   │   │   │   ├── Sale.kt
│   │   │   │   │   │   ├── Product.kt
│   │   │   │   │   │   ├── Report.kt
│   │   │   │   │   │   └── Team.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   │   ├── SalesRepository.kt
│   │   │   │   │   │   ├── InventoryRepository.kt
│   │   │   │   │   │   ├── ReportRepository.kt
│   │   │   │   │   │   └── TeamRepository.kt
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── auth/
│   │   │   │   │       │   ├── LoginUseCase.kt
│   │   │   │   │       │   └── RegisterUseCase.kt
│   │   │   │   │       ├── sales/
│   │   │   │   │       │   ├── GetSalesUseCase.kt
│   │   │   │   │       │   └── CreateSaleUseCase.kt
│   │   │   │   │       ├── inventory/
│   │   │   │   │       │   └── GetInventoryUseCase.kt
│   │   │   │   │       ├── report/
│   │   │   │   │       │   └── GenerateReportUseCase.kt
│   │   │   │   │       └── team/
│   │   │   │   │           └── GetTeamUseCase.kt
│   │   │   │   │
│   │   │   │   ├── data/
│   │   │   │   │   ├── source/
│   │   │   │   │   │   ├── remote/
│   │   │   │   │   │   │   └── ApiService.kt
│   │   │   │   │   │   └── local/
│   │   │   │   │   │       └── Database.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── AuthRepositoryImpl.kt
│   │   │   │   │   │   ├── SalesRepositoryImpl.kt
│   │   │   │   │   │   ├── InventoryRepositoryImpl.kt
│   │   │   │   │   │   ├── ReportRepositoryImpl.kt
│   │   │   │   │   │   └── TeamRepositoryImpl.kt
│   │   │   │   │   ├── mapper/
│   │   │   │   │   │   ├── UserMapper.kt
│   │   │   │   │   │   ├── SaleMapper.kt
│   │   │   │   │   │   ├── ProductMapper.kt
│   │   │   │   │   │   └── TeamMapper.kt
│   │   │   │   │   └── mock/
│   │   │   │   │       ├── FakeAuthDataSource.kt
│   │   │   │   │       ├── FakeSalesDataSource.kt
│   │   │   │   │       ├── FakeInventoryDataSource.kt
│   │   │   │   │       ├── FakeReportDataSource.kt
│   │   │   │   │       └── FakeTeamDataSource.kt
│   │   │   │   │
│   │   │   │   └── presentation/
│   │   │   │       ├── app/                  ✅
│   │   │   │       │   ├── App.kt
│   │   │   │       │   ├── NavGraph.kt
│   │   │   │       │   └── PlaceholderScreen.kt
│   │   │   │       ├── auth/
│   │   │   │       │   ├── screen/
│   │   │   │       │   │   ├── LoginScreen.kt
│   │   │   │       │   │   ├── RegisterScreen.kt
│   │   │   │       │   │   └── ForgotPasswordScreen.kt
│   │   │   │       │   ├── viewmodel/
│   │   │   │       │   │   └── AuthViewModel.kt
│   │   │   │       │   └── state/
│   │   │   │       │       ├── AuthUiState.kt
│   │   │   │       │       ├── AuthUiEvent.kt
│   │   │   │       │       └── AuthUiEffect.kt
│   │   │   │       ├── dashboard/
│   │   │   │       │   ├── screen/
│   │   │   │       │   │   └── DashboardScreen.kt
│   │   │   │       │   ├── viewmodel/
│   │   │   │       │   │   └── DashboardViewModel.kt
│   │   │   │       │   ├── component/
│   │   │   │       │   │   ├── KpiCard.kt
│   │   │   │       │   │   └── TransactionItem.kt
│   │   │   │       │   └── state/
│   │   │   │       │       └── DashboardUiState.kt
│   │   │   │       ├── sales/
│   │   │   │       │   ├── screen/
│   │   │   │       │   │   ├── SalesListScreen.kt
│   │   │   │       │   │   ├── CreateSaleScreen.kt
│   │   │   │       │   │   └── SaleDetailScreen.kt
│   │   │   │       │   ├── viewmodel/
│   │   │   │       │   │   └── SalesViewModel.kt
│   │   │   │       │   └── state/
│   │   │   │       │       └── SalesUiState.kt
│   │   │   │       ├── inventory/
│   │   │   │       │   ├── screen/
│   │   │   │       │   │   ├── InventoryScreen.kt
│   │   │   │       │   │   ├── LowStockScreen.kt
│   │   │   │       │   │   └── AdjustmentScreen.kt
│   │   │   │       │   ├── viewmodel/
│   │   │   │       │   │   └── InventoryViewModel.kt
│   │   │   │       │   └── state/
│   │   │   │       │       └── InventoryUiState.kt
│   │   │   │       ├── reports/
│   │   │   │       │   ├── screen/
│   │   │   │       │   │   └── ReportsScreen.kt
│   │   │   │       │   ├── viewmodel/
│   │   │   │       │   │   └── ReportsViewModel.kt
│   │   │   │       │   ├── component/
│   │   │   │       │   │   ├── LineChart.kt
│   │   │   │       │   │   ├── PieChart.kt
│   │   │   │       │   │   └── BarChart.kt
│   │   │   │       │   └── state/
│   │   │   │       │       └── ReportsUiState.kt
│   │   │   │       ├── team/
│   │   │   │       │   ├── screen/
│   │   │   │       │   │   ├── TeamScreen.kt
│   │   │   │       │   │   └── RoleManagementScreen.kt
│   │   │   │       │   ├── viewmodel/
│   │   │   │       │   │   └── TeamViewModel.kt
│   │   │   │       │   └── state/
│   │   │   │       │       └── TeamUiState.kt
│   │   │   │       └── settings/
│   │   │   │           ├── screen/
│   │   │   │           │   ├── SettingsScreen.kt
│   │   │   │           │   ├── ProfileScreen.kt
│   │   │   │           │   └── PreferencesScreen.kt
│   │   │   │           ├── viewmodel/
│   │   │   │           │   └── SettingsViewModel.kt
│   │   │   │           └── state/
│   │   │   │               └── SettingsUiState.kt
│   │   │   │
│   │   │   ├── composeResources/
│   │   │   └── commonTest/
│   │   │       └── kotlin/
│   │   │           └── org/salestrack/app/
│   │   │               ├── presentation/
│   │   │               │   └── auth/
│   │   │               │       └── AuthViewModelTest.kt
│   │   │               └── domain/
│   │   │                   └── auth/
│   │   │                       └── LoginUseCaseTest.kt
│   │   │
│   │   ├── androidMain/
│   │   │   ├── kotlin/org/salestrack/app/
│   │   │   │   └── core/utils/
│   │   │   │       └── TimeProvider.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/
��   │   │
│   │   ├── iosMain/
│   │   │   └── kotlin/org/salestrack/app/
│   │   │       └── core/utils/
│   │   │           └── TimeProvider.kt
│   │   │
│   │   └── jvmMain/
│   │       └── kotlin/org/salestrack/app/
│   │           └── core/utils/
│   │               └── TimeProvider.kt
│   │
│   ├── build.gradle.kts                    ✅
│   └── composeResources/
│
├── gradle/
│   ├── gradle-wrapper.jar
│   ├── gradle-wrapper.properties
│   └── libs.versions.toml                  ✅
│
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── DEVELOPMENT.md                          ✅ (Nuevo)
├── plan_ui_mvvm_clean.md                   ✅
└── .gitignore
```

---

## 📋 Definición de Terminado (DoD)

### Para cada Pantalla/Feature:

- ✅ **UiState** - Clase data con todos los estados posibles
- ✅ **UiEvent** - Sealed class con eventos del usuario
- ✅ **UiEffect** - Sealed class con efectos secundarios (navegación, snackbar)
- ✅ **ViewModel** - Observable con UiState y receptor de UiEvent
- ✅ **Screen** - Composable que renderiza según UiState
- ✅ **Preview** - Estados principales en Preview (@Preview)
- ✅ **Manejo de errores** - Capa visual para loading, error, empty, content
- ✅ **Nombres claros** - Variables/funciones descriptivas
- ✅ **Tests** - 80%+ coverage de ViewModel y UseCase
- ✅ **Sin lógica en UI** - Todo en ViewModel/UseCase
- ✅ **Responsabilidad única** - Cada función hace una cosa
- ✅ **Reutilizable** - Componentes pueden usarse en múltiples pantallas

---

## 🧪 Testing Roadmap

```
commonTest/
├── presentation/
│   ├── auth/
│   │   └── AuthViewModelTest.kt           (Estados, eventos, efectos)
│   ├── dashboard/
│   │   └── DashboardViewModelTest.kt
│   └── sales/
│       └── SalesViewModelTest.kt
│
└── domain/
    ├── auth/
    │   └── LoginUseCaseTest.kt            (Mock de repository)
    ├── sales/
    │   └── GetSalesUseCaseTest.kt
    └── inventory/
        └── GetInventoryUseCaseTest.kt
```

### Herramientas
- **Kotlin Test** - Testing framework nativo
- **JUnit 4** - Assertions y runners
- **Mockito** - Mockear repositorios
- **Cobertura**: 80%+ en lógica de negocio

---

## 🎨 Guía de UI/UX

### Espaciado (Spacing)
```kotlin
XS = 4.dp    // Muy pequeño
S  = 8.dp    // Pequeño
M  = 16.dp   // Medio
L  = 24.dp   // Grande
XL = 32.dp   // Muy grande
```

### Tipografía
```kotlin
// Títulos principales
fontSize = 28.sp, fontWeight = Bold

// Subtítulos
fontSize = 16.sp, fontWeight = SemiBold

// Cuerpo
fontSize = 14.sp, fontWeight = Normal

// Pequeño
fontSize = 12.sp, fontWeight = Normal
```

### Colores
```kotlin
Primary       = #2563EB (claro) / #60A5FA (oscuro)
Secondary     = #0EA5E9 (claro) / #38BDF8 (oscuro)
Error         = #DC2626 (claro) / #F87171 (oscuro)
Success       = #10B981 (claro) / #34D399 (oscuro)
Warning       = #F59E0B (claro) / #FBBF24 (oscuro)
```

### Componentes Reutilizables
- **KpiCard** - Para mostrar métricas
- **ErrorDialog** - Para errores
- **LoadingDialog** - Para esperas
- **ConfirmDialog** - Para confirmaciones
- **SnackbarMessage** - Para notificaciones

---

## 🚀 Quick Start para Próxima Fase

### 1. Crear estructura Auth
```bash
# Crear directorios
mkdir -p src/commonMain/kotlin/org/salestrack/app/presentation/auth/{screen,viewmodel,state}
mkdir -p src/commonMain/kotlin/org/salestrack/app/domain/repository
mkdir -p src/commonMain/kotlin/org/salestrack/app/domain/usecase/auth
```

### 2. Copiar template de ViewModel
```kotlin
// Template base para copiar y adaptar
class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.Login -> login(event.email, event.password)
            // ...
        }
    }
    
    private fun login(email: String, password: String) {
        // Implementar
    }
}
```

### 3. Tests desde el inicio
```kotlin
// Template para test
@Test
fun testLoginSuccess() {
    val viewModel = AuthViewModel(fakeRepository)
    viewModel.onEvent(AuthUiEvent.Login("admin", "admin123"))
    
    assertTrue(viewModel.uiState.value is AuthUiState.Success)
}
```

---

## 📊 Timeline Propuesto

| Semana | Fase | Horas | Estado |
|--------|------|-------|--------|
| 1 | Fundación | 16 | ✅ Completado |
| 2 | Auth | 12 | 🔜 Próximo |
| 3 | Dashboard | 10 | ⏳ Pendiente |
| 4 | Ventas | 12 | ⏳ Pendiente |
| 5 | Inventario | 8 | ⏳ Pendiente |
| 6 | Reportes | 10 | ⏳ Pendiente |
| 7 | Equipo | 8 | ⏳ Pendiente |
| 8 | Configuración | 6 | ⏳ Pendiente |
| 9 | Polish & Tests | 8 | ⏳ Pendiente |
| 10 | Deploy & Docs | 6 | ⏳ Pendiente |
| **Total** | | **96** | |

---

## ✨ Principios Clave

1. **Una responsabilidad por clase/función**
2. **Datos fluyen hacia abajo, eventos hacia arriba (unidireccional)**
3. **Tests desde el inicio (TDD)**
4. **Componentes reutilizables**
5. **Nombres claros sin abreviaturas**
6. **Documentación en código (KDoc)**
7. **DRY (Don't Repeat Yourself)**
8. **Responsive en todas las plataformas**

---

## 🔗 Referencias

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/help/compose-multiplatform/)
- [MVVM Pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [Clean Code](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)

---

**Última actualización**: 24 de Marzo de 2026  
**Mantenedor**: GitHub Copilot (IA Assistant)  
**Estado**: En Desarrollo ✍️


