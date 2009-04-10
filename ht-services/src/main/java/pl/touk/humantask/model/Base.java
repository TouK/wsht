/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.lang.reflect.Method;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.collections.BeanMap;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base class for WS-HumanTask domain model. Implements common methods like: toString, equals, hashCode.
 * 
 * @author Witold Wołejszo
 */
@MappedSuperclass
public abstract class Base {

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }

    // @Override
    // public final int hashCode() {
    //        
    // HashCodeBuilder builder = new HashCodeBuilder();
    //
    // Method[] methods = this.getClass().getMethods();
    //
    // for (Method method : methods) {
    // if (method.isAnnotationPresent(Id.class)) {
    // try {
    // builder.append(method.invoke(this, (Object[]) null));
    // } catch (Exception e) {
    // }
    // }
    // }
    //
    // return builder.toHashCode();
    // }
    //
    // @Override
    // public final boolean equals(Object obj) {
    // if (this == obj) {
    // return true;
    // }
    //
    // if (obj == null || obj.getClass() != this.getClass()) {
    // return false;
    // }
    //
    // EqualsBuilder builder = new EqualsBuilder();
    //
    // Method[] methods = this.getClass().getMethods();
    //
    // for (Method method : methods) {
    // if (method.isAnnotationPresent(Id.class)) {
    // try {
    // builder.append(method.invoke(this, (Object[]) null), method.invoke(obj, (Object[]) null));
    // } catch (Exception e) {
    // }
    // }
    // }
    //
    // return builder.isEquals();
    // }
}
