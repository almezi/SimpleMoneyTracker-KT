# CATEGORIES

Fixed list. IDs are stable and never change; display names live in string resources.
`countsInTotals` false means the category is excluded from all totals (shown in Daftar with the "Tidak dihitung" label).

## Income (9)

| ID | Name (id-ID) | countsInTotals |
|---|---|---|
| inc_salary | Gaji | true |
| inc_freelance | Freelance / Side Income | true |
| inc_bonus_thr | Bonus & THR | true |
| inc_investment | Investasi | true |
| inc_sales | Penjualan | true |
| inc_refund | Refund | true |
| inc_transfer_in | Transfer Masuk | false |
| inc_gift | Hadiah / Pemberian | true |
| inc_other | Lainnya | true |

## Expense (7 groups)

Groups: Kebutuhan sehari-hari, Kesehatan, Lifestyle, Transportasi & perjalanan, Keuangan, Pendidikan & karier, Lainnya.

Group IDs: `grp_daily`, `grp_health`, `grp_lifestyle`, `grp_transport`, `grp_finance`, `grp_education`, `grp_other`.

Categories under each group (about 45 in total, overlapping ones merged), ID format `exp_<group>_<name>`:

| Group | ID | Name (id-ID) | countsInTotals |
|---|---|---|---|
| Keuangan | exp_finance_investment | Investasi | false |
| Keuangan | exp_finance_savings | Tabungan | false |
| (rest) | | PASTE YOUR CLEANED LIST HERE | true |

Each group also needs its own "Lainnya" fallback if your cleaned list has one.

## Rules
- Only Transfer Masuk, Investasi (expense) and Tabungan are excluded from totals
- IDs never change, even if the display name changes
- Tests reference categories by ID, never by name