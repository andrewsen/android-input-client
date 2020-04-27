package ua.senko.remoteinput.data

inline fun <T> T.applyIf(cond: Boolean, block: T.() -> Unit): T {
    return if (cond) {
        this.apply(block)
    } else this
}

inline fun <T> T.alsoIf(cond: Boolean, block: (T) -> Unit): T {
    return if (cond) {
        this.also(block)
    } else this
}