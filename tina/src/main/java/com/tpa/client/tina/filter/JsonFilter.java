package com.tpa.client.tina.filter;

import com.tpa.client.tina.TinaException;
import com.tpa.client.tina.TinaFilter;
import com.tpa.client.tina.TinaFilterResult;
import com.tpa.client.tina.enu.FilterCode;
import com.tpa.client.tina.model.TinaBaseRequest;
import com.tpa.client.tina.utils.JSONHelper;

/**
 * Created by tangqianfeng on 2019/1/14.
 */

public class JsonFilter implements TinaFilter{
    @Override
    public TinaFilterResult filter(TinaBaseRequest request, byte[] body, Class expect) {
        String source = new String(body);

        Object response =  JSONHelper.jsonToObject(source , expect);
        if (response == null) {
            return new TinaFilterResult(FilterCode.FAIL, TinaException.OTHER_EXCEPTION, "数据格式错误" , null);
        }
        return new TinaFilterResult(FilterCode.SUCCESS , response);
    }

    public static TinaFilter build() {
        return new JsonFilter();
    }
}
