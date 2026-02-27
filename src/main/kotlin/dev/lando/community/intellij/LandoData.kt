package dev.lando.community.intellij

/**
 * Data class representing the service data for a Lando application.
 *
 * @property service The name of the service.
 * @property urls The list of URLs associated with the service.
 * @property type The type of the service.
 * @property version The version of the service.
 * @property via The name of the service that hosts this service.
 * @property webroot The webroot directory for the service.
 * @property config The configuration map for the service.
 * @property healthy The health status of the service.
 * @property meUser The user associated with the service.
 * @property hasCerts A boolean indicating whether the service has certificates.
 * @property api The Lando API version used to build the service.
 * @property hostnames The list of hostnames associated with the service.
 */
data class ServiceData(
    val service: String,
    val urls: List<String>,
    val type: String,
    val version: String,
    val via: String,
    val webroot: String,
    val config: Map<String, Any>,
    val healthy: String,
    val meUser: String,
    val hasCerts: Boolean,
    val api: Int,
    val hostnames: List<String>
)
