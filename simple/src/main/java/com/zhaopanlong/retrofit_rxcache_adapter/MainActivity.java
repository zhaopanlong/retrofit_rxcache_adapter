package com.zhaopanlong.retrofit_rxcache_adapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zchu.rxcache.RxCache;
import com.zchu.rxcache.diskconverter.GsonDiskConverter;
import com.zhaopanlong.rxcacheadapter.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final String TAG = "MainActivity";
    Button test;
    TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test = findViewById(R.id.button);
        content = findViewById(R.id.textView);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //网络请求
                ApiServer apiServer = create(ApiServer.class);
                apiServer.baidu().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i(TAG, "onSubscribe");
                    }

                    @Override
                    public void onNext(String s) {
                        Log.i(TAG, "onNext" + s);
                        content.setText(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError");
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete");
                    }
                });
            }
        });
        ActivityCompat.requestPermissions(MainActivity.this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE);

        //rxcache的default配置
        RxCache.initializeDefault(new RxCache.Builder().appVersion(2)
                .diskDir(new File(getCacheDir().getPath() + File.separator + "data-cache"))
                .diskConverter(new GsonDiskConverter())
                .diskMax(20 * 1024 * 1024)
                .memoryMax(0)
                .setDebug(BuildConfig.DEBUG)
                .build());
    }

    public <T> T create(Class<T> service) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(76760, TimeUnit.MILLISECONDS)
                .connectTimeout(76760, TimeUnit.MILLISECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(getHttpLogIntercptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("https://www.baidu.com/")
                .build();
        return retrofit.create(service);
    }

    private HttpLoggingInterceptor getHttpLogIntercptor() {
        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("HttpLoggingInterceptor", message);
            }
        });
        interceptor.setLevel(level);
        return interceptor;
    }
}
