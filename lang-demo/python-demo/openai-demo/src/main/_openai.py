import logging
import sys

import httpx
from openai import OpenAI

from definition import root

logging.basicConfig(
    level=logging.INFO,
    encoding="utf-8",
    filename='_openai.log',
    format='%(asctime)s - %(levelname)s - %(message)s'
)

client = OpenAI(
    api_key=sys.argv[1],
    http_client=httpx.Client(proxies="socks5://127.0.0.1:10808")
)

prompt = ""
file = open(f'{root}/input/prompt.txt', 'r', encoding='UTF-8')
for line in file:
    prompt += line

messages = [{"role": "system", "content": prompt}]
logging.info(messages[-1])
question = ""
while True:
    line = sys.stdin.readline()
    if line != "\\n\n":
        question = question + line
        continue
    message = {"role": "user", "content": question}
    messages.append(message)
    question = ""

    logging.info(messages[-1])
    completion = client.chat.completions.create(
        model="gpt-4",
        messages=messages
    )

    answer = completion.choices[0].message.content
    print(answer)
    logging.info(answer)
