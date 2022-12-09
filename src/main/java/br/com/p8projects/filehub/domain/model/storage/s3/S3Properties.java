package br.com.p8projects.filehub.domain.model.storage.s3;

import br.com.p8projects.filehub.domain.model.OptionalProperty;
import br.com.p8projects.filehub.domain.model.storage.StorageProperties;
import br.com.p8projects.filehub.domain.model.storage.EnumStorageType;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class S3Properties implements StorageProperties {

    private String region;
    private String secretKeyId;
    private String secretKey;
    private String bucket;
    @OptionalProperty
    private String baseDir;

    @Override
    public EnumStorageType getType() {
        return EnumStorageType.AWS_S3;
    }

    public String getBaseDir() {
        return baseDir != null ? baseDir : "";
    }

    public void setBaseDir(String baseDir) {
        if(baseDir.startsWith("/")) {
            baseDir = baseDir.substring(1);
        }
        if(!baseDir.endsWith("/")) {
            baseDir += "/";
        }
        this.baseDir = baseDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        S3Properties that = (S3Properties) o;
        return region.equals(that.region) && secretKeyId.equals(that.secretKeyId) && secretKey.equals(that.secretKey) && bucket.equals(that.bucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, secretKeyId, secretKey, bucket);
    }

}
