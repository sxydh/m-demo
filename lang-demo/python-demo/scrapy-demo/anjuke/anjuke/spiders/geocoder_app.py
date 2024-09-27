import logging
import os
import time

from m_pyutil.msqlite import select_one, save
from m_pyutil.mtencent_map import TencentMap

DB_FILE = "anjuke.db"


def geocoder_handler():
    tencent_map = TencentMap(key=os.environ.get("TENCENT_MAP_KEY"))
    while True:
        new_house = select_one(sql="select id, city, address from anjuke_new_house where location is null limit 1",
                               f=DB_FILE)
        if not new_house:
            break

        city = new_house[1]
        address = new_house[2]
        if not address or len(address) == 0:
            return
        address = address.split("]")[1]
        address = f"{city}市{address}"

        response = tencent_map.geocoder(address=address)
        if response.status_code != 200:
            logging.warning(f"{address} geocoder failed, status_code: {response.status_code}")
            continue
        result = response.json()
        status = result.get("status")
        if status != 0:
            logging.warning(f"{address} geocoder failed, status: {status}")
            continue
        location = result.get("result").get("location")
        lng = location.get("lng")
        lat = location.get("lat")
        location = f"{lng},{lat}"

        save(sql="update anjuke_new_house set location=? where id=?",
             params=[location, new_house[0]],
             f=DB_FILE)
        time.sleep(0.2)


if __name__ == '__main__':
    geocoder_handler()
