/**
 * This is a command-line interface for chatting with DeepSeek API.
 * It handles session management, PoW challenge solving, and streaming responses.
 */

const readline = require('readline');
const fs = require('fs');
const path = require('path');
const {init, solve} = require('./tools/solve_challenge.js');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const LOG_FILE = path.join(__dirname, 'chat.log');

function logToFile(message) {
    fs.appendFileSync(LOG_FILE, message + '\n', 'utf8');
}

/**
 * Prompts the user for input.
 *
 * @param {string} question - The prompt text
 * @returns {Promise<string>} The user's input
 */
function ask(question) {
    return new Promise(resolve => rl.question(question, resolve));
}

// Configuration constants
const API_BASE_URL = "https://chat.deepseek.com/api/v0";
const USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
const CLIENT_VERSION = "1.7.0";
const APP_VERSION = "20241129.1";

async function createSession(headers) {
    logToFile(`[${new Date().toISOString()}] Creating new session...`);
    const createResponse = await fetch(`${API_BASE_URL}/chat_session/create`, {
        method: "POST",
        headers: headers,
        body: JSON.stringify({})
    });

    if (!createResponse.ok) {
        throw new Error(`Failed to create session: ${createResponse.status} ${await createResponse.text()}`);
    }

    const createData = await createResponse.json();
    const sessionId = createData.data?.biz_data?.id;
    if (!sessionId) {
        throw new Error("Failed to retrieve session ID from response: " + JSON.stringify(createData));
    }
    logToFile(`[${new Date().toISOString()}] Created Session ID: ${sessionId}`);
    return sessionId;
}

async function deleteSession(headers, sessionId) {
    logToFile(`[${new Date().toISOString()}] Deleting session ${sessionId}...`);
    try {
        const deleteResponse = await fetch(`${API_BASE_URL}/chat_session/delete`, {
            method: "POST",
            headers: headers,
            body: JSON.stringify({ "chat_session_id": sessionId })
        });
        if (deleteResponse.ok) {
            logToFile(`[${new Date().toISOString()}] Session deleted successfully.`);
        } else {
            logToFile(`[${new Date().toISOString()}] Failed to delete session: ${deleteResponse.status}`);
        }
    } catch (err) {
        logToFile(`[${new Date().toISOString()}] Error deleting session: ${err}`);
    }
}

async function fetchChallenge(headers) {
    logToFile(`[${new Date().toISOString()}] Fetching challenge...`);
    const challengeResponse = await fetch(`${API_BASE_URL}/chat/create_pow_challenge`, {
        method: "POST",
        headers: headers,
        body: JSON.stringify({
            "target_path": "/api/v0/chat/completion"
        })
    });

    if (!challengeResponse.ok) {
        console.error("Failed to fetch challenge:", challengeResponse.status, await challengeResponse.text());
        return null;
    }

    const challengeData = await challengeResponse.json();
    const challengeObj = challengeData.data?.biz_data?.challenge;
    if (!challengeObj) {
        console.error("Invalid challenge response format:", JSON.stringify(challengeData));
        return null;
    }
    logToFile(`[${new Date().toISOString()}] Challenge received. Difficulty: ${challengeObj.difficulty}`);
    return challengeObj;
}

async function processStream(reader, parentMessageIdCallback) {
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop(); 

        for (const line of lines) {
            const trimmedLine = line.trim();
            if (!trimmedLine) continue;

            if (trimmedLine.startsWith('data: ')) {
                const dataStr = trimmedLine.slice(6);
                if (dataStr === '[DONE]') continue;
                try {
                    const data = JSON.parse(dataStr);

                    if (data.response_message_id) {
                        parentMessageIdCallback(data.response_message_id);
                    }

                    if (data.v) {
                        if (typeof data.v === 'string') {
                            process.stdout.write(data.v);
                        } else if (data.v.response && data.v.response.fragments) {
                            for (const fragment of data.v.response.fragments) {
                                if (fragment.content) {
                                    process.stdout.write(fragment.content);
                                }
                            }
                        }
                    }
                } catch (e) {
                    // Ignore parse errors
                }
            }
        }
    }
}

async function main() {
    let sessionId;
    let isNewSession = false;
    let commonHeaders;

    try {
        logToFile(`[${new Date().toISOString()}] Initializing WASM...`);
        await init();
        logToFile(`[${new Date().toISOString()}] WASM Initialized.`);

        const token = await ask("Enter Token (Bearer ...): ");
        const cleanToken = token.replace(/^Bearer\s+/i, '').trim();

        sessionId = await ask("Enter Session ID (optional, press Enter to create new): ");
        
        commonHeaders = {
            "content-type": "application/json",
            "authorization": `Bearer ${cleanToken}`,
            "user-agent": USER_AGENT,
            "x-client-platform": "web",
            "x-client-version": CLIENT_VERSION,
            "x-app-version": APP_VERSION
        };

        if (!sessionId) {
            sessionId = await createSession(commonHeaders);
            isNewSession = true;
        }

        let parentMessageId = null;

        while (true) {
            const prompt = await ask("\nMe: ");
            if (prompt.toLowerCase() === 'exit') break;

            const challengeObj = await fetchChallenge(commonHeaders);
            if (!challengeObj) continue;

            const start = Date.now();
            const answer = solve(
                challengeObj.challenge,
                challengeObj.salt,
                challengeObj.difficulty,
                challengeObj.expire_at
            );
            logToFile(`[${new Date().toISOString()}] Solved in ${Date.now() - start}ms. Answer: ${answer}`);

            const powResponseObj = {
                algorithm: challengeObj.algorithm,
                challenge: challengeObj.challenge,
                salt: challengeObj.salt,
                answer: answer,
                signature: challengeObj.signature,
                target_path: challengeObj.target_path
            };
            
            const powResponseBase64 = Buffer.from(JSON.stringify(powResponseObj)).toString('base64');

            logToFile(`[${new Date().toISOString()}] Sending completion request...`);
            
            const completionHeaders = {
                ...commonHeaders,
                "x-ds-pow-response": powResponseBase64
            };

            const completionResponse = await fetch(`${API_BASE_URL}/chat/completion`, {
                method: "POST",
                headers: completionHeaders,
                body: JSON.stringify({
                    "chat_session_id": sessionId,
                    "parent_message_id": parentMessageId,
                    "prompt": prompt,
                    "ref_file_ids": [],
                    "thinking_enabled": false,
                    "search_enabled": false,
                    "preempt": false
                })
            });

            if (!completionResponse.ok) {
                console.error("Completion request failed:", completionResponse.status, await completionResponse.text());
                continue;
            }

            process.stdout.write("DeepSeek: ");
            
            await processStream(completionResponse.body.getReader(), (id) => {
                parentMessageId = id;
            });
            
            console.log("\n");
        }
    } catch (error) {
        console.error("Error:", error);
    } finally {
        if (isNewSession && sessionId && commonHeaders) {
            await deleteSession(commonHeaders, sessionId);
        }
        rl.close();
    }
}

main();
