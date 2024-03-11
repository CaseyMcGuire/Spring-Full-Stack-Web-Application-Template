
import entries from '../entries.js';
import * as React from 'react';
import ReactDOMServer from 'react-dom/server';
import fs from 'fs';


function MyComponent() {
  return (
    <div>
      hello there
    </div>
  )
}
console.log(entries);

for (const entry in entries) {
  console.log(entry)
  const data = fs.readFileSync(entries[entry] + '.tsx', 'utf8');
  console.log(data);
}

console.log(ReactDOMServer.renderToStaticMarkup(<MyComponent />));
