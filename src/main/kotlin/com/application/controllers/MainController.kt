package com.application.controllers

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class MainController {

  @GetMapping("/")
  @ResponseBody
  fun home(): String {
    return generateHtml("index")
  }

  @GetMapping("/graphiql")
  @ResponseBody
  fun graphiql(): String {
    return generateHtml("graphiql")
  }


  private fun generateHtml(entryKey: String) = createHTML().html {
    lang = "en"
    head {
      meta(charset = "UTF-8")
      title("Title")
      script(type = "text/javascript", src = "/assets/javascripts/react.production.min.js") {}
      script(type = "text/javascript", src = "/assets/javascripts/react-dom.production.min.js") {}
      link {
        rel = "stylesheet"
        href = "/bundles/styles.css"
      }
      style {
        unsafe {
          +"""
                * {
                    margin: 0;
                    padding: 0;
                    font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI",
                    "Roboto", "Oxygen", "Ubuntu", "Cantarell", "Fira Sans",
                    "Droid Sans", "Helvetica Neue", sans-serif;
                }
                """
        }
      }
    }
    body {
      div {
        id = "root"
      }
      script(src = "/bundles/$entryKey.bundle.js") {}
    }
  }
}