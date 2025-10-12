import json
import os
from collections import deque
from datetime import datetime

import requests
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import TextLoader
from langchain_community.vectorstores import FAISS
from langchain_huggingface import HuggingFaceEmbeddings


class RAGSystem:

    def __init__(self, url, model):
        self.url = url
        self.model = model
        self.text_splitter = None
        self.embeddings = None
        self.vector_store = None
        self.retriever = None
        self.history = deque(maxlen=100)
        self.setup_components()

    def setup_components(self):
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=1000,
            chunk_overlap=200,
            separators=["\n\n", "\n", "。", "！", "？", "……", ""],
            add_start_index=True
        )

        self.embeddings = HuggingFaceEmbeddings(
            model_name="sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
            model_kwargs={'device': 'cpu'},
            encode_kwargs={'normalize_embeddings': True}
        )

    def process_documents(self, file_paths):
        documents = []
        for file_path in file_paths:
            try:
                loader = TextLoader(file_path, encoding='utf-8')
                doc_list = loader.load()
                for doc in doc_list:
                    doc.metadata.update({
                        'file_path': file_path,
                        'file_name': os.path.basename(file_path),
                        'file_type': os.path.splitext(file_path)[1],
                        'load_time': datetime.now().isoformat()
                    })

                documents.extend(doc_list)
                print(f"TextLoader.load: {file_path}")
            except Exception as e:
                print(e)

        if not documents:
            print(f"documents is empty")
            return

        chunks = self.text_splitter.split_documents(documents)
        self.vector_store = FAISS.from_documents(chunks, self.embeddings)
        self.retriever = self.vector_store.as_retriever(
            search_type="mmr",
            search_kwargs={
                "k": 6,  # 最终返回的文档数量
                "fetch_k": 10,  # 初始检索的文档数量
                "lambda_mult": 0.7  # 多样性与相关性的平衡系数
            }
        )
        print(f"documents.len: {len(documents)}")

    def query_stream(self, question):
        relevant_docs = self.retriever.invoke(question)

        print(f"relevant_documents.len: {len(relevant_docs)}")
        for i, doc in enumerate(relevant_docs):
            file_name = doc.metadata.get('file_name')
            print(f"relevant_documents[{i}].file_name: {file_name}")

        context = "\n\n".join(doc.page_content for doc in relevant_docs)
        system_message = f"""
参考文档：
{context}

结合参考文档和历史对话回答问题"""

        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": system_message},
                *self.history,
                {"role": "user", "content": question}
            ],
            "stream": True
        }
        response = requests.post(
            self.url,
            json=payload,
            stream=True
        )

        full_response = ""
        for line in response.iter_lines():
            if line:
                data = json.loads(line)
                if "message" in data and "content" in data["message"]:
                    chunk = data["message"]["content"]
                    full_response += chunk
                    print(chunk, end="", flush=True)
                if data.get("done", False):
                    break

        self.history.append({"role": "user", "content": question})
        self.history.append({"role": "assistant", "content": full_response})


def main():
    rag_system = RAGSystem(
        url="http://localhost:11434/api/chat",
        model="huihui_ai/deepseek-r1-abliterated:70b"
    )
    rag_system.process_documents(["./document.txt"])

    while True:
        question = input("\nquestion ('quit' to exit): \n")
        if question.lower() == 'quit':
            break

        try:
            rag_system.query_stream(question)
        except Exception as e:
            print(e)


if __name__ == "__main__":
    main()
