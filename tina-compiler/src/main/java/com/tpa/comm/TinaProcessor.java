package com.tpa.comm;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;


/**
 * Created by tangqianfeng on 17/3/4.
 */
@AutoService(Processor.class)
public class TinaProcessor extends AbstractProcessor {

    private boolean mIsFileCreated = false;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ConfigId.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (mIsFileCreated) {
            return true;
        }
        mIsFileCreated = true;
        Set<? extends Element> testSet = roundEnvironment.getElementsAnnotatedWith(ConfigId.class);
        for (Element ee : testSet) {
            if (ee instanceof TypeElement) {
                String name = ee.getAnnotation(ConfigId.class).value();

                ParameterSpec parameterSpec = ParameterSpec.builder(int.class, "tinaType")
                        .addAnnotation(ClassName.get("com.tpa.client.tina.Tina", "TinaType"))
                        .build();

                MethodSpec methodSpec1 = MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(ClassName.get("com.tpa.client.tina", "Tina"))
//                    .addParameter(int.class, "tinaType")
                        .addParameter(parameterSpec)
                        .addStatement("return com.tpa.client.tina.Tina.build($N).config($S)", "tinaType", name)
                        .build();

                MethodSpec methodSpec2 = MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(ClassName.get("com.tpa.client.tina", "Tina"))
                        .addStatement("return com.tpa.client.tina.Tina.build($L).config($S)", 10009, name)
                        .build();

                TypeSpec tinaType = TypeSpec.classBuilder(name + "Tina")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodSpec1)
                        .addMethod(methodSpec2)
                        .build();

                JavaFile javaFile = JavaFile.builder("com.tpa.temp", tinaType)
                        .build();


                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
