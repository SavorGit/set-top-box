package tv.danmaku.ijk.media.exo2;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.google.android.exoplayer2.util.Util.castNonNull;
import static java.lang.Math.min;

/**
 * @Description
 * @Author zhanghaiqiang
 * @Date 2020/11/3 16:47
 */
public final class MyFileDataSource extends BaseDataSource {

    private static final String TAG = "ZHQ";

    /** Thrown when a {@link MyFileDataSource} encounters an error reading a file. */
    public static class FileDataSourceException extends IOException {

        public FileDataSourceException(IOException cause) {
            super(cause);
        }

        public FileDataSourceException(String message, IOException cause) {
            super(message, cause);
        }
    }

    /** {@link DataSource.Factory} for {@link MyFileDataSource} instances. */
    public static final class Factory implements DataSource.Factory {

        @Nullable
        private TransferListener listener;

        /**
         * Sets a {@link TransferListener} for {@link MyFileDataSource} instances created by this factory.
         *
         * @param listener The {@link TransferListener}.
         * @return This factory.
         */
        public Factory setListener(@Nullable TransferListener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public MyFileDataSource createDataSource() {
            MyFileDataSource dataSource = new MyFileDataSource();
            if (listener != null) {
                dataSource.addTransferListener(listener);
            }
            return dataSource;
        }
    }

    @Nullable private RandomAccessFile file;
    @Nullable private Uri uri;
    private long bytesRemaining, length, position;
    private boolean opened;
    private int openCount = 0;
    private boolean _waiting=true;
    private Handler handler = new Handler(Looper.getMainLooper());
    public MyFileDataSource() {
        super(/* isNetwork= */ false);
    }

    @Override
    public long open(DataSpec dataSpec) throws FileDataSourceException {
        try {
            Uri uri = dataSpec.uri;
            this.uri = uri;

            transferInitializing(dataSpec);

            this.file = openLocalFile(uri);
            file.seek(dataSpec.position);
            bytesRemaining = dataSpec.length == C.LENGTH_UNSET ? file.length() - dataSpec.position
                    : dataSpec.length;
            length = dataSpec.length;
            position = dataSpec.position;
            if (bytesRemaining < 0) {
                throw new EOFException();
            }
        } catch (IOException e) {
            throw new FileDataSourceException(e);
        }

        opened = true;
        transferStarted(dataSpec);

        openCount++;
        Log.d(TAG, "open " + dataSpec.uri + " seekPosition=" + dataSpec.position +" bytesRemaining=" +bytesRemaining);
        return bytesRemaining;
    }

    private Runnable exitWaiting = new Runnable() {
        @Override
        public void run() {
            _waiting= false;
        }
    };

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws FileDataSourceException {
        Log.d(TAG, "read offset=" + offset + " readLength=" + readLength+" position="+position);
        if (readLength == 0) {
            return 0;
        } else if (bytesRemaining == 0) {
            return C.RESULT_END_OF_INPUT;
        } else {
            handler.removeCallbacks(exitWaiting);
            handler.postDelayed(exitWaiting,1000*30);
            while (opened && !RangeManagerFactory.getInstance().hasWroteRange(this.uri.getPath(), position, readLength)) {
                Log.e(TAG, "player read faster than write, will hold.");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    opened = false;
                    e.printStackTrace();
                }
            }

//            if (!opened) {
//                return 0;
//            }

            int bytesRead = 0;
            try {
                bytesRead = castNonNull(file).read(buffer, offset, (int) min(bytesRemaining, readLength));
            }/* catch (IOException e) {
                throw new FileDataSourceException(e);
            } */catch (Exception e) {
                Log.e(TAG, e.getMessage());
                if (e instanceof IOException) {
                    throw new FileDataSourceException((IOException) e);
                }
            }

            if (bytesRead > 0) {
                bytesRemaining -= bytesRead;
                position += bytesRead;
                bytesTransferred(bytesRead);
            }
//            Log.d(TAG, "read bytesRemaining=" +bytesRemaining);

            return bytesRead;
        }
    }

    @Override
    @Nullable
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() throws FileDataSourceException {
        uri = null;
        try {
            if (file != null) {
                file.close();
            }
        } catch (IOException e) {
            throw new FileDataSourceException(e);
        } finally {
            file = null;
            Log.d(TAG, "MyFileDataSource----close()............");
            if (opened) {
                opened = false;
                transferEnded();
            }
        }
    }

    private static RandomAccessFile openLocalFile(Uri uri) throws FileDataSourceException {
        try {
            return new RandomAccessFile(Assertions.checkNotNull(uri.getPath()), "r");
        } catch (FileNotFoundException e) {
            if (!TextUtils.isEmpty(uri.getQuery()) || !TextUtils.isEmpty(uri.getFragment())) {
                throw new FileDataSourceException(
                        String.format(
                                "uri has query and/or fragment, which are not supported. Did you call Uri.parse()"
                                        + " on a string containing '?' or '#'? Use Uri.fromFile(new File(path)) to"
                                        + " avoid this. path=%s,query=%s,fragment=%s",
                                uri.getPath(), uri.getQuery(), uri.getFragment()),
                        e);
            }
            throw new FileDataSourceException(e);
        }
    }
}
