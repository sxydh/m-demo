package cn.net.bhe.stscommon.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class Invt {

    private String invtId;
    private String resId;
    private BigDecimal quantity;

}
