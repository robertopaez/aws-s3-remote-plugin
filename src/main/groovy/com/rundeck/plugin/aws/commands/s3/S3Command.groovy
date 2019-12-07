package com.rundeck.plugin.aws.commands.s3

import com.rundeck.plugin.aws.commands.s3.model.FileOps
import com.rundeck.plugin.aws.commands.s3.model.FileOpsBuilder
import com.rundeck.plugin.aws.commands.s3.model.FileSystemOps
import com.rundeck.plugin.aws.commands.s3.model.FileTransferData
import com.rundeck.plugin.aws.commands.s3.model.S3Ops
import com.rundeck.plugin.aws.options.s3.S3CopyOptions
import com.rundeck.plugin.aws.options.s3.S3DeleteBucketOptions
import com.rundeck.plugin.aws.options.s3.S3ListOptions
import com.rundeck.plugin.aws.options.s3.S3SyncOptions
import com.rundeck.plugin.aws.util.AwsPluginUtil
import com.rundeck.plugin.aws.util.AwsS3Builder
import org.rundeck.toolbelt.Command
import org.rundeck.toolbelt.CommandOutput
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.CreateBucketResponse
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable
import java.text.DateFormat
import java.text.SimpleDateFormat

@Command(description = "S3 commands" , synonyms = "s3")
class S3Command {

    static String FILE_DELIMITER="/"
    boolean debug = false

    @Command(synonyms = "ls", description = "List objects from a bucket")
    void list(S3ListOptions options, CommandOutput output){
        def bucket = AwsPluginUtil.parseConfig(options.bucket)
        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)
        def recursiveString = AwsPluginUtil.parseConfig(options.recursive)
        def prefix = AwsPluginUtil.parseConfig(options.prefix)
        def format = AwsPluginUtil.parseConfig(options.format)
        boolean recursive = false

        if(recursiveString){
            recursive = Boolean.valueOf(recursiveString)
        }

        output.info("bucket: ${bucket}")
        output.info("accesskey: ${accessKey}")
        output.info("endpoint: ${endpoint}")
        output.info("region: ${region}")

        S3Client s3 = new AwsS3Builder().endpoint(endpoint)
                                        .accessKey(accessKey)
                                        .secretKey(secretKey)
                                        .region(region).build()


        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        try{

            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucket)

            if(!recursive){
                requestBuilder.delimiter(S3Command.FILE_DELIMITER)
            }

            if(prefix){
                if (!prefix.endsWith(FILE_DELIMITER)) {
                    prefix += FILE_DELIMITER;
                }
                requestBuilder.prefix(prefix)
            }

            ListObjectsV2Request request = requestBuilder.build()
            ListObjectsV2Iterable response = s3.listObjectsV2Paginator(request)

            long count =0
            for (ListObjectsV2Response page : response) {
                page.contents().forEach{ object ->

                    Date lastModified = Date.from(object.lastModified)

                    if(format == "json"){
                        def data = [object.key, dateFormat.format(lastModified), object.size, object.owner.displayName, object.eTag(), object.storageClass]
                        output.output(data.toString())
                    }else if(format == "readable"){
                        output.output("* ${count} " + object.owner + "\t" + dateFormat.format(lastModified) + "\t" + object.key)
                    }else if(format == "key"){
                        output.output("${object.key}")
                    }

                    count++
                }
            }

            if(count==0){
                output.warning("No keys found on $bucket")
            }

        }catch(Exception e){
            output.error(e.getMessage())
            System.exit(1)
        }
    }

    @Command(synonyms = "mb", description = "Create a bucket")
    void createBucket(S3ListOptions options, CommandOutput output){
        def bucket = AwsPluginUtil.parseConfig(options.bucket)
        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)

        output.info("bucket: ${bucket}")
        output.info("accesskey: ${accessKey}")
        output.info("endpoint: ${endpoint}")
        output.info("region: ${region}")

        S3Client s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region).build()

        CreateBucketResponse response = null

        Region regionAws

        if(region){
            regionAws = Region.of(region)
        }else{
            regionAws = Region.of(AwsS3Builder.DEFAULT_REGION)
        }


        CreateBucketRequest bucketBuild = CreateBucketRequest.builder()
                                                            .bucket(bucket)
                                                            .createBucketConfiguration(
                                                                    CreateBucketConfiguration.builder()
                                                                            .locationConstraint(regionAws.id())
                                                                            .build())
                                                            .build()


        try {
            response = s3.createBucket(bucketBuild);
        } catch (Exception e) {
            output.error(e.message)
            System.exit(1)

        }


        if(response){
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
        def forceStr = AwsPluginUtil.parseConfig(options.force)

        boolean force = false
        if(forceStr == "true"){
            force = true
        }

        output.info("bucket: ${bucket}")
        output.info("accesskey: ${accessKey}")
        output.info("endpoint: ${endpoint}")
        output.info("region: ${region}")

        S3Client s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region).build()


        if(force){
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucket)
            ListObjectsV2Request request = requestBuilder.build()
            ListObjectsV2Iterable response = s3.listObjectsV2Paginator(request)

            for (ListObjectsV2Response page : response) {
                page.contents().forEach{ object ->
                    def ops = new S3Ops(s3, bucket, object.key())
                    ops.delete()
                }
            }
        }

        try {DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucket).build();
            s3.deleteBucket(deleteBucketRequest)
        } catch (Exception e) {
            handleError(e.message, output)
        }

        output.warning("${bucket} was deleted");

    }

    @Command(synonyms = "cp", description = "Copies a local file or S3 object to another location locally or in S3.")
    void copyObjects(S3CopyOptions options, CommandOutput output) {

        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)

        def source = AwsPluginUtil.parseConfig(options.source)
        def destination = AwsPluginUtil.parseConfig(options.destination)

        def recursiveStr = AwsPluginUtil.parseConfig(options.recursive)

        String exclude = AwsPluginUtil.parseConfig(options.exclude)
        String include = AwsPluginUtil.parseConfig(options.include)

        output.info("source: ${source}")
        output.info("destination: ${destination}")
        output.info("recursive: ${recursiveStr}")
        output.info("exclude: ${exclude}")
        output.info("include: ${include}")

        output.info("endpoint: ${destination}")
        output.info("accessKey: ${destination}")

        boolean recursive = false

        if (recursiveStr) {
            recursive = true
        }

        URI sourceURI = source.toURI()
        URI destinationURI = destination.toURI()

        if(!sourceURI.scheme){
            this.handleError("source parse URI failed", output)
        }

        if(!destinationURI.scheme){
            this.handleError("destination parse URI failed", output)
        }

        if(sourceURI.scheme != "s3" && sourceURI.scheme != "file"){
            this.handleError("source can just be s3:// or file://", output)
        }

        if(destinationURI.scheme != "s3" && destinationURI.scheme != "file"){
            this.handleError("destination can just be s3:// or file://", output)
        }

        if(sourceURI.scheme == "file" && destinationURI.scheme == "file"){
            this.handleError("source and destination cannot be file", output)
        }

        S3Client s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region).build()

        if(sourceURI.scheme == "s3"){
            boolean isFile = false

            def bucket = sourceURI.host
            def key = AwsPluginUtil.getS3Key(sourceURI)


            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()

            try{
                def response = s3.getObject(request)
                isFile = true
            }catch(Exception){
                isFile = false
            }

            if(!isFile && destinationURI.scheme == "file" && !destinationURI.path.endsWith("/")){
                this.handleError("when the source is a path, the destination must end with /", output)
            }
        }


        FileOps sourceOps = new FileOpsBuilder().path(sourceURI)
                                                .s3(s3).builder()

        def listFiles = null

        try{
            listFiles = sourceOps.listFiles(recursive, include, exclude)
        }catch(Exception e){
            this.handleError(e.message, output)
        }

        if(!listFiles){
            this.handleError("source files is empty", output)
        }

        List<FileTransferData> transfersFiles = []

        listFiles.each {file->
            if(sourceOps.exists(file)){
                def transferData = new FileTransferData()
                transferData.source = file
                transferData.setDestinationValue(destinationURI)

                transfersFiles << transferData
            }
        }

        transfersFiles.each {transferFile->
            this.processTransfer(transferFile, s3, output)
        }

    }

    @Command(synonyms = "rm", description = "Remove objects from a bucket")
    void remove(S3ListOptions options, CommandOutput output){
        def bucket = AwsPluginUtil.parseConfig(options.bucket)
        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)
        def recursiveString = AwsPluginUtil.parseConfig(options.recursive)
        def prefix = AwsPluginUtil.parseConfig(options.prefix)

        boolean recursive = false

        if(recursiveString){
            recursive = Boolean.valueOf(recursiveString)
        }

        output.info("bucket: ${bucket}")
        output.info("accesskey: ${accessKey}")
        output.info("endpoint: ${endpoint}")
        output.info("region: ${region}")

        S3Client s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region).build()

        try{

            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucket)

            if(!recursive){
                requestBuilder.delimiter(S3Command.FILE_DELIMITER)
            }

            if(prefix){
                if (!prefix.endsWith(FILE_DELIMITER)) {
                    prefix += FILE_DELIMITER;
                }
                requestBuilder.prefix(prefix)
            }

            ListObjectsV2Request request = requestBuilder.build()
            ListObjectsV2Iterable response = s3.listObjectsV2Paginator(request)

            for (ListObjectsV2Response page : response) {
                page.contents().forEach{ object ->
                    output.output("Removing ${object.key()}")
                    def ops = new S3Ops(s3, bucket, object.key())
                    ops.delete()
                }
            }

        }catch(Exception e){
            output.error(e.getMessage())
            System.exit(1)
        }
    }

    def processTransfer(FileTransferData transferData, S3Client s3, CommandOutput output, boolean delete = false){
        if(transferData.source.type == "s3" && transferData.destination.type == "s3"){

            //source from s3
            String bucketSource = transferData.source.bucket
            String objectSource = transferData.source.key

            String bucketDestination = transferData.destination.bucket
            String objectDestination = transferData.destination.key

            CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketSource, objectSource, bucketDestination, objectDestination)
            s3.copyObject(copyObjRequest)

            if(delete){
                def ops = new S3Ops(s3, bucketSource, objectSource)
                ops.delete()
            }
        }

        if(transferData.source.type == "s3" && transferData.destination.type == "file"){

            //source from s3
            String bucket = transferData.source.bucket
            String object = transferData.source.key

            AwsPluginUtil.downloadObject(s3, output, bucket, object, transferData.destination.path)

            if(delete){
                def ops = new S3Ops(s3, bucket, object)
                ops.delete()
            }

        }

        if(transferData.source.type == "file" && transferData.destination.type == "s3"){
            //destination from s3
            def bucket = transferData.destination.bucket
            def object = transferData.destination.key

            AwsPluginUtil.putObject(s3, output, bucket, object, transferData.source.path)

            if(delete){
                def ops = new FileSystemOps(new File(transferData.source.path))
                ops.delete()
            }
        }

    }

    @Command(synonyms = "mv", description = "Copies a local file or S3 object to another location locally or in S3.")
    void moveObjects(S3CopyOptions options, CommandOutput output) {

        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)

        def source = AwsPluginUtil.parseConfig(options.source)
        def destination = AwsPluginUtil.parseConfig(options.destination)

        def recursiveStr = AwsPluginUtil.parseConfig(options.recursive)
        def exclude = AwsPluginUtil.parseConfig(options.exclude)
        def include = AwsPluginUtil.parseConfig(options.include)

        output.info("source: ${source}")
        output.info("destination: ${destination}")

        boolean recursive = false

        if (recursiveStr) {
            recursive = true
        }

        URI sourceURI = source.toURI()
        URI destinationURI = destination.toURI()

        if(!sourceURI.scheme){
            this.handleError("source parse URI failed", output)
        }

        if(!destinationURI.scheme){
            this.handleError("destination parse URI failed", output)
        }

        if(sourceURI.scheme != "s3" && sourceURI.scheme != "file"){
            this.handleError("source can just be s3:// or file://", output)
        }

        if(destinationURI.scheme != "s3" && destinationURI.scheme != "file"){
            this.handleError("destination can just be s3:// or file://", output)
        }

        if(sourceURI.scheme == "file" && destinationURI.scheme == "file"){
            this.handleError("source and destination cannot be file", output)
        }

        S3Client s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region).build()

        if(sourceURI.scheme == "s3"){
            boolean isFile = false

            def bucket = sourceURI.host
            def key = AwsPluginUtil.getS3Key(sourceURI)


            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()

            try{
                def response = s3.getObject(request)
                isFile = true
            }catch(Exception){
                isFile = false
            }

            if(!isFile && destinationURI.scheme == "file" && !destinationURI.path.endsWith("/")){
                this.handleError("when the source is a path, the destination must end with /", output)
            }
        }


        FileOps sourceOps = new FileOpsBuilder().path(sourceURI)
                .s3(s3).builder()

        def listFiles = null

        try{
            listFiles = sourceOps.listFiles(recursive, include, exclude)
        }catch(Exception e){
            output.error(e.message);
            System.exit(1);
        }

        if(!listFiles){
            this.handleError("source files is empty", output)
        }

        List<FileTransferData> transfersFiles = []

        listFiles.each {file->
            if(sourceOps.exists(file)){
                def transferData = new FileTransferData()
                transferData.source = file
                transferData.setDestinationValue(destinationURI)

                transfersFiles << transferData
            }
        }

        transfersFiles.each {transferFile->
            this.processTransfer(transferFile, s3, output, true)
        }

    }

    @Command(synonyms = "sync", description = "Syncs  directories  and S3 prefixes.")
    void syncObjects(S3SyncOptions options, CommandOutput output) {

        def accessKey = AwsPluginUtil.parseConfig(options.accessKey)
        def secretKey = AwsPluginUtil.parseConfig(options.secretKey)
        def endpoint = AwsPluginUtil.parseConfig(options.endpoint)
        def region = AwsPluginUtil.parseConfig(options.region)

        def source = AwsPluginUtil.parseConfig(options.source)
        def destination = AwsPluginUtil.parseConfig(options.destination)

        def deleteStr = AwsPluginUtil.parseConfig(options.delete)
        def exclude = AwsPluginUtil.parseConfig(options.exclude)
        def include = AwsPluginUtil.parseConfig(options.include)

        output.info("Sync folders:")
        output.info("source: ${source}")
        output.info("destination: ${destination}")
        output.info("exclude: ${exclude}")
        output.info("include: ${include}")
        output.info("delete: ${deleteStr}")

        boolean delete = false

        if (deleteStr) {
            delete = true
        }

        URI sourceURI = source.toURI()
        URI destinationURI = destination.toURI()

        if(!sourceURI.scheme){
            this.handleError("source parse URI failed", output)
        }

        if(!destinationURI.scheme){
            this.handleError("destination parse URI failed", output)
        }

        if(sourceURI.scheme != "s3" && sourceURI.scheme != "file"){
            this.handleError("source can just be s3:// or file://", output)
        }

        if(destinationURI.scheme != "s3" && destinationURI.scheme != "file"){
            this.handleError("destination can just be s3:// or file://", output)
        }

        if(sourceURI.scheme == "file" && destinationURI.scheme == "file"){
            this.handleError("source and destination cannot be file", output)
        }

        S3Client s3 = new AwsS3Builder().endpoint(endpoint)
                .accessKey(accessKey)
                .secretKey(secretKey)
                .region(region).build()

        if(sourceURI.scheme == "s3"){
            boolean isFile = false

            def bucket = sourceURI.host
            def key = AwsPluginUtil.getS3Key(sourceURI)


            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()

            try{
                def response = s3.getObject(request)
                isFile = true
            }catch(Exception){
                isFile = false
            }

            if(!isFile && destinationURI.scheme == "file" && !destinationURI.path.endsWith("/")){
                this.handleError("when the source is a path, the destination must end with /", output)
            }
        }


        FileOps sourceOps = new FileOpsBuilder().path(sourceURI)
                .s3(s3).builder()

        FileOps destinationOps = new FileOpsBuilder().path(destinationURI)
                .s3(s3).builder()

        def listSourceFiles = null
        def listDestinationFiles = null

        try{
            listSourceFiles = sourceOps.listFiles(true, include, exclude)
        }catch(Exception e){
            this.handleError(e.message, output)
        }

        try{
            listDestinationFiles = destinationOps.listFiles(true, include, exclude)
        }catch(Exception e){
            output.error(e.message);
            System.exit(1);
        }

        def newObjects = AwsPluginUtil.difference(listDestinationFiles, listSourceFiles)

        newObjects.each { object->
            def transferData = new FileTransferData()
            transferData.source = object
            transferData.setDestinationValue(destinationURI)

            this.processTransfer(transferData, s3, output, false )
        }

        def oldObjects =  AwsPluginUtil.difference(listSourceFiles, listDestinationFiles)

        if(delete){
            oldObjects.each { object->
                output.output("Removing ${object.name}")
                FileOps fileOps = new FileOpsBuilder().path(object.getUri()).s3(s3).builder()
                fileOps.delete()
            }
        }

        if(newObjects.size()==0 && oldObjects.size()==0){
            output.info("Not difference found")
        }
    }


    def handleError(String message, CommandOutput output){
        if(debug){
            output.error(message)
            throw new Exception(message)
        }else{
            output.error(message)
            System.exit(1)
        }

    }

}
