import { exec } from 'child_process';
import path from 'path';
import iconv from 'iconv-lite';

const cmdPath = path.join(__dirname, 'node_modules', '.bin', 'http-server');
const binaryEncoding = 'binary';
const encoding = 'cp936';

exec(`${cmdPath} ROOT -p 3000`,
    {
        encoding: binaryEncoding
    },
    (error, stdout, stderr) => {
        if (error) {
            console.error(iconv.decode(Buffer.from(error.message, binaryEncoding), encoding));
        } else {
            console.log(iconv.decode(Buffer.from(stdout, binaryEncoding), encoding));
        }
    });