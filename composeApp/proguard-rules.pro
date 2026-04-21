# Evita que R8 borre el puente entre la App y el modulo Compose
-keep class org.salestrack.app.AppKt { *; }

# Protege todas las funciones Composable (necesario para el runtime de Compose)
-keepnames class androidx.compose.runtime.Composer { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
    @androidx.compose.runtime.ReadOnlyComposable <methods>;
}