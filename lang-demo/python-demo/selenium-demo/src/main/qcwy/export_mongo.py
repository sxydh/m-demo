from m_pyutil.mmongo import MongoCli

if __name__ == '__main__':
    # 需要额外执行以下脚本
    # db.qcwy_job.find({}).forEach(e => {
    #     db.qcwy_job.updateOne({ _id: e._id }, { $set: { raw: JSON.parse(e.raw) } });
    # });

    mongo_cli = MongoCli(host='192.168.233.129',
                         database='qcwy')
    mongo_cli.import_sqlite(f='qcwy.db',
                            table='qcwy_job',
                            sql='select raw from qcwy_job order by id',
                            collection='qcwy_job')
    mongo_cli.close()
