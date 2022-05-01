import * as React from "react";
import * as ReactDOM from "react-dom";
import {QueryRenderer, graphql} from "react-relay";
import {RelayConfig} from "relay/RelayConfig";
import {AppQuery} from "__generated__/AppQuery.graphql";
import {BrowserRouter, Switch, Route, Link} from "react-router-dom";
import {createUseStyles}  from "react-jss";
import Foo from "pages/Foo";
import {useLazyLoadQuery} from "react-relay/hooks";
import {RelayEnvironmentProvider} from "react-relay/hooks";

const styles = createUseStyles({
  foo: {
    color: 'blue',
    margin: '10px'
  },
  bar: {
    color: 'green',
    margin: '20px'
  },
  baz: {
    color: 'grey',
  }
});

export function App() {
  console.log("alskjdflasjdf")
  return (
    <RelayEnvironmentProvider environment={RelayConfig.getEnvironment()}>
      <React.Suspense fallback={null}>
        <AppImpl />
      </React.Suspense>
    </RelayEnvironmentProvider>
  );
}

function AppImpl() {
  const query = graphql`
    query AppQuery {
      foo
    }
  `;

  const response = useLazyLoadQuery<AppQuery>(query, {})
  console.log(response)
  return <Body />
}

const Body = () => {
  return (<BrowserRouter>
    <Link to="/">Home</Link>
    <Link to="/there">There</Link>
    <Link to="/foo_bar">foo bar</Link>
    <Switch>
      <Route exact path="/" component={Foo}/>
      <Route exact path="/there" component={Bar}/>
      <Route exact path="/foo_bar" component={Baz}/>
      <Route component={Forohfor}/>
    </Switch>
  </BrowserRouter>);
};

const Bar = () => {
  return <div className={styles().bar}>there</div>;
};

const Baz = () => {
  return <div className={styles().baz}>baz</div>;
};

const Forohfor = () => {
  return <div>404</div>;
};

ReactDOM.render(
  <App/>,
  document.getElementById("root")
);
