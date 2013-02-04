package org.robotninjas.riemann.sample;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD, PARAMETER})
public @interface LatencyTimer {
}
