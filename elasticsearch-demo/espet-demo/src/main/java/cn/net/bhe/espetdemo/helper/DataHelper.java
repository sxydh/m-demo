package cn.net.bhe.espetdemo.helper;

import cn.net.bhe.mutil.*;
import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class DataHelper {

    private static final Snowflake SNOW_FLAKE = new Snowflake(ProcessHandle.current().pid() % 1024);
    private static final Random RANDOM = new Random();
    private static final ArrayList<String> EN_3_ARR = new ArrayList<>();
    private static final ArrayList<Long> USER_IDS = new ArrayList<>();
    private static final ArrayList<Long> PROM_IDS = new ArrayList<>();
    private static final HashMap<Long, String> PROD_MAP = new HashMap<>();
    private static final List<Long> PROD_IDS;
    private static final Date DATE = DtUtils.date();
    private static final String[] OS = {"U", "P", "D", "A", "L"};
    private static final String[] PM = {"A", "W", "C", "D"};
    private static final ArrayList<String> SA_ARR = new ArrayList<>();
    private static final ArrayList<String> PH_ARR = new ArrayList<>();
    private static final ArrayList<String> SC_ARR = new ArrayList<>();
    private static final ArrayList<String> EN_5_ARR = new ArrayList<>();
    private static final ArrayList<BigDecimal> SC_BIG_ARR = new ArrayList<>();
    private static final ArrayList<BigDecimal> TA_BIG_ARR = new ArrayList<>();
    private static final ArrayList<BigDecimal> DA_BIG_ARR = new ArrayList<>();
    private static final ArrayList<String> IT_ARR = new ArrayList<>();
    private static final ArrayList<BigDecimal> UP_BIG_ARR = new ArrayList<>();
    private static final ArrayList<BigDecimal> QTY_BIG_ARR = new ArrayList<>();
    private static final ArrayList<BigDecimal> DISC_BIG_ARR = new ArrayList<>();
    private static final ArrayList<String> RATTR_ARR = new ArrayList<>();

    static {
        for (int i = 0; i < 100; i++) {
            EN_3_ARR.add(StrUtils.randomEn(3));
        }
        for (int i = 0; i < 500; i++) {
            USER_IDS.add(SNOW_FLAKE.nextId());
        }
        for (int i = 0; i < 100; i++) {
            PROM_IDS.add(SNOW_FLAKE.nextId());
        }
        for (int i = 0; i < 20; i++) {
            PROD_MAP.put(SNOW_FLAKE.nextId(), StrUtils.randomChs(RANDOM.nextInt(7)));
        }
        PROD_IDS = new ArrayList<>(PROD_MAP.keySet());
        for (int i = 0; i < 1000; i++) {
            SA_ARR.add(AddrUtils.ranChn(5));
        }
        for (int i = 0; i < 1000; i++) {
            PH_ARR.add(StrUtils.randomPhone());
        }
        for (int i = 0; i < 500; i++) {
            SC_ARR.add(CpUtils.ranChnCp());
        }
        for (int i = 0; i < 500; i++) {
            EN_5_ARR.add(StrUtils.randomEn(5));
        }
        for (int i = 0; i < 100; i++) {
            SC_BIG_ARR.add(BigDecimal.valueOf(RANDOM.nextInt(1000) / 100 + 10));
        }
        for (int i = 0; i < 100; i++) {
            TA_BIG_ARR.add(BigDecimal.valueOf(RANDOM.nextInt(50000) / 100.0 + 200));
        }
        for (int i = 0; i < 100; i++) {
            DA_BIG_ARR.add(BigDecimal.valueOf(RANDOM.nextInt(10000) / 100.0));
        }
        for (int i = 0; i < 500; i++) {
            IT_ARR.add(CpUtils.ranChnCp());
        }
        for (int i = 0; i < 100; i++) {
            UP_BIG_ARR.add(BigDecimal.valueOf(RANDOM.nextInt(80000) / 100.0));
        }
        for (int i = 0; i < 100; i++) {
            QTY_BIG_ARR.add(BigDecimal.valueOf(RANDOM.nextInt(2000) / 100.0));
        }
        for (int i = 0; i < 100; i++) {
            DISC_BIG_ARR.add(BigDecimal.valueOf(RANDOM.nextInt(15000) / 100.0));
        }
        for (int i = 0; i < 100; i++) {
            RATTR_ARR.add(WdUtils.random(RANDOM.nextInt(10) + 1));
        }
    }

    public static List<String> create(int n, int threads, int allocSec) {
        List<String> files = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            String path = FlUtils.getRootTmp() + File.separator + "order";
            FlUtils.mkdir(path);
            String file = path + File.separator + DtUtils.ts17() + ".txt";
            files.add(file);
            try (FileWriter fw = new FileWriter(file)) {
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    int j = n / threads;
                    while (j-- > 0) {
                        bw.write(JSON.toJSONString(getOrder(allocSec)));
                        bw.write(System.lineSeparator());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return files;
    }

    private static Order getOrder(int allocSec) {
        Order order = new Order();
        order.setOrderId(SNOW_FLAKE.nextId());
        order.setOrderNumber(EN_3_ARR.get(RANDOM.nextInt(EN_3_ARR.size())) + order.getOrderId());
        order.setUserId(USER_IDS.get(RANDOM.nextInt(USER_IDS.size())));
        order.setOrderDate(DtUtils.addSeconds(DATE, RANDOM.nextInt(allocSec)));
        order.setOrderStatus(OS[RANDOM.nextInt(OS.length)]);
        order.setPaymentMethod(PM[RANDOM.nextInt(PM.length)]);
        order.setShippingAddress(SA_ARR.get(RANDOM.nextInt(SA_ARR.size())));
        order.setContactNumber(PH_ARR.get(RANDOM.nextInt(PH_ARR.size())));
        order.setEmail(null);
        order.setShippingCompany(SC_ARR.get(RANDOM.nextInt(SC_ARR.size())));
        order.setTrackingNumber(EN_5_ARR.get(RANDOM.nextInt(EN_5_ARR.size())) + order.getOrderId().toString().substring(8));
        order.setShippingCost(SC_BIG_ARR.get(RANDOM.nextInt(SC_BIG_ARR.size())));
        order.setTotalAmount(TA_BIG_ARR.get(RANDOM.nextInt(TA_BIG_ARR.size())));
        order.setDiscountAmount(DA_BIG_ARR.get(RANDOM.nextInt(DA_BIG_ARR.size())));
        order.setPaidAmount(order.getTotalAmount().subtract(order.getDiscountAmount()));
        order.setInvoiceTitle(IT_ARR.get(RANDOM.nextInt(IT_ARR.size())));
        order.setTaxNumber(EN_5_ARR.get(RANDOM.nextInt(EN_5_ARR.size())) + order.getOrderId().toString().substring(11));
        order.setNotes(order.getOrderNumber() + "###" + order.getOrderStatus() + "###" + order.getPaymentMethod() + "###" + order.getTotalAmount());
        order.setPromotionId(PROM_IDS.get(RANDOM.nextInt(PROM_IDS.size())));
        order.setCreateTime(order.getOrderDate());
        order.setUpdateTime(order.getOrderDate());
        order.setRattr(rattrFun());
        order.setRattr2(rattrFun());
        order.setRattr3(null);
        order.setRattr4(null);
        order.setRattr5(null);
        order.setRattr6(null);
        order.setRattr7(null);
        order.setOrderDetailList(new ArrayList<>());
        //noinspection ConstantValue
        for (int i = 0; i < RANDOM.nextInt(1); i++) {
            OrderDetail orderDetail = getOrderDetail(order);
            order.getOrderDetailList().add(orderDetail);
        }
        return order;
    }

    private static String rattrFun() {
        return RANDOM.nextBoolean() ? RATTR_ARR.get(RANDOM.nextInt(RATTR_ARR.size())) : null;
    }

    private static OrderDetail getOrderDetail(Order order) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setDetailId(SNOW_FLAKE.nextId());
        orderDetail.setOrderId(order.getOrderId());
        orderDetail.setProductId(PROD_IDS.get(RANDOM.nextInt(PROD_IDS.size())));
        orderDetail.setProductName(PROD_MAP.get(orderDetail.getProductId()));
        orderDetail.setUnitPrice(UP_BIG_ARR.get(RANDOM.nextInt(UP_BIG_ARR.size())));
        orderDetail.setQuantity(QTY_BIG_ARR.get(RANDOM.nextInt(QTY_BIG_ARR.size())));
        orderDetail.setSubtotal(orderDetail.getUnitPrice().multiply(orderDetail.getQuantity()));
        orderDetail.setDiscount(DISC_BIG_ARR.get(RANDOM.nextInt(DISC_BIG_ARR.size())));
        orderDetail.setProductStatus(order.getOrderStatus());
        return orderDetail;
    }

    @Data
    @Accessors(chain = true)
    public static class Order {
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private Date orderDate;
        private String orderStatus;
        private String paymentMethod;
        private String shippingAddress;
        private String contactNumber;
        private String email;
        private String shippingCompany;
        private String trackingNumber;
        private BigDecimal shippingCost;
        private BigDecimal totalAmount;
        private BigDecimal discountAmount;
        private BigDecimal paidAmount;
        private String invoiceTitle;
        private String taxNumber;
        private String notes;
        private Long promotionId;
        private Date createTime;
        private Date updateTime;
        private String rattr;
        private String rattr2;
        private String rattr3;
        private String rattr4;
        private String rattr5;
        private String rattr6;
        private String rattr7;
        private ArrayList<OrderDetail> orderDetailList;
    }

    @Data
    @Accessors(chain = true)
    public static class OrderDetail {
        private Long detailId;
        private Long orderId;
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private BigDecimal quantity;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private String productStatus;
    }

}