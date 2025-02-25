import whisper

# tiny/base/small/medium/large
model = whisper.load_model("base")
result = model.transcribe("C:/Users/Administrator/Desktop/output.mp3")
print(result["text"])
