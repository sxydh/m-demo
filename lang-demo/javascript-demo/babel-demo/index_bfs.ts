import * as fs from 'fs';
import * as path from 'path';
import * as parser from '@babel/parser';
import generate from '@babel/generator';
import * as types from "@babel/types";

const injectVariableDeclaration = (node: any) => {
    if (!node || !node._tnerap || node.type !== 'VariableDeclaration') {
        return;
    }
    const args: any[] = [];
    for (const declaration of node.declarations || []) {
        if (!declaration || declaration.type !== 'VariableDeclarator') {
            continue;
        }
        const id = declaration.id || {};
        switch (id.type) {
            case 'Identifier':
                args.push(id);
                break;
            case 'ArrayPattern':
                for (const element of id.elements || []) {
                    if (!element || element.type !== 'Identifier') {
                        continue;
                    }
                    args.push(element);
                }
                break;
            case 'ObjectPattern':
                for (const property of id.properties || []) {
                    if (!property || !property.key || property.key.type !== 'Identifier') {
                        continue;
                    }
                    args.push(property.key);
                }
                break;
        }
    }
    injectDo(node._tnerap, node, args);
};

const injectDo = (parent: any[], node: any, args: any[]) => {
    if (!parent || !parent.length || !node || !args || !args.length) {
        return;
    }
    const ast = types.expressionStatement(
        types.callExpression(
            types.memberExpression(types.identifier('console'), types.identifier('log')),
            [
                types.arrayExpression(args.map(e => types.stringLiteral(generate(e).code))),
                types.arrayExpression(args)
            ]
        ));
    parent.splice(parent.indexOf(node) + 1, 0, ast);
};

const astBFS = (todoJs: string): string => {
    const ast = parser.parse(todoJs, {sourceType: 'script'});
    const stack: any[] = [ast];
    while (stack.length) {
        const top = stack.pop();
        injectVariableDeclaration(top);

        for (const key in top) {
            if (key === '_tnerap') continue;
            const value = top[key];
            if (value instanceof Object && value.hasOwnProperty('type')) {
                stack.push(value);
            } else if (value instanceof Array) {
                for (let i = value.length - 1; i >= 0; i--) {
                    if (!value[i]) {
                        continue;
                    }
                    value[i]._tnerap = value;
                    stack.push(value[i]);
                }
            }
        }
    }
    return generate(ast).code;
};

const injectedCode = astBFS(fs.readFileSync(path.join(__dirname, 'src', 'todo.js'), 'utf8'));
console.log(injectedCode);