package com.mockdonalds.app.core.centerpost

/**
 * Observable content with its loading and failure states made explicit.
 *
 * [CenterPostSubjectInteractor.flow] emits `T` and nothing else, which forces every presenter
 * into `content?.field ?: emptyList()` — where `null` means "still loading" *or* "genuinely
 * empty" *or* "failed", indistinguishably — and lets an exception from `createObservable`
 * propagate straight into composition. [CenterPostSubjectInteractor.contentState] wraps the
 * same stream in this type so the three cases are separable and a failure is a state rather
 * than a crash.
 *
 * ## iOS interop
 *
 * This type is Kotlin-side only — `core:centerpost` is not exported to the iOS framework, and
 * sealed *interfaces* do not bridge cleanly to Swift (see
 * `.agents/standards/ios-interop.md`). Presenters must flatten it into primitive `UiState`
 * fields (`isLoading: Boolean`, `errorMessage: String?`) rather than putting it on a
 * `UiState` directly.
 *
 * @see CenterPostResult for the one-shot (imperative) equivalent used by [CenterPostInteractor].
 */
public sealed interface CenterPostContentState<out T> {

    /** No value has arrived yet for the current params. */
    public data object Loading : CenterPostContentState<Nothing>

    /** The stream produced a value. */
    public data class Content<out T>(public val data: T) : CenterPostContentState<T>

    /** The stream failed. Terminal for the current params — re-invoke to retry. */
    public data class Error(public val error: CenterPostException) : CenterPostContentState<Nothing>
}

/** The value if loaded, else `null`. Prefer an exhaustive `when` where all three cases matter. */
public val <T> CenterPostContentState<T>.dataOrNull: T?
    get() = when (this) {
        is CenterPostContentState.Content -> data
        is CenterPostContentState.Error, CenterPostContentState.Loading -> null
    }

/** True while waiting for the first value. */
public val CenterPostContentState<*>.isLoading: Boolean
    get() = this is CenterPostContentState.Loading

/** The failure if the stream errored, else `null`. */
public val CenterPostContentState<*>.errorOrNull: CenterPostException?
    get() = (this as? CenterPostContentState.Error)?.error

/** Wraps any throwable as a [CenterPostException], preserving one that already is. */
internal fun Throwable.asCenterPostException(): CenterPostException =
    this as? CenterPostException ?: CenterPostExecutionException(this)
