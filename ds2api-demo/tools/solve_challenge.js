/**
 * This module provides functionality to solve DeepSeek PoW challenges using WebAssembly.
 * It loads a specific WASM file containing the SHA3/Keccak implementation required for the proof-of-work algorithm.
 */

const fs = require('fs');
const path = require('path');

// Path to the WASM binary file
const wasmPath = path.join(__dirname, '../doc/sha3_wasm_bg.7b9ca65ddd.wasm');
const wasmBuffer = fs.readFileSync(wasmPath);

let wasmInstance;
let wasmMemory;
let cachedTextEncoder = new TextEncoder('utf-8');

/**
 * Initializes the WebAssembly module.
 * Must be called before using the solve function.
 */
async function init() {
    const module = await WebAssembly.compile(wasmBuffer);
    const instance = await WebAssembly.instantiate(module, {});
    wasmInstance = instance.exports;
    wasmMemory = wasmInstance.memory;
}

let WASM_VECTOR_LEN = 0;

/**
 * Encodes a string into the WASM memory buffer.
 *
 * @param {string} arg - The string to pass to WASM
 * @param {Function} malloc - The WASM memory allocation function
 * @returns {number} The pointer to the allocated memory in WASM
 */
function passStringToWasm(arg, malloc) {
    const buf = cachedTextEncoder.encode(arg);
    const ptr = malloc(buf.length, 1) >>> 0;
    const mem = new Uint8Array(wasmMemory.buffer);
    mem.subarray(ptr, ptr + buf.length).set(buf);
    WASM_VECTOR_LEN = buf.length;
    return ptr;
}

/**
 * Solves the Proof of Work challenge.
 *
 * @param {string} challenge - The challenge string provided by the server
 * @param {string} salt - The salt string provided by the server
 * @param {number} difficulty - The target difficulty (number of iterations or complexity)
 * @param {number} expireAt - The expiration timestamp
 * @returns {number|null} The calculated answer (nonce) or null if no solution found
 * @throws {Error} If WASM is not initialized
 */
function solve(challenge, salt, difficulty, expireAt) {
    if (!wasmInstance) {
        throw new Error('WASM not initialized');
    }

    // Construct the prefix string as expected by the WASM logic
    const prefix = `${salt}_${expireAt}_`;

    // Allocate stack space for the return value (16 bytes)
    const retPtr = wasmInstance.__wbindgen_add_to_stack_pointer(-16);

    try {
        // Pass string arguments to WASM memory
        const challengePtr = passStringToWasm(challenge, wasmInstance.__wbindgen_export_0);
        const challengeLen = WASM_VECTOR_LEN;

        const prefixPtr = passStringToWasm(prefix, wasmInstance.__wbindgen_export_0);
        const prefixLen = WASM_VECTOR_LEN;

        // Call the solve function in WASM
        wasmInstance.wasm_solve(retPtr, challengePtr, challengeLen, prefixPtr, prefixLen, difficulty);

        // Read the result from the return pointer
        const memData = new DataView(wasmMemory.buffer);
        const success = memData.getInt32(retPtr + 0, true);
        const answer = memData.getFloat64(retPtr + 8, true);

        if (success !== 0) {
            return answer;
        } else {
            return null;
        }
    } finally {
        // Clean up stack pointer
        wasmInstance.__wbindgen_add_to_stack_pointer(16);
    }
}

async function runTest() {
    await init();
    const challenge = "49f3832737fe81c662d96b972f54e717fcc3bfe03ebcc5d3bd42f6944572ec61";
    const salt = "ef8e9dcebf44ed19a09f";
    const difficulty = 144000;
    const expireAt = 1773146020322;

    console.log("Solving challenge...");
    const start = Date.now();
    const answer = solve(challenge, salt, difficulty, expireAt);
    const end = Date.now();

    console.log("Answer:", answer);
    console.log("Expected: 24713");
    console.log("Time taken:", end - start, "ms");
}

if (require.main === module) {
    runTest();
}

module.exports = {init, solve};
