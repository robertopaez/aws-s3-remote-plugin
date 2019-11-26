package com.rundeck.plugin.aws.commands.s3

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.ListObjectsV2Result
import com.amazonaws.services.s3.model.ListVersionsRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.S3VersionSummary
import com.amazonaws.services.s3.model.VersionListing
import com.rundeck.plugin.aws.options.s3.S3CopyOptions
import com.rundeck.plugin.aws.options.s3.S3DeleteBucketOptions
import com.rundeck.plugin.aws.options.s3.S3ListOptions
import com.rundeck.plugin.aws.util.AwsPluginUtil
import com.rundeck.plugin.aws.util.AwsS3Builder
import org.rundeck.toolbelt.Command
import org.rundeck.toolbelt.CommandOutput

import java.nio.file.Files
import java.nio.file.Paths
import java.text.DateFormat
import java.text.SimpleDateFormat

@Command(description = "S3 commands" , synonyms = "s3")
class S3Command {


    @Command(synonyms = "ls", description = "List objects from a bucket")
    void list(S3ListOptions options, CommandOutput output){
        def bucket = AwsPluginUtil.parseConfig(options.bucket)
        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)
        def pathStyle = AwsPluginUtil.parseConfig(options.pathStyle)
        def format = "humanreadble" //hummanreable

        output.info("bucket: ${bucket}")
        output.info("accesskey: ${accessKey}")
        output.info("endpoint: ${endpoint}")
        output.info("region: ${region}")
        output.info("pathStyle: ${pathStyle}")

        AmazonS3 s3 = new AwsS3Builder().endpoint(endpoint)
                                        .accessKey(accessKey)
                                        .secretKey(secretKey)
                                        .region(region)
                                        .pathStyle(pathStyle!=null?true:false).build()


        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        try{
            ListObjectsV2Result result = s3.listObjectsV2(bucket)
            List<S3ObjectSummary> objects = result.getObjectSummaries()

            for (S3ObjectSummary os : objects) {
                if(format == "json"){
                    def data = [os.key, dateFormat.format(os.lastModified), os.size, os.owner.displayName, os.ETag, os.storageClass]
                    output.println(data.toString())
                }else{
                    output.println("* " + os.owner + "\t" + dateFormat.format(os.lastModified) + "\t" + os.key)
                }

            }
        }catch(Exception e){
            output.error(e.getMessage())
            System.exit(1)
        }

        /*
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket)

        ListObjectsV2Result result = s3.listObjectsV2(req)

        while (result.isTruncated()){
            result = s3.listObjectsV2(req)
            List<S3ObjectSummary> objects = result.getObjectSummaries()

            for (S3ObjectSummary os : objects) {
                if(format == "json"){
                    def data = [os.key, dateFormat.format(os.lastModified), os.size, os.owner.displayName, os.ETag, os.storageClass]
                    output.println(data.toString())
                }else{
                    output.println("* " + os.owner + "\t" + dateFormat.format(os.lastModified) + "\t" + os.key)
                }
            }

            // If there are more than maxKeys keys in the bucket, get a continuation token
            // and list the next objects.
            String token = result.getNextContinuationToken();
            output.info("Next Continuation Token: " + token);
            req.setContinuationToken(token);

            s3.download()
        }

         */
    }

    @Command(synonyms = "mb", description = "Create a bucket")
    void createBucket(S3ListOptions options, CommandOutput output){
        def bucket = AwsPluginUtil.parseConfig(options.bucket)
        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)
        def pathStyle = AwsPluginUtil.parseConfig(options.pathStyle)

        output.info("bucket: ${bucket}")
        output.info("accesskey: ${accessKey}")
        output.info("endpoint: ${endpoint}")
        output.info("region: ${region}")
        output.info("pathStyle: ${pathStyle}")

        AmazonS3 s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region)
                .pathStyle(pathStyle!=null?true:false).build()


        Bucket b = null
        if (s3.doesBucketExistV2(bucket)) {
            output.warning("Bucket ${bucket} already exists")
            System.exit(1);

        } else {
            try {
                b = s3.createBucket(bucket)
            } catch (AmazonS3Exception e) {
                output.error(e.getErrorMessage())
                System.exit(1)

            }
        }

        if(b){
            output.warning("${bucket} was created");
        }
    }

    @Command(synonyms = "rb", description = "Delete a bucket")
    void deleteBucket(S3DeleteBucketOptions options, CommandOutput output){
        def bucket = AwsPluginUtil.parseConfig(options.bucket)
        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)
        def pathStyle = AwsPluginUtil.parseConfig(options.pathStyle)
        def forceStr = AwsPluginUtil.parseConfig(options.force)

        boolean force = false
        if(forceStr){
            force = true
        }

        output.info("bucket: ${bucket}")
        output.info("accesskey: ${accessKey}")
        output.info("endpoint: ${endpoint}")
        output.info("region: ${region}")
        output.info("pathStyle: ${pathStyle}")

        AmazonS3 s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region)
                .pathStyle(pathStyle!=null?true:false).build()

        if(force){
            try {
                output.info(" - removing objects from bucket");
                ObjectListing object_listing = s3.listObjects(bucket);
                while (true) {
                    for (Iterator<?> iterator =
                            object_listing.getObjectSummaries().iterator();
                         iterator.hasNext(); ) {
                        S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
                        s3.deleteObject(bucket, summary.getKey());
                    }

                    // more object_listing to retrieve?
                    if (object_listing.isTruncated()) {
                        object_listing = s3.listNextBatchOfObjects(object_listing);
                    } else {
                        break;
                    }
                }

                output.info(" - removing versions from bucket");
                VersionListing version_listing = s3.listVersions(
                        new ListVersionsRequest().withBucketName(bucket));
                while (true) {
                    for (Iterator<?> iterator =
                            version_listing.getVersionSummaries().iterator();
                         iterator.hasNext(); ) {
                        S3VersionSummary vs = (S3VersionSummary) iterator.next();
                        s3.deleteVersion(
                                bucket, vs.getKey(), vs.getVersionId());
                    }

                    if (version_listing.isTruncated()) {
                        version_listing = s3.listNextBatchOfVersions(
                                version_listing);
                    } else {
                        break;
                    }
                }

                output.info(" OK, bucket ready to delete!");
            } catch (AmazonServiceException e) {
                output.error(e.getErrorMessage());
                System.exit(1);
            }
        }

        try {
            s3.deleteBucket(bucket);
        } catch (AmazonServiceException e) {
            output.error(e.getErrorMessage());
            System.exit(1);
        }
    }

    @Command(synonyms = "cp", description = "Copies a local file or S3 object to another location locally or in S3.")
    void copyObjects(S3CopyOptions options, CommandOutput output) {

        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)
        def pathStyle = AwsPluginUtil.parseConfig(options.pathStyle)

        def source = AwsPluginUtil.parseConfig(options.source)
        def destination = AwsPluginUtil.parseConfig(options.destination)

        def recursiveStr = AwsPluginUtil.parseConfig(options.recursive)
        def excludeStr = AwsPluginUtil.parseConfig(options.exclude)
        def includeStr = AwsPluginUtil.parseConfig(options.include)

        boolean recursive = false
        boolean exclude = false
        boolean include = false

        if (recursiveStr) {
            recursive = true
        }

        if (excludeStr) {
            exclude = true
        }

        if (includeStr) {
            include = true
        }

        URI sourceURI = source.toURI()
        URI destinationURI = destination.toURI()

        if(!sourceURI.scheme){
            output.error("source parse URI failed");
            System.exit(1);
        }

        if(!destinationURI.scheme){
            output.error("destination parse URI failed");
            System.exit(1);
        }

        if(sourceURI.scheme != "s3" && sourceURI.scheme != "file"){
            output.error("source can just be s3:// or file://");
            System.exit(1);
        }

        if(destinationURI.scheme != "s3" && destinationURI.scheme != "file"){
            output.error("destination can just be s3:// or file://");
            System.exit(1);
        }

        if(sourceURI.scheme == "file" && destinationURI.scheme == "file"){
            output.error("source and destination cannot be file");
            System.exit(1);
        }

        AmazonS3 s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region)
                .pathStyle(pathStyle!=null?true:false).build()


        if(sourceURI.scheme == "s3" && destinationURI.scheme == "s3"){
            //move objects from buckets
        }

        if(sourceURI.scheme == "s3" && destinationURI.scheme == "file"){
            //destination from s3
            def bucket = sourceURI.host
            def object = sourceURI.path

            output.info("sourceURI: ${sourceURI.toString()}")
            output.info("bucket: ${bucket}")
            output.info("object: ${object}")
            File file = new File(AwsPluginUtil.getFile(destinationURI))

            if(AwsPluginUtil.isDirectory(destinationURI)){
                if(!file.exists()){
                    output.error("${file} folder doesn't exist")
                    System.exit(1)
                }

                if(recursive){
                    //iterate
                }else{
                    //TODO: extract source file name
                    output.info("downloading ${sourceURI} on ${destinationURI}")
                }
            }else{
                output.info("parent path: ${file.parentFile}")
                output.info("name path: ${file.name}")
                if(!file.parentFile.exists()){
                    output.error("${file.parentFile} folder doesn't exist")
                    System.exit(1)
                }

                if(recursive){
                    output.error("cannot export a folder on a file")
                    System.exit(1)
                }

                output.info("downloading file ${sourceURI} on ${destinationURI}")
            }


            //TODO: validar si el path es un directorio
            //si es un directorio se debe extaer el nombre del archivo
            //si es recursivo, el destination path no puede ser un archivo, solo un directorio

            //AwsPluginUtil.downloadObject(s3, output,bucket , object )
        }

        if(sourceURI.scheme == "file" && destinationURI.scheme == "s3"){

            File file = new File(AwsPluginUtil.getFile(sourceURI))

            if(AwsPluginUtil.isDirectory(sourceURI)){
                if(!file.exists()){
                    output.error("${file} folder doesn't exist")
                    System.exit(1)
                }
                //iterate over the folder

            }else{
                if(!file.exists()){
                    output.error("${file.parentFile} file doesn't exist")
                    System.exit(1)
                }

                //destination from s3
                def bucket = destinationURI.host
                def object = destinationURI.path

                if(object.startsWith("/")){
                    object = object.replaceFirst("/","")
                }

                //TODO: check if the name must be set on s3 path
                output.info("upload file ${sourceURI} on ${destinationURI}")

                AwsPluginUtil.putObject(s3, output, bucket, object, file.absolutePath)
            }

        }



    }



}
