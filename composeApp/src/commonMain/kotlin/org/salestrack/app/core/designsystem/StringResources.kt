package org.salestrack.app.core.designsystem

/**
 * Recursos de cadenas centralizados para facilitar la localización futura.
 * Las aplicaciones reales usarían archivos XML de recursos, pero por ahora
 * se mantienen aquí para compatibilidad multiplatforma.
 */
object StringResources {
    // Navegación
    object Navigation {
        const val DASHBOARD = "Dashboard"
        const val SALES = "Ventas"
        const val INVENTORY = "Inventario"
        const val REPORTS = "Reportes"
        const val TEAM = "Equipo"
        const val SETTINGS = "Configuración"
    }
    
    // Descripciones de contenido (para accesibilidad)
    object ContentDescription {
        const val DASHBOARD = "Panel de control"
        const val SALES = "Gestión de ventas"
        const val INVENTORY = "Gestión de inventario"
        const val REPORTS = "Reportes y análisis"
        const val TEAM = "Gestión del equipo"
        const val SETTINGS = "Configuración de la aplicación"
        const val CONSTRUCTION_ICON = "Pantalla en construcción"
    }
    
    // Mensajes generales
    object Messages {
        const val SCREEN_UNDER_CONSTRUCTION = "Pantalla en construcción"
        const val NAVIGATE_INSTRUCTION = "Usa la barra de navegación inferior para acceder a otras secciones"
    }
}

