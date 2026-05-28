package org.paperhub.storage.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.paperhub.config.MinioProperties;
import org.paperhub.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class MinioStorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioStorageService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    /**
     * Upload PDF files under a stable business path so later review and download APIs can find them.
     */
    public String uploadPdf(Long litRequestId, Long userId, MultipartFile file) {
        ensureBucket();
        String objectName = buildObjectName(litRequestId, userId);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType("application/pdf")
                    .build());
            return objectName;
        } catch (Exception ex) {
            throw new BizException("上传 PDF 到 MinIO 失败");
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build());
            }
        } catch (Exception ex) {
            throw new BizException("MinIO 存储桶不可用");
        }
    }

    private String buildObjectName(Long litRequestId, Long userId) {
        LocalDate today = LocalDate.now();
        return "assist/"
                + today.getYear() + "/"
                + String.format("%02d", today.getMonthValue()) + "/"
                + litRequestId + "/"
                + userId + "-"
                + UUID.randomUUID().toString().replace("-", "")
                + ".pdf";
    }
}
