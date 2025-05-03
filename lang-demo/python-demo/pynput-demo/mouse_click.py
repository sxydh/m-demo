from pynput import mouse

def on_click(x, y, _, pressed):
    if pressed:
        print(f"鼠标点击位置: ({x}, {y})")

# 监听鼠标事件
with mouse.Listener(on_click=on_click) as listener:
    listener.join()
