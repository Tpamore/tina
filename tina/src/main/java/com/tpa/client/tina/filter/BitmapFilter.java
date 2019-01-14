package com.tpa.client.tina.filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tpa.client.tina.TinaException;
import com.tpa.client.tina.TinaFilter;
import com.tpa.client.tina.TinaFilterResult;
import com.tpa.client.tina.enu.FilterCode;
import com.tpa.client.tina.model.TinaBaseRequest;

/**
 * Created by tangqianfeng on 2019/1/14.
 */

public class BitmapFilter implements TinaFilter{
    @Override
    public TinaFilterResult filter(TinaBaseRequest request, byte[] body, Class expect) {
        if (body == null || body.length <= 0) {
            return new TinaFilterResult(FilterCode.FAIL, TinaException.IOEXCEPTION, "数据加载失败" , null);
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(body, 0, body.length);
        if (bitmap == null) {
            return new TinaFilterResult(FilterCode.FAIL, TinaException.OTHER_EXCEPTION, "加载图片失败" , null);
        }
        return new TinaFilterResult(FilterCode.SUCCESS , bitmap);
    }
}
