import * as http from 'http';
import {IncomingMessage, Server, ServerResponse} from "node:http";

const hostname = '127.0.0.1';
const port = 3000;

const server: Server = http.createServer((req: IncomingMessage, res: ServerResponse) => {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });
    req.on('end', () => {
        console.log(body);
    });

    res.statusCode = 200;
    res.setHeader('Content-Type', 'text/plain; charset=utf-8');
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE');
    res.end('Hello, World!');
});

server.listen(port, hostname, () => {

});