package com.starschema.brilliant.tableautracker.config

import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig

const val EVENT_QUERY_DEFAULT_LIMIT = 5000
const val EVENT_QUERY_DEFAULT_OFFSET = 0

val config: ApplicationConfig = HoconApplicationConfig(ConfigFactory.load())


