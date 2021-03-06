/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Utility to instrument a given class, memoizing all it's zero-argument method invocations. 
 * Note that the memoization behaves a little different from guava's Suppliers.memoize in that
 * it caches exception results as well as regularly returned values.
 */
public class MemoizingProxy {
	
	static class Result {
		
		Throwable e;
		Object v;
		
		Result(Callable<?> computer) {
			try {
				v = computer.call();
			} catch (Throwable e) {
				this.e = e;
			}
		}

		public Object get() throws Exception {
			if (e!=null) {
				throw ExceptionUtil.exception(e);
			}
			return v;
		}
	}


	/**
	 * Memoizes all zero-argument public methods for a given duration.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> klass, Duration duration, Object... args) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(klass);
		enhancer.setCallback(new MethodInterceptor() {
			Cache<String, Result> cache = CacheBuilder.newBuilder()
					.expireAfterWrite(duration.toMillis(), TimeUnit.MILLISECONDS)
					.build();
			
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if (Modifier.isPublic(method.getModifiers()) && (args==null || args.length==0)) {
					synchronized (cache) {
						String mname = method.getName();
						Result r = cache.get(mname, () -> new Result(() -> { 
							try {
								return proxy.invokeSuper(obj, args);
							} catch (Throwable e) {
								throw ExceptionUtil.exception(e);
							}	
						}));
						return r.get();
					}
				} else {
					return proxy.invokeSuper(obj, args);
				}
			}
		});
		Class<?>[] argumentTypes = new Class<?>[args.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			argumentTypes[i] = args[i].getClass();
		}
		return (T) enhancer.create(argumentTypes, args);
	}
	
}
