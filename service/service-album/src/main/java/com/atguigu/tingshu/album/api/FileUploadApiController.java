package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.config.MinioConstantProperties;
import com.atguigu.tingshu.common.result.Result;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FilenameUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("api/album")
public class FileUploadApiController {

    @Autowired
    private MinioConstantProperties minioConstantProperties;

    @PostMapping("/fileUpload")
    public Result<String> fileUpload(MultipartFile file) throws Exception {
        String URL = "";

        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConstantProperties.getEndpointUrl())
                .credentials(minioConstantProperties.getAccessKey(), minioConstantProperties.getSecreKey())
                .build();
        boolean flag = minioClient.bucketExists(BucketExistsArgs.builder()
                                                .bucket(minioConstantProperties.getBucketName())
                                                .build());
        if (flag) System.out.println("Bucket" + minioConstantProperties.getBucketName()+"alread exists");
        else minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConstantProperties.getBucketName()).build());

        String fileName = UUID.randomUUID().toString().replaceAll("-", "")+"."+ FilenameUtils.getExtension(file.getOriginalFilename());

        minioClient.putObject(PutObjectArgs.builder()
                                            .bucket(minioConstantProperties.getBucketName())
                                            .object(fileName)
                                            .stream(file.getInputStream(), file.getSize(), -1)
                                            .contentType(file.getContentType())
                                            .build());
        URL = minioConstantProperties.getEndpointUrl()+"/"+minioConstantProperties.getBucketName()+"/"+fileName;
        System.out.println(URL);
        return Result.ok(URL);

    }
}
