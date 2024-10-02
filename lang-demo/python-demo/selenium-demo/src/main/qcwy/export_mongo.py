from m_pyutil.mmongo import MongoCli

if __name__ == '__main__':
    mongo_cli = MongoCli(host='192.168.233.129',
                         database='qcwy')
    mongo_cli.import_sqlite(f='qcwy.db',
                            table='qcwy_job',
                            sql='select uid, queue_uid, raw from qcwy_job order by id',
                            collection='qcwy_job')
    mongo_cli.close()
