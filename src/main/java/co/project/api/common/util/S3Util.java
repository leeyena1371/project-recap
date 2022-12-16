package co.project.api.common.util;

import co.dearu.live.api.common.config.AWSConfig;
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class S3Util {

    @Autowired
    AWSConfig awsConfig;

    @Value("${aws.s3.bucket-name}")
    private String AWS_S3_BUCKET_NAME;

    @Value("${aws.cloudfront.keyfile}")
    private String AWS_CLOUDFRONT_KEYFILE;

    @Value("${aws.cloudfront.keypair}")
    private String AWS_CLOUDFRONT_KEYPAIR;

    @Value("${aws.cloudfront.cdn}")
    private String AWS_CLOUDFRONT_CDN;

    @Value("${common.replay-possible-time}")
    private String REPLAY_POSSIBLE_TIME;

    public String getObject(String url){

        String prifix = "s3://" + AWS_S3_BUCKET_NAME + "/";
        int prifixLength = prifix.length();
        String s3ObjectKey = url.substring(prifixLength);
        S3Client s3Client = awsConfig.getAmazonS3Client();
        try {
            ResponseInputStream<GetObjectResponse> getObjectResponse = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(AWS_S3_BUCKET_NAME)
                    .key(s3ObjectKey)
                    .build());
            return s3ObjectKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getS3ObjectKey(String url){

        String prifix = "s3://" + AWS_S3_BUCKET_NAME + "/";
        int prifixLength = prifix.length();
        String s3ObjectKey = url.substring(prifixLength);

        boolean isS3ObjectExist = false;
        S3Client s3Client = awsConfig.getAmazonS3Client();
        try {
            HeadObjectRequest objectRequest = HeadObjectRequest.builder()
                    .key(s3ObjectKey)
                    .bucket(AWS_S3_BUCKET_NAME)
                    .build();

            HeadObjectResponse objectHead = s3Client.headObject(objectRequest);

            isS3ObjectExist = true;

        } catch (Exception e) {
            e.printStackTrace();
            isS3ObjectExist = false;
        }

        if(isS3ObjectExist){
            return s3ObjectKey;
        }
        else{
            return null;
        }
    }

    public String getCloudFrontUrl(String s3ObjectKey) throws Exception {
        ClassPathResource crtResource = new ClassPathResource(AWS_CLOUDFRONT_KEYFILE);

        SignerUtils.Protocol protocol = SignerUtils.Protocol.https;
        String distributionDomain = AWS_CLOUDFRONT_CDN;   // cdn 도메인 (property에서 관리할 것)
        File privateKeyFile = new File("/"+crtResource.getURI().getPath().substring(1));// 받아올 S3 경로
        String keyPairId = AWS_CLOUDFRONT_KEYPAIR;     // Cloudfront 내 퍼블릭키 생성되면 발급되는 ID (property에서 관리할 것)
        Date dateLessThan = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(Long.parseLong(REPLAY_POSSIBLE_TIME)));


        String url = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
                protocol, distributionDomain, privateKeyFile,
                s3ObjectKey, keyPairId, dateLessThan);

        return url;
    }
}
