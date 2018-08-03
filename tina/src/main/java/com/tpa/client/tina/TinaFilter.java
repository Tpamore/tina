package com.tpa.client.tina;


import com.tpa.client.tina.model.TinaBaseRequest;

/**
 * Created by tangqianfeng on 16/9/25.
 */
public interface TinaFilter {
    /**
     *
     * @param request
     *          请求request
     * @param body
     *          response byte数据
     * @param expect
     *           期望的返回class type
     * @return
     */
        public TinaFilterResult filter(TinaBaseRequest request , byte[] body , Class expect);
}
