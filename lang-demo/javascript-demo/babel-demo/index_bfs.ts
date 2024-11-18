import * as fs from 'fs';
import * as path from 'path';
import * as parser from '@babel/parser';
import generate from '@babel/generator';
import * as types from "@babel/types";

const injectVariableDeclaration = (node: any) => {
    if (node && node.type === 'VariableDeclaration') {
        const declarations = node.declarations;
        if (declarations && declarations.length === 1) {
            const declaration = declarations[0];
            if (declaration) {
                const id = declaration.id;
                if (id && id.type === 'Identifier') {
                    const parent: [any] = node._tnerap;
                    if (parent instanceof Array) {
                        const logAst = types.expressionStatement(
                            types.callExpression(
                                types.memberExpression(types.identifier('console'), types.identifier('log')),
                                [
                                    types.stringLiteral(`[${id.name}]`),
                                    id
                                ]
                            ));
                        parent.splice(parent.indexOf(node) + 1, 0, logAst);
                    }
                }
            }
        }
    }
};

const injectAssignmentExpression = (node: any) => {
    if (node && node.type === 'ExpressionStatement') {
        const expression = node.expression;
        if (expression && expression.type === 'AssignmentExpression') {
            const left = expression.left;
            if (left && left.type === 'Identifier') {
                const parent: [any] = node._tnerap;
                if (parent instanceof Array) {
                    const logAst = types.expressionStatement(
                        types.callExpression(
                            types.memberExpression(types.identifier('console'), types.identifier('log')),
                            [
                                types.stringLiteral(`[${left.name}]`),
                                left
                            ]
                        ));
                    parent.splice(parent.indexOf(node) + 1, 0, logAst);
                }
            }
        }
    }
};

const src = path.join(__dirname, 'src');
const todoJs: string = fs.readFileSync(path.join(src, 'todo.js'), 'utf8');

const ast = parser.parse(todoJs, {sourceType: 'module'});
const stack: [any] = [ast];
while (stack.length > 0) {
    const top = stack.pop();
    injectVariableDeclaration(top);
    injectAssignmentExpression(top);

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