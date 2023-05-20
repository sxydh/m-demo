package cn.net.bhe.stsinvt.controller;

import cn.net.bhe.stscommon.domain.Invt;
import cn.net.bhe.stscommon.domain.Ret;
import cn.net.bhe.stsinvt.service.InvtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invt")
public class InvtController {

    @Autowired
    private InvtService invtService;

    @PutMapping("/update")
    public Ret<Invt> update(@RequestBody Invt invt) {
        return Ret.ok(invtService.update(invt));
    }

}
