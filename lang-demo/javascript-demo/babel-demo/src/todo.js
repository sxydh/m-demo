// noinspection JSUnusedGlobalSymbols,JSUnusedAssignment

let a = 'a';
let b = 'b', b2 = 'b2';
let c = c2 = c3 = 'c';
let [d, d2, [d3, d4]] = ['d', 'd2', ['d3', 'd4']];
let {e, e2, e3: {e4, e5}} = {e: 'e', e2: 'e2', e3: {e4: 'e4', e5: 'e5'}};

a = 'a';
c = c2 = c3 = 'c';
[d, d2, [d3, d4]] = ['d', 'd2', ['d3', 'd4']];