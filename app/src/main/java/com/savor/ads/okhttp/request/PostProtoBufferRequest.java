package com.savor.ads.okhttp.request;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostProtoBufferRequest extends OkHttpRequest {
    private static MediaType MEDIA_TYPE_PROTO = MediaType.parse("application/x-protobuf;charset=utf-8");

    private byte[] content;
    private MediaType mediaType;
    public PostProtoBufferRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers,MediaType mediaType, byte[] content) {
        super(url, tag, params, headers);
        this.content = content;
        this.mediaType = mediaType;
        if (mediaType==null){
                this.mediaType = MEDIA_TYPE_PROTO;
        }
    }

    @Override
    protected RequestBody buildRequestBody() {

        return RequestBody.create(mediaType, content);
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.post(requestBody).build();
    }
}
