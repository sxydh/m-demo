package cn.net.bhe.jnademo;

import cn.net.bhe.mutil.FlUtils;
import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.File;

public interface JnaFs extends Library {

    JnaFs INSTANCE = Native.load(FlUtils.getRootTmp() + File.separator + "app.dll", JnaFs.class);

    int Fs();

}
