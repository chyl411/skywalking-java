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

package org.apache.skywalking.apm.plugin.spring.annotations;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class SpringAnnotationInterceptor implements InstanceMethodsAroundInterceptor {
    public static Logger ZTE_LOGGER = null;
    private static final ILog LOGGER = LogManager.getLogger("sw");

    public static void initLogger() {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> loggerClazz = contextClassLoader.loadClass("org.slf4j.impl.StaticLoggerBinder");
            Method getSingleton = loggerClazz.getMethod("getSingleton");
            Object object = getSingleton.invoke(null);
            Method getLoggerFactory = loggerClazz.getMethod("getLoggerFactory");
            Object factory = getLoggerFactory.invoke(object);

            Class<?> factoryClazz = contextClassLoader.loadClass("org.slf4j.ILoggerFactory");
            Method getLogger = factoryClazz.getMethod("getLogger", String.class);
            ZTE_LOGGER = (org.slf4j.Logger) getLogger.invoke(factory, "sw");
        } catch (Exception e) {
            LOGGER.error("logback init failed", e);
        }
    }

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        MethodInterceptResult result) throws Throwable {
        if (ZTE_LOGGER == null) {
            initLogger();
        }
        // do not log the spring cglib enhanced classes, which simple recognized as contains $$
        if (ZTE_LOGGER != null) {
            ZTE_LOGGER.info("{}", method.getDeclaringClass().getName() + "#" + method.getName());
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
        Class<?>[] argumentsTypes, Throwable t) {
    }
}
