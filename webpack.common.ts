import path from "path";

import { Configuration } from "webpack";
import stylexPlugin from "@stylexjs/unplugin";
import MiniCssExtractPlugin from "mini-css-extract-plugin";

const config : Configuration = {
  entry: {
    index : './src/main/web-frontend/App',
    graphiql: '/src/main/web-frontend/pages/GraphiqlPage'
  },
  resolve: {
    extensions: [".ts", ".tsx", ".js", ".json"],
    modules: [
      path.resolve('./src/main/web-frontend'),
      path.resolve('./node_modules')
    ]
  },
  devtool: 'eval-source-map',
  output: {
    filename: '[name].bundle.js',
    path: path.resolve(__dirname, 'src/main/resources/static/bundles'),
    publicPath: '/bundles/',
    module: true,
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: [{loader: 'babel-loader'}]
      },
      {
        enforce: "pre",
        test: /\.js$/,
        loader: "source-map-loader",
        exclude: [
          /node_modules\/monaco-editor/,
          /node_modules\/monaco-graphql/,
        ],
      },
      {
        test: /\.css$/i,
        use: [MiniCssExtractPlugin.loader, 'css-loader'],
        sideEffects: true,
      },
    ]
  },
  plugins: [
    stylexPlugin.webpack({
      useCSSLayers: true,
      treeshakeCompensation: true,
      aliases: {
        "*": path.resolve("./src/main/web-frontend/*"),
      },
      cssInjectionTarget: (fileName: string) => fileName === 'stylex.css',
    }),
    new MiniCssExtractPlugin()
  ],
  externalsType: "module",
  externals: [
    'sanitize-html',
    'react',
    'react-dom',
    "react-dom/client",
    "react/jsx-runtime",
    'highlight.js'
  ],
  experiments: {
    outputModule: true,
  },
};

export default config;
