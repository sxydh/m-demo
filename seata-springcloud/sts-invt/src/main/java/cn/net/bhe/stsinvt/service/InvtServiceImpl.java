package cn.net.bhe.stsinvt.service;

import cn.net.bhe.stscommon.domain.Invt;
import cn.net.bhe.stsinvt.mapper.InvtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvtServiceImpl implements InvtService {

    @Autowired
    private InvtMapper invtMapper;

    public Invt update(Invt invt) {
        invtMapper.update(invt);
        return invtMapper.selectByResId(invt.getResId());
    }

}
