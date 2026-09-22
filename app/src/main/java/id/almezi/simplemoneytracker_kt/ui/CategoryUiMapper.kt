package id.almezi.simplemoneytracker_kt.ui

import id.almezi.simplemoneytracker_kt.R

fun getCategoryNameRes(id: String): Int {
    return when (id) {
        "inc_salary" -> R.string.cat_inc_salary
        "inc_freelance" -> R.string.cat_inc_freelance
        "inc_bonus_thr" -> R.string.cat_inc_bonus_thr
        "inc_investment" -> R.string.cat_inc_investment
        "inc_sales" -> R.string.cat_inc_sales
        "inc_refund" -> R.string.cat_inc_refund
        "inc_transfer_in" -> R.string.cat_inc_transfer_in
        "inc_gift" -> R.string.cat_inc_gift
        "inc_other" -> R.string.cat_inc_other

        "exp_finance_investment" -> R.string.cat_exp_finance_investment
        "exp_finance_savings" -> R.string.cat_exp_finance_savings
        "exp_daily_food" -> R.string.cat_exp_daily_food
        "exp_daily_groceries" -> R.string.cat_exp_daily_groceries
        "exp_daily_household" -> R.string.cat_exp_daily_household
        "exp_daily_other" -> R.string.cat_exp_daily_other
        "exp_health_doctor" -> R.string.cat_exp_health_doctor
        "exp_health_medicine" -> R.string.cat_exp_health_medicine
        "exp_health_gym" -> R.string.cat_exp_health_gym
        "exp_health_other" -> R.string.cat_exp_health_other
        "exp_lifestyle_clothing" -> R.string.cat_exp_lifestyle_clothing
        "exp_lifestyle_electronics" -> R.string.cat_exp_lifestyle_electronics
        "exp_lifestyle_grooming" -> R.string.cat_exp_lifestyle_grooming
        "exp_lifestyle_other" -> R.string.cat_exp_lifestyle_other
        "exp_transport_fuel" -> R.string.cat_exp_transport_fuel
        "exp_transport_parking" -> R.string.cat_exp_transport_parking
        "exp_transport_public" -> R.string.cat_exp_transport_public
        "exp_transport_travel" -> R.string.cat_exp_transport_travel
        "exp_transport_other" -> R.string.cat_exp_transport_other
        "exp_finance_transfer_out" -> R.string.cat_exp_finance_transfer_out
        "exp_finance_insurance" -> R.string.cat_exp_finance_insurance
        "exp_finance_tax" -> R.string.cat_exp_finance_tax
        "exp_finance_other" -> R.string.cat_exp_finance_other
        "exp_education_course" -> R.string.cat_exp_education_course
        "exp_education_books" -> R.string.cat_exp_education_books
        "exp_education_other" -> R.string.cat_exp_education_other
        "exp_other" -> R.string.cat_exp_other
        else -> R.string.tambah_category_label
    }
}
