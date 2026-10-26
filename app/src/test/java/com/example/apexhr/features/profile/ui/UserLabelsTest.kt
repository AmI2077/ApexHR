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
        assertEquals(listOf(UserLabel.Resource(R.string.profile_position_employee)), position("Employee"))
    }

    @Test
    fun `role codes are translated ignoring case and separators`() {
        assertEquals(listOf(UserLabel.Resource(R.string.profile_position_manager)), position("MANAGER"))
        assertEquals(listOf(UserLabel.Resource(R.string.profile_position_admin)), position("admin"))
        assertEquals(listOf(UserLabel.Resource(R.string.profile_position_admin)), position("Administrator"))
        assertEquals(listOf(UserLabel.Resource(R.string.profile_position_superadmin)), position("super_admin"))
        assertEquals(listOf(UserLabel.Resource(R.string.profile_position_superadmin)), position("Super Admin"))
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
                UserLabel.Resource(R.string.profile_position_employee),
                UserLabel.Resource(R.string.profile_department_unassigned),
            ),
            userInfoLabels(position = "Employee", department = "Unassigned"),
        )
    }
}
