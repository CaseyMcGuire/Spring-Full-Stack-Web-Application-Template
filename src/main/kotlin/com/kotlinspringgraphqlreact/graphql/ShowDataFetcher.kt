package com.kotlinspringgraphqlreact.graphql

import com.kotlinspringgraphqlreact.graphql.types.Actor
import com.kotlinspringgraphqlreact.graphql.types.Show
import com.netflix.graphql.dgs.*


@DgsComponent
class ShowsDataFetcher {
  private val shows = listOf(
    Show("Stranger Things", 2016),
    Show("Ozark", 2017),
    Show("The Crown", 2016),
    Show("Dead to Me", 2019),
    Show("Orange is the New Black", 2013))

  @DgsQuery
  fun shows(@InputArgument titleFilter : String?): List<Show> {
    return if(titleFilter != null) {
      shows.filter { it.title?.contains(titleFilter) ?: false }
    } else {
      shows
    }
  }

  @DgsData(parentType = "Show", field = "actors")
  fun actors(dfe: DgsDataFetchingEnvironment): List<Actor> {
    val show = dfe.getSource<Show>()
    return listOf(Actor("laksjdf"))
  }



}