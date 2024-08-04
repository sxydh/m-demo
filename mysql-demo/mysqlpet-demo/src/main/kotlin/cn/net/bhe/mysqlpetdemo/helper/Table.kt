package cn.net.bhe.mysqlpetdemo.helper

object Table {
    @Synchronized
    fun init(index: Boolean) {
        val conn = getConn()
        conn.use {
            val statement = conn.createStatement()
            statement.use {
                statement.execute(
                    " create table if not exists t_order ( " +
                            " order_id bigint, " +
                            " order_number varchar(30), " +
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
}