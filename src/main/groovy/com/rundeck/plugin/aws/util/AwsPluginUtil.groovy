package com.rundeck.plugin.aws.util


import org.rundeck.toolbelt.CommandOutput
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import java.nio.file.Path
import java.nio.file.Paths

class AwsPluginUtil {

    static String parseConfig(String config){
        if(!config){
            return null
        }
        if(config.contains("\${")){
            return null
        }else{
            return config
        }
    }

    def static downloadObject(S3Client s3, CommandOutput output, String bucket_name, String key_name, String file_path){
        output.output("Downloading ${key_name} from S3 bucket ${bucket_name} to ${file_path}")

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket_name)
                .key(key_name)
                .build()

        try {

            File file = new File(file_path)
            if(file.exists()){
                file.delete()
            }
            s3.getObject(request, ResponseTransformer.toFile(Paths.get(file_path)))

        } catch (Exception e) {
            output.error(e.getMessage())
            System.exit(1)
        }
    }

    def static  putObject(S3Client s3, CommandOutput output, String bucket_name, String key_name, String file_path){
        output.output("Uploading ${file_path}  to S3 bucket ${bucket_name}/${key_name}")
        try {


            PutObjectRequest request = PutObjectRequest.builder()
                                                       .bucket(bucket_name)
                                                       .key(key_name)
                                                       .build()

            s3.putObject(request, RequestBody.fromFile(Paths.get(file_path)))

        } catch (Exception e) {
            output.warning(e.message)
            System.exit(1)
        }
    }

    static boolean isDirectory(URI uri){
        String path = getFile(uri)
        File file = new File(path)
        if(file.isDirectory()){
            return true
        }

        if(path.endsWith("/")){
            return true
        }

        return false
    }

    static String getFile(URI uri) {
        return uri.getQuery() == null ? uri.getPath() : uri.getPath() + "?" + uri.getQuery();
    }

    static String getS3Key(URI uri){
        def object = uri.path

        if(object.startsWith("/")){
            object = object.replaceFirst("/","")
        }

        return object
    }

    static getFileHash(File file){
        Path.metaClass.getMd5 << { ->
            def digest = java.security.MessageDigest.getInstance("MD5")
            delegate.withInputStream { stream ->
                stream.eachByte 4096, { buffer, length ->
                    digest.update( buffer, 0, length )
                }
            }
            digest.digest().encodeHex() as String
        }

        File.metaClass.getMd5 << { -> delegate.toPath().md5 }

        file.md5

    }


    static <T> List<T> difference(Collection<T> list1, Collection<T> list2) {

        if (list1.isEmpty()) {
            if (list2.isEmpty()) {
                return new ArrayList<T>();
            }
            return new ArrayList<T>(list2);
        }

        if (list2.isEmpty()) {
            return new ArrayList<T>();
        }

        List<T> difference = new ArrayList<T>();

        for (T object : list2) {
            if (list1.contains(object) == false) {
                difference.add(object);
            }
        }

        return difference;

    }


}
