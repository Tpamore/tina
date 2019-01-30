package com.tpa.client.tina.annotation;

import com.tpa.client.tina.TinaDataException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface TinaAnnotationHandler<T extends Annotation> {
    public void hanld(T annotation , Object host ,Field field) throws TinaDataException;
}
