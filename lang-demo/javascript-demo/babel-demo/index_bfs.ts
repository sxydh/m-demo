import * as fs from 'fs';
import * as path from 'path';
import * as parser from '@babel/parser';
import generate from '@babel/generator';
import * as types from "@babel/types";

const injectVariableDeclaration = (node: any) => {
    if (!node || node.type !== 'VariableDeclaration' || !node.declarations) {
        return;
    }
    const ids = node.declarations.map((e: any) => e.id).filter(Boolean);
    const args: any[] = [];
    for (const id of ids) {
        switch (id.type) {
            case 'Identifier':
                args.push(id);
                break;
            case 'ObjectPattern':
                for (const property of id.properties || []) {
                    const key = property.key;
                    if (!key || key.type !== 'Identifier') {
                        continue;
                    }
                    args.push(key);
                }
                break;
            case 'ArrayPattern':
                for (const element of id.elements || []) {
                    if (!element || element.type !== 'Identifier') {
                        continue;
                    }
                    args.push(element);
                }
                break;
        }
    }
    injectedAst(node, args);
};

const injectAssignmentExpression = (node: any) => {
    if (!node || node.type !== 'ExpressionStatement') {
        return;
    }
    const stack = [node.expression];
    const args: any[] = [];
    while (stack.length) {
        const top = stack.pop();
        if (!top || top.type !== 'AssignmentExpression') {
            continue;
        }
        if (top.left && top.left.type === 'Identifier') {
            args.push(top);
        }
        if (top.right && top.right.type === 'AssignmentExpression') {
            stack.push(top.right);
        }
    }
    injectedAst(node, args);
};

const injectedAst = (node: any, args: any[]) => {
    const parent: any[] = node._tnerap;
    if (parent instanceof Array) {
        const ast = types.expressionStatement(
            types.callExpression(
                types.memberExpression(types.identifier('console'), types.identifier('log')),
                [
                    types.stringLiteral(`[${args.map(e => e.name).join(',')}]`),
                    ...args
                ]
            ));
        parent.splice(parent.indexOf(node) + 1, 0, ast);
    }
};

const bfs = () => {
    const src = path.join(__dirname, 'src');
    const todoJs: string = fs.readFileSync(path.join(src, 'todo.js'), 'utf8');

    const ast = parser.parse(todoJs, {sourceType: 'module'});
    const stack: any[] = [ast];
    while (stack.length) {
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
                    if (!ele) {
                        continue;
                    }
                    ele._tnerap = value;
                    stack.push(ele);
                }
            }
        }
    }

    const output = generate(ast);
    const tmp = path.join(src, 'tmp');
    fs.mkdirSync(tmp, {recursive: true});
    fs.writeFileSync(path.join(tmp, 'todo.js'), output.code);
};

bfs();