const babel = require("@babel/core");
const types = require("@babel/types");
const generator = require("@babel/generator");

const ast = babel.parse(`
let a = 1;
a = 2;
let b = 2;
`);

babel.traverse(ast, {
    AssignmentExpression(path) {
        try {
            path.insertBefore(types.expressionStatement(types.stringLiteral("// 1")));
        } catch (e) {
            console.log(e);
        }
    }
});

let t = generator.default(ast).code;

console.log(t);