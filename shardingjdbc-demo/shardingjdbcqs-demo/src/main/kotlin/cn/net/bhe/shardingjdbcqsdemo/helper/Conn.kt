package cn.net.bhe.shardingjdbcqsdemo.helper

import com.alibaba.druid.pool.DruidDataSource
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory
import java.sql.Connection
import java.util.*
import javax.sql.DataSource

object Conn {

    private val DATA_SOURCE: DataSource

    init {
        try {
            val dataSourceMap = HashMap<String, DataSource>()
            val dataSource1 = DruidDataSource()
            dataSource1.driverClassName = "com.mysql.cj.jdbc.Driver"
            dataSource1.url = "jdbc:mysql://192.168.233.129:3306/sj_demo"
            dataSource1.username = "root"
            dataSource1.password = "123"
            dataSource1.initialSize = 32
            dataSource1.minIdle = 32
            dataSource1.maxActive = 64
            dataSourceMap["ds0"] = dataSource1

            val dataSource2 = DruidDataSource()
            dataSource2.driverClassName = "com.mysql.cj.jdbc.Driver"
            dataSource2.url = "jdbc:mysql://192.168.233.130:3306/sj_demo"
            dataSource2.username = "root"
            dataSource2.password = "123"
            dataSource1.initialSize = 32
            dataSource1.minIdle = 32
            dataSource1.maxActive = 64
            dataSourceMap["ds1"] = dataSource2

            val tableRuleConfig = TableRuleConfiguration("t_order", "ds\${0..1}.t_order\${0..3}")
            tableRuleConfig.databaseShardingStrategyConfig = InlineShardingStrategyConfiguration("user_id", "ds\${user_id % 2}")
            tableRuleConfig.tableShardingStrategyConfig = InlineShardingStrategyConfiguration("order_id", "t_order\${order_id % 4}")

            val shardingRuleConfig = ShardingRuleConfiguration()
            shardingRuleConfig.tableRuleConfigs.add(tableRuleConfig)
            DATA_SOURCE = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, Properties())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun getConn(): Connection {
        return DATA_SOURCE.connection
    }

}