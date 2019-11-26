package com.rundeck.plugin.aws.options.s3

import com.lexicalscope.jewel.cli.Option

interface S3CopyOptions extends AWSConnectionBase{

    @Option(description="Source path URL .<LocalPath> <S3Uri>")
    String getSource();

    @Option(description="Destination path URL. <LocalPath> <S3Uri>")
    String getDestination();

    @Option(description="Command is performed on all files or objects under the specified directory or prefix.", defaultToNull = true)
    String getRecursive();

    @Option(description="Don't exclude files or objects in the command that match the specified pattern", defaultToNull = true)
    String getInclude();

    @Option(description="Exclude all files or objects from the command that matches the specified pattern", defaultToNull = true)
    String getExclude();
}
