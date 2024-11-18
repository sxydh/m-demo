import * as fs from 'fs';
import * as path from 'path';
import * as parser from '@babel/parser';
import traverse from '@babel/traverse';
import * as types from '@babel/types';
import generate from '@babel/generator';

const filePath = path.join(__dirname, 'src', 'todo.js');
const todoJs: string = fs.readFileSync(filePath, 'utf8');

// https://babeljs.io/docs/babel-parser
const ast = parser.parse(todoJs);

// https://babeljs.io/docs/babel-traverse
traverse(ast, {
    Declaration(path) {
        // https://babeljs.io/docs/babel-types
        const semicolon = types.identifier(';');
        path.insertBefore(semicolon);
    }
});

// https://babeljs.io/docs/babel-generator
const output = generate(ast);
console.log(output.code);