#!/bin/bash
for host in hadoop01 hadoop02 hadoop03
do
    echo ====== $host ======
    ssh $host jps
done