package com.rundeck.plugin.aws.util

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder

class AwsS3Builder {

    final static String DEFAULT_REGION="us-west-1"

    String accessKey
    String secretKey
    String endpoint
    String region

    AwsS3Builder accessKey(String accessKey){
        this.accessKey=accessKey
        this
    }

    AwsS3Builder secretKey(String secretKey){
        this.secretKey=secretKey
        this
    }
    AwsS3Builder endpoint(String endpoint){
        this.endpoint=endpoint
        this
    }
    AwsS3Builder region(String region){
        this.region=region
        this
    }

    S3Client build(){

        S3ClientBuilder builder = S3Client.builder()


        if(endpoint){
            builder.endpointOverride(new URI(endpoint))
        }

        if(region){
            Region region = Region.of(this.region)
            builder.region(region)
        }else{
            Region region = Region.of(DEFAULT_REGION)
            builder.region(region)
        }

        builder.credentialsProvider(StaticCredentialsProvider.create(new AwsBasicCredentials(accessKey, secretKey)))

        S3Client s3 = builder.build()

        return s3
    }


}
