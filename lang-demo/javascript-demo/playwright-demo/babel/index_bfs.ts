import * as fs from 'fs';
import * as path from 'path';
import * as parser from '@babel/parser';
import generate from '@babel/generator';
import * as types from "@babel/types";

const injectAst = (node: any) => {
    if (!node || !node._tnerap || !'VariableDeclaration,ExpressionStatement'.includes(node.type)) {
        return;
    }
    const stack = [];
    const args: any[] = [];
    stack.push(...[...(node.declarations || [])].reverse());
    stack.push(node.expression);
    while (stack.length) {
        const top = stack.pop();
        if (!top) {
            continue;
        }
        if (top.type === 'Identifier' || top.type === 'MemberExpression') {
            args.push(top);
        }
        stack.push(top.right);
        stack.push(top.init);
        stack.push(top.left);
        stack.push(...[...(top.expressions || [])].reverse());
        stack.push(...[...(top.elements || [])].reverse());
        stack.push(...[...(top.properties || [])].reverse());
        stack.push(top.argument);
        stack.push(top.value);
        stack.push(top.id);
    }
    injectedAst(node, args, node._tnerap);
};

const injectedAst = (node: any, args: any[], parent: any[]) => {
    if (!parent || !parent.length || !args || !args.length) {
        return;
    }
    const ast = types.expressionStatement(
        types.callExpression(
            types.memberExpression(types.identifier('console'), types.identifier('log')),
            [
                types.stringLiteral(`[${args.map(e => generate(e).code).join(',')}]`),
                ...args
            ]
        ));
    parent.splice(parent.indexOf(node) + 1, 0, ast);
};

const bfs = () => {
    const src = path.join(__dirname, 'src');
    const todoJs: string = fs.readFileSync(path.join(src, 'todo.js'), 'utf8');

    const ast = parser.parse(todoJs, {sourceType: 'module'});
    const stack: any[] = [ast];
    while (stack.length) {
        const top = stack.pop();
        injectAst(top);

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