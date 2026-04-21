# 1. Proteger la inicialización de Firebase y DI (Lo que causó el crash)
-keep class org.salestrack.app.core.di.** { *; }

# 2. Proteger todos los ViewModels (R8 los eliminó casi todos)
-keep class * extends androidx.lifecycle.ViewModel
-keep class org.salestrack.app.presentation.feature.**ViewModel { *; }

# 3. Proteger Casos de Uso y Repositorios
# Sin esto, la lógica de negocio desaparece
-keep class org.salestrack.app.domain.usecase.** { *; }
-keep class org.salestrack.app.domain.repository.** { *; }
-keep class org.salestrack.app.data.repository.** { *; }
-keep class org.salestrack.app.data.source.** { *; }

# 4. Proteger Modelos y Serialización
# Si usas Kotlin Serialization, esto es vital para que no fallen los JSON
-keep class org.salestrack.app.domain.model.** { *; }
-keepattributes *Annotation*, InnerClasses, Signature
-keepclassmembers class org.salestrack.app.domain.model.** {
    *** Companion;
    *** $serializer;
}

# 5. Proteger Koin y módulos de dependencias
-keep class org.salestrack.app.presentation.app.di.** { *; }

# 6. Proteger Recursos generados por Compose Multiplatform
-keep class salestrack.composeapp.generated.resources.** { *; }