package org.salestrack.app.domain.usecase.team

import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.TeamMemberPerformance

class GetTeamSalesUseCase {
    operator fun invoke(
        sales: List<Sale>,
        members: List<TeamMember>,
        category: String?,
    ): List<TeamMemberPerformance> {
        val salesBySeller = sales
            .asSequence()
            .filter { !it.isDeleted }
            .filter { category.isNullOrBlank() || it.category == category }
            .groupBy { it.sellerName }

        return members
            .filter { it.isActive }
            .map { member ->
                val memberSales = salesBySeller[member.fullName].orEmpty()
                val total = memberSales.sumOf { it.netTotal }
                val count = memberSales.size
                TeamMemberPerformance(
                    memberId = member.id,
                    memberName = member.fullName,
                    role = member.role,
                    totalSold = total,
                    transactionCount = count,
                    averageTicket = if (count == 0) 0.0 else total / count,
                )
            }
            .sortedByDescending { it.totalSold }
    }
}

