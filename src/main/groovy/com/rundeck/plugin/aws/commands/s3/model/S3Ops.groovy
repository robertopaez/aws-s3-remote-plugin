package com.rundeck.plugin.aws.commands.s3.model


import com.rundeck.plugin.aws.commands.s3.S3Command
import com.rundeck.plugin.aws.util.AwsPluginUtil
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable

import java.util.regex.Pattern

class S3Ops implements FileOps {

    URI path
    S3Client s3
    String bucket
    String key
    Boolean isFile

    S3Ops(URI path, S3Client s3, Boolean isFile) {
        this.path = path
        this.s3 = s3
        this.isFile = isFile

        this.bucket = path.host
        this.key = AwsPluginUtil.getS3Key(path)
    }

    S3Ops(S3Client s3, String bucket, String key) {
        this.s3 = s3
        this.bucket = bucket
        this.key = key
    }

    @Override
    URI getPath() {
        return path
    }

    @Override
    List<FileData> listFiles(boolean recursive, String include, String exclude, boolean createFolder) {
        def bucket = path.host
        def prefix = path.path

        List<FileData> list = []

        if(isFile){
            list << new FileData(bucket, prefix.replaceFirst("/",""))
            return list
        }

        if(include=="false"){
            include = null
        }

        if(exclude=="false"){
            exclude = null
        }


        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucket)

        if(!recursive){
            requestBuilder.delimiter(S3Command.FILE_DELIMITER)
        }

        if(prefix){
            if(prefix.startsWith(S3Command.FILE_DELIMITER)){
                prefix= prefix.replaceFirst(S3Command.FILE_DELIMITER,"")
            }

            if (!prefix.endsWith(S3Command.FILE_DELIMITER)) {
                prefix += S3Command.FILE_DELIMITER;
            }
            requestBuilder.prefix(prefix)
        }

        ListObjectsV2Request request = requestBuilder.build()
        ListObjectsV2Iterable response = s3.listObjectsV2Paginator(request)

        Pattern excludePattern = exclude!=null?~/${exclude}/:null
        Pattern includePattern = include!=null?~/${include}/:null

        for (ListObjectsV2Response page : response) {
            page.contents().forEach{ object ->
                if(object.key != prefix){
                    if(checkAddFile(object,includePattern, excludePattern )) {
                        list << new FileData(object, bucket, prefix)
                    }
                }
            }
        }

        return list
    }

    @Override
    boolean delete() {
        try{
            s3.deleteObject(DeleteObjectRequest.builder().bucket(this.bucket).key(this.key).build())
            return true
        }catch(Exception e){
            println(e.message)
            return false
        }
    }

    @Override
    boolean exists(FileData file) {
        return true
    }

    def checkAddFile = { S3Object object, Pattern includePattern, Pattern excludePattern   ->
        Boolean excludeFile = null
        Boolean includeFile = null

        if(excludePattern){
            if(checkFilenamePattern(object,excludePattern)) {
                excludeFile = true
            }else{
                excludeFile = false
            }
        }

        if(includePattern){
            if(checkFilenamePattern(object,includePattern)) {
                includeFile=true
            }else{
                includeFile=false
            }
        }

        if(!excludeFile==null && includeFile==null){
            return true
        }else{
            if(includeFile==null){
                if(excludeFile){
                    return false
                }else{
                    return true
                }
            }

            if(excludeFile == null){
                if(includeFile){
                    return true
                }else{
                    return false
                }
            }

            if(includeFile!=null && excludeFile!=null){
                if(includeFile && !excludeFile){
                    return true
                }else{
                    return false
                }
            }
        }
    }

    def checkFilenamePattern = { S3Object object, Pattern filePattern ->
        if (filePattern.matcher(object.key()).find()) {
            return true
        }
        return false
    }

}
