import base64
import xml.etree.ElementTree as ET


def decode_node(node):
    if node is None or node.text is None:
        return ""

    text = node.text.strip()
    if node.get("base64") == "true":
        try:
            return base64.b64decode(text).decode("utf-8", errors="replace")
        except Exception:
            return base64.b64decode(text).decode("latin1", errors="replace")

    return text


def parse_burp_xml(input_file, output_file):
    tree = ET.parse(input_file)
    root = tree.getroot()

    with open(output_file, "w", encoding="utf-8") as out:
        for idx, item in enumerate(root.findall("item"), 1):
            time = item.findtext("time", "")
            url = item.findtext("url", "")
            host = item.findtext("host", "")
            port = item.findtext("port", "")
            method = item.findtext("method", "")
            path = item.findtext("path", "")

            request = decode_node(item.find("request"))
            response = decode_node(item.find("response"))

            out.write(f"===== ITEM {idx} =====\n")
            out.write(f"Time: {time}\n")
            out.write(f"URL: {url}\n")
            out.write(f"Host: {host}:{port}\n")
            out.write(f"Method: {method}\n")
            out.write(f"Path: {path}\n\n")

            out.write("----- REQUEST -----\n")
            out.write(request + "\n\n")

            out.write("----- RESPONSE -----\n")
            out.write(response + "\n\n")

            out.write("=" * 80 + "\n\n")


if __name__ == "__main__":
    input_file = r"C:\Users\Administrator\Desktop\1040"
    output_file = f"{input_file}_output"
    parse_burp_xml(input_file, output_file)
    print(f"Done. Output written to {output_file}")
