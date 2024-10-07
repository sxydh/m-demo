// qcwy_job 清空数据
db.qcwy_job.deleteMany({});

// qcwy_job 转换字段
db.qcwy_job.find({}).forEach(e => {
    db.qcwy_job.updateOne(
        {
            _id: e._id
        },
        {
            $set: { raw: JSON.parse(e.raw) }
        }
    );
});

// qcwy_job 查询职位列表
db.qcwy_job.aggregate(
    {
        $unwind: '$raw.resultbody.job.items'
    },
    {
        $project: {
            uid: '$uid',
            queue_uid: '$queue_uid',
            item: '$raw.resultbody.job.items'
        }
    },
    {
        $project: {
            uid: 1,
            queue_uid: 1,
            item: 1,
            company: '$item.companyName',
            job_name: '$item.jobName',
            job_work_year: '$item.workYearString',
            job_salary_min: '$item.jobSalaryMin',
            job_salary_max: '$item.jobSalaryMax'
        }
    },
    {
        $sort: {
            job_salary_min: 1
        }
    }
);

// qcwy_job 统计职位数量（职能维度）
db.qcwy_job.aggregate(
    {
        $project: {
            uid: 1,
            queue_uid: 1,
            total_count: {
                $size: '$raw.resultbody.job.items'
            }
        }
    },
    {
        $group: {
            _id: '$queue_uid',
            count: { $sum: '$total_count' }
        }
    },
    {
        $sort: {
            count: -1
        }
    }
);


db.qcwy_job.aggregate(
    {
        $group: {
            _id: {
                queue_uid: '$queue_uid',
                uid: {
                    $arrayElemAt: [
                        {
                            $split: ['$uid', '&pageNum=']
                        },
                        0
                    ]
                }
            },
            total_count: {
                $first: '$raw.resultbody.job.totalCount'
            }
        }
    },
    {
        $group: {
            _id: {
                queue_uid: '$_id.queue_uid',
                uid: {
                    $arrayElemAt: [
                        {
                            $split: ['$_id.uid', '&companySize=']
                        },
                        0
                    ]
                }
            },
            total_count: {
                $sum: '$total_count'
            }
        }
    },
    {
        $project: {
            _id: '$_id.queue_uid',
            total_count: '$total_count'
        }
    },
    {
        $sort: {
            total_count: -1
        }
    }
);