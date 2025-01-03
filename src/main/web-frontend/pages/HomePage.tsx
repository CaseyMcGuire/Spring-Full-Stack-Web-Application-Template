import * as React from 'react';
import {graphql} from "react-relay";
import FooBar from "./FooBar";
import {useLazyLoadQuery} from "react-relay/hooks";
import {HomePageQuery} from "../__generated__/HomePageQuery.graphql";
import * as stylex from '@stylexjs/stylex';

const styles = stylex.create({
  root: {
    backgroundColor: 'lightblue',
    fontSize: 16,
    lineHeight: 1.5,
    color: 'rgb(60,60,60)',
  }
});

export default function HomePage() {
    const query = graphql`
      query HomePageQuery {
        bar(baz: "asldkfj")
        ...FooBar_murp
      }
    `;

  const result = useLazyLoadQuery<HomePageQuery>(query, {})
  return (
    <div {...stylex.props(styles.root)}>
      <div>{result?.bar}</div>
      <FooBar murp={result}/>
    </div>
  )

}




