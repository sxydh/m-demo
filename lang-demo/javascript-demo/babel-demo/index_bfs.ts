import * as fs from 'fs';
import * as path from 'path';
import * as parser from '@babel/parser';
import generate from '@babel/generator';
import * as types from "@babel/types";

const injectVariableDeclaration = (node: any) => {
    if (!node) {
        return;
    }
    if (node.type !== 'VariableDeclaration' || !node.declarations) {
        return;
    }
    const ids = [];
    for (const declaration of node.declarations) {
        const id = declaration.id;
        if (!id) {
            continue;
        }
        if (id.type === 'Identifier') {
            ids.push(id);
            continue;
        }
        if (id.type === 'ObjectPattern' && id.properties) {
            for (const property of id.properties) {
                const key = property.key;
                if (!key) {
                    continue;
                }
                if (key.type !== 'Identifier') {
                    continue;
                }
                ids.push(key);
            }
        }
    }
    const parent: [any] = node._tnerap;
    if (parent instanceof Array) {
        const logAst = types.expressionStatement(
            types.callExpression(
                types.memberExpression(types.identifier('console'), types.identifier('log')),
                [
                    types.stringLiteral(`[${ids.map(e => e.name).join(',')}]`),
                    ...ids
                ]
            ));
        parent.splice(parent.indexOf(node) + 1, 0, logAst);
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
const tmp = path.join(src, 'tmp');
fs.mkdirSync(tmp, {recursive: true});
fs.writeFileSync(path.join(tmp, 'todo.js'), output.code);