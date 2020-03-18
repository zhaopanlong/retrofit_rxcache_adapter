package com.zhaopanlong.rxcacheadapter;

import com.zchu.rxcache.data.CacheResult;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class CacheObservable<T> extends Observable<T> {

    private final Observable<CacheResult<T>> upstream;

    CacheObservable(Observable<CacheResult<T>> upstream) {
        this.upstream = upstream;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        upstream.subscribe(new CacheObserver<T>(observer));
    }

    private static class CacheObserver<R> implements Observer<CacheResult<R>> {

        private final Observer<? super R> observer;

        CacheObserver(Observer<? super R> observer) {
            this.observer = observer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            observer.onSubscribe(d);
        }

        @Override
        public void onNext(CacheResult<R> rCacheResult) {
            if (rCacheResult != null && rCacheResult.getData() != null) {
                observer.onNext(rCacheResult.getData());
            }
        }

        @Override
        public void onError(Throwable e) {
            observer.onComplete();
        }

        @Override
        public void onComplete() {
            observer.onComplete();
        }
    }
}
