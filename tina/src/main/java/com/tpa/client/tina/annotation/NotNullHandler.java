package com.tpa.client.tina.annotation;


import com.tpa.client.tina.TinaDataException;

import java.lang.reflect.Field;

public class NotNullHandler implements TinaAnnotationHandler<NotNull>{
    @Override
    public void hanld(NotNull annotation, Object host, Field field) throws TinaDataException{
        try {
            Object o = field.get(host);
            if (o == null) {
                throw new TinaDataException(annotation.message());
            }
        } catch (IllegalAccessException e) {
        }
    }
}
