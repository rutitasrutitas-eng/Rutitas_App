package mx.edu.uttt.rutitas.utils

import mx.edu.uttt.rutitas.BuildConfig

object AppConfig {
    /** true = todo funciona con datos locales simulados (sin servidor). */
    val DEMO_MODE: Boolean = BuildConfig.DEMO_MODE

    /** Dominios institucionales permitidos para crear cuenta -> nombre de la institución. */
    val INSTITUTIONS: Map<String, String> = linkedMapOf(
        "uaeh.edu.mx" to "Universidad Autónoma del Estado de Hidalgo",
        "itpachuca.edu.mx" to "Instituto Tecnológico de Pachuca",
        "upp.edu.mx" to "Universidad Politécnica de Pachuca",
        "upmetropolitana.edu.mx" to "Universidad Politécnica Metropolitana de Hidalgo",
        "uttt.edu.mx" to "Universidad Tecnológica de Tula-Tepeji",
        "utvm.edu.mx" to "Universidad Tecnológica del Valle del Mezquital",
        "unideh.edu.mx" to "Universidad Digital del Estado de Hidalgo",
        "upiih.ipn.mx" to "UPIIH – Instituto Politécnico Nacional"
    )

    val ALLOWED_DOMAINS: List<String> get() = INSTITUTIONS.keys.toList()

    /** Devuelve el dominio permitido al que pertenece el correo (acepta subdominios, ej. alumni.uaeh.edu.mx). */
    private fun matchDomain(email: String): String? {
        val domain = email.trim().lowercase().substringAfterLast('@', "")
        return INSTITUTIONS.keys.firstOrNull { domain == it || domain.endsWith(".$it") }
    }

    fun isInstitutionalEmail(email: String): Boolean = matchDomain(email) != null

    fun institutionFor(email: String): String? = matchDomain(email)?.let { INSTITUTIONS[it] }
}
