package com.application.controllers

import com.application.views.ReactPage
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
    return ReactPage("index", "Home").render()
  }

  @GetMapping("/graphiql")
  @ResponseBody
  fun graphiql(): String {
    return ReactPage("graphiql", "GraphiQL").render()
  }

}