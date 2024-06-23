import sys

import httpx
from openai import OpenAI

from definition import root

client = OpenAI(
    api_key=sys.argv[1],
    http_client=httpx.Client(proxies="socks5://127.0.0.1:10808")
)

prompt = ""
file = open(f'{root}/input/prompt.txt', 'r', encoding='UTF-8')
for line in file:
    prompt += line

messages = [{"role": "system", "content": prompt}]
content = ""
while True:
    line = input()
    if line != "\\n":
        content = content + line
        continue
    message = {"role": "user", "content": content}
    messages.append(message)

    completion = client.chat.completions.create(
        model="gpt-4",
        messages=messages
    )

    content = completion.choices[0].message.content
    print(content)
