package id.almezi.simplemoneytracker_kt.data

object CategorySeed {
    val all = listOf(
        Category("inc_salary", "inc_salary_name", null, true, "pemasukan"),
        Category("inc_freelance", "inc_freelance_name", null, true, "pemasukan"),
        Category("inc_bonus_thr", "inc_bonus_thr_name", null, true, "pemasukan"),
        Category("inc_investment", "inc_investment_name", null, true, "pemasukan"),
        Category("inc_sales", "inc_sales_name", null, true, "pemasukan"),
        Category("inc_refund", "inc_refund_name", null, true, "pemasukan"),
        Category("inc_transfer_in", "inc_transfer_in_name", null, false, "pemasukan"),
        Category("inc_gift", "inc_gift_name", null, true, "pemasukan"),
        Category("inc_other", "inc_other_name", null, true, "pemasukan"),

        Category("exp_finance_investment", "exp_finance_investment_name", "grp_finance", false, "pengeluaran"),
        Category("exp_finance_savings", "exp_finance_savings_name", "grp_finance", false, "pengeluaran"),
        Category("exp_daily_food", "exp_daily_food_name", "grp_daily", true, "pengeluaran"),
        Category("exp_daily_groceries", "exp_daily_groceries_name", "grp_daily", true, "pengeluaran"),
        Category("exp_daily_household", "exp_daily_household_name", "grp_daily", true, "pengeluaran"),
        Category("exp_daily_other", "exp_daily_other_name", "grp_daily", true, "pengeluaran"),
        Category("exp_health_doctor", "exp_health_doctor_name", "grp_health", true, "pengeluaran"),
        Category("exp_health_medicine", "exp_health_medicine_name", "grp_health", true, "pengeluaran"),
        Category("exp_health_gym", "exp_health_gym_name", "grp_health", true, "pengeluaran"),
        Category("exp_health_other", "exp_health_other_name", "grp_health", true, "pengeluaran"),
        Category("exp_lifestyle_clothing", "exp_lifestyle_clothing_name", "grp_lifestyle", true, "pengeluaran"),
        Category("exp_lifestyle_electronics", "exp_lifestyle_electronics_name", "grp_lifestyle", true, "pengeluaran"),
        Category("exp_lifestyle_grooming", "exp_lifestyle_grooming_name", "grp_lifestyle", true, "pengeluaran"),
        Category("exp_lifestyle_other", "exp_lifestyle_other_name", "grp_lifestyle", true, "pengeluaran"),
        Category("exp_transport_fuel", "exp_transport_fuel_name", "grp_transport", true, "pengeluaran"),
        Category("exp_transport_parking", "exp_transport_parking_name", "grp_transport", true, "pengeluaran"),
        Category("exp_transport_public", "exp_transport_public_name", "grp_transport", true, "pengeluaran"),
        Category("exp_transport_travel", "exp_transport_travel_name", "grp_transport", true, "pengeluaran"),
        Category("exp_transport_other", "exp_transport_other_name", "grp_transport", true, "pengeluaran"),
        Category("exp_finance_transfer_out", "exp_finance_transfer_out_name", "grp_finance", true, "pengeluaran"),
        Category("exp_finance_insurance", "exp_finance_insurance_name", "grp_finance", true, "pengeluaran"),
        Category("exp_finance_tax", "exp_finance_tax_name", "grp_finance", true, "pengeluaran"),
        Category("exp_finance_other", "exp_finance_other_name", "grp_finance", true, "pengeluaran"),
        Category("exp_education_course", "exp_education_course_name", "grp_education", true, "pengeluaran"),
        Category("exp_education_books", "exp_education_books_name", "grp_education", true, "pengeluaran"),
        Category("exp_education_other", "exp_education_other_name", "grp_education", true, "pengeluaran"),
        Category("exp_other", "exp_other_name", "grp_other", true, "pengeluaran")
    )
}
