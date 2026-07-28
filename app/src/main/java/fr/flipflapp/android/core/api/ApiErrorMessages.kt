package fr.flipflapp.android.core.api

fun ApiError.userMessage(): String = when (this) {
    ApiError.Unauthorized -> "Votre session a expiré. Reconnectez-vous."
    ApiError.Forbidden -> "Vous n’êtes pas autorisé à effectuer cette action."
    ApiError.NotFound -> "Ce contenu n’est plus disponible."
    is ApiError.Validation -> "Certaines informations doivent être corrigées."
    ApiError.Offline -> "Pas de connexion réseau. Vérifiez votre connexion."
    ApiError.TimedOut -> "Le serveur met trop de temps à répondre. Réessayez."
    ApiError.Cancelled -> ""
    ApiError.InvalidResponse, ApiError.IncompatibleResponse ->
        "L’application n’a pas pu lire la réponse du serveur."
    is ApiError.Server -> "Le serveur a renvoyé une erreur (${statusCode}). Réessayez plus tard."
    ApiError.RequestEncoding -> "La requête n’a pas pu être préparée."
}
