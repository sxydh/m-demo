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
    const ast = types.expressionStatement(types.callExpression(
        types.identifier('_noitcnuf'),
        [
            types.stringLiteral(args.map(e => generate(e).code).join(',')),
            ...args
        ]));
    parent.splice(parent.indexOf(node) + 1, 0, ast);
};

export const astBFS = (todoJs: string): string => {
    const ast = parser.parse(todoJs, {sourceType: 'script'});
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
    return generate(ast).code;
};