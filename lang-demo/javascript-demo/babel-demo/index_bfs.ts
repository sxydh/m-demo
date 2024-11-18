import * as fs from 'fs';
import * as path from 'path';
import * as parser from '@babel/parser';
import generate from '@babel/generator';
import * as types from "@babel/types";

const filePath = path.join(__dirname, 'src', 'todo.js');
const todoJs: string = fs.readFileSync(filePath, 'utf8');

const logAst = types.expressionStatement(
    types.callExpression(
        types.memberExpression(types.identifier('console'), types.identifier('log')),
        [types.numericLiteral(1)]
    ));

const ast = parser.parse(todoJs, {sourceType: 'module'});
const stack: [any] = [ast];
while (stack.length > 0) {
    const top = stack.pop();
    if (top.type === 'VariableDeclaration') {
        const parent: [any] = top._tnerap;
        if (parent instanceof Array) {
            parent.splice(parent.indexOf(top), 0, logAst);
        }
    }

    for (const key in top) {
        if (key === '_tnerap') continue;

        const value = top[key];
        if (value instanceof Object && value.hasOwnProperty('type')) {
            stack.push(value);
        } else if (value instanceof Array) {
            for (let i = value.length - 1; i >= 0; i--) {
                const ele = value[i];
                if (ele) {
                    ele._tnerap = value;
                    stack.push(ele);
                }
            }
        }
    }
}

const output = generate(ast);
console.log(output.code);