package cn.net.bhe.mysqlpetdemo.insert

import cn.net.bhe.mutil.*
import cn.net.bhe.mysqlpetdemo.helper.getConn
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.max
import kotlin.system.measureTimeMillis

val EXECUTOR_SERVICE: ExecutorService = Executors.newFixedThreadPool(64)
val SNOW_FLAKE = Snowflake(ProcessHandle.current().pid() % 1024)
val RANDOM = Random()
val QUEUE: LinkedBlockingQueue<Order> = LinkedBlockingQueue(10000)
var FLAG = false

fun main() {
    initTable(index = false)
    val threads = 32
    val tableSize = 20000000
    val allocSec = 1800
    val isBatch = true
    EXECUTOR_SERVICE.execute {
        try {
            initData(tableSize, allocSec)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    for (i in 0..<threads) {
        EXECUTOR_SERVICE.execute {
            try {
                val insertResult: Array<Long>
                val mills = measureTimeMillis {
                    insertResult = doInsert(isBatch)
                }
                println("[${Thread.currentThread().name.padStart(16)}] size = ${insertResult[0]}, tps = ${(insertResult[0] / (mills / 1000))}, maxMills = ${insertResult[1]}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun doInsert(isBatch: Boolean): Array<Long> {
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
            var index = 0
            var maxMills = 0L
            while (!FLAG) {
                val order = QUEUE.take()
                index++
                val start = System.currentTimeMillis()
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
                statement.addBatch()
                if (isBatch) {
                    if ((index != 0 && index % 100 == 0) || FLAG) {
                        statement.executeBatch()
                    }
                } else {
                    statement.execute()
                }
                maxMills = max(maxMills, System.currentTimeMillis() - start)
            }
            return arrayOf(index.toLong(), maxMills)
        }
    }
}

fun initData(n: Int, allocSec: Int) {
    val en3Arr = ArrayList<String>()
    for (i in 0..<100) {
        en3Arr.add(StrUtils.randomEn(3))
    }
    val userIds = ArrayList<Long>()
    for (i in 0..500) {
        userIds.add(SNOW_FLAKE.nextId())
    }
    val promIds = ArrayList<Long>()
    for (i in 0..100) {
        promIds.add(SNOW_FLAKE.nextId())
    }
    val prodMap = HashMap<Long, String>()
    for (i in 0..20) {
        prodMap[SNOW_FLAKE.nextId()] = StrUtils.randomChs(RANDOM.nextInt(7))
    }
    val prodIds = prodMap.keys.toList()
    val date = DtUtils.date()
    val os = arrayOf("U", "P", "D", "A", "L")
    val pm = arrayOf("A", "W", "C", "D")
    val saArr = ArrayList<String>()
    for (i in 0..<1000) {
        saArr.add(AddrUtils.ranChn(5))
    }
    val phArr = ArrayList<String>()
    for (i in 0..<1000) {
        phArr.add(StrUtils.randomPhone())
    }
    val scArr = ArrayList<String>()
    for (i in 0..<500) {
        scArr.add(CpUtils.ranChnCp())
    }
    val en5Arr = ArrayList<String>()
    for (i in 0..<500) {
        en5Arr.add(StrUtils.randomEn(5))
    }
    val scBigArr = ArrayList<BigDecimal>()
    for (i in 0..<100) {
        scBigArr.add(BigDecimal(RANDOM.nextInt(1000) / 100 + 10))
    }
    val taBigArr = ArrayList<BigDecimal>()
    for (i in 0..<100) {
        taBigArr.add(BigDecimal(RANDOM.nextInt(50000) / 100.0 + 200))
    }
    val daBigArr = ArrayList<BigDecimal>()
    for (i in 0..<100) {
        daBigArr.add(BigDecimal(RANDOM.nextInt(10000) / 100.0))
    }
    val itArr = ArrayList<String>()
    for (i in 0..<500) {
        itArr.add(CpUtils.ranChnCp())
    }
    val upBigArr = ArrayList<BigDecimal>()
    for (i in 0..<100) {
        upBigArr.add(BigDecimal(RANDOM.nextInt(80000) / 100.0))
    }
    val qtyBigArr = ArrayList<BigDecimal>()
    for (i in 0..<100) {
        qtyBigArr.add(BigDecimal(RANDOM.nextInt(2000) / 100.0))
    }
    val discBigArr = ArrayList<BigDecimal>()
    for (i in 0..<100) {
        discBigArr.add(BigDecimal(RANDOM.nextInt(15000) / 100.0))
    }
    val rattrArr = ArrayList<String>()
    for (i in 0..<100) {
        rattrArr.add(WdUtils.random(RANDOM.nextInt(10) + 1))
    }
    for (i in 0..<n) {
        val order = Order()
        order.orderId = SNOW_FLAKE.nextId()
        order.orderNumber = en3Arr[RANDOM.nextInt(en3Arr.size)] + order.orderId
        order.userId = userIds[RANDOM.nextInt(userIds.size)]
        order.orderDate = DtUtils.addSeconds(date, RANDOM.nextInt(allocSec).toLong())
        order.orderStatus = os[RANDOM.nextInt(os.size)]
        order.paymentMethod = pm[RANDOM.nextInt(pm.size)]
        order.shippingAddress = saArr[RANDOM.nextInt(saArr.size)]
        order.contactNumber = phArr[RANDOM.nextInt(phArr.size)]
        order.email = null
        order.shippingCompany = scArr[RANDOM.nextInt(scArr.size)]
        order.trackingNumber = en5Arr[RANDOM.nextInt(en5Arr.size)] + order.orderId.toString().substring(8)
        order.shippingCost = scBigArr[RANDOM.nextInt(scBigArr.size)]
        order.totalAmount = taBigArr[RANDOM.nextInt(taBigArr.size)]
        order.discountAmount = daBigArr[RANDOM.nextInt(daBigArr.size)]
        order.paidAmount = order.totalAmount!!.subtract(order.discountAmount)
        order.invoiceTitle = itArr[RANDOM.nextInt(itArr.size)]
        order.taxNumber = en5Arr[RANDOM.nextInt(en5Arr.size)] + order.orderId.toString().substring(11)
        order.notes = "${order.orderNumber}###${order.orderStatus}###${order.paymentMethod}###${order.totalAmount}"
        order.promotionId = promIds[RANDOM.nextInt(promIds.size)]
        order.createTime = order.orderDate
        order.updateTime = order.orderDate
        val rattrFun = { if (RANDOM.nextBoolean()) rattrArr[RANDOM.nextInt(rattrArr.size)] else null }
        order.rattr = rattrFun()
        order.rattr2 = rattrFun()
        order.rattr3 = null
        order.rattr4 = null
        order.rattr5 = null
        order.rattr6 = null
        order.rattr7 = null
        order.orderDetailList = ArrayList()
        for (li in 0..<RANDOM.nextInt(4)) {
            val orderDetail = OrderDetail()
            orderDetail.detailId = SNOW_FLAKE.nextId()
            orderDetail.orderId = order.orderId
            orderDetail.productId = prodIds[RANDOM.nextInt(prodIds.size)]
            orderDetail.productName = prodMap[orderDetail.productId]
            orderDetail.unitPrice = upBigArr[RANDOM.nextInt(upBigArr.size)]
            orderDetail.quantity = qtyBigArr[RANDOM.nextInt(qtyBigArr.size)]
            orderDetail.subtotal = orderDetail.unitPrice!!.multiply(orderDetail.quantity)
            orderDetail.discount = discBigArr[RANDOM.nextInt(discBigArr.size)]
            orderDetail.productStatus = order.orderStatus
            order.orderDetailList!!.add(orderDetail)
        }
        QUEUE.put(order)
        if (i != 0 && i % 1000000 == 0) {
            println(i)
        }
    }
    FLAG = true
}

@Synchronized
fun initTable(index: Boolean) {
    val conn = getConn()
    conn.use {
        val statement = conn.createStatement()
        statement.use {
            statement.execute(
                " create table if not exists t_order ( " +
                        " order_id bigint, " +
                        " order_number varchar(20), " +
                        " user_id bigint, " +
                        " order_date datetime, " +
                        " order_status char(1), " +
                        " payment_method char(1), " +
                        " shipping_address varchar(200), " +
                        " contact_number char(11), " +
                        " email varchar(50), " +
                        " shipping_company varchar(200), " +
                        " tracking_number varchar(20), " +
                        " shipping_cost decimal(10, 2), " +
                        " total_amount decimal(10, 2), " +
                        " discount_amount decimal(10, 2), " +
                        " paid_amount decimal(10, 2), " +
                        " invoice_title varchar(50), " +
                        " tax_number varchar(20), " +
                        " notes varchar(200), " +
                        " promotion_id bigint, " +
                        " create_time datetime, " +
                        " update_time datetime, " +
                        " rattr varchar(100), " +
                        " rattr2 varchar(100), " +
                        " rattr3 varchar(100), " +
                        " rattr4 varchar(100), " +
                        " rattr5 varchar(100), " +
                        " rattr6 varchar(100), " +
                        " rattr7 varchar(100) " +
                        " ) "
            )
            if (index) {
                try {
                    statement.execute(" alter table t_order add primary key (order_id) ")
                    statement.execute(" create unique index idx_order_number on t_order (order_number) ")
                    statement.execute(" create index idx_order_date on t_order (order_date) ")
                    statement.execute(" create index idx_create_time on t_order (create_time) ")
                    statement.execute(" create index idx_update_time on t_order (update_time) ")
                    statement.execute(" create index idx_order_date_user_id on t_order (order_date, user_id) ")
                    statement.execute(" create index idx_order_date_order_status on t_order (order_date, order_status) ")
                    statement.execute(" create index idx_order_date_payment_method on t_order (order_date, payment_method) ")
                } catch (e: Exception) {
                    // TODO NOTHING
                }
            }
            statement.execute(
                " create table if not exists t_order_li ( " +
                        " detail_id bigint, " +
                        " order_id bigint, " +
                        " product_id bigint, " +
                        " product_name varchar(200), " +
                        " unit_price decimal(10, 2), " +
                        " quantity decimal(10, 2), " +
                        " subtotal decimal(10, 2), " +
                        " discount decimal(10, 2) ," +
                        " product_status char(1), " +
                        " primary key (detail_id) " +
                        " ) "
            )
        }
    }
}


class Order {
    var orderId: Long? = null
    var orderNumber: String? = null
    var userId: Long? = null
    var orderDate: Date? = null
    var orderStatus: String? = null
    var paymentMethod: String? = null
    var shippingAddress: String? = null
    var contactNumber: String? = null
    var email: String? = null
    var shippingCompany: String? = null
    var trackingNumber: String? = null
    var shippingCost: BigDecimal? = null
    var totalAmount: BigDecimal? = null
    var discountAmount: BigDecimal? = null
    var paidAmount: BigDecimal? = null
    var invoiceTitle: String? = null
    var taxNumber: String? = null
    var notes: String? = null
    var promotionId: Long? = null
    var createTime: Date? = null
    var updateTime: Date? = null
    var rattr: String? = null
    var rattr2: String? = null
    var rattr3: String? = null
    var rattr4: String? = null
    var rattr5: String? = null
    var rattr6: String? = null
    var rattr7: String? = null
    var orderDetailList: ArrayList<OrderDetail>? = null
}


class OrderDetail {
    var detailId: Long? = null
    var orderId: Long? = null
    var productId: Long? = null
    var productName: String? = null
    var unitPrice: BigDecimal? = null
    var quantity: BigDecimal? = null
    var subtotal: BigDecimal? = null
    var discount: BigDecimal? = null
    var productStatus: String? = null
}