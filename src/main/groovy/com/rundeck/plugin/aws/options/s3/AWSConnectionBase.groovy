package com.rundeck.plugin.aws.options.s3

import com.lexicalscope.jewel.cli.Option

interface AWSConnectionBase {

    @Option(description="Access Key")
    String getAccessKey();

    @Option(description="Secret Key")
    String getSecretKey();

    @Option(description="Optional, a custom S3 compatible endpoint to use, such as https://my-host.com/s3", defaultToNull = true)
    String getEndpoint();

    @Option(description="AWS region name to use", defaultToNull = true)
    String getRegion();


}
