/**
 * @generated SignedSource<<eedb4efa542a5752835df4b238cf4b9b>>
 * @lightSyntaxTransform
 */

/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ReaderFragment } from 'relay-runtime';
import { FragmentRefs } from "relay-runtime";
export type FooBar_murp$data = {
  readonly foo: string;
  readonly murp: string;
  readonly " $fragmentType": "FooBar_murp";
};
export type FooBar_murp$key = {
  readonly " $data"?: FooBar_murp$data;
  readonly " $fragmentSpreads": FragmentRefs<"FooBar_murp">;
};

const node: ReaderFragment = {
  "argumentDefinitions": [],
  "kind": "Fragment",
  "metadata": null,
  "name": "FooBar_murp",
  "selections": [
    {
      "alias": null,
      "args": null,
      "kind": "ScalarField",
      "name": "foo",
      "storageKey": null
    },
    {
      "alias": "murp",
      "args": [
        {
          "kind": "Literal",
          "name": "baz",
          "value": "aljskdfasf"
        }
      ],
      "kind": "ScalarField",
      "name": "bar",
      "storageKey": "bar(baz:\"aljskdfasf\")"
    }
  ],
  "type": "Query",
  "abstractKey": null
};

(node as any).hash = "692b8eb8df20e2f8f44d76dce9ba7ce8";

export default node;
