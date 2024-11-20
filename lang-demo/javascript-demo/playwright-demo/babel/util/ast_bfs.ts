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

const injectAssignmentExpression = (node: any) => {
    if (!node || !node._tnerap || node.type !== 'ExpressionStatement') {
        return;
    }
    const expression = node.expression;
    if (!expression || !expression.left) {
        return;
    }
    const args: any[] = [];
    const left = expression.left;
    switch (left.type) {
        case 'Identifier':
            args.push(left);
            break;
        case 'MemberExpression':
            let isPush = true;
            const stack = [left];
            while (stack.length) {
                const top = stack.pop() || {};
                const object = top.object;
                const property = top.property;
                if (!object || !property) {
                    continue;
                }
                const includeTypes = ['Identifier', 'MemberExpression'];
                if (!includeTypes.includes(object.type) || !includeTypes.includes(property.type)) {
                    isPush = false;
                    break;
                }
                stack.push(object);
                stack.push(property);
            }
            if (isPush) {
                args.push(left);
            }
            break;
        case 'ArrayPattern':
            for (const element of left.elements || []) {
                if (!element || element.type !== 'Identifier') {
                    continue;
                }
                args.push(element);
            }
            break;
    }
    injectDo(node._tnerap, node, args);
};
const injectDo = (parent: any[], node: any, args: any[]) => {
    if (!parent || !parent.length || !node || !args || !args.length) {
        return;
    }
    const ast = types.tryStatement(
        types.blockStatement([
            types.expressionStatement(
                types.callExpression(
                    types.identifier('_noitcnuf'),
                    [
                        types.arrayExpression(args.map(e => types.stringLiteral(generate(e).code))),
                        types.arrayExpression(args),
                        types.stringLiteral('-1')
                    ]))
        ]),
        types.catchClause(
            types.identifier('e'),
            types.blockStatement([]))
    );
    parent.splice(parent.indexOf(node) + 1, 0, ast);
};

export const astBFS = (todoJs: string): string => {
    const ast = parser.parse(generate(parser.parse(todoJs, {sourceType: 'module'})).code, {sourceType: 'module'});
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
