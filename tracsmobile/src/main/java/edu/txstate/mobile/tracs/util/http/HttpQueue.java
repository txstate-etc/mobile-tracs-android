package edu.txstate.mobile.tracs.util.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;

@SuppressLint("StaticFieldLeak")
public class HttpQueue {
    private static HttpQueue httpQueue;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private final int THREAD_COUNT = 12;

    private static Context context;

    private HttpQueue(Context context) {
        HttpQueue.context = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<>(20);
                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized HttpQueue getInstance(Context context) {
        if (httpQueue == null) {
            httpQueue = new HttpQueue(context);
        }
        return httpQueue;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = this.newRequestQueue(context.getApplicationContext(), THREAD_COUNT);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, Object tag) {
        req.setTag(tag);
        req.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    private RequestQueue newRequestQueue (Context context, int threads) {
        final String DEFAULT_CACHE_DIR = "volley";
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

        HttpStack stack = new HurlStack();
        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network, threads);
        queue.start();

        return queue;
    }
}
