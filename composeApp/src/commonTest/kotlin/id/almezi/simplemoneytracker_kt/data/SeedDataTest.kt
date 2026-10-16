package id.almezi.simplemoneytracker_kt.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType

/**
 * Guards the reference data that `Seeding.kt` writes on first launch.
 *
 * These are invariants, not implementation details. A duplicate id or an
 * expense category without a group would only surface as a wrong row on a
 * user's device, where it is very hard to trace back to the seed list.
 */
class SeedDataTest {

    @Test
    fun `category ids are unique`() {
        val ids = CategorySeed.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "duplicate category id in CategorySeed")
    }

    @Test
    fun `category id prefixes match the category type`() {
        // The UI branches on the prefix to pick an icon and the quick chips,
        // so a mislabelled id would show the wrong affordance.
        for (category in CategorySeed.all) {
            val expected = if (category.type == TransactionType.Income.storageValue) "inc_" else "exp_"
            assertTrue(
                category.id.startsWith(expected),
                "${category.id} does not start with $expected but is a ${category.type}",
            )
        }
    }

    @Test
    fun `every expense category belongs to a group`() {
        // Income categories are ungrouped by design; expenses are not, or the
        // picker renders them outside any section.
        for (category in CategorySeed.all.filter { it.type == TransactionType.Expense.storageValue }) {
            assertTrue(
                !category.groupId.isNullOrBlank(),
                "${category.id} is an expense with no group",
            )
        }
    }

    @Test
    fun `both transaction types are represented`() {
        // Otherwise the add screen has nothing to show for one direction.
        assertTrue(CategorySeed.all.any { it.type == TransactionType.Income.storageValue })
        assertTrue(CategorySeed.all.any { it.type == TransactionType.Expense.storageValue })
    }

    @Test
    fun `every category points at a string key rather than literal text`() {
        // AGENTS.md: no hard-coded UI text. Seeds reference SakuStrings instead.
        for (category in CategorySeed.all) {
            assertTrue(
                category.nameRes.endsWith("_name"),
                "${category.id} name key ${category.nameRes} should end with _name",
            )
        }
    }

    @Test
    fun `quick chip categories exist`() {
        // TransactionForm offers a fixed set of quick chips. Every one must be
        // a real category or the chip opens an empty picker.
        val ids = CategorySeed.all.map { it.id }.toSet()
        val quickChips = listOf(
            "exp_daily_food", "exp_daily_groceries", "exp_transport_fuel",
            "exp_daily_household", "exp_daily_other",
            "inc_salary", "inc_freelance", "inc_bonus_thr", "inc_other",
        )
        for (id in quickChips) {
            assertTrue(id in ids, "quick chip $id is not in CategorySeed")
        }
    }

    @Test
    fun `every group referenced by a category is a known group`() {
        // Guards against a typo in a groupId, which would silently drop the
        // category out of the picker section.
        val groups = CategorySeed.all.mapNotNull { it.groupId }.toSet()
        for (category in CategorySeed.all) {
            val groupId = category.groupId ?: continue
            assertTrue(groupId in groups, "${category.id} references unknown group $groupId")
        }
    }

    @Test
    fun `account ids are unique`() {
        val ids = AccountSeed.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "duplicate account id in AccountSeed")
    }

    @Test
    fun `the default account is part of the seed`() {
        // Backup import and the Tambah screen both fall back to it, so seeding
        // without it would leave transactions pointing at a missing account.
        assertTrue(
            AccountSeed.all.any { it.id == AccountSeed.DEFAULT_ACCOUNT_ID },
            "default account missing from AccountSeed",
        )
    }

    @Test
    fun `accounts are seeded in the declared sort order`() {
        assertEquals(
            AccountSeed.all.map { it.id }.sortedBy { AccountSeed.all.first { a -> a.id == it }.sortOrder },
            AccountSeed.all.map { it.id },
            "AccountSeed.all must already be ordered by sortOrder",
        )
    }

    @Test
    fun `ids do not collide across categories and accounts`() {
        assertTrue(CategorySeed.all.none { it.id == AccountSeed.DEFAULT_ACCOUNT_ID })
    }
}