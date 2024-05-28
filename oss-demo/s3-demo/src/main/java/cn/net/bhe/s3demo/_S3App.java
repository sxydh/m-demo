package cn.net.bhe.s3demo;

import cn.net.bhe.mutil.CpUtils;
import cn.net.bhe.mutil.FlUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class _S3App {

    public static void main(String[] args) throws Exception {
        // 创建客户端
        String accessKeyId = System.getProperty("accessKeyId");
        String secretAccessKey = System.getProperty("secretAccessKey");
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.EU_NORTH_1)
                .build();

        // 上传文件
        File file = getFile();
        String keyName = file.getName();
        String bucketName = System.getProperty("bucketName");
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();
        s3Client.putObject(putObjectRequest,
                RequestBody.fromFile(file));

        // 获取列表
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
        List<S3Object> contents = listObjectsV2Response.contents();
        for (S3Object content : contents) {
            System.out.println(content);
        }

        // 关闭客户端
        s3Client.close();
    }


    private static File getFile() throws Exception {
        FlUtils.mkdir(FlUtils.getRootTmp());
        String path = FlUtils.getRootTmp() + File.separator + UUID.randomUUID();
        FlUtils.write(CpUtils.ranChnCp(), path, false);
        return new File(path);
    }

}
