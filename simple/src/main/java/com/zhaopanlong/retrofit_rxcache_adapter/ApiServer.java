package com.zhaopanlong.retrofit_rxcache_adapter;

import com.zhaopanlong.rxcacheadapter.Cache;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface ApiServer {
    @Cache
    @GET("s?wd=hello%20world&rsv_spt=1&rsv_iqid=0xb77af5ee0000e28d&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&rsv_dl=tb&rsv_sug3=11&rsv_sug1=9&rsv_sug7=100&rsv_sug2=0&inputT=3224&rsv_sug4=4563")
    public Observable<String> baidu();
}
