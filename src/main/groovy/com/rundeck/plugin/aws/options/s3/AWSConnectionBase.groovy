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


    @Option(description="Optional, boolean, default=False, set to True if you need to define the bucket in your S3 like endpoint URL. e.g https://<s3_like_end_point_url>/<your_bucket_name>", defaultToNull = true)
    String getPathStyle();

}
