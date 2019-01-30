package com.tp.library;

import com.tpa.client.tina.annotation.AutoMode;
import com.tpa.client.tina.annotation.Cache;
import com.tpa.client.tina.annotation.NotNull;
import com.tpa.client.tina.annotation.NumberScale;
import com.tpa.client.tina.annotation.Post;
import com.tpa.client.tina.model.TinaBaseRequest;

import java.util.concurrent.TimeUnit;

/**
 * Created by tangqianfeng on 2019/1/14.
 */

public interface LoginContract {

    @Post("/helloworld")
    @Cache(expire = 5 , unit = TimeUnit.MINUTES)
    class Request extends TinaBaseRequest {
        public String sex = "男";
        public String name = "tqf";
    }

    @Post("/helloworld2")
    @Cache(expire = 10)
    class Request2 extends TinaBaseRequest {
        public String sex = "男";
        public String name = "tqf";
    }

    @AutoMode
    class Response {
        public String code;
        public String message;

        @NumberScale(2)
        public String number;

        @NotNull(message = "status不能为空")
        public String status;
    }
}
