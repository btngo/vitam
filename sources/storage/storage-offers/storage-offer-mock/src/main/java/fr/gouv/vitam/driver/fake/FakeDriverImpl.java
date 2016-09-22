package fr.gouv.vitam.driver.fake;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import fr.gouv.vitam.common.BaseXx;
import fr.gouv.vitam.storage.driver.Connection;
import fr.gouv.vitam.storage.driver.Driver;
import fr.gouv.vitam.storage.driver.exception.StorageDriverException;
import fr.gouv.vitam.storage.driver.model.GetObjectRequest;
import fr.gouv.vitam.storage.driver.model.GetObjectResult;
import fr.gouv.vitam.storage.driver.model.PutObjectRequest;
import fr.gouv.vitam.storage.driver.model.PutObjectResult;
import fr.gouv.vitam.storage.driver.model.RemoveObjectRequest;
import fr.gouv.vitam.storage.driver.model.RemoveObjectResult;
import fr.gouv.vitam.storage.driver.model.StorageCapacityRequest;
import fr.gouv.vitam.storage.driver.model.StorageCapacityResult;

/**
 * Driver implementation for test only
 */
public class FakeDriverImpl implements Driver {

    @Override
    public Connection connect(String s, Properties properties) throws StorageDriverException {
        if (properties.contains("fail")) {
            throw new StorageDriverException(getName(), StorageDriverException.ErrorCode.INTERNAL_SERVER_ERROR,
                "Intentionaly thrown");
        }
        return new ConnectionImpl();
    }

    @Override
    public boolean isStorageOfferAvailable(String s, Properties properties) throws StorageDriverException {
        if (properties.contains("fail")) {
            throw new StorageDriverException(getName(), StorageDriverException.ErrorCode.INTERNAL_SERVER_ERROR,
                "Intentionaly thrown");
        }
        return true;
    }

    @Override
    public String getName() {
        return "Fake driver";
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    class ConnectionImpl implements Connection {

        @Override
        public StorageCapacityResult getStorageCapacity(StorageCapacityRequest objectRequest)
            throws StorageDriverException {
            if ("daFakeTenant".equals(objectRequest.getTenantId())) {
                throw new StorageDriverException("driverInfo", StorageDriverException.ErrorCode
                    .INTERNAL_SERVER_ERROR, "ExceptionTest");
            }
            StorageCapacityResult result = new StorageCapacityResult();
            result.setUsableSpace(1000000);
            result.setUsedSpace(99999);
            return result;
        }

        @Override
        public GetObjectResult getObject(GetObjectRequest objectRequest) throws StorageDriverException {
            GetObjectResult result = new GetObjectResult();
            result.setObject(new ByteArrayInputStream("fakefile".getBytes()));
            return result;
        }

        @Override
        public PutObjectResult putObject(PutObjectRequest objectRequest) throws StorageDriverException {
            PutObjectResult putObjectResult = new PutObjectResult();
            putObjectResult.setDistantObjectId(objectRequest.getGuid());
            if ("digest_bad_test".equals(objectRequest.getGuid())) {
                putObjectResult.setDigestHashBase16("different_digest_hash");
            } else {
                try {
                    byte[] bytes = IOUtils.toByteArray(objectRequest.getDataStream());                    
                    MessageDigest messageDigest = MessageDigest.getInstance(objectRequest.getDigestAlgorithm());
                    putObjectResult.setDigestHashBase16(BaseXx.getBase16(messageDigest.digest(bytes)));
                } catch (NoSuchAlgorithmException | IOException e) {
                    throw new StorageDriverException(getName(), StorageDriverException.ErrorCode
                        .INTERNAL_SERVER_ERROR, e);
                }
            }
            return putObjectResult;
        }

        @Override
        public PutObjectResult putObject(PutObjectRequest objectRequest, File file) throws StorageDriverException {
            return new PutObjectResult();
        }

        @Override
        public RemoveObjectResult removeObject(RemoveObjectRequest objectRequest) throws StorageDriverException {
            return new RemoveObjectResult();
        }
        
        @Override
        public Boolean objectExistsInOffer(GetObjectRequest request) throws StorageDriverException {
            return "already_in_offer".equals(request.getGuid());
        }

        @Override
        public void close() throws StorageDriverException {
            // Empty
        }
    }
}
