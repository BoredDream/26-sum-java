package com.training.system.info.annotation;

import java.lang.annotation.*;

/**
 * Excel列映射注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {
    int index() default 0;
    String header() default "";
}
