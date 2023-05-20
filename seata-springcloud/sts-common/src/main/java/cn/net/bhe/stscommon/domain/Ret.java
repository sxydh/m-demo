package cn.net.bhe.stscommon.domain;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@RequiredArgsConstructor
@Accessors(chain = true)
public class Ret<T> {

    public static final int OK = 200;
    public static final int INTERNAL_SERVER_ERROR = 500;

    @NonNull
    private int code;
    private T data;
    private String msg;

    private Ret() {
    }

    public static <X> Ret<X> ok() {
        return ok(null);
    }

    public static <X> Ret<X> ok(X data) {
        return new Ret<X>(OK).setData(data);
    }

    public static <X> Ret<X> fail() {
        return fail(null);
    }

    public static <X> Ret<X> fail(String msg) {
        return new Ret<X>(INTERNAL_SERVER_ERROR).setMsg(msg);
    }

}
