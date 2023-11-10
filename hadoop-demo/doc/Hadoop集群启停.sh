#!/bin/bash
if [ $# -lt 1 ]
then
    echo "No Args Input..."
    exit;
fi

case $1 in
"start")
    echo "====== 启动 ======"
    echo "------ 启动HDFS ------"
    ssh hadoop01 "/opt/module/hadoop/hadoop-3.1.3/sbin/start-dfs.sh"
    echo "------ 启动YARN ------"
    ssh hadoop02 "/opt/module/hadoop/hadoop-3.1.3/sbin/start-yarn.sh"
    echo "------ 启动HistoryServer ------"
    ssh hadoop01 "/opt/module/hadoop/hadoop-3.1.3/bin/mapred --daemon start historyserver"
;;
"stop")
    echo "====== 停止 ======"
    echo "------ 停止HistoryServer ------"
    ssh hadoop01 "/opt/module/hadoop/hadoop-3.1.3/bin/mapred --daemon stop historyserver"
    echo "------ 停止YARN ------"
    ssh hadoop02 "/opt/module/hadoop/hadoop-3.1.3/sbin/stop-yarn.sh"
    echo "------ 停止HDFS ------"
    ssh hadoop01 "/opt/module/hadoop/hadoop-3.1.3/sbin/stop-dfs.sh"
;;
*)
    echo "Input Args Error..."
;;
esac