package com.rundeck.plugin.aws.options.s3

import com.lexicalscope.jewel.cli.Option

interface S3RemoveOptions extends AWSConnectionBase{

    @Option(description="Bucket name")
    String getBucket();

    @Option(description="Prefix")
    String getPrefix();

    @Option(description="Recursive", defaultToNull = true)
    String getRecursive();



}
