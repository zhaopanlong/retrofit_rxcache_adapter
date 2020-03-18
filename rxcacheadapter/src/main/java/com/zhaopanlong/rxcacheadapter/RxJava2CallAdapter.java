/*
 * Copyright (C) 2016 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhaopanlong.rxcacheadapter;

import android.util.Log;


import com.zchu.rxcache.RxCache;
import com.zchu.rxcache.stategy.CacheAndRemoteStrategy;
import com.zchu.rxcache.stategy.FirstCacheStrategy;
import com.zchu.rxcache.stategy.IStrategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


import javax.annotation.Nullable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;

final class RxJava2CallAdapter<R> implements CallAdapter<R, Object> {
    public static final String TAG = "RxJava2CallAdapter";
    private final Type responseType;
    private final @Nullable
    Scheduler scheduler;
    private final boolean isAsync;
    private final boolean isResult;
    private final boolean isBody;
    private final boolean isFlowable;
    private final boolean isSingle;
    private final boolean isMaybe;
    private final boolean isCompletable;
    private final Annotation[] annotations;

    RxJava2CallAdapter(Type responseType, @Nullable Scheduler scheduler, boolean isAsync,
                       boolean isResult, boolean isBody, boolean isFlowable, boolean isSingle, boolean isMaybe,
                       boolean isCompletable, Annotation[] annotations) {
        this.responseType = responseType;
        this.scheduler = scheduler;
        this.isAsync = isAsync;
        this.isResult = isResult;
        this.isBody = isBody;
        this.isFlowable = isFlowable;
        this.isSingle = isSingle;
        this.isMaybe = isMaybe;
        this.isCompletable = isCompletable;
        this.annotations = annotations;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public Object adapt(Call<R> call) {
        Observable<Response<R>> responseObservable = isAsync
                ? new CallEnqueueObservable<>(call)
                : new CallExecuteObservable<>(call);
        boolean cache = false;
        CacheStrategy strategy = null;
        String url = null;
        for (Annotation annotation : annotations) {

            if (annotation instanceof GET) {
                url = ((GET) annotation).value();
            } else if (annotation instanceof POST) {
                url = ((POST) annotation).value();
            } else if (annotation instanceof DELETE) {
                url = ((DELETE) annotation).value();
            } else if (annotation instanceof Cache) {
                Log.i(TAG,"哈哈哈哈哈：找到了cache");
                strategy = ((Cache) annotation).strategy();
                cache = true;
            }
        }

        Observable<?> observable;

        if (isResult) {
            observable = new ResultObservable<>(responseObservable);
        } else if (isBody) {
            //在这里才能进行缓存
            observable = new BodyObservable<>(responseObservable);

            if (cache) {
                //判断缓存类型
                IStrategy cacheStrategy;
                if (strategy == CacheStrategy.FirstCacheStrategy) {
                    cacheStrategy = new FirstCacheStrategy();
                } else if (strategy == CacheStrategy.CacheAndRemoteStrategy) {
                    cacheStrategy = new CacheAndRemoteStrategy();
                } else {
                    cacheStrategy = new CacheAndRemoteStrategy();
                }
                observable = new CacheObservable<>(cacheStrategy.execute(RxCache.getDefault(), url, observable, responseType));
                return observable;
            }
         /*   if (cache) {
                //需要缓存
                Observable<?> cachecache = new CacheObservable<>(RxCacheHelper.loadCache(RxCache.getDefault(), url, responseType, true));
                observable = new CacheObservable<>(RxCacheHelper.loadRemote(RxCache.getDefault(), url, observable, CacheTarget.MemoryAndDisk, false));
                return Observable.concat(cachecache, observable);
            }*/
        } else {
            observable = responseObservable;
        }
   /*     Observable<?> cacheObservable = null;
        if (cache) {
            CacheAndRemoteStrategy cacheStrategy = new CacheAndRemoteStrategy();
            // OnlyCacheStrategy cacheStrategy = new OnlyCacheStrategy();
          //  cacheObservable = cacheStrategy.execute(RxCache.getDefault(), url, observable, responseType);

            Observable<?> cachecache = RxCacheHelper.loadCache(RxCache.getDefault(), url, responseType, true);
            Observable<?> remote =RxCacheHelper.loadRemote(RxCache.getDefault(), url, observable, CacheTarget.MemoryAndDisk, false);
            return Observable.concat(Arrays.asList(cachecache,remote));
        }*/


        if (scheduler != null) {
            observable = observable.subscribeOn(scheduler);
        }

        if (isFlowable) {
            return observable.toFlowable(BackpressureStrategy.LATEST);
        }
        if (isSingle) {
            return observable.singleOrError();
        }
        if (isMaybe) {
            return observable.singleElement();
        }
        if (isCompletable) {
            return observable.ignoreElements();
        }

        return RxJavaPlugins.onAssembly(observable);


    }
}
