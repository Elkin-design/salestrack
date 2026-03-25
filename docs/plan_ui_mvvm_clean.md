# 📱 Plan UI-First - SalesTrack (Compose Multiplatform)

---

## 1. 🎯 Objetivo

Construir la app en `composeApp/src/commonMain` siguiendo:

- MVVM
- Clean Architecture
- SOLID
- Clean Code
- Diseño orientado a testabilidad desde el inicio

**Enfoque inicial:** interfaces gráficas completas y navegables con datos mock.

**Principio rector (DRS v3.0):** PARIDAD TOTAL DE FUNCIONALIDADES — todo lo que el usuario puede hacer en Android debe poder hacerlo exactamente igual en Desktop, y viceversa. Las diferencias entre plataformas son ÚNICAMENTE de interacción (táctil vs teclado/mouse) y de integración con el SO, NUNCA de capacidades funcionales.

---

## 2. 🧱 Principios obligatorios

1. La UI no contiene lógica de negocio.
2. ViewModel expone `UiState` y recibe `UiEvent`.
3. Casos de uso en dominio orquestan reglas de negocio.
4. Repositorios definidos por interfaces en dominio.
5. Implementaciones concretas en data.
6. Dependencias apuntan hacia adentro (`presentation -> domain <- data`).
7. Toda dependencia externa debe abstraerse.
8. Cada feature debe incluir pruebas mínimas desde el inicio.
9. Toda pantalla debe existir en Android **y** Desktop con paridad funcional total.

---

## 3. 🗂️ Estructura base (commonMain)

```text
org.salestrack.app/
  core/
    designsystem/
    navigation/
    result/
    utils/
    dispatcher/
    testing/
  domain/
    model/
    repository/
    usecase/
    error/
  data/
    source/
    repository/
    mapper/
    mock/
  presentation/
    app/
    feature/
      auth/
      dashboard/
      sales/
      inventory/
      reports/
      team/
      settings/
```

---

## 4. 🚀 Fases de desarrollo UI-First

### 🔹 Fase 1 — Auth + Navegación base

**Pantallas:**
- `SplashScreen` — verificación de sesión activa
- `LoginScreen` — email/password + Google Sign-In (RF-002, RF-003)
- `RegisterScreen` — registro con validación de fortaleza de contraseña (RF-001)
- `ForgotPasswordScreen` — envío de correo de restablecimiento (RF-004)

**Flujos:**
- Sesión persistente con token Firebase; navegación automática al dashboard si hay sesión activa (RF-002)
- Cierre de sesión desde menú de perfil con limpieza de caché local (RF-005)

**Diferencias de interacción por plataforma:**
- Android: teclado táctil, botón de Google Sign-In estándar
- Desktop: foco automático en campo email, Tab entre campos, Enter para confirmar

**Tests mínimos:**
- `LoginUseCase`: credenciales válidas, credenciales inválidas, error de red
- `RegisterUseCase`: email duplicado, contraseña débil, registro exitoso
- `ViewModel`: estado inicial, loading, error de login, login exitoso

---

### 🔹 Fase 2 — Dashboard + Registro de ventas

**Pantallas:**
- `DashboardScreen` — total vendido hoy, número de transacciones, producto más vendido, estado de sincronización (RF-018, RF-020)
- `SaleListScreen` — listado cronológico con búsqueda y filtro por categoría (RF-019, RF-021)
- `AddSaleSheet/Modal` — formulario de venta rápida con autocompletado (RF-008, RF-009)
- `EditSaleScreen` — edición de campos con historial de cambios (RF-010)
- `SaleDetailScreen` — detalle con timestamp UTC + hora local (RF-012)

**Flujos:**
- Android: FAB (+) → bottom sheet con autocompletado de producto, controles +/- de cantidad, cálculo de total en tiempo real
- Desktop: Ctrl+N → modal centrado con foco en campo Producto, navegación con Tab, Enter para guardar
- Descuentos y devoluciones con recálculo de total neto (RF-014)
- Escaneo de código de barras: Android vía cámara ML Kit, Desktop vía lector HID USB/Bluetooth (RF-015)
- Importación masiva por CSV: Android vía selector de archivos del SO, Desktop vía diálogo de apertura (RF-016)
- Eliminación con soft-delete y diálogo de confirmación (RF-011)

**Diferencias de interacción por plataforma:**
- Android: gestos swipe para editar/eliminar, categorías como chips seleccionables
- Desktop: click derecho o botón en tabla, atajos Ctrl+N / Enter / Esc / Tab (RF-017)

**Tests mínimos:**
- `AddSaleUseCase`: happy path, producto sin stock, precio inválido
- `EditSaleUseCase`: modificación válida, restricción por rol
- `ImportSalesCsvUseCase`: CSV válido 100 filas, CSV con errores de formato, archivo vacío
- `DashboardViewModel`: estado inicial, actualización en tiempo real, error de carga

---

### 🔹 Fase 3 — Reportes y estadísticas

**Pantallas:**
- `DailyReportScreen` — total, número de ventas, promedio, desglose por categoría, lista de transacciones, navegación entre días (RF-023)
- `WeeklyReportScreen` — gráfica de barras, total semanal, comparación vs semana anterior (RF-024)
- `MonthlyReportScreen` — agrupación por semanas, mes pico, comparación vs mes anterior (RF-025)
- `AnnualReportScreen` — agrupación por meses, mes pico, comparación vs año anterior (RF-026)
- `CustomRangeReportScreen` — selector de rango arbitrario fecha-desde / fecha-hasta (RF-027)
- `ReportFilterScreen` — filtro por una o varias categorías, persistente durante sesión (RF-029)

**Flujos:**
- Gráficas interactivas: Android con tap para ver detalle del día, Desktop con hover para tooltip y zoom (RF-028)
- Sección de producto más vendido por volumen y por valor en todos los reportes (RF-030)
- Tabla con columnas ordenables adaptada a cada plataforma (RF-031)
- Modo pantalla completa: Android landscape expandido, Desktop ventana maximizada sin barras de herramienta (RF-032)

**Diferencias de interacción por plataforma:**
- Android: gráficas compactas con tap para detalle, filtros como chips
- Desktop: gráficas expandidas con hover/tooltip/zoom, filtros como checkboxes o dropdown, tabla con ordenamiento por clic en cabecera

**Tests mínimos:**
- `GetDailyReportUseCase`: día con ventas, día sin ventas, día con filtro de categoría
- `GetWeeklyReportUseCase`: cálculo correcto de variación porcentual, semana sin ventas previas
- `GetCustomRangeReportUseCase`: rango válido, rango invertido (desde > hasta), rango con 10,000 registros en < 5 s
- `ReportViewModel`: cambio de periodo, aplicación de filtros, estado vacío

---

### 🔹 Fase 4 — Equipo + Roles

**Pantallas:**
- `TeamOverviewScreen` — ranking de vendedores con total vendido, número de transacciones y ticket promedio (RF-034, RF-035)
- `TeamMemberDetailScreen` — detalle de ventas de un vendedor específico con filtro de categoría (RF-038)
- `ConsolidatedTeamReportScreen` — reporte sumado de todos los miembros con columna de vendedor (RF-036)
- `ManageTeamScreen` — invitar miembro por correo, editar roles, eliminar miembros (RF-033, RF-061)
- `RolePermissionsScreen` — visualización de permisos por rol (Administrador / Supervisor / Vendedor) (RF-037)

**Flujos:**
- Roles:
    - **Administrador**: acceso total + gestión del equipo
    - **Supervisor**: ver reportes del equipo, sin gestión de usuarios
    - **Vendedor**: solo sus propias ventas
- Exportación del reporte consolidado del equipo a PDF y Excel con columna de vendedor incluida (RF-054)
- Filtro por vendedor en cualquier reporte del equipo (RF-038)

**Tests mínimos:**
- `GetTeamSalesUseCase`: varios vendedores, vendedor sin ventas, periodo sin registros
- `InviteMemberUseCase`: correo válido, correo ya miembro, rol inválido
- `TeamViewModel`: estado inicial admin, restricciones supervisor, restricciones vendedor
- Permisos por rol: vendedor intenta acceder a vista de equipo (debe denegar)

---

### 🔹 Fase 5 — Catálogo e inventario

**Pantallas:**
- `ProductCatalogScreen` — lista con búsqueda por nombre/categoría/código de barras, tabla con ordenamiento en Desktop (RF-046)
- `AddProductScreen` — alta con nombre, descripción, precio, unidad, código de barras (opcional), categoría, stock inicial y umbral mínimo (RF-039, RF-045)
- `EditProductScreen` — edición; Desktop permite edición inline en tabla (RF-040)
- `StockAdjustmentScreen` — ajuste manual (entrada / inventario físico / pérdida) con motivo obligatorio (RF-042)
- `StockMovementHistoryScreen` — historial completo: ventas, ajustes, devoluciones con columnas fecha/tipo/cantidad/motivo/vendedor/plataforma (RF-043)

**Flujos:**
- Descuento automático de stock al registrar venta de producto del catálogo, reflejado en tiempo real en todos los dispositivos (RF-041)
- Alerta de stock bajo: Android FCM + notificación local, Desktop notificación nativa del SO (toast Windows / banner macOS / notify-send Linux) (RF-044)
- Importar catálogo por CSV (RF-047); exportar catálogo a Excel o CSV (RF-048)
- Al registrar una devolución, el stock se ajusta automáticamente si el producto está en catálogo (RF-014)

**Tests mínimos:**
- `DeductStockUseCase`: stock suficiente, stock insuficiente, producto sin inventario vinculado
- `AdjustStockUseCase`: ajuste positivo, ajuste negativo a cero, motivo vacío debe fallar
- `StockAlertUseCase`: stock cae al umbral exacto, stock cae por debajo, stock suficiente (no alerta)
- `ImportCatalogCsvUseCase`: 200 productos válidos, campo precio faltante, código de barras duplicado

---

### 🔹 Fase 6 — Exportación + Configuración

**Pantallas:**
- `ExportReportScreen` — selector de formato (PDF / Excel / CSV) y destino (RF-049, RF-050, RF-051, RF-052)
- `PrintScreen` — impresión directa: Android Print Framework WiFi/BT, Desktop Java Print API (RF-053)
- `BackupScreen` — exportación completa JSON + Excel de todos los datos históricos (RF-059)
- `SettingsScreen` — moneda y formato numérico, zona horaria, tema visual, tamaño de fuente (Desktop) (RF-055, RF-056, RF-058)
- `CategoryManagementScreen` — alta, edición y eliminación de categorías personalizadas, sincronizadas en tiempo real (RF-057)
- `NotificationSettingsScreen` — recordatorio diario configurable con hora, Android FCM + local, Desktop nativo (RF-060)

**Flujos:**
- PDF incluye: logo del negocio, periodo, tabla de transacciones con vendedor (si es reporte de equipo), totales y gráficas (RF-049, CA-013)
- Excel incluye hoja Resumen + hoja Detalle, compatible con Microsoft Excel y Google Sheets (RF-050, CA-014)
- Destino de exportación: Android vía cuadro de compartir del SO (WhatsApp, email, Drive, etc.) + guardar local; Desktop vía diálogo nativo Guardar como (RF-052)
- Los cambios de configuración se sincronizan entre todos los dispositivos del usuario en tiempo real (RF-006)

**Tests mínimos:**
- `ExportPdfUseCase`: reporte individual, reporte de equipo con columna vendedor, reporte vacío
- `ExportExcelUseCase`: 10,000 filas en < 10 s, hoja Resumen generada correctamente
- `BackupUseCase`: incluye 100% de ventas y movimientos, archivo válido
- Persistencia de configuración: moneda, zona horaria, tema

---

## 5. ✅ Definición de terminado (DoD)

Cada pantalla debe incluir:

- UiState, UiEvent, UiEffect
- ViewModel + Screen
- Previews de estados principales
- Estados: loading, error, vacío y contenido
- Navegación funcional entre pantallas
- Sin lógica de negocio en composables
- Funciones pequeñas y responsabilidad única
- Versión Android y versión Desktop con paridad funcional total

**Testing mínimo:**
- UseCase: mínimo 3 pruebas (happy path, error, borde)
- ViewModel: mínimo 4 pruebas (inicial, success, error, evento inválido/repetido)
- Bug corregido = test de regresión agregado

---

## 6. 🧠 Reglas de calidad

- Aplicar SOLID por capa
- Funciones cortas y descriptivas
- Evitar estados mutables compartidos sin control
- Manejo de errores con Result/Either
- Tests desde primeras features
- Dominio desacoplado de frameworks
- Inyección por constructor
- Toda diferencia entre plataformas debe ser de interacción o integración con el SO, nunca de capacidad funcional

---

## 7. 🧪 Reglas de testabilidad (obligatorias)

- ❌ Nada de `Dispatchers.IO` directo → usar `DispatcherProvider`
- ❌ Nada de acceso directo a fecha/hora → usar `TimeProvider`
- ❌ Nada de singletons mutables
- ✅ UseCases deterministas
- ✅ Estados de UI inmutables
- ✅ Dependencias por interfaces
- ✅ Mappers con tests dedicados
- ✅ Tests no acoplados a implementación interna

---

## 8. 📊 Estrategia mínima de pruebas

- `commonTest` como base

**Prioridad:**
1. Dominio (UseCases)
2. ViewModels
3. Data (repositorios y mappers)

**Cobertura sugerida:**
- Dominio: >= 80%
- ViewModels: >= 70%
- Data crítica: >= 70%

**Casos críticos de negocio a cubrir obligatoriamente:**
- Registro de venta con descuento automático de stock (CA-010)
- Sincronización multi-dispositivo: venta en Android visible en Desktop en < 5 s (CA-016)
- Modo offline: registro sin internet + sincronización al reconectar en < 30 s (CA-015)
- Control de acceso por roles: vendedor no accede a datos de otros (CA-017)
- Alertas de stock bajo: notificación en < 10 s tras descuento (CA-011)

---

## 9. 🔗 Trazabilidad RF → Feature

| Feature | Requerimientos cubiertos |
|---|---|
| Auth | RF-001 a RF-007 |
| Dashboard + Ventas | RF-008 a RF-022 |
| Reportes | RF-023 a RF-032 |
| Equipo | RF-033 a RF-038 |
| Catálogo e inventario | RF-039 a RF-048 |
| Exportación + Configuración | RF-049 a RF-061 |

---

## 10. 🔍 Checklist por Pull Request

- [ ] Respeta arquitectura por capas
- [ ] No hay lógica de negocio en UI
- [ ] Dependencias inyectadas por constructor
- [ ] Incluye/actualiza tests
- [ ] Manejo de errores explícito
- [ ] Estados UI completos (loading/error/empty/content)
- [ ] Nombres claros y responsabilidades únicas
- [ ] Pantalla implementada en Android **y** Desktop con paridad funcional
- [ ] Diferencias entre plataformas son solo de interacción o integración con el SO

---

## 11. 📐 Diferencias de interacción permitidas por plataforma

| Aspecto de UX | 📱 Android | 🖥️ Desktop |
|---|---|---|
| Navegación principal | Bottom Navigation Bar (5 tabs) | Sidebar lateral siempre visible |
| Registro de venta | FAB (+) flotante → bottom sheet | Ctrl+N → modal centrado o panel derecho |
| Atajos de teclado | No aplica | Ctrl+N nueva venta, Ctrl+E exportar, Ctrl+P imprimir, Ctrl+R reportes |
| Escaneo de código | Cámara del dispositivo (ML Kit) | Lector HID USB/Bluetooth |
| Tablas de datos | Listas deslizables con cards | Tablas con columnas ordenables |
| Gráficas | Compactas con tap para detalle | Expandidas con hover/tooltip/zoom |
| Modo pantalla completa | Rotación landscape | Ventana maximizada sin barras |
| Exportar / compartir | Intent compartir del SO Android | Diálogo Guardar como del SO |
| Imprimir | Android Print Framework (WiFi/BT) | Impresora local o de red del SO |
| Notificaciones | FCM + notificación local | Toast (Windows) / Banner (macOS) / notify-send (Linux) |
| Widget | Widget de pantalla de inicio | No aplica (tooltip en barra de tareas) |
| Tamaño de pantalla | 5" a 12" con layout adaptativo | Redimensionable; mínimo 1024×768 |

---

## 12. 🏁 Criterio de éxito del plan UI-First

El plan se considera exitoso cuando:

- Las pantallas del MVP son navegables y consistentes en Android **y** Desktop
- Cada pantalla tiene paridad funcional total entre plataformas
- La lógica de negocio está en dominio y es reutilizable
- Cada feature crítica tiene cobertura de pruebas
- Se puede integrar backend real sin reescribir UI
- Los 20 criterios de aceptación del DRS v3.0 (CA-001 a CA-020) son verificables con las pantallas mock

---

*Basado en DRS SalesTrack v3.0 — Marzo 2026 — Confidencial, uso interno del equipo de desarrollo.*