package cn.net.bhe.stsinvt.service;

import cn.net.bhe.stscommon.domain.Invt;
import cn.net.bhe.stsinvt.mapper.InvtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvtServiceImpl implements InvtService {

    @Autowired
    private InvtMapper invtMapper;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Invt update(Invt invt) {
        invtMapper.update(invt);
        return invtMapper.selectByResId(invt.getResId());
    }

}
