package id.almezi.simplemoneytracker_kt.data

object AccountSeed {
    const val DEFAULT_ACCOUNT_ID = "acc_tunai"

    val all = listOf(
        Account(DEFAULT_ACCOUNT_ID, "Tunai", 0),
        Account("acc_bca", "BCA", 1),
        Account("acc_gopay", "GoPay", 2)
    )
}