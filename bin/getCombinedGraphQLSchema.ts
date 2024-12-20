import fs from "fs";

import {join} from "path";

import { makeExecutableSchema } from '@graphql-tools/schema'
import { printSchema } from "graphql/utilities";

/**
 * This function takes all GraphQL schema files starting from the `schemaDirectory` and combines
 * them into a single schema.
 */

const schemaDirectory = join(__dirname, '../src/main/resources/schema');

function combineFiles(path: string, graphqlFiles: string[]) {
  for (const file of fs.readdirSync(path, 'utf8')) {
    const newPath = `${path}/${file}`
    const isFile = fs.lstatSync(newPath).isFile()
    if (isFile) {
      if (file.endsWith(".graphql")) {
        graphqlFiles.push(newPath)
      }
    }
    else {
      combineFiles(newPath, graphqlFiles)
    }
  }
}

export default function getGraphqlSchema(): string {
  const graphqlFiles: string[] = []
  combineFiles(schemaDirectory, graphqlFiles)
  const combinedSchema = makeExecutableSchema({
    typeDefs: graphqlFiles.map(file => fs.readFileSync(file, 'utf8'))
  })
  return printSchema(combinedSchema)
}
