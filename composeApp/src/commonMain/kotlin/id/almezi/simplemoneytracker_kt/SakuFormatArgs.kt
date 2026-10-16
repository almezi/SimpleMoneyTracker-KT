package id.almezi.simplemoneytracker_kt

/**
 * Positional argument substitution for `SakuStrings` templates.
 *
 * `java.lang.String.format` is JVM-only and does not exist on Kotlin/Native,
 * so the 16 call sites that use `%1$s`-style placeholders go through this
 * instead. Supports the indexed positional forms Saku actually uses
 * (`%1$s`, `%2$d`, ...) plus plain `%s`/`%d`, and literal `%%`.
 *
 * Deliberately minimal: Saku has no locale-sensitive formatting in UI copy, and
 * money goes through `formatRupiah` which does its own dot grouping.
 */
fun formatArgs(template: String, vararg args: Any?): String {
    val out = StringBuilder(template.length)
    var i = 0
    var nextArg = 0
    while (i < template.length) {
        val c = template[i]
        if (c != '%') {
            out.append(c); i++; continue
        }
        if (i + 1 >= template.length) { out.append(c); break }

        when (val n = template[i + 1]) {
            '%' -> { out.append('%'); i += 2 }
            's', 'd' -> {
                out.append(args.getOrNull(nextArg)?.toString() ?: "")
                nextArg++; i += 2
            }
            in '1'..'9' -> {
                // Positional: %N$s or %N$d.
                //
                // The '$' between the index and the conversion is part of Java's
                // syntax and is required: "%1$s" is index 1, string. Reading it
                // as "%1" followed by literal "$s" silently leaves the whole
                // placeholder in the output, so consume it here.
                var j = i + 1
                val num = StringBuilder()
                while (j < template.length && template[j].isDigit()) { num.append(template[j]); j++ }
                if (j < template.length && template[j] == '$') j++
                val conversion = if (j < template.length) template[j] else ' '
                val index = (num.toString().toIntOrNull() ?: 1) - 1
                if (conversion == 's' || conversion == 'd') {
                    out.append(args.getOrNull(index)?.toString() ?: "")
                    j++
                } else {
                    out.append('%'); out.append(num)
                }
                i = j
            }
            else -> { out.append(c); i++ }
        }
    }
    return out.toString()
}

/** `template.format(args)` inside an interpolation, e.g. `"${SakuStrings.x.format(y)}"`. */
fun String.format(args: Any?): String = formatArgs(this, args)

/** Multi-argument overload so `${SakuStrings.x.format(a, b)}` keeps working. */
fun String.format(vararg args: Any?): String = formatArgs(this, *args)
