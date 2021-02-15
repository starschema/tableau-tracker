package com.starschema.brilliant.tableautracker

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.starschema.brilliant.tableautracker.config.config
import com.starschema.brilliant.tableautracker.db.DbUtils
import com.starschema.brilliant.tableautracker.exception.exceptionHandler
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import org.slf4j.LoggerFactory

fun Application.main() {
    val log = LoggerFactory.getLogger("AppStartup")
    install(XForwardedHeaderSupport)
    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(StatusPages, exceptionHandler())
    install(ContentNegotiation) {
        jackson {
            configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    var uiType = config.property("ktor.deployment.ui").getString()

    log.info("Connecting to database ${config.property("ktor.database.url").getString()}")
    DbUtils.updateLiquibase()

    routing {
        route("", routing_1_0())

        /**
         * Bind the 3 frontends to 3 endpoints based on the config
         */
        get("/extension"){
            call.respondRedirect("/extension/index.html")
        }
        static("/extension/") {
            resources("static/extension/$uiType")
            defaultResource("index.html", "static/extension/$uiType")
        }
        get("/wdc"){
            call.respondRedirect("/wdc/index.html")
        }
        static("/wdc/") {
            resources("static/wdc/$uiType")
            defaultResource("index.html", "static/wdc/$uiType")
        }
        static("/") {
            resources("static/registration/$uiType")
            defaultResource("index.html", "static/registration/$uiType")
        }
        /**
         * Host the example workbook
         */
        static("/example") {
            resource("Tableau_Tracker_Demo.twbx", "Tableau_Tracker_Demo.twbx", "static")
        }
    }
}

