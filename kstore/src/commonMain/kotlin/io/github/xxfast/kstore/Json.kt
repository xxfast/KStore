package io.github.xxfast.kstore

import kotlinx.serialization.json.Json

public val DefaultJson: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
