import m_pyutil.msqlite


def get_sqlite_connection(f='anjuke.db'):
    return m_pyutil.msqlite.get_conn(f)
