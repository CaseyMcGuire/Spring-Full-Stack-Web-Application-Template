package com.application.graphql

import com.netflix.graphql.dgs.*


@DgsComponent
class QueryDataFetcher {
  @DgsQuery
  fun foo() = "Foo"

  @DgsQuery
  fun bar(@InputArgument baz: String) = baz
}