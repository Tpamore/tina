package com.tpa.client.tina;


import com.tpa.client.tina.enu.FilterCode;

/**
 * Created by tangqianfeng on 17/2/4.
 */

public class TinaFilterResult {
    public FilterCode code;
    public String errorMsg;
    public int errorCode;
    public Object response;

    public TinaFilterResult(FilterCode code , int errorCode , String errorMsg , Object response){
        this.code = code;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.response = response;
    }

    public TinaFilterResult(FilterCode code , Object response) {
        this.code = code;
        this.response = response;
    }

}
