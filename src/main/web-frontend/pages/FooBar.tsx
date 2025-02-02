import { graphql} from "react-relay";
import * as React from "react";
import { FooBar_murp$key } from "__generated__/FooBar_murp.graphql";
import {useFragment} from "react-relay/hooks";

export default function FooBar(props: {murp: FooBar_murp$key}) {
  const data = useFragment(
    graphql`
      fragment FooBar_murp on Query {
        foo
        murp: bar(baz: "aljskdfasf")
      }
    `,
    props.murp
  )

  return (
    <div>
      <div>
        foo: {data.foo}
        murp: {data.murp}
      </div>
    </div>
  )
}
