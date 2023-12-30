package cn.net.bhe.mysqlpetdemo.insert

import cn.net.bhe.mutil.*
import cn.net.bhe.mysqlpetdemo.helper.getConn
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

fun main() {
    initTable()
    val executorService = Executors.newFixedThreadPool(20)
    for (i in 0..<1) {
        executorService.execute {
            try {
                val data = initData(10000, 120)
                val mills = measureTimeMillis {
                    doInsert(data)
                }
                println("[${Thread.currentThread().name}] mills = $mills, size = ${data.size}, tps = ${data.size / (mills / 1000)}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun doInsert(list: List<Order>) {
    val conn = getConn()
    conn.use {
        val statement = conn.prepareStatement(
            " insert into t_order (order_id, order_number, user_id, order_date, order_status, payment_method, " +
                    " shipping_address, contact_number, email, shipping_company, tracking_number, " +
                    " shipping_cost, total_amount, discount_amount, paid_amount, invoice_title, " +
                    " tax_number, notes, promotion_id) " +
                    " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
        )
        statement.use {
            for ((index, order) in list.withIndex()) {
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
                statement.addBatch()
                if (index % 100 == 0) {
                    statement.executeBatch()
                }
            }
            statement.executeBatch()
        }
    }
}

fun initData(n: Int, allocSec: Int): List<Order> {
    val snowflake = Snowflake(Thread.currentThread().threadId())
    val random = Random()
    val userIds = ArrayList<Long>()
    for (i in 0..500) {
        userIds.add(snowflake.nextId())
    }
    val promIds = ArrayList<Long>()
    for (i in 0..100) {
        promIds.add(snowflake.nextId())
    }
    val prodMap = HashMap<Long, String>()
    for (i in 0..20) {
        prodMap[snowflake.nextId()] = StrUtils.randomChs(random.nextInt(7))
    }
    val prodIds = prodMap.keys.toList()
    val date = DtUtils.date()
    val os = arrayOf("U", "P", "D", "A", "L")
    val pm = arrayOf("A", "W", "C", "D")
    val list = ArrayList<Order>()
    for (i in 0..<n) {
        val order = Order()
        order.orderId = snowflake.nextId()
        order.orderNumber = StrUtils.randomEn(3) + order.orderId
        order.userId = userIds[random.nextInt(userIds.size)]
        order.orderDate = DtUtils.addSeconds(date, random.nextInt(allocSec).toLong())
        order.orderStatus = os[random.nextInt(os.size)]
        order.paymentMethod = pm[random.nextInt(pm.size)]
        order.shippingAddress = AddrUtils.ranChn(5)
        order.contactNumber = StrUtils.randomPhone()
        order.email = null
        order.shippingCompany = CpUtils.ranChnCp()
        order.trackingNumber = StrUtils.randomEn(5) + order.orderId.toString().substring(8)
        order.shippingCost = BigDecimal(random.nextInt(1000) / 100 + 10)
        order.totalAmount = BigDecimal(random.nextInt(50000) / 100.0 + 200)
        order.discountAmount = BigDecimal(random.nextInt(10000) / 100.0)
        order.paidAmount = order.totalAmount!!.subtract(order.discountAmount)
        order.invoiceTitle = CpUtils.ranChnCp()
        order.taxNumber = StrUtils.randomEn(7) + order.orderId.toString().substring(11)
        order.notes = "${order.orderNumber}###${order.orderStatus}###${order.paymentMethod}###${order.totalAmount}"
        order.promotionId = promIds[random.nextInt(promIds.size)]
        order.orderDetailList = ArrayList()
        for (li in 0..<random.nextInt(10)) {
            val orderDetail = OrderDetail()
            orderDetail.detailId = snowflake.nextId()
            orderDetail.orderId = order.orderId
            orderDetail.productId = prodIds[random.nextInt(prodIds.size)]
            orderDetail.productName = prodMap[orderDetail.productId]
            orderDetail.unitPrice = BigDecimal(random.nextInt(80000) / 100.0)
            orderDetail.quantity = BigDecimal(random.nextInt(2000) / 100.0)
            orderDetail.subtotal = orderDetail.unitPrice!!.multiply(orderDetail.quantity)
            orderDetail.discount = BigDecimal(random.nextInt(15000) / 100.0)
            orderDetail.productStatus = order.orderStatus
            order.orderDetailList!!.add(orderDetail)
        }
        list.add(order)
    }
    return list
}

@Synchronized
fun initTable() {
    val conn = getConn()
    conn.use {
        val statement = conn.createStatement()
        statement.use {
            val torder = statement.execute(
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
                        " primary key (order_id) " +
                        " ) "
            )
            if (torder) {
                statement.execute(" create unique index idx_order_number on t_order (order_number) ")
                statement.execute(" create index idx_order_date on t_order (order_date) ")
                statement.execute(" create index idx_order_date_order_status on t_order (order_date, order_status) ")
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