package com.example.apexhr.features.profile.ui

import com.example.apexhr.R
import com.example.apexhr.features.profile.ui.models.UserLabel
import com.example.apexhr.features.profile.ui.models.userInfoLabels
import org.junit.Assert.assertEquals
import org.junit.Test

class UserLabelsTest {

    private fun position(value: String?) = userInfoLabels(position = value, department = null)

    private fun department(value: String?) = userInfoLabels(position = null, department = value)

    @Test
    fun `employee position is translated`() {
        assertEquals(listOf(UserLabel.Resource(R.string.role_employee)), position("Employee"))
    }

    @Test
    fun `role codes are translated ignoring case and separators`() {
        assertEquals(listOf(UserLabel.Resource(R.string.role_manager)), position("MANAGER"))
        assertEquals(listOf(UserLabel.Resource(R.string.role_admin)), position("admin"))
        assertEquals(listOf(UserLabel.Resource(R.string.role_admin)), position("Administrator"))
        assertEquals(listOf(UserLabel.Resource(R.string.role_chief)), position("super_admin"))
        assertEquals(listOf(UserLabel.Resource(R.string.role_chief)), position("Super Admin"))
    }

    @Test
    fun `all web roles have russian names`() {
        val expected = mapOf(
            "HR" to R.string.role_hr,
            "HumanResources" to R.string.role_hr,
            "Merchandiser" to R.string.role_merchandiser,
            "support_operator" to R.string.role_support,
            "Support" to R.string.role_support,
            "helpdesk" to R.string.role_support,
            "Manager" to R.string.role_manager,
            "SuperAdmin" to R.string.role_chief,
            "chief" to R.string.role_chief,
        )
        expected.forEach { (code, res) ->
            assertEquals(code, listOf(UserLabel.Resource(res)), position(code))
        }
    }

    @Test
    fun `unassigned depends on field`() {
        assertEquals(listOf(UserLabel.Resource(R.string.profile_position_unassigned)), position("Unassigned"))
        assertEquals(listOf(UserLabel.Resource(R.string.profile_department_unassigned)), department(" unassigned "))
    }

    @Test
    fun `department does not use position translations`() {
        assertEquals(listOf(UserLabel.Text("Employee")), department("Employee"))
    }

    @Test
    fun `russian and unknown values are kept as is`() {
        assertEquals(listOf(UserLabel.Text("Сотрудник")), position("Сотрудник"))
        assertEquals(listOf(UserLabel.Text("Тестовый департамент")), department("Тестовый департамент"))
        assertEquals(listOf(UserLabel.Text("Team Lead")), position(" Team Lead "))
    }

    @Test
    fun `blank parts are skipped`() {
        assertEquals(emptyList<UserLabel>(), userInfoLabels(position = " ", department = null))
        assertEquals(
            listOf(
                UserLabel.Resource(R.string.role_employee),
                UserLabel.Resource(R.string.profile_department_unassigned),
            ),
            userInfoLabels(position = "Employee", department = "Unassigned"),
        )
    }
}
