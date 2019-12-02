package com.rundeck.plugin.aws.options.s3

import com.lexicalscope.jewel.cli.Option

interface S3SyncOptions extends AWSConnectionBase{

    @Option(description="Source path URL .<LocalPath> <S3Uri>")
    String getSource();

    @Option(description="Destination path URL. <LocalPath> <S3Uri>")
    String getDestination();

    @Option(description="Don't exclude files or objects in the command that match the specified pattern", defaultToNull = true)
    String getInclude();

    @Option(description="Exclude all files or objects from the command that matches the specified pattern", defaultToNull = true)
    String getExclude();

    @Option(description="Files that exist in the destination but not in the source are deleted during sync", defaultToNull = true)
    String getDelete();
}
