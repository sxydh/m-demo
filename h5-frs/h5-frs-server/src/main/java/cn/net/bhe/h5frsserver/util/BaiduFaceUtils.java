package cn.net.bhe.h5frsserver.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.Header;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Administrator
 * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-w2sr6pvyXzJ">人脸识别</a>
 */
@Component
public class BaiduFaceUtils {

    /**
     * 服务器地址
     */
    private static final String SERVER = "https://aip.baidubce.com/";
    private static final String CODE_ACCESS_TOKEN_EXPIRED = "110";
    /**
     * 应用的API Key
     */
    private final String clientId;
    /**
     * 应用的Secret Key
     */
    private final String clientSecret;
    private String accessToken;

    private static final String HDN_CT = "Content-Type";
    private static final String HDV_AJ = "application/json";

    /**
     * 获取AccessToken
     */
    private static final String API_TOKEN = "/oauth/2.0/token";
    /**
     * 人脸检测
     */
    private static final String API_DETECT = "/rest/2.0/face/v3/detect";
    /**
     * 创建用户组
     */
    private static final String API_GROUP_ADD = "/rest/2.0/face/v3/faceset/group/add";
    /**
     * 组列表查询
     */
    private static final String API_GROUP_LIST = "/rest/2.0/face/v3/faceset/group/getlist";
    /**
     * 删除用户组
     */
    private static final String API_GROUP_DELETE = "/rest/2.0/face/v3/faceset/group/delete";
    /**
     * 获取用户列表
     */
    private static final String API_USER_LIST = "/rest/2.0/face/v3/faceset/group/getusers";
    /**
     * 用户信息查询
     */
    private static final String API_USER_GET = "/rest/2.0/face/v3/faceset/user/get";
    /**
     * 删除用户
     */
    private static final String API_USER_DELETE = "/rest/2.0/face/v3/faceset/user/delete";
    /**
     * 人脸注册
     */
    private static final String API_FACE_ADD = "/rest/2.0/face/v3/faceset/user/add";
    /**
     * 人脸列表
     */
    private static final String API_FACE_LIST = "/rest/2.0/face/v3/faceset/face/getlist";
    /**
     * 人脸删除
     */
    private static final String API_FACE_DELETE = "/rest/2.0/face/v3/faceset/face/delete";
    /**
     * 人脸搜索
     */
    private static final String API_FACE_SEARCH = "/rest/2.0/face/v3/search";

    public BaiduFaceUtils(
            @Value("${frs.baidu.clientId:-1}")
            String clientId,
            @Value("${frs.baidu.clientSecret:-1}")
            String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private String get(Supplier<URI> uriSupplier, Supplier<Header[]> headersSupplier) throws Exception {
        String ret = HttpClientUtils.get(uriSupplier.get(), headersSupplier.get());
        JSONObject retJson = JSON.parseObject(ret);
        String errorCode = retJson.getString("error_code");
        // accessToken检查
        if (CODE_ACCESS_TOKEN_EXPIRED.equals(errorCode)) {
            accessToken = getToken();
            return HttpClientUtils.get(uriSupplier.get(), headersSupplier.get());
        }
        return ret;
    }

    private String post(Supplier<URI> uriSupplier, Supplier<Header[]> headersSupplier, Supplier<Map<String, Object>> bodySupplier) throws Exception {
        String ret = HttpClientUtils.post(uriSupplier.get(), headersSupplier.get(), bodySupplier.get());
        JSONObject retJson = JSON.parseObject(ret);
        String errorCode = retJson.getString("error_code");
        // accessToken检查
        if (CODE_ACCESS_TOKEN_EXPIRED.equals(errorCode)) {
            ret = getToken();
            retJson = JSON.parseObject(ret);
            accessToken = retJson.getString("access_token");
            Asserts.check(ObjectUtils.isNotEmpty(accessToken), "获取accessToken失败！");
            return HttpClientUtils.post(uriSupplier.get(), headersSupplier.get(), bodySupplier.get());
        }
        return ret;
    }

    /**
     * 获取AccessToken
     *
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-ZrbC4DkR2rP">获取AccessToken</a>
     */
    public String getToken() throws Exception {
        return HttpClientUtils.get(new URIBuilder(SERVER + API_TOKEN)
                        .addParameter("grant_type", "client_credentials")
                        .addParameter("client_id", clientId)
                        .addParameter("client_secret", clientSecret)
                        .build(),
                null);
    }

    /**
     * 人脸检测
     *
     * @param image 图片信息
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-w2sr6pvyXzJ">人脸检测</a>
     */
    public String detectFace(String image) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_DETECT,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "image", image,
                        "image_type", "BASE64",
                        "face_field", "age,beauty,expression,face_shape,gender,glasses,landmark,landmark150,quality,eye_status,emotion,face_type,mask,spoofing",
                        "face_type", "LIVE",
                        "liveness_control", "NORMAL"));
    }

    /**
     * 创建用户组
     *
     * @param groupId 用户组id
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-sVgQXDpqrMd">创建用户组</a>
     */
    public String addGroup(String groupId) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_GROUP_ADD,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "group_id", groupId));
    }

    /**
     * 组列表查询
     *
     * @param start  起始序号
     * @param length 返回数量
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-PoFdvitBK3b">组列表查询</a>
     */
    public String listGroup(String start, String length) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_GROUP_LIST,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "start", start,
                        "length", length));
    }

    /**
     * 删除用户组
     *
     * @param groupId 用户组id
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-Nia7CbnVkwZ">删除用户组</a>
     */
    public String deleteGroup(String groupId) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_GROUP_DELETE,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "group_id", groupId));
    }

    /**
     * 获取用户列表
     *
     * @param groupId 用户组id
     * @param start   起始序号
     * @param length  返回数量
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-WwqU2DmXUYN">获取用户列表</a>
     */
    public String listUser(String groupId, String start, String length) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_USER_LIST,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "group_id", groupId,
                        "start", start,
                        "length", length));
    }

    /**
     * 用户信息查询
     *
     * @param groupId 用户组id
     * @param userId  用户id
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-T3aj3knpq9W">用户信息查询</a>
     */
    public String getUser(String groupId, String userId) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_USER_GET,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "group_id", groupId,
                        "user_id", userId));
    }

    /**
     * 删除用户
     *
     * @param groupId 用户组id
     * @param userId  用户id
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-sjbaBxxM46u">删除用户</a>
     */
    public String deleteUser(String groupId, String userId) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_USER_DELETE,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "group_id", groupId,
                        "user_id", userId));
    }

    /**
     * 人脸注册
     *
     * @param image    图片信息
     * @param groupId  用户组id
     * @param userId   用户id
     * @param userInfo 用户资料
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-eSX4WxmdzLE">人脸注册</a>
     */
    public String addFace(String image, String groupId, String userId, String userInfo) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_FACE_ADD,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "image", image,
                        "image_type", "BASE64",
                        "group_id", groupId,
                        "user_id", userId,
                        "user_info", userInfo,
                        "quality_control", "NORMAL",
                        "liveness_control", "NORMAL",
                        "action_type", "APPEND"));
    }

    /**
     * 获取用户人脸列表
     *
     * @param groupId 用户组id
     * @param userId  用户id
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-8D5DsUtNHhT">获取用户人脸列表</a>
     */
    public String listFace(String groupId, String userId) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_FACE_LIST,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "group_id", groupId,
                        "user_id", userId));
    }

    /**
     * 人脸删除
     *
     * @param logId     请求标识码
     * @param groupId   用户组id
     * @param userId    用户id
     * @param faceToken 需要删除的人脸图片token
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-GKx4KDjYuvZ">人脸删除</a>
     */
    public String deleteFace(String logId, String groupId, String userId, String faceToken) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_FACE_DELETE,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "logId", logId,
                        "group_id", groupId,
                        "user_id", userId,
                        "face_token", faceToken));
    }

    /**
     * 人脸搜索
     *
     * @param image       请求标识码
     * @param groupIdList 从指定的group中进行查找
     * @param maxUserNum  查找后返回的用户数量。返回相似度最高的几个用户，默认为1，最多返回50个。
     * @return result
     * @throws Exception exception
     * @see <a href="https://cloud.baidu.com/apiexplorer/index.html?Product=GWSE-nmhroEsyriA&Api=GWAI-adiJw5iQYte">人脸搜索</a>
     */
    public String searchFace(String image, String groupIdList, String maxUserNum) throws Exception {
        return post(() -> HttpClientUtils.uri(SERVER + API_FACE_SEARCH,
                        "access_token", accessToken),
                () -> HttpClientUtils.buildHeaders(HDN_CT, HDV_AJ),
                () -> CollUtils.map(
                        "image", image,
                        "image_type", "BASE64",
                        "group_id_list", groupIdList,
                        "max_user_num", maxUserNum));
    }

}
