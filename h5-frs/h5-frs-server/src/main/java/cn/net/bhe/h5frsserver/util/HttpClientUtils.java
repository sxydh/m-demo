package cn.net.bhe.h5frsserver.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Administrator
 */
public class HttpClientUtils {

    public static URI uri(String url, String... kvs) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            Queue<String> qkvs = new LinkedList<>(Arrays.asList(Optional.ofNullable(kvs).orElse(new String[0])));
            while (!qkvs.isEmpty()) {
                String k = qkvs.poll();
                String v = qkvs.poll();
                uriBuilder.addParameter(k, v);
            }
            return uriBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Header[] buildHeaders(String... kvs) {
        List<Header> headers = new ArrayList<>();
        Queue<String> qkvs = new LinkedList<>(Arrays.asList(Optional.ofNullable(kvs).orElse(new String[0])));
        while (!qkvs.isEmpty()) {
            String k = qkvs.poll();
            String v = qkvs.poll();
            headers.add(new BasicHeader(k, v));
        }
        return headers.toArray(Header[]::new);
    }

    public static String get(URI uri) throws Exception {
        return get(uri, null);
    }

    public static String get(URI uri, Header[] headers) throws Exception {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(uri);
            if (ObjectUtils.isNotEmpty(headers)) {
                httpGet.setHeaders(headers);
            }
            return getHttpResponse(closeableHttpClient, httpGet);
        }
    }

    public static byte[] getAsBytes(URI uri) throws Exception {
        return getAsBytes(uri, null);
    }

    public static byte[] getAsBytes(URI uri, Header[] headers) throws Exception {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(uri);
            if (ObjectUtils.isNotEmpty(headers)) {
                httpGet.setHeaders(headers);
            }
            return getHttpResponseAsBytes(closeableHttpClient, httpGet);
        }
    }

    public static String post(URI uri, Map<String, Object> body) throws Exception {
        return post(uri, null, body);
    }

    public static String post(URI uri, Header[] headers, Map<String, Object> body) throws Exception {
        return post(uri, headers, JSON.toJSONString(body));
    }

    public static String post(URI uri, String body) throws Exception {
        return post(uri, null, body);
    }

    public static String post(URI uri, Header[] headers, String body) throws Exception {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(uri);
            if (ObjectUtils.isNotEmpty(headers)) {
                httpPost.setHeaders(headers);
            }
            StringEntity stringEntity = new StringEntity(body, StandardCharsets.UTF_8);
            httpPost.setEntity(stringEntity);
            return getHttpResponse(closeableHttpClient, httpPost);
        }
    }

    public static String put(URI uri, Map<String, Object> body) throws Exception {
        return put(uri, null, body);
    }

    public static String put(URI uri, Header[] headers, Map<String, Object> body) throws Exception {
        return put(uri, headers, JSON.toJSONString(body));
    }

    public static String put(URI uri, String body) throws Exception {
        return put(uri, null, body);
    }

    public static String put(URI uri, Header[] headers, String body) throws Exception {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(uri);
            if (ObjectUtils.isNotEmpty(headers)) {
                httpPut.setHeaders(headers);
            }
            StringEntity stringEntity = new StringEntity(body, StandardCharsets.UTF_8);
            httpPut.setEntity(stringEntity);
            return getHttpResponse(closeableHttpClient, httpPut);
        }
    }

    public static String delete(URI uri) throws Exception {
        return delete(uri, null);
    }

    public static String delete(URI uri, Header[] headers) throws Exception {
        try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(uri);
            if (ObjectUtils.isNotEmpty(headers)) {
                httpDelete.setHeaders(headers);
            }
            return getHttpResponse(closeableHttpClient, httpDelete);
        }
    }

    private static String getHttpResponse(CloseableHttpClient closeableHttpClient, HttpUriRequest request) throws Exception {
        try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(request)) {
            String resStr = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8).trim();
            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (!Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED).contains(statusCode)) {
                throw new HttpException(Objects.toString(resStr, String.valueOf(statusCode)));
            }
            return resStr;
        }
    }

    private static byte[] getHttpResponseAsBytes(CloseableHttpClient closeableHttpClient, HttpUriRequest request) throws Exception {
        try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(request)) {
            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
            if (!Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED).contains(statusCode)) {
                throw new HttpException(String.valueOf(statusCode));
            }
            InputStream inputStream = closeableHttpResponse.getEntity().getContent();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int i;
            byte[] data = new byte[4];
            while ((i = inputStream.read(data, 0, data.length)) > 0) {
                buffer.write(data, 0, i);
            }
            return buffer.toByteArray();
        }
    }

}
