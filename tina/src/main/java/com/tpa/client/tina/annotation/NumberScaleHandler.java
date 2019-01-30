package com.tpa.client.tina.annotation;

import com.tpa.client.tina.TinaDataException;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class NumberScaleHandler implements TinaAnnotationHandler<NumberScale> {

    public static final String[] NUMBER_PARSE = {"String", "float", "double"};

    @Override
    public void hanld(NumberScale numberScale, Object host, Field field) throws TinaDataException {

        if (!isCanParse(field)) {
            return;
        }

        int scale = numberScale.value();
        String typeName = field.getType().getSimpleName();
        switch (typeName) {
            case "String":
                try {
                    field.setAccessible(true);
                    String fieldValue = (String) field.get(host);
                    if (fieldValue == null || "".equals(fieldValue)) {
                        field.set(host, "0");
                    } else {
                        String parseValue = numberFormat(Double.parseDouble(fieldValue), scale);
                        field.set(host, parseValue);
                    }
                } catch (Exception e) {
                }
                break;
            case "float":
                try {
                    field.setAccessible(true);
                    float fieldValue = field.getFloat(host);
                    float parseValue = Float.parseFloat(numberFormat(fieldValue, scale));
                    field.setFloat(host, parseValue);
                } catch (Exception e) {
                }
                break;
            case "double":
                try {
                    field.setAccessible(true);
                    double fieldValue = field.getDouble(host);
                    double parseValue = Double.parseDouble(numberFormat(fieldValue, scale));
                    field.setDouble(host, parseValue);
                } catch (Exception e) {
                }
                break;
        }
    }

    private String numberFormat(double value, int scale) {

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(scale);

        nf.setRoundingMode(RoundingMode.HALF_EVEN);
        /**
         * 是否需要用逗号隔开
         */
        nf.setGroupingUsed(true);
        return nf.format(value);
    }

    /**
     * 判断字段是否可以转换成数字
     *
     * @param field
     * @return
     */
    private boolean isCanParse(Field field) {
        String simpleName = field.getType().getSimpleName();
        for (String name : NUMBER_PARSE) {
            if (name.equals(simpleName)) {
                return true;
            }
        }
        return false;
    }
}
