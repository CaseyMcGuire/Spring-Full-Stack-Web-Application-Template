import getGraphqlSchema from "./getCombinedGraphQLSchema";
import fs from "fs";
import { execSync } from 'child_process'


/**
 * DGS supports defining your GraphQL schema across multiple files whereas Relay does not. As such,
 * this script combines multiple GraphQL files into a single schema, writes it to a file that can be
 * read by Relay, runs the Relay compiler, and then comments out the schema in the file (so you can
 * view the combined schema if you wish)
 */
const existingSchemaFile = 'src/main/resources/relay/schema.graphql'

const combinedSchema = getGraphqlSchema()
fs.writeFileSync(existingSchemaFile, combinedSchema)
execSync("relay-compiler")
fs.unlinkSync(existingSchemaFile)