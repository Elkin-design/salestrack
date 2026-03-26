package org.salestrack.app.domain.usecase.team

import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.UserRole
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTeamSalesUseCaseTest {

    private val useCase = GetTeamSalesUseCase()

    @Test
    fun should_rank_members_by_total_sales() {
        val members = listOf(
            TeamMember("U-1", "Ana", "ana@test.com", UserRole.Admin),
            TeamMember("U-2", "Luis", "luis@test.com", UserRole.Seller),
        )
        val sales = listOf(
            Sale("1", "Cafe", "Bebidas", 1, 10_000.0, 0.0, 1_000L, "Ana"),
            Sale("2", "Pan", "Comidas", 1, 5_000.0, 0.0, 1_000L, "Luis"),
        )

        val ranking = useCase(sales, members, category = null)

        assertEquals("Ana", ranking.first().memberName)
        assertEquals(10_000.0, ranking.first().totalSold)
    }

    @Test
    fun should_filter_by_category() {
        val members = listOf(
            TeamMember("U-1", "Ana", "ana@test.com", UserRole.Admin),
        )
        val sales = listOf(
            Sale("1", "Cafe", "Bebidas", 1, 10_000.0, 0.0, 1_000L, "Ana"),
            Sale("2", "Pan", "Comidas", 1, 5_000.0, 0.0, 1_000L, "Ana"),
        )

        val ranking = useCase(sales, members, category = "Bebidas")

        assertEquals(10_000.0, ranking.first().totalSold)
        assertEquals(1, ranking.first().transactionCount)
    }
}

