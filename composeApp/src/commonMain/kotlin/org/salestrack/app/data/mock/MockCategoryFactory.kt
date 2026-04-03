package org.salestrack.app.data.mock

import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.Category

object MockCategoryFactory {
    fun create(timeProvider: TimeProvider): List<Category> {
        val now = timeProvider.nowMillis()
        return listOf(
            Category(
                id = "C-BEB",
                name = "Bebidas",
                colorHex = "#1E88E5",
                updatedAtMillis = now,
            ),
            Category(
                id = "C-SNK",
                name = "Snacks",
                colorHex = "#F4511E",
                updatedAtMillis = now,
            ),
            Category(
                id = "C-HGR",
                name = "Hogar",
                colorHex = "#43A047",
                updatedAtMillis = now,
            ),
        )
    }
}
