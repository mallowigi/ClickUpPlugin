package com.mallowigi.clickup.api

import com.mallowigi.clickup.api.model.ClickUpList
import com.mallowigi.clickup.api.model.Space
import com.mallowigi.clickup.api.model.Team
import org.junit.Assert.*
import org.junit.Test

/** Parsing tests for the ClickUp models using the production [ClickUpJson] config. */
class ClickUpJsonTest {

  @Test
  fun `ignores unknown keys`() {
    val team = ClickUpJson.decodeFromString(
      Team.serializer(),
      """{"id":"1","name":"W","members":[{"id":9}],"unknown":true}""",
    )
    assertEquals("1", team.id)
    assertNull(team.color)
  }

  @Test
  fun `maps private serial name`() {
    val space = ClickUpJson.decodeFromString(Space.serializer(), """{"id":"s","name":"S","private":true}""")
    assertTrue(space.isPrivate)
  }

  @Test
  fun `defaults private to false when absent`() {
    val space = ClickUpJson.decodeFromString(Space.serializer(), """{"id":"s","name":"S"}""")
    assertFalse(space.isPrivate)
  }

  @Test
  fun `maps task_count serial name`() {
    val list = ClickUpJson.decodeFromString(ClickUpList.serializer(), """{"id":"l","name":"L","task_count":7}""")
    assertEquals(7, list.taskCount)
  }

  @Test
  fun `coerces null task_count to null default`() {
    val list = ClickUpJson.decodeFromString(ClickUpList.serializer(), """{"id":"l","name":"L","task_count":null}""")
    assertNull(list.taskCount)
  }
}
