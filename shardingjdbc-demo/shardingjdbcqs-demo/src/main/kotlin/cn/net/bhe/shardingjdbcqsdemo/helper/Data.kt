package cn.net.bhe.shardingjdbcqsdemo.helper

import cn.net.bhe.mutil.*
import com.alibaba.fastjson2.JSON
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.math.BigDecimal
import java.util.*

object Data {

    private val snowflake = Snowflake(ProcessHandle.current().pid() % 1024)
    private val random = Random()
    private val en3Arr = ArrayList<String>()
    private val userIds = ArrayList<Long>()
    private val promIds = ArrayList<Long>()
    private val prodMap = HashMap<Long, String>()
    private val prodIds: List<Long>
    private val date = DtUtils.date()
    private val os = arrayOf("U", "P", "D", "A", "L")
    private val pm = arrayOf("A", "W", "C", "D")
    private val saArr = ArrayList<String>()
    private val phArr = ArrayList<String>()
    private val scArr = ArrayList<String>()
    private val en5Arr = ArrayList<String>()
    private val scBigArr = ArrayList<BigDecimal>()
    private val taBigArr = ArrayList<BigDecimal>()
    private val daBigArr = ArrayList<BigDecimal>()
    private val itArr = ArrayList<String>()
    private val upBigArr = ArrayList<BigDecimal>()
    private val qtyBigArr = ArrayList<BigDecimal>()
    private val discBigArr = ArrayList<BigDecimal>()
    private val rattrArr = ArrayList<String>()
    private var allocSec: Int = 3600

    init {
        for (i in 0..<100) {
            en3Arr.add(StrUtils.randomEn(3))
        }
        for (i in 0..500) {
            userIds.add(snowflake.nextId())
        }
        for (i in 0..100) {
            promIds.add(snowflake.nextId())
        }
        for (i in 0..20) {
            prodMap[snowflake.nextId()] = StrUtils.randomChs(random.nextInt(7))
        }
        prodIds = prodMap.keys.toList()
        for (i in 0..<1000) {
            saArr.add(AddrUtils.ranChn(5))
        }
        for (i in 0..<1000) {
            phArr.add(StrUtils.randomPhone())
        }
        for (i in 0..<500) {
            scArr.add(CpUtils.ranChnCp())
        }
        for (i in 0..<500) {
            en5Arr.add(StrUtils.randomEn(5))
        }
        for (i in 0..<100) {
            scBigArr.add(BigDecimal(random.nextInt(1000) / 100 + 10))
        }
        for (i in 0..<100) {
            taBigArr.add(BigDecimal(random.nextInt(50000) / 100.0 + 200))
        }
        for (i in 0..<100) {
            daBigArr.add(BigDecimal(random.nextInt(10000) / 100.0))
        }
        for (i in 0..<500) {
            itArr.add(CpUtils.ranChnCp())
        }
        for (i in 0..<100) {
            upBigArr.add(BigDecimal(random.nextInt(80000) / 100.0))
        }
        for (i in 0..<100) {
            qtyBigArr.add(BigDecimal(random.nextInt(2000) / 100.0))
        }
        for (i in 0..<100) {
            discBigArr.add(BigDecimal(random.nextInt(15000) / 100.0))
        }
        for (i in 0..<100) {
            rattrArr.add(WdUtils.random(random.nextInt(10) + 1))
        }
    }

    fun create(n: Int, threads: Int, allocSec: Int): List<String> {
        Data.allocSec = allocSec
        val files = ArrayList<String>()
        for (i in 0..<threads) {
            val path = "${FlUtils.getRootTmp()}${File.separator}order"
            FlUtils.mkdir(path)
            val file = "$path/${DtUtils.ts17()}.txt"
            files.add(file)
            FileWriter(file).use { fw ->
                BufferedWriter(fw).use { bw ->
                    var j = n / threads
                    while (j-- > 0) {
                        bw.write(JSON.toJSONString(getOrder()))
                        bw.write(System.lineSeparator())
                    }
                }
            }
        }
        return files
    }

    private fun getOrder(): Order {
        val order = Order()
        order.orderId = snowflake.nextId()
        order.orderNumber = en3Arr[random.nextInt(en3Arr.size)] + order.orderId
        order.userId = userIds[random.nextInt(userIds.size)]
        order.orderDate = DtUtils.addSeconds(date, random.nextInt(allocSec).toLong())
        order.orderStatus = os[random.nextInt(os.size)]
        order.paymentMethod = pm[random.nextInt(pm.size)]
        order.shippingAddress = saArr[random.nextInt(saArr.size)]
        order.contactNumber = phArr[random.nextInt(phArr.size)]
        order.email = null
        order.shippingCompany = scArr[random.nextInt(scArr.size)]
        order.trackingNumber = en5Arr[random.nextInt(en5Arr.size)] + order.orderId.toString().substring(8)
        order.shippingCost = scBigArr[random.nextInt(scBigArr.size)]
        order.totalAmount = taBigArr[random.nextInt(taBigArr.size)]
        order.discountAmount = daBigArr[random.nextInt(daBigArr.size)]
        order.paidAmount = order.totalAmount!!.subtract(order.discountAmount)
        order.invoiceTitle = itArr[random.nextInt(itArr.size)]
        order.taxNumber = en5Arr[random.nextInt(en5Arr.size)] + order.orderId.toString().substring(11)
        order.notes = "${order.orderNumber}###${order.orderStatus}###${order.paymentMethod}###${order.totalAmount}"
        order.promotionId = promIds[random.nextInt(promIds.size)]
        order.createTime = order.orderDate
        order.updateTime = order.orderDate
        val rattrFun = { if (random.nextBoolean()) rattrArr[random.nextInt(rattrArr.size)] else null }
        order.rattr = rattrFun()
        order.rattr2 = rattrFun()
        order.rattr3 = null
        order.rattr4 = null
        order.rattr5 = null
        order.rattr6 = null
        order.rattr7 = null
        order.orderDetailList = ArrayList()
        for (li in 0..<random.nextInt(4)) {
            val orderDetail = OrderDetail()
            orderDetail.detailId = snowflake.nextId()
            orderDetail.orderId = order.orderId
            orderDetail.productId = prodIds[random.nextInt(prodIds.size)]
            orderDetail.productName = prodMap[orderDetail.productId]
            orderDetail.unitPrice = upBigArr[random.nextInt(upBigArr.size)]
            orderDetail.quantity = qtyBigArr[random.nextInt(qtyBigArr.size)]
            orderDetail.subtotal = orderDetail.unitPrice!!.multiply(orderDetail.quantity)
            orderDetail.discount = discBigArr[random.nextInt(discBigArr.size)]
            orderDetail.productStatus = order.orderStatus
            order.orderDetailList!!.add(orderDetail)
        }
        return order
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