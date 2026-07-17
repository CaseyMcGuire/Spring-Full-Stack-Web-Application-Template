package com.application.config

import com.application.spa.AppSpaApplication
import io.github.caseymcguire.sparouting.spring.config.SinglePageApplicationConfig
import org.springframework.stereotype.Component

/**
 * Registers the main `app` SPA with the spa-routing starter, which turns each route defined
 * in [AppSpaApplication] into a GET mapping rendered by [AppSpaHtmlRenderer].
 *
 * No route rules are declared, so every route (and every `/__spa/route-decision` check the
 * client makes on in-page navigation) is allowed. Add [io.github.caseymcguire.sparouting.spring.rules.SpaRouteRule]s
 * here to gate pages (e.g. require login) — the client wiring in App.tsx already honors them.
 */
@Component
class AppSpaConfig : SinglePageApplicationConfig {
  override val application = AppSpaApplication
}
