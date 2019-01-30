package com.tpa.client.tina;

/**
 * Created by tangqianfeng on 17/4/10.
 */

public class TinaException {

    public static final int IOEXCEPTION = -0X101;
    public static final int DATA_EXCEPTION = -0X102;
    public static final int OTHER_EXCEPTION = -0X103;

    private int code;
    private String errorMsg;

    public TinaException(int code , String errorMsg) {
        this.errorMsg = errorMsg;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
