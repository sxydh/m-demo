package cn.net.bhe.stscommon.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class Order {

    private String orderId;
    private String goodsId;
    private BigDecimal quantity;
    private Invt invt;

}
