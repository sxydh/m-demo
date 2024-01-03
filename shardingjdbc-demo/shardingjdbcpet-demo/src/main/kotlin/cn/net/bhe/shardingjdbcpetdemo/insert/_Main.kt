package cn.net.bhe.shardingjdbcpetdemo.insert

import cn.net.bhe.shardingjdbcpetdemo.helper.Conn.getConn
import cn.net.bhe.shardingjdbcpetdemo.helper.Data
import cn.net.bhe.shardingjdbcpetdemo.helper.Order
import com.alibaba.fastjson2.JSON
import java.io.BufferedReader
import java.io.FileReader
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

fun main() {
    val threads = 1
    val tableSize = 120000
    val allocSec = 1800
    val isBatch = true

    val files = Data.create(tableSize, threads, allocSec)
    val service = Executors.newFixedThreadPool(64)
    for (file in files) {
        service.execute {
            try {
                val res: Map<String, Any>
                val mills = measureTimeMillis {
                    res = doInsert(file, isBatch)
                }
                val size = res["size"] as Int
                val tps = (size / (mills / 1000.0)).toInt()
                val rtMap = TreeMap<Long, BigDecimal>()
                for (entry in (res["rtMap"] as Map<*, *>)) {
                    val value = ((entry.value as Int) * 1.0 / size * 100).toBigDecimal().setScale(2, RoundingMode.HALF_UP)
                    rtMap[entry.key as Long] = value
                }
                println("[${Thread.currentThread().name.padStart(16)}] size = $size, tps = $tps, rtMap = $rtMap")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun doInsert(file: String, isBatch: Boolean): Map<String, Any> {
    val conn = getConn()
    conn.use {
        val statement = conn.prepareStatement(
            " insert into t_order (order_id, order_number, user_id, order_date, order_status, payment_method, " +
                    " shipping_address, contact_number, email, shipping_company, tracking_number, " +
                    " shipping_cost, total_amount, discount_amount, paid_amount, invoice_title, " +
                    " tax_number, notes, promotion_id, create_time, update_time, rattr, rattr2, " +
                    " rattr3, rattr4, rattr5, rattr6, rattr7) " +
                    " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
        )
        statement.use {
            FileReader(file).use { fr ->
                BufferedReader(fr).use { br ->
                    var size = 0
                    val rtMap: HashMap<Long, Int> = HashMap()
                    while (true) {
                        val orderString = br.readLine()
                        if (orderString != null) {
                            val start = System.currentTimeMillis()
                            val order = JSON.parseObject(orderString, Order::class.java)
                            statement.setLong(1, order.orderId!!)
                            statement.setString(2, order.orderNumber)
                            statement.setLong(3, order.userId!!)
                            statement.setObject(4, order.orderDate)
                            statement.setString(5, order.orderStatus)
                            statement.setString(6, order.paymentMethod)
                            statement.setString(7, order.shippingAddress)
                            statement.setString(8, order.contactNumber)
                            statement.setString(9, order.email)
                            statement.setString(10, order.shippingCompany)
                            statement.setString(11, order.trackingNumber)
                            statement.setBigDecimal(12, order.shippingCost)
                            statement.setBigDecimal(13, order.totalAmount)
                            statement.setBigDecimal(14, order.discountAmount)
                            statement.setBigDecimal(15, order.paidAmount)
                            statement.setString(16, order.invoiceTitle)
                            statement.setString(17, order.taxNumber)
                            statement.setString(18, order.notes)
                            statement.setLong(19, order.promotionId!!)
                            statement.setObject(20, order.createTime)
                            statement.setObject(21, order.updateTime)
                            statement.setString(22, order.rattr)
                            statement.setString(23, order.rattr2)
                            statement.setString(24, order.rattr3)
                            statement.setString(25, order.rattr4)
                            statement.setString(26, order.rattr5)
                            statement.setString(27, order.rattr6)
                            statement.setString(28, order.rattr7)
                            if (isBatch) {
                                statement.addBatch()
                                if (size != 0 && size % 100 == 0) {
                                    statement.executeBatch()
                                }
                            } else {
                                statement.execute()
                            }
                            size++
                            val rt = (System.currentTimeMillis() - start) / 10 * 10
                            rtMap[rt] = if (rtMap[rt] == null) 1 else (rtMap[rt] as Int) + 1
                        } else {
                            if (isBatch) {
                                statement.executeBatch()
                            }
                            break
                        }
                    }
                    val res = HashMap<String, Any>()
                    res["size"] = size
                    res["rtMap"] = rtMap
                    return res
                }
            }
        }
    }
}