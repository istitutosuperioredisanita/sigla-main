package it.cnr.contab.doccont00.ejb;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.METHOD })
public @interface TipoObbligazione {
    Tipo value();
    enum Tipo { BASE, RESIDUO, PLURIENNALE }
}