package com.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class MainApplication

fun main(args: Array<String>) {
  runApplication<MainApplication>(*args)
}