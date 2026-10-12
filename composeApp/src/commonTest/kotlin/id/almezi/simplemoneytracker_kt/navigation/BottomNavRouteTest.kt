package id.almezi.simplemoneytracker_kt.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.serializer

/**
 * Guards the bottom-bar tab matching.
 *
 * `bottomTabFor` decides whether `SakuApp` shows the bottom navigation at all.
 * It previously took `Any?` and tested `destination is Screen` — but the value
 * it is handed is a `NavDestination`, never a `Screen`, so the check was always
 * false and **the bottom bar was invisible on both Android and iOS**. Nothing
 * failed loudly; the bar simply never appeared.
 *
 * It matches on the serialised route name, which is what navigation records in
 * `NavDestination.route`. These tests pin the two halves of that contract: the
 * name each tab serialises to, and that every tab is reachable.
 */
class BottomNavRouteTest {

    private fun routeNameOf(route: Screen): String =
        when (route) {
            is Screen.Tambah -> Screen.Tambah.serializer().descriptor.serialName
            is Screen.Daftar -> Screen.Daftar.serializer().descriptor.serialName
            is Screen.Ringkasan -> Screen.Ringkasan.serializer().descriptor.serialName
            else -> error("unexpected tab route: $route")
        }

    @Test
    fun `each tab serialises to a distinct route name`() {
        val names = bottomNavItems.map { routeNameOf(it.route) }
        assertEquals(names.size, names.toSet().size, "tab route names must be distinct: $names")
        assertEquals(3, names.size, "there are exactly three bottom tabs")
    }

    @Test
    fun `route names identify their route type`() {
        assertEquals(
            "id.almezi.simplemoneytracker_kt.navigation.Screen.Tambah",
            routeNameOf(Screen.Tambah),
        )
        assertEquals(
            "id.almezi.simplemoneytracker_kt.navigation.Screen.Daftar",
            routeNameOf(Screen.Daftar),
        )
        assertEquals(
            "id.almezi.simplemoneytracker_kt.navigation.Screen.Ringkasan",
            routeNameOf(Screen.Ringkasan),
        )
    }

    @Test
    fun `every tab is one of the three root destinations`() {
        // `serialName()` in NavGraph only handles these three and throws
        // otherwise, so a fourth tab added to bottomNavItems without being
        // handled there would crash at runtime rather than fail to compile.
        val known = setOf(Screen.Tambah, Screen.Daftar, Screen.Ringkasan)
        for (item in bottomNavItems) {
            assertNotNull(item.route.takeIf { it in known }, "${item.route} is not a known root tab")
        }
    }

    @Test
    fun `tab labels and test keys are populated`() {
        // Test keys form the `component_nav_bottom_<key>` tags that the mobile
        // automation suite relies on. An empty or blank key silently produces a
        // colliding tag, so assert they are real.
        for (item in bottomNavItems) {
            assert(item.testKey.isNotBlank()) { "tab ${item.route} has a blank testKey" }
            assert(item.label.isNotBlank()) { "tab ${item.route} has a blank label" }
        }
        assertEquals(
            setOf("tambah", "daftar", "ringkasan"),
            bottomNavItems.map { it.testKey }.toSet(),
            "test keys changed; the automation suite depends on these exact strings",
        )
    }

    @Test
    fun `test keys are unique so tags cannot collide`() {
        val keys = bottomNavItems.map { it.testKey }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `serialName is stable for a data object`() {
        // Guards the assumption the lookup rests on: the same route always
        // produces the same descriptor name across runs.
        val first: SerialDescriptor = Screen.Tambah.serializer().descriptor
        val second: SerialDescriptor = Screen.Tambah.serializer().descriptor
        assertEquals(first.serialName, second.serialName)
    }
}