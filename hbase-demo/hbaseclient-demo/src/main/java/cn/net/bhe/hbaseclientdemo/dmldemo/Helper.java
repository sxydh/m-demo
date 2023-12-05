package cn.net.bhe.hbaseclientdemo.dmldemo;

import cn.net.bhe.mutil.StrUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;

public class Helper {

    public static void print(Result row) {
        if (row == null) {
            return;
        }
        Cell[] cells = row.rawCells();
        for (int i = 0, j = cells.length - 1; i < cells.length; i++) {
            Cell cell = cells[i];
            System.out.print(new String(CellUtil.cloneRow(cell)));
            System.out.print(StrUtils.DOT);
            System.out.print(new String(CellUtil.cloneFamily(cell)));
            System.out.print(StrUtils.DOT);
            System.out.print(new String(CellUtil.cloneQualifier(cell)));
            System.out.print(StrUtils.DOT);
            System.out.print(new String(CellUtil.cloneValue(cell)));
            if (i != j) {
                System.out.print(StrUtils.COMMA);
            }
        }
        System.out.println();
    }

}
