package com.rundeck.plugin.aws.util

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
import org.rundeck.toolbelt.CommandOutput

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

    def static downloadObject(AmazonS3 s3, CommandOutput output, String bucket_name, String key_name){
        output.info("Downloading ${key_name} from S3 bucket ${bucket_name}")
        try {
            S3Object o = s3.getObject(bucket_name, key_name)
            S3ObjectInputStream s3is = o.getObjectContent()
            FileOutputStream fos = new FileOutputStream(new File(key_name))
            byte[] read_buf = new byte[1024]
            int read_len = 0
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len)
            }
            s3is.close()
            fos.close()
        } catch (AmazonServiceException e) {
            output.error(e.getErrorMessage())
            System.exit(1)
        } catch (FileNotFoundException e) {
            output.error(e.getMessage())
            System.exit(1)
        } catch (IOException e) {
            output.error(e.getMessage())
            System.exit(1)
        }
        output.info("Done!")

    }

    def static  putObject(AmazonS3 s3, CommandOutput output, String bucket_name, String key_name, String file_path){
        output.info("Uploading ${key_name} to S3 bucket ${bucket_name}")
        try {
            s3.putObject(bucket_name, key_name, new File(file_path));
        } catch (AmazonServiceException e) {
            output.error(e.getErrorMessage())
            System.exit(1);
        }
        output.info("Done!")
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

}
