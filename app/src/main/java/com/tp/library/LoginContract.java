package com.tp.library;

import com.tpa.client.tina.annotation.AutoMode;
import com.tpa.client.tina.annotation.Cache;
import com.tpa.client.tina.annotation.Post;
import com.tpa.client.tina.model.TinaBaseRequest;

/**
 * Created by tangqianfeng on 2019/1/14.
 */

public interface LoginContract {

    @Post("/helloworld")
    @Cache(expire = 10)
    class Request extends TinaBaseRequest {
        public String sex = "男";
        public String name = "tqf";
    }

    @AutoMode
    class Response {
        public String code;
        public String message;
    }
}
