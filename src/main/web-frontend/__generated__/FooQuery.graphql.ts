/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
import { FragmentRefs } from "relay-runtime";
export type FooQueryVariables = {};
export type FooQueryResponse = {
    readonly bar: string;
    readonly " $fragmentRefs": FragmentRefs<"FooBar_murp">;
};
export type FooQuery = {
    readonly response: FooQueryResponse;
    readonly variables: FooQueryVariables;
};



/*
query FooQuery {
  bar(baz: "asldkfj")
  ...FooBar_murp
}

fragment FooBar_murp on Query {
  foo
}
*/

const node: ConcreteRequest = (function () {
    var v0 = ({
        "alias": null,
        "args": [
            {
                "kind": "Literal",
                "name": "baz",
                "value": "asldkfj"
            }
        ],
        "kind": "ScalarField",
        "name": "bar",
        "storageKey": "bar(baz:\"asldkfj\")"
    } as any);
    return {
        "fragment": {
            "argumentDefinitions": [],
            "kind": "Fragment",
            "metadata": null,
            "name": "FooQuery",
            "selections": [
                (v0 /*: any*/),
                {
                    "args": null,
                    "kind": "FragmentSpread",
                    "name": "FooBar_murp"
                }
            ],
            "type": "Query",
            "abstractKey": null
        },
        "kind": "Request",
        "operation": {
            "argumentDefinitions": [],
            "kind": "Operation",
            "name": "FooQuery",
            "selections": [
                (v0 /*: any*/),
                {
                    "alias": null,
                    "args": null,
                    "kind": "ScalarField",
                    "name": "foo",
                    "storageKey": null
                }
            ]
        },
        "params": {
            "cacheID": "2755608d3c6cdeef1d7c59ca72037167",
            "id": null,
            "metadata": {},
            "name": "FooQuery",
            "operationKind": "query",
            "text": "query FooQuery {\n  bar(baz: \"asldkfj\")\n  ...FooBar_murp\n}\n\nfragment FooBar_murp on Query {\n  foo\n}\n"
        }
    } as any;
})();
(node as any).hash = 'b03a2748be840aaf8f16509cf45220ad';
export default node;
