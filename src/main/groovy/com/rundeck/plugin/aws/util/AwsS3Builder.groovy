package com.rundeck.plugin.aws.util

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder

class AwsS3Builder {

    final static String DEFAULT_REGION="us-west-1"
    final static String DEFAULT_ENDPOINT="https://s3.amazonaws.com"

    String accessKey
    String secretKey
    String endpoint
    String region
    Boolean pathStyle

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
    AwsS3Builder pathStyle(Boolean pathStyle){
        this.pathStyle=pathStyle
        this
    }

    AmazonS3 build(){
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()

        if(endpoint){
            AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(endpoint!=null?endpoint:DEFAULT_ENDPOINT,
                    region!=null?region:DEFAULT_REGION );
            builder.withEndpointConfiguration(endpointConfig)
        }

        if(!endpoint && region){
            builder.withRegion(region)
        }

        if(pathStyle){
            builder.withPathStyleAccessEnabled(true)
        }

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey)
        builder.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
        AmazonS3 s3 = builder.build()

        return s3
    }

}
