package com.rundeck.plugin.aws.options.s3

import com.lexicalscope.jewel.cli.Option

interface S3DeleteBucketOptions extends AWSConnectionBase{

    @Option(description="Bucket name")
    String getBucket();

    @Option(description="Deletes all objects in the bucket including the bucket itself", defaultToNull = true)
    String getForce();


}
