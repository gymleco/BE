package kr.co.gymleco.infra.storage;

import kr.co.gymleco.config.GymlecoProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.util.List;

@Component
public class S3ObjectStorage implements ObjectStorage{
    private final S3Client client;
    private final GymlecoProperties.Storage config;
    public S3ObjectStorage(GymlecoProperties properties){
        this.config = properties.storage();
        var builder = S3Client.builder().region(Region.of(config.region()));
        String endpoint = config.endpoint();
        if(endpoint != null && !endpoint.isBlank()){
            builder.endpointOverride(URI.create(endpoint)).serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(config.accessKey(), config.secretKey())));
        } else{
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        this.client = builder.build();
    }
    @Override
    public String put(String key, byte[] content, String contentType){
        client.putObject(
            PutObjectRequest.builder().bucket(config.bucket()).key(key).contentType(contentType).cacheControl("public, max-age=31436000, immutable").build(), RequestBody.fromBytes(content));
        return key;
    }
    @Override
    public void delete(String keyPrefix) {
        ListObjectsV2Response listed = client.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(config.bucket())
                .prefix(keyPrefix)
                .build());

        List<ObjectIdentifier> targets = listed.contents().stream()
            .map(o -> ObjectIdentifier.builder().key(o.key()).build())
            .toList();

        if (targets.isEmpty()) {
            return;
        }
        client.deleteObjects(DeleteObjectsRequest.builder()
            .bucket(config.bucket())
            .delete(Delete.builder().objects(targets).build())
            .build());
    }
    @Override
    public String publicUrl(String key){
        String base = config.cdnBaseUrl();
        if(base == null || base.isBlank()){
            return key;
        }
        return base.replaceAll("/$","") + "/" + key;
    }

}
