import * as fs from 'fs';
import * as path from 'path';
import * as parser from '@babel/parser';
import generate from '@babel/generator';
import * as types from "@babel/types";

const injectVariableDeclaration = (node: any) => {
    if (!node || !node._tnerap || node.type !== 'VariableDeclaration') {
        return;
    }
    const stack = [...[...(node.declarations || [])].reverse()];
    const args: any[] = [];
    while (stack.length) {
        const top = stack.pop();
        if (!top) {
            continue;
        }
        if (top.type === 'Identifier') {
            args.push(top);
        }
        // 定位 a 变量
        // let a = 1;
        stack.push(top.id);
        // 定位 b 变量
        // let {a: {b}} = {};
        stack.push(top.value);
        // 定位 a 变量
        // let {a = 1} = null;
        stack.push(top.left);
        // 定位 a, b, c 变量
        // let [a, b, c] = [1, 2, 3];
        stack.push(...[...(top.elements || [])].reverse());
        // 定位 a, b, c 变量
        // let {a, b, c} = {a: 1, b: 2, c: 3};
        stack.push(...[...(top.properties || [])].reverse());
    }
    injectedAst(node, args, node._tnerap);
};

const injectAssignmentExpression = (node: any) => {
    if (!node || !node._tnerap || node.type !== 'ExpressionStatement') {
        return;
    }
    const stack = [node.expression];
    const args: any[] = [];
    while (stack.length) {
        const top = stack.pop();
        if (!top) {
            continue;
        }
        if (top.type === 'Identifier') {
            args.push(top);
        }
        // 定位 a, b, c 变量
        // a = b = c = 5;
        if (top.right && top.right.type === 'AssignmentExpression') {
            stack.push(top.right);
        }
        // 定位 a 变量
        // a = 1;
        stack.push(top.left);
        // 定位 a, b, c 变量
        // [a, b, c] = [1, 2, 3];
        stack.push(...[...(top.elements || [])].reverse());
        //
        stack.push(top.argument);
    }
    injectedAst(node, args, node._tnerap);
};

const injectedAst = (node: any, args: any[], parent: any[]) => {
    if (!parent || !parent.length) {
        return;
    }
    const ast = types.expressionStatement(
        types.callExpression(
            types.memberExpression(types.identifier('console'), types.identifier('log')),
            [
                types.stringLiteral(`[${args.map(e => e.name).join(',')}]`),
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