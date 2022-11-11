package cn.net.bhe.h5frsserver.controller;

import cn.net.bhe.h5frsserver.util.BaiduFaceUtils;
import cn.net.bhe.h5frsserver.util.CollUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 人脸接口
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/frs")
public class FrsController {

    static final String BAIDU_CODE_OK = "0";
    static final String BAIDU_GROUP_DEFAULT = "DEFAULT";
    static final String ACTION_FRONT = "front";
    static final String ACTION_SIDE = "side";

    static final LoadingCache<String, String> CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .refreshAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public String load(String key) {
                    return null;
                }
            });

    /**
     * 人脸置信度阈值
     */
    @Value("${frs.baidu.faceProbabilityMin:1}")
    private String faceProbabilityMin;
    /**
     * 判断图片是否为合成图阈值
     */
    @Value("${frs.baidu.spoofingMax:0}")
    private String spoofingMax;
    /**
     * 正视角度阈值
     */
    @Value("${frs.baidu.front:0}")
    private String front;
    /**
     * 侧视角度阈值
     */
    @Value("${frs.baidu.side:90}")
    private String side;
    /**
     * 用户的匹配得分阈值
     */
    @Value("${frs.baidu.scoreMin:100}")
    private String scoreMin;

    @Autowired
    private BaiduFaceUtils baiduFaceUtils;

    private Map<String, Object> ok(Object data) {
        return CollUtils.map(
                "code", HttpStatus.SC_OK,
                "data", data);
    }

    private Map<String, Object> fail(int code, String msg) {
        return CollUtils.map(
                "code", code,
                "msg", msg);
    }

    /**
     * 百度检测人脸
     */
    @RequestMapping(value = "/bd/detectFace")
    public Object bdDetectFace(@RequestBody JSONObject reqJson) {
        String image = reqJson.getString("image");
        String action = reqJson.getString("action");
        Float width = reqJson.getFloat("width");
        Float height = reqJson.getFloat("height");
        Assert.isTrue(ObjectUtils.isNotEmpty(image), "缺少image！");
        Assert.isTrue(ObjectUtils.isNotEmpty(action), "缺少action！");
        Assert.isTrue(ObjectUtils.isNotEmpty(width), "缺少width！");
        Assert.isTrue(ObjectUtils.isNotEmpty(height), "缺少height！");
        try {
            String ret = baiduFaceUtils.detectFace(image);
            JSONObject retJson = JSON.parseObject(ret);
            String code = retJson.getString("error_code");
            String msg = retJson.getString("error_msg");
            if (!BAIDU_CODE_OK.equals(code)) {
                return fail(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
            }
            retJson = retJson.getJSONObject("result");
            // 人脸数量检测
            int faceNum = retJson.getInteger("face_num");
            if (faceNum == 1) {
                JSONObject face = retJson.getJSONArray("face_list").getJSONObject(0);
                String faceToken = (String) face.remove("face_token");
                // 位置检测
                JSONObject landmark150 = face.getJSONObject("landmark150");
                float xmin = landmark150.values().stream().map(e -> (JSONObject) e).map(e -> e.getFloat("x")).min(Float::compareTo).orElse(0f);
                float xmax = landmark150.values().stream().map(e -> (JSONObject) e).map(e -> e.getFloat("x")).max(Float::compareTo).orElse(0f);
                float ymin = landmark150.values().stream().map(e -> (JSONObject) e).map(e -> e.getFloat("y")).min(Float::compareTo).orElse(0f);
                float ymax = landmark150.values().stream().map(e -> (JSONObject) e).map(e -> e.getFloat("y")).max(Float::compareTo).orElse(0f);
                if (xmin > 0 && xmax < width
                        && ymin > 0 && ymax < height) {
                    // 人脸置信度检测
                    float faceProbability = face.getFloat("face_probability");
                    float faceProbabilityMinValue = Float.parseFloat(faceProbabilityMin);
                    if (faceProbability > faceProbabilityMinValue) {
                        // 角度检测
                        JSONObject angle = face.getJSONObject("angle");
                        float yaw = angle.getFloat("yaw");
                        boolean yawBool = false;
                        // 正视
                        if (ACTION_FRONT.equals(action)) {
                            float frontValue = Float.parseFloat(front);
                            if (Math.abs(yaw) <= frontValue) {
                                yawBool = true;
                            }
                        }
                        // 摇头
                        else if (ACTION_SIDE.equals(action)) {
                            float sideValue = Float.parseFloat(side);
                            if (Math.abs(yaw) >= sideValue) {
                                yawBool = true;
                            }
                        }
                        if (yawBool) {
                            // 合成图检测
                            float spoofing = face.getFloat("spoofing");
                            float spoofingMaxValue = Float.parseFloat(spoofingMax);
                            if (spoofing < spoofingMaxValue) {
                                // 检测通过，缓存该人脸，用于后续注册或登录。
                                face.put("face_token", faceToken);
                                CACHE.put(faceToken, image);
                            }
                        }
                    }
                }
            }
            return ok(retJson);
        } catch (Exception e) {
            return fail(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * 百度注册人脸
     */
    @RequestMapping(value = "/bd/addFace")
    public Object bdAddFace(@RequestBody JSONObject reqJson) {
        String userId = reqJson.getString("userId");
        JSONArray faceTokens = reqJson.getJSONArray("faceTokens");
        Assert.isTrue(ObjectUtils.isNotEmpty(userId), "缺少userId！");
        Assert.isTrue(ObjectUtils.isNotEmpty(faceTokens), "缺少faceTokens！");
        try {
            // 删除用户
            baiduFaceUtils.deleteUser(BAIDU_GROUP_DEFAULT, userId);
            // 注册人脸
            for (Object e : faceTokens) {
                String faceToken = e.toString();
                String image = CACHE.get(faceToken);
                if (ObjectUtils.isEmpty(image)) {
                    return fail(HttpStatus.SC_INTERNAL_SERVER_ERROR, "无效的faceToken'" + faceToken + "'！");
                }
                String ret = baiduFaceUtils.addFace(
                        image,
                        BAIDU_GROUP_DEFAULT,
                        userId,
                        null);
                JSONObject retJson = JSON.parseObject(ret);
                String code = retJson.getString("error_code");
                String msg = retJson.getString("error_msg");
                if (!BAIDU_CODE_OK.equals(code)) {
                    return fail(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
                }
            }
            return ok(null);
        } catch (Exception e) {
            return fail(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * 百度搜索人脸
     */
    @RequestMapping(value = "/bd/searchFace")
    public Object bdSearchFace(@RequestBody JSONObject reqJson) {
        String faceToken = reqJson.getString("faceToken");
        Assert.isTrue(ObjectUtils.isNotEmpty(faceToken), "缺少faceToken！");
        try {
            String image = CACHE.get(faceToken);
            Assert.isTrue(ObjectUtils.isNotEmpty(image), "无效的faceToken'" + faceToken + "'！");
            String ret = baiduFaceUtils.searchFace(
                    image,
                    BAIDU_GROUP_DEFAULT,
                    "10");
            JSONObject retJson = JSON.parseObject(ret);
            String code = retJson.getString("error_code");
            String msg = retJson.getString("error_msg");
            if (!BAIDU_CODE_OK.equals(code)) {
                return fail(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
            }
            JSONObject result = retJson.getJSONObject("result");
            JSONArray userList = result.getJSONArray("user_list");
            Assert.isTrue(ObjectUtils.isNotEmpty(userList), "没有匹配到任何人脸！");
            // 得分过滤
            float scoreMinValue = Float.parseFloat(scoreMin);
            List<JSONObject> fuserList = userList.stream().map(ele -> (JSONObject) ele)
                    .filter(e -> e.getFloat("score") > scoreMinValue)
                    .sorted((e1, e2) -> Float.compare(e2.getFloat("score"), e1.getFloat("score")))
                    .collect(Collectors.toList());
            if (ObjectUtils.isEmpty(fuserList)) {
                return fail(HttpStatus.SC_EXPECTATION_FAILED, "匹配得分不足！");
            }
            String[] userIds = fuserList.stream().map(e -> e.getString("user_id")).toArray(String[]::new);
            Assert.isTrue(userIds.length == 1, "存在多个用户" + Arrays.toString(userIds) + "！");
            retJson = new JSONObject();
            String userId = userIds[0];
            String frsCode = UUID.randomUUID().toString();
            CACHE.put(frsCode, userId);
            retJson.put("frsCode", frsCode);
            retJson.put("score", fuserList.get(0).getFloat("score"));
            return ok(retJson);
        } catch (Exception e) {
            return fail(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }

}
