import json

from m_pyutil import mhttp
from m_pyutil.mhttp import MyHTTPRequestHandler
from m_pyutil.msqlite import selectd

from src.main.ssq._ssq_app import DB_FILE


def get_handler(handler: MyHTTPRequestHandler):
    rows = selectd(sql='select d, r, r2, r3, r4, r5, r6, b from t_ssq order by d desc limit 30',
                   f=DB_FILE)
    handler.send_response(200)
    handler.send_header('Content-type', 'application/json')
    handler.end_headers()
    handler.wfile.write(json.dumps(rows).encode('utf-8'))


if __name__ == '__main__':
    mhttp.Server(get_handler=get_handler)
