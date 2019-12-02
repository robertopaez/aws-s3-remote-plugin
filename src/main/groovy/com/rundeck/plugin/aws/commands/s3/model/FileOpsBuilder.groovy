package com.rundeck.plugin.aws.commands.s3.model

import software.amazon.awssdk.services.s3.S3Client

class FileOpsBuilder {

    URI path
    S3Client s3

    FileOpsBuilder path(URI path){
        this.path = path
        return this
    }

    FileOpsBuilder s3(S3Client s3){
        this.s3 = s3
        return this
    }

    FileOps builder(){
        if(path.scheme == "s3"){
            return new S3Ops(path, s3)
        }
        if(path.scheme == "file"){
            return new FileSystemOps(path)
        }
    }




}
