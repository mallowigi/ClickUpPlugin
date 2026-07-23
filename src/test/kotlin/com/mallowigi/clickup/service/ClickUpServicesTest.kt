package com.mallowigi.clickup.service

import com.mallowigi.clickup.api.ClickUpHttpTransport
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Offline tests for the per-domain services: verify each hits the correct endpoint path,
 * forwards the token, and unwraps the response payload (with real JSON parsing via the fake).
 */
class ClickUpServicesTest {

  private val transport = FakeClickUpHttpTransport()

  private val teams = object : TeamsService() {
    override val http: ClickUpHttpTransport = transport
    override val tokenProvider: () -> String? = { TOKEN }
  }
  private val spaces = object : SpacesService() {
    override val http: ClickUpHttpTransport = transport
    override val tokenProvider: () -> String? = { TOKEN }
  }
  private val folders = object : FoldersService() {
    override val http: ClickUpHttpTransport = transport
    override val tokenProvider: () -> String? = { TOKEN }
  }
  private val lists = object : ListsService() {
    override val http: ClickUpHttpTransport = transport
    override val tokenProvider: () -> String? = { TOKEN }
  }

  @Test
  fun `getTeams hits team and unwraps teams`() = runBlocking {
    transport.stub("/team", """{"teams":[{"id":"1","name":"Workspace","color":"#fff"}]}""")

    val result = teams.getTeams()

    assertEquals(listOf("/team"), transport.requestedPaths)
    assertEquals(TOKEN, transport.lastToken)
    assertEquals(1, result.size)
    assertEquals("1", result[0].id)
    assertEquals("Workspace", result[0].name)
  }

  @Test
  fun `getSpaces hits team space and unwraps spaces`() = runBlocking {
    transport.stub("/team/42/space", """{"spaces":[{"id":"s1","name":"Space","private":true}]}""")

    val result = spaces.getSpaces("42")

    assertEquals(listOf("/team/42/space"), transport.requestedPaths)
    assertEquals(1, result.size)
    assertEquals("s1", result[0].id)
    assertTrue(result[0].isPrivate)
  }

  @Test
  fun `getFolders hits space folder and unwraps folders`() = runBlocking {
    transport.stub(
      "/space/s1/folder",
      """{"folders":[{"id":"f1","name":"Folder","lists":[{"id":"l1","name":"L"}]}]}""",
    )

    val result = folders.getFolders("s1")

    assertEquals(listOf("/space/s1/folder"), transport.requestedPaths)
    assertEquals("f1", result[0].id)
    assertEquals(1, result[0].lists.size)
  }

  @Test
  fun `getFolderlessLists hits space list and unwraps lists`() = runBlocking {
    transport.stub("/space/s1/list", """{"lists":[{"id":"l1","name":"L","task_count":3}]}""")

    val result = lists.getFolderlessLists("s1")

    assertEquals(listOf("/space/s1/list"), transport.requestedPaths)
    assertEquals("l1", result[0].id)
    assertEquals(3, result[0].taskCount)
  }

  @Test
  fun `getLists hits folder list and unwraps lists`() = runBlocking {
    transport.stub("/folder/f1/list", """{"lists":[{"id":"l1","name":"L"}]}""")

    val result = lists.getLists("f1")

    assertEquals(listOf("/folder/f1/list"), transport.requestedPaths)
    assertEquals("l1", result[0].id)
  }

  @Test
  fun `missing collection decodes to empty list`() = runBlocking {
    transport.stub("/team", """{}""")

    assertTrue(teams.getTeams().isEmpty())
  }

  private companion object {
    const val TOKEN = "pk_test_token"
  }
}
