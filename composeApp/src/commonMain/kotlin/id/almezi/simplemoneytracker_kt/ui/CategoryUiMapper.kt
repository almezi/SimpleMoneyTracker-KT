package id.almezi.simplemoneytracker_kt.ui

import id.almezi.simplemoneytracker_kt.SakuStrings

import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.ui.components.SakuPickerOptionGroup

fun getCategoryName(id: String): String {
    return when (id) {
        "inc_salary" -> SakuStrings.cat_inc_salary
        "inc_freelance" -> SakuStrings.cat_inc_freelance
        "inc_bonus_thr" -> SakuStrings.cat_inc_bonus_thr
        "inc_investment" -> SakuStrings.cat_inc_investment
        "inc_sales" -> SakuStrings.cat_inc_sales
        "inc_refund" -> SakuStrings.cat_inc_refund
        "inc_transfer_in" -> SakuStrings.cat_inc_transfer_in
        "inc_gift" -> SakuStrings.cat_inc_gift
        "inc_other" -> SakuStrings.cat_inc_other

        "exp_finance_investment" -> SakuStrings.cat_exp_finance_investment
        "exp_finance_savings" -> SakuStrings.cat_exp_finance_savings
        "exp_daily_food" -> SakuStrings.cat_exp_daily_food
        "exp_daily_groceries" -> SakuStrings.cat_exp_daily_groceries
        "exp_daily_household" -> SakuStrings.cat_exp_daily_household
        "exp_daily_other" -> SakuStrings.cat_exp_daily_other
        "exp_health_doctor" -> SakuStrings.cat_exp_health_doctor
        "exp_health_medicine" -> SakuStrings.cat_exp_health_medicine
        "exp_health_gym" -> SakuStrings.cat_exp_health_gym
        "exp_health_other" -> SakuStrings.cat_exp_health_other
        "exp_lifestyle_clothing" -> SakuStrings.cat_exp_lifestyle_clothing
        "exp_lifestyle_electronics" -> SakuStrings.cat_exp_lifestyle_electronics
        "exp_lifestyle_grooming" -> SakuStrings.cat_exp_lifestyle_grooming
        "exp_lifestyle_other" -> SakuStrings.cat_exp_lifestyle_other
        "exp_transport_fuel" -> SakuStrings.cat_exp_transport_fuel
        "exp_transport_parking" -> SakuStrings.cat_exp_transport_parking
        "exp_transport_public" -> SakuStrings.cat_exp_transport_public
        "exp_transport_travel" -> SakuStrings.cat_exp_transport_travel
        "exp_transport_other" -> SakuStrings.cat_exp_transport_other
        "exp_finance_transfer_out" -> SakuStrings.cat_exp_finance_transfer_out
        "exp_finance_insurance" -> SakuStrings.cat_exp_finance_insurance
        "exp_finance_tax" -> SakuStrings.cat_exp_finance_tax
        "exp_finance_other" -> SakuStrings.cat_exp_finance_other
        "exp_education_course" -> SakuStrings.cat_exp_education_course
        "exp_education_books" -> SakuStrings.cat_exp_education_books
        "exp_education_other" -> SakuStrings.cat_exp_education_other
        "exp_other" -> SakuStrings.cat_exp_other
        else -> SakuStrings.tambah_category_label
    }
}

fun categoryDisplayName(category: Category): String =
    category.customName ?: getCategoryName(category.id)

fun matchesCategoryQuery(label: String, query: String): Boolean {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return true
    return label.lowercase().contains(needle)
}

fun filterCategoryGroups(
    groups: List<SakuPickerOptionGroup>,
    query: String
): List<SakuPickerOptionGroup> {
    val needle = query.trim()
    if (needle.isEmpty()) return groups
    return groups.mapNotNull { group ->
        val matches = group.options.filter { matchesCategoryQuery(it.label, needle) }
        if (matches.isEmpty()) null else group.copy(options = matches)
    }
}
