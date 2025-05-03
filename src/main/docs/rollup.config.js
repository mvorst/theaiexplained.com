import babel from '@rollup/plugin-babel';
import {nodeResolve} from '@rollup/plugin-node-resolve';
import replace from '@rollup/plugin-replace';
import commonjs from '@rollup/plugin-commonjs';
import nodePolyfills from 'rollup-plugin-polyfill-node';
import multiInput from 'rollup-plugin-multi-input';

let config = {
  external: [
    'react',
    'react-dom',
    'axios'
  ],
  plugins: [
    multiInput(),
    // styles(),
    nodeResolve({
      extensions: ['.js', '.jsx', '.css'],
      browser: true,
    }),
    commonjs({
      include: [
        'node_modules/**'
      ],
      exclude: [
        'node_modules/process-es6/**'
      ]
    }),
    nodePolyfills(),
    babel({
      babelHelpers: "bundled",
      exclude: '**/node_modules/**',
      presets: [
        '@babel/preset-react',
          ["@babel/preset-env", {"targets": {"node": "10"}}]
      ]}),
    replace({
      'preventAssignment': true,
      'process.env.NODE_ENV': JSON.stringify('production'),
    })
  ],
  output: {
    format: 'iife',
    sourcemap: true,
    sourcemapExcludeSources: true,
    dir: 'dist',
    // entryFileNames: '[name]-dist.js',
    globals: {
      'react': 'React',
      'react-dom': 'ReactDOM',
      'axios': 'axios'
    },
  }
}

export default config