import uiautomator2 as u2

# 如果有多个安卓设备则需要指定设备标识
d = u2.connect()
print(d.info)
