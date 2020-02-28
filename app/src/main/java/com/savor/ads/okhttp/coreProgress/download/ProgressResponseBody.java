package com.savor.ads.okhttp.coreProgress.download;

import android.util.Log;

import com.savor.ads.utils.LogUtils;

import java.io.IOException;


import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 包装的响体，处理进度
 * Created by Administrator on 2016/6/14.
 */
public class ProgressResponseBody extends ResponseBody {
    private final ResponseBody responseBody;
    private String fileName;
    private BufferedSource bufferedSource;

    public ProgressResponseBody(ResponseBody responseBody, String filename) {
        this.responseBody = responseBody;
        this.fileName = filename;
    }

    public ProgressResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }


    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                LogUtils.v("fileName="+fileName+":read-----" + totalBytesRead);
                return bytesRead;
            }
        };
    }
}
