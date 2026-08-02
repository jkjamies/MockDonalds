package com.jkjamies.sampleplatter.core.strata

/**
 * Observable content with its loading and failure states made explicit.
 *
 * [StrataSubjectInteractor.flow] emits `T` and nothing else, which forces every presenter
 * into `content?.field ?: emptyList()` — where `null` means "still loading" *or* "genuinely
 * empty" *or* "failed", indistinguishably — and lets an exception from `createObservable`
 * propagate straight into composition. [StrataSubjectInteractor.contentState] wraps the
 * same stream in this type so the three cases are separable and a failure is a state rather
 * than a crash.
 *
 * ## iOS interop
 *
 * This type is Kotlin-side only — `core:strata` is not exported to the iOS framework, and
 * sealed *interfaces* do not bridge cleanly to Swift (see
 * `.agents/standards/ios-interop.md`). Presenters must flatten it into primitive `UiState`
 * fields (`isLoading: Boolean`, `errorMessage: String?`) rather than putting it on a
 * `UiState` directly.
 *
 * @see StrataResult for the one-shot (imperative) equivalent used by [StrataInteractor].
 */
public sealed interface StrataContentState<out T> {

    /** No value has arrived yet for the current params. */
    public data object Loading : StrataContentState<Nothing>

    /** The stream produced a value. */
    public data class Content<out T>(public val data: T) : StrataContentState<T>

    /** The stream failed. Terminal for the current params — re-invoke to retry. */
    public data class Error(public val error: StrataException) : StrataContentState<Nothing>
}

/** The value if loaded, else `null`. Prefer an exhaustive `when` where all three cases matter. */
public val <T> StrataContentState<T>.dataOrNull: T?
    get() = when (this) {
        is StrataContentState.Content -> data
        is StrataContentState.Error, StrataContentState.Loading -> null
    }

/** True while waiting for the first value. */
public val StrataContentState<*>.isLoading: Boolean
    get() = this is StrataContentState.Loading

/** The failure if the stream errored, else `null`. */
public val StrataContentState<*>.errorOrNull: StrataException?
    get() = (this as? StrataContentState.Error)?.error

/** Wraps any throwable as a [StrataException], preserving one that already is. */
internal fun Throwable.asStrataException(): StrataException =
    this as? StrataException ?: StrataExecutionException(this)
