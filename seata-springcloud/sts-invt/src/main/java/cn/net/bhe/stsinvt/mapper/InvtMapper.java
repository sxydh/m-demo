package cn.net.bhe.stsinvt.mapper;

import cn.net.bhe.stscommon.domain.Invt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InvtMapper {

    @Update(" update sts_invt set quantity = quantity + #{quantity} where res_id = #{resId} ")
    void update(Invt invt);

    @Select(" select t.* from sts_invt t where t.res_id = #{resId} ")
    Invt selectByResId(String resId);

}
