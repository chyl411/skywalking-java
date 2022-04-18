/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.hibernate;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

public class HibernateQueryInterceptor implements InstanceMethodsAroundInterceptor {
    private Method getNumberOfManagedEntities = null;

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret) throws Throwable {
        if (getNumberOfManagedEntities == null) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> loggerClazz = contextClassLoader.loadClass(HibernateQueryInstrumentation.ENHANCE_CLASS);
            getNumberOfManagedEntities = loggerClazz.getMethod("getNumberOfManagedEntities");
        }
        if (getNumberOfManagedEntities == null) {
            return ret;
        }
        Object object = getNumberOfManagedEntities.invoke(objInst);
        if (object instanceof Integer && (int) object > 10000) {
            throw new RuntimeException("throws: query result set could not more than 10000 !!!");
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
    }
}
