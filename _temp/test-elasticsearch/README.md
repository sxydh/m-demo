# Profile

Elasticsearch测试

## Quick Start

### TestRefreshPolicy

测试RefreshPolicy不同策略下的性能：NONE，IMMEDIATE。

```bat
# 并发多个客户端
start cmd /k java -Xms256m -Xmx256m -DrefreshPolicy=IMMEDIATE -DindexName=test_index01 -DproducerCount=5 -DconsumerCount=20 -DmaxSize=1000 -DappId=01 -DidPrefix= -DvaluePrefix= -cp test-elasticsearch-1.0-SNAPSHOT-jar-with-dependencies.jar cn.net.bhe.testelasticsearch.TestRefreshPolicy
start cmd /k java -Xms256m -Xmx256m -DrefreshPolicy=IMMEDIATE -DindexName=test_index01 -DproducerCount=5 -DconsumerCount=20 -DmaxSize=1000 -DappId=02 -DidPrefix= -DvaluePrefix= -cp test-elasticsearch-1.0-SNAPSHOT-jar-with-dependencies.jar cn.net.bhe.testelasticsearch.TestRefreshPolicy
start cmd /k java -Xms256m -Xmx256m -DrefreshPolicy=IMMEDIATE -DindexName=test_index01 -DproducerCount=5 -DconsumerCount=20 -DmaxSize=1000 -DappId=03 -DidPrefix= -DvaluePrefix= -cp test-elasticsearch-1.0-SNAPSHOT-jar-with-dependencies.jar cn.net.bhe.testelasticsearch.TestRefreshPolicy
start cmd /k java -Xms256m -Xmx256m -DrefreshPolicy=IMMEDIATE -DindexName=test_index01 -DproducerCount=5 -DconsumerCount=20 -DmaxSize=1000 -DappId=04 -DidPrefix= -DvaluePrefix= -cp test-elasticsearch-1.0-SNAPSHOT-jar-with-dependencies.jar cn.net.bhe.testelasticsearch.TestRefreshPolicy
start cmd /k java -Xms256m -Xmx256m -DrefreshPolicy=IMMEDIATE -DindexName=test_index01 -DproducerCount=5 -DconsumerCount=20 -DmaxSize=1000 -DappId=05 -DidPrefix= -DvaluePrefix= -cp test-elasticsearch-1.0-SNAPSHOT-jar-with-dependencies.jar cn.net.bhe.testelasticsearch.TestRefreshPolicy

# 合并csv文件
copy *.csv merge.csv
```
