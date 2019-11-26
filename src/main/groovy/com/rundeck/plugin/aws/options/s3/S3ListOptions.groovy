package com.rundeck.plugin.aws.options.s3

import com.lexicalscope.jewel.cli.Option

interface S3ListOptions extends AWSConnectionBase{

    @Option(description="Bucket name")
    String getBucket();


}
