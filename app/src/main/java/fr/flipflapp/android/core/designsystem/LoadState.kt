package fr.flipflapp.android.core.designsystem

sealed interface LoadState<out T> {
    data object Idle : LoadState<Nothing>
    data object Loading : LoadState<Nothing>
    data class Content<T>(val value: T) : LoadState<T>
    data object Empty : LoadState<Nothing>
    data class Failed(val message: String) : LoadState<Nothing>
}
