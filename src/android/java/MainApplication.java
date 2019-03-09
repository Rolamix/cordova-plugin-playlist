package __PACKAGE_NAME__;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.devbrackets.android.exomedia.ExoMedia;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import okhttp3.OkHttpClient;

import com.rolamix.plugins.audioplayer.manager.PlaylistManager;

public class MainApplication extends Application {
    @Nullable
    private PlaylistManager playlistManager;

    @Override
    public void onCreate() {
        super.onCreate();

        playlistManager = new PlaylistManager(this);
        configureExoMedia();
    }

    @Nullable
    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    private void configureExoMedia() {
        // Registers the media sources to use the OkHttp client instead of the standard Apache one
        // Note: the OkHttpDataSourceFactory can be found in the ExoPlayer extension library `extension-okhttp`
        ExoMedia.setDataSourceFactoryProvider(new ExoMedia.DataSourceFactoryProvider() {
            @NonNull
            @Override
            public DataSource.Factory provide(@NonNull String userAgent, @Nullable TransferListener listener) {
                // Updates the network data source to use the OKHttp implementation and allows it to follow redirects
                OkHttpClient httpClient = new OkHttpClient().newBuilder().followRedirects(true).followSslRedirects(true).build();
                DataSource.Factory upstreamFactory = new OkHttpDataSourceFactory(httpClient, userAgent, listener);

                // Adds a cache around the upstreamFactory.
                // This sets a cache of 100MB, we might make this configurable.
                Cache cache = new SimpleCache(getCacheDir(), new LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024));
                return new CacheDataSourceFactory(cache, upstreamFactory, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
            }
        });
    }
}
