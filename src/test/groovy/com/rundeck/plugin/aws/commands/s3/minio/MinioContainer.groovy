package com.rundeck.plugin.aws.commands.s3.minio

import org.testcontainers.containers.GenericContainer

class MinioContainer  extends GenericContainer<MinioContainer> {

    private static final Integer DEFAULT_PORT = 9000;
    private String accessKey
    private String secretKey

    MinioContainer() {
        this("minio/minio:RELEASE.2019-09-18T21-55-05Z")
    }

    MinioContainer(String dockerImageName) {
        super(dockerImageName);
        withExposedPorts(DEFAULT_PORT)
        withCommand('server /data --compat')
        withAccess 'TEST_KEY', UUID.randomUUID().toString()


    }

    MinioContainer withAccess(String accessKey, String secretKey) {
        withEnv MINIO_ACCESS_KEY: accessKey, MINIO_SECRET_KEY: secretKey
        this.accessKey = accessKey
        this.secretKey = secretKey
        return self()
    }

    def getEndpoint() {
        return "http://${containerIpAddress}:${firstMappedPort}"
        //new MinioClient("http://${containerIpAddress}:${firstMappedPort}", accessKey, secretKey)
    }

    @Override
    void close() {
        super.close()
    }

}
