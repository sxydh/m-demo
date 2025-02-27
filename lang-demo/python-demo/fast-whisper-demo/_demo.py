from faster_whisper import WhisperModel

# https://github.com/SYSTRAN/faster-whisper
model = WhisperModel("medium", device="cuda", compute_type="float16")

segments, info = model.transcribe("C:/Users/Administrator/Desktop/output.mp3", beam_size=5)
print("Detected language '%s' with probability %f" % (info.language, info.language_probability))
for segment in segments:
    print("[%.2fs -> %.2fs] %s" % (segment.start, segment.end, segment.text))
