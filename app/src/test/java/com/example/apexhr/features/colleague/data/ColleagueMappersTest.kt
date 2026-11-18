package com.example.apexhr.features.colleague.data

import com.example.apexhr.features.colleague.data.dto.ColleagueLeaderboardEntryDto
import com.example.apexhr.features.colleague.domain.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ColleagueMappersTest {

    @Test
    fun `role is chosen like in web`() {
        assertEquals(UserRole.Employee, userRoleOf(listOf("Employee"), canAccessAdminPanel = false))
        assertEquals(UserRole.Employee, userRoleOf(null, canAccessAdminPanel = false))
        assertEquals(UserRole.Manager, userRoleOf(listOf("employee", "Manager"), canAccessAdminPanel = false))
        assertEquals(UserRole.Chief, userRoleOf(listOf("Admin", "SuperAdmin"), canAccessAdminPanel = true))
        assertEquals(UserRole.Admin, userRoleOf(listOf("admin"), canAccessAdminPanel = true))
        assertEquals(UserRole.Hr, userRoleOf(listOf("HR"), canAccessAdminPanel = false))
        assertEquals(UserRole.Merchandiser, userRoleOf(listOf("Merchandiser"), canAccessAdminPanel = false))
        assertEquals(UserRole.Support, userRoleOf(listOf("Support_Operator"), canAccessAdminPanel = false))
        assertEquals(UserRole.Admin, userRoleOf(listOf("Employee"), canAccessAdminPanel = true))
    }

    @Test
    fun `score is taken from leaderboard ignoring case`() {
        val board = listOf(
            ColleagueLeaderboardEntryDto(userId = "AAA", thanksCount = 7),
            ColleagueLeaderboardEntryDto(userId = "bbb", thanksCount = 2),
        )

        assertEquals(7L, board.thanksScoreOf("aaa", limit = 200))
    }

    @Test
    fun `missing user has zero only when leaderboard is not cut by limit`() {
        val board = listOf(
            ColleagueLeaderboardEntryDto(userId = "a", thanksCount = 7),
            ColleagueLeaderboardEntryDto(userId = "b", thanksCount = 2),
        )

        assertEquals(0L, board.thanksScoreOf("c", limit = 200))
        assertNull(board.thanksScoreOf("c", limit = 2))
    }
}