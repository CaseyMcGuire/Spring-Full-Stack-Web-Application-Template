package com.kotlinspringgraphqlreact.controllers

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class MainController {

  @GetMapping("/")
  @ResponseBody
  fun home(): String {
    return createHTML().html {
      attributes["lang"] = "en"
      head {
        meta {
          charset = "UTF-8"
        }
        title {
          +"Title"
        }
        script {
          type = "text/javascript"
          src = "/assets/javascripts/react.production.min.js"
        }
        script {
          type = "text/javascript"
          src = "/assets/javascripts/react-dom.production.min.js"
        }
        style {
          unsafe {
            // Using unsafe to embed raw CSS
            +"""
                    * {
                        margin: 0;
                        padding: 0;
                        /* Taken from: https://furbo.org/2018/03/28/system-fonts-in-css/ */
                        font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI",
                        "Roboto", "Oxygen", "Ubuntu", "Cantarell", "Fira Sans",
                        "Droid Sans", "Helvetica Neue", sans-serif;
                    }
                    """.trimIndent()
          }
        }
      }
      body {
        div {
          id = "root"
        }
        // Since 'th:src' is a Thymeleaf attribute, we can still represent it as a custom attribute
        script {
          src = "/bundles/index.bundle.js"
        }
      }
    }
  }
}