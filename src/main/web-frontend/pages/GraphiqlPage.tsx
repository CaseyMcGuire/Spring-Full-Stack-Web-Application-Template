
import {createGraphiQLFetcher} from "@graphiql/toolkit";
import * as React from "react";
import {GraphiQL} from "graphiql";
import 'graphiql/graphiql.css';
import CsrfUtils from "../utils/CsrfUtils";
import {renderComponent} from "../utils/ReactPageUtils";

function GraphiQLPage() {
  const fetcher = createGraphiQLFetcher({
    url: window.location.origin + '/graphql',
  });
  return (
    <GraphiQL headers={getHeaders()} fetcher={fetcher} />
  )
}
function getHeaders(): string {
  const csrfToken = CsrfUtils.getToken();
  return JSON.stringify(
    {
      'X-XSRF-TOKEN': csrfToken
    },
    null,
    2
  )
}

renderComponent(
  <GraphiQLPage />
);