package com.application.views

import kotlinx.html.*
import kotlinx.html.stream.createHTML

class ReactPage(
  private val bundleName: String,
  private val pageTitle: String
) : RenderablePage {

  companion object {
    private const val REACT_VERSION = "19.2.6"
    val REACT_INPUT = ImportMapInput("react", "https://esm.sh/react@$REACT_VERSION")
    val REACT_SUBPATH_INPUT = ImportMapInput("react/", "https://esm.sh/react@$REACT_VERSION/")
    val REACT_DOM_INPUT = ImportMapInput("react-dom", "https://esm.sh/react-dom@$REACT_VERSION")
    val REACT_DOM_SUBPATH_INPUT = ImportMapInput("react-dom/", "https://esm.sh/react-dom@$REACT_VERSION/")
  }

  private var customHead: (HEAD.() -> Unit)? = null
  private var customBody: (BODY.() -> Unit)? = null
  private var importMapInputs: List<ImportMapInput> = emptyList()

  fun customHead(block: HEAD.() -> Unit): ReactPage {
    customHead = block
    return this
  }

  fun importMapInputs(vararg inputs: ImportMapInput): ReactPage {
    importMapInputs = inputs.toList()
    return this
  }

  fun customBody(block: BODY.() -> Unit): ReactPage {
    customBody = block
    return this
  }

  override fun render(): String {
    return createHTML().html {
      head {
        meta(charset = "UTF-8")
        title {
          +pageTitle
        }
        meta {
          name = "viewport"
          content = "initial-scale=1.0, maximum-scale=1.0, width=device-width"
        }
        script(type = "importmap") {
          unsafe {
            raw(
              """
            {
              "imports": {
                ${
                listOf(
                  REACT_INPUT,
                  REACT_SUBPATH_INPUT,
                  REACT_DOM_INPUT,
                  REACT_DOM_SUBPATH_INPUT,
                  *importMapInputs.toTypedArray()
                ).joinToString(",\n") { "\"${it.name}\" : \"${it.pathOrUrl}\"" }
              }
              }
            }
            """.trimIndent()
            )
          }
        }
        style {
          unsafe {
            +"""

                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    html, body, #root {
                        height: 100%;
                    }
            """.trimIndent()
          }
        }
        customHead?.invoke(this)
      }
      body {
        div {
          attributes["id"] = "root"
        }
        script {
          src = "/bundles/${bundleName}.bundle.js"
          type = "module"
        }
        customBody?.invoke(this)
      }
    }
  }
}



data class ImportMapInput(val name: String, val pathOrUrl: String)