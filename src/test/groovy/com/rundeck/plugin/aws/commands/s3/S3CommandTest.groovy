package com.rundeck.plugin.aws.commands.s3

import com.rundeck.plugin.aws.commands.s3.minio.MinioContainer
import com.rundeck.plugin.aws.options.s3.S3CopyOptions
import com.rundeck.plugin.aws.options.s3.S3DeleteBucketOptions
import com.rundeck.plugin.aws.options.s3.S3ListOptions
import com.rundeck.plugin.aws.options.s3.S3SyncOptions
import groovy.io.FileType
import org.rundeck.toolbelt.CommandOutput
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class S3CommandTest extends Specification {

    @Shared
    public MinioContainer minio = new MinioContainer<>()

    @Shared
    String accessKey = "admin"

    @Shared
    String secretKey = "admin456"

    def setupSpec() {

        minio.withAccess(accessKey, secretKey)
        minio.start()
    }

    void cleanupSpec() {
        minio.stop()
    }

    def "test create bucket"(){
        given:

        def bucket = "rundeck"
        def options = Mock(S3ListOptions){
            getBucket()>>bucket
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.createBucket(options,out)

        then:
        1 * out.warning("${bucket} was created")

    }

    def "test list empty bucket"(){
        given:

        def bucket = "rundeck"
        def options = Mock(S3ListOptions){
            getBucket()>>bucket
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        //command.createBucket(options,out)

        command.list(options, out)

        then:
        //1 * out.warning("${bucket} was created")
        1 * out.warning("No keys found on $bucket")

    }

    def "test upload file to bucket"(){
        given:

        def temp = File.createTempFile("test",".txt")
        temp.text="This is a test"
        temp.deleteOnExit()
        def bucket = "rundeck"

        def source = "file://${temp.getAbsolutePath()}"
        def destination = "s3://${bucket}"

        def options = Mock(S3ListOptions){
            getBucket()>>bucket
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()
        command.copyObjects(copyOptions,out )

        then:
        1 * out.output("Uploading ${temp.getAbsolutePath()}  to S3 bucket ${bucket}/${temp.getName()}")
    }

    def "test upload folder to bucket"(){
        given:

        Path testDir = Files.createTempDirectory("copy-test")
        def temp = File.createTempFile("test",".txt", testDir.toFile())
        temp.text="This is a test"
        temp.deleteOnExit()

        def temp1 = File.createTempFile("test1",".txt", testDir.toFile())
        temp1.text="This is a test"
        temp1.deleteOnExit()

        def bucket = "rundeck"

        def source = "file://${testDir.toFile().getAbsolutePath()}"
        def destination = "s3://${bucket}"

        def options = Mock(S3ListOptions){
            getBucket()>>bucket
            getFormat()>>"key"
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.copyObjects(copyOptions,out )
        command.list(options,out)

        then:
        1 * out.output("Uploading ${temp.getAbsolutePath()}  to S3 bucket ${bucket}/${temp.getName()}")
        1 * out.output("Uploading ${temp1.getAbsolutePath()}  to S3 bucket ${bucket}/${temp1.getName()}")
        1 * out.output("${temp1.getName()}")
        1 * out.output("${temp.getName()}")

    }

    def "test upload folder to bucket include/exclude"(){
        given:

        Path testDir = Files.createTempDirectory("copy-test")
        def temp = File.createTempFile("test",".txt", testDir.toFile())
        temp.text="This is a test"
        temp.deleteOnExit()

        def temp1 = File.createTempFile("test1",".txt", testDir.toFile())
        temp1.text="This is a test"
        temp1.deleteOnExit()

        def temp2 = File.createTempFile("test2",".yaml", testDir.toFile())
        temp2.text="This is a test"
        temp2.deleteOnExit()

        def temp3 = File.createTempFile("test3",".yaml", testDir.toFile())
        temp3.text="This is a test"
        temp3.deleteOnExit()

        def bucket = "rundeck"

        def source = "file://${testDir.toFile().getAbsolutePath()}"
        def destination = "s3://${bucket}"

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
            getExclude()>>exclude
            getInclude()>>include
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.copyObjects(copyOptions,out )

        then:
        txt * out.output("Uploading ${temp.getAbsolutePath()}  to S3 bucket ${bucket}/${temp.getName()}")
        txt * out.output("Uploading ${temp1.getAbsolutePath()}  to S3 bucket ${bucket}/${temp1.getName()}")
        yaml * out.output("Uploading ${temp2.getAbsolutePath()}  to S3 bucket ${bucket}/${temp2.getName()}")
        yaml * out.output("Uploading ${temp3.getAbsolutePath()}  to S3 bucket ${bucket}/${temp3.getName()}")

        where:
        exclude | include | txt | yaml
        ".yaml" | null    | 1   | 0
        ".txt"  | null    | 0   | 1
        null    | null    | 1   | 1
        null    | ".yaml" | 0   | 1
        null    | ".txt"  | 1   | 0

    }

    def "test download files from bucket"(){
        given:
        def bucket = "test"

        //load folder to bucket
        def listFiles = copyFile(bucket)

        Path testDir = Files.createTempDirectory("copy-test2")

        def source = "s3://${bucket}"
        def destination = "${testDir.toFile().getAbsolutePath()}/"

       def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getRecursive()>>true
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.copyObjects(copyOptions,out )

        def listResult = filesFromFolder(testDir)

        then:
        listResult.size() == listFiles.size()
    }

    def "test download single file from bucket"(){
        given:
        def bucket = "test2"

        //load folder to bucket
        def listFiles = copyFile(bucket)

        Path testDir = Files.createTempDirectory("copy-test14")

        def source = "s3://${bucket}/${listFiles.get(0).name}"
        def destination = "${testDir.toFile().getAbsolutePath()}/test.txt"

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getRecursive()>>true
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.copyObjects(copyOptions,out )

        def listResult = filesFromFolder(testDir)

        then:
        listResult.size() == 1
    }

    def "test download single file from bucket, file doesnt exists"(){
        given:
        def bucket = "test33"

        //load folder to bucket
        def listFiles = copyFile(bucket)

        Path testDir = Files.createTempDirectory("copy-test14")

        def source = "s3://${bucket}/nofile.txt"
        def destination = "${testDir.toFile().getAbsolutePath()}/test.txt"

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getRecursive()>>true
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()
        command.debug = true

        String errorMessage = ""
        try{
            command.copyObjects(copyOptions,out )
        }catch(Exception e){
            errorMessage = e.message
        }

        then:
        errorMessage !=null
        errorMessage.contains("The specified key does not exist")
    }


    def "test upload folder to bucket bad format"(){
        given:

        createBucket("rundeck1")

        File parent = new File(System.getProperty("java.io.tmpdir"))
        File temp = new File(parent, "test1")

        if (temp.exists()) {
            temp.delete()
        }

        temp.mkdir()

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()
        command.debug = true

        String errorMessage = ""
        try{
            command.copyObjects(copyOptions,out )
        }catch(Exception e){
            errorMessage = e.message
        }

        deleteBucket("rundeck1")

        then:
        1 * out.error(message)
        errorMessage == message

        where:
        source                  | destination               | error   | message
        "dsadsad"               | "dsadsadsa"               | true    | "source parse URI failed"
        "s3://dasdsad"          | "dsadsadsa"               | true    | "destination parse URI failed"
        "http://dasdsad"        | "s3://dasdsad"            | true    | "source can just be s3:// or file://"
        "s3://dasdsad"          | "http://dsadsadsa"        | true    | "destination can just be s3:// or file://"
        "file://dasdsad"        | "file://dsadsadsa"        | true    | "source and destination cannot be file"
        "s3://dasdsad"          | "http://dsadsadsa"        | true    | "destination can just be s3:// or file://"

    }

    def "test move folder to bucket bad format"(){
        given:

        copyFile("rundeck2")

        File parent = new File(System.getProperty("java.io.tmpdir"))
        File temp = new File(parent, "test1")

        if (temp.exists()) {
            temp.delete()
        }

        temp.mkdir()


        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()
        command.debug = true

        String errorMessage = ""
        try{
            command.moveObjects(copyOptions,out )
        }catch(Exception e){
            errorMessage = e.message
        }

        deleteBucket("rundeck2")

        then:
        errorMessage == message
        error * out.error(_)

        where:
        source                  | destination               | error   | message
        "dsadsad"               | "dsadsadsa"               | 1    | "source parse URI failed"
        "s3://dasdsad"          | "dsadsadsa"               | 1    | "destination parse URI failed"
        "http://dasdsad"        | "s3://dasdsad"            | 1    | "source can just be s3:// or file://"
        "s3://dasdsad"          | "http://dsadsadsa"        | 1    | "destination can just be s3:// or file://"
        "file://dasdsad"        | "file://dsadsadsa"        | 1    | "source and destination cannot be file"
        "s3://dasdsad"          | "http://dsadsadsa"        | 1    | "destination can just be s3:// or file://"
        "s3://rundeck2"          | "file://dsadsadsa.txt"    | 1    | "when the source is a path, the destination must end with /"
        "s3://rundeck2/test"     | "file://somepath/"        | 1    | "source files is empty"
        "s3://rundeck2"          | "${System.getProperty("java.io.tmpdir")}test1"           | 1    | "when the source is a path, the destination must end with /"
        "s3://rundeck2"          | "${System.getProperty("java.io.tmpdir")}test1/"           | 0    | ""
    }

    def "test sync files from bucket"(){
        given:
        def bucket = "testsync"

        //load folder to bucket
        def listFiles = copyFile(bucket)

        Path testDir = Files.createTempDirectory("copy-test2")

        def source = "s3://${bucket}"
        def destination = "${testDir.toFile().getAbsolutePath()}/"

        def copyOptions = Mock(S3SyncOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.syncObjects(copyOptions,out )

        def listResult = filesFromFolder(testDir)

        then:
        listResult.size() == listFiles.size()
    }

    def "test sync files from bucket subpath"(){
        given:
        def bucket = "testsync2"
        def subpath = "subpath"
        //load folder to bucket
        def listFiles = copyFile(bucket, subpath)

        Path testDir = Files.createTempDirectory("copy-test-sync")

        def source = "s3://${bucket}/${subpath}"

        def destination = "${testDir.toFile().getAbsolutePath()}/"

        def copyOptions = Mock(S3SyncOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.syncObjects(copyOptions,out )

        def listResult = filesFromFolder(testDir)

        then:
        listResult.size() == listFiles.size()
    }

    def "test sync single file from bucket"(){
        given:
        def bucket = "testsync3"

        //load folder to bucket
        def listFiles = copyFile(bucket)

        Path testDir = Files.createTempDirectory("copy-test2")

        def source = "s3://${bucket}/${listFiles.get(0).name}"
        def destination = "${testDir.toFile().getAbsolutePath()}/"

        def copyOptions = Mock(S3SyncOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.syncObjects(copyOptions,out )

        def listResult = filesFromFolder(testDir)

        then:
        listResult.size() == 1
    }

    def "test sync folder to bucket"(){
        given:

        Path testDir = Files.createTempDirectory("copy-test")
        def temp = File.createTempFile("test",".txt", testDir.toFile())
        temp.text="This is a test"
        temp.deleteOnExit()

        def temp1 = File.createTempFile("test1",".txt", testDir.toFile())
        temp1.text="This is a test"
        temp1.deleteOnExit()

        def bucket = "rundeck"

        def source = "file://${testDir.toFile().getAbsolutePath()}"
        def destination = "s3://${bucket}"

        def copyOptions = Mock(S3SyncOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.syncObjects(copyOptions,out )

        then:
        1 * out.output("Uploading ${temp.getAbsolutePath()}  to S3 bucket ${bucket}/${temp.getName()}")
        1 * out.output("Uploading ${temp1.getAbsolutePath()}  to S3 bucket ${bucket}/${temp1.getName()}")

    }

    def "test sync folder to bucket bad format"(){
        given:

        createBucket("rundeck4")

        File parent = new File(System.getProperty("java.io.tmpdir"))
        File temp = new File(parent, "test")

        if (temp.exists()) {
            temp.delete()
        }

        temp.mkdir()


        def copyOptions = Mock(S3SyncOptions){
            getSource()>>source
            getDestination()>>destination
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()
        command.debug = true

        String errorMessage = ""
        try{
            command.syncObjects(copyOptions,out )
        }catch(Exception e){
            errorMessage = e.message
        }

        deleteBucket("rundeck4")


        then:
        errorCalls * out.error(message)
        errorMessage == message

        where:
        source                  | destination               | errorCalls   | message
        "dsadsad"               | "dsadsadsa"               | 1    | "source parse URI failed"
        "s3://dasdsad"          | "dsadsadsa"               | 1    | "destination parse URI failed"
        "http://dasdsad"        | "s3://dasdsad"            | 1    | "source can just be s3:// or file://"
        "s3://dasdsad"          | "http://dsadsadsa"        | 1    | "destination can just be s3:// or file://"
        "file://dasdsad"        | "file://dsadsadsa"        | 1    | "source and destination cannot be file"
        "s3://dasdsad"          | "http://dsadsadsa"        | 1    | "destination can just be s3:// or file://"


    }

    def "test move folder to bucket"(){
        given:

        Path testDir = Files.createTempDirectory("copy-test")
        def temp = File.createTempFile("test",".txt", testDir.toFile())
        temp.text="This is a test"
        temp.deleteOnExit()

        def temp1 = File.createTempFile("test1",".txt", testDir.toFile())
        temp1.text="This is a test"
        temp1.deleteOnExit()

        def bucket = "rundeck"

        def source = "file://${testDir.toFile().getAbsolutePath()}"
        def destination = "s3://${bucket}"

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getRecursive()>>true
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()

        command.moveObjects(copyOptions,out )

        then:
        1 * out.output("Uploading ${temp.getAbsolutePath()}  to S3 bucket ${bucket}/${temp.getName()}")
        1 * out.output("Uploading ${temp1.getAbsolutePath()}  to S3 bucket ${bucket}/${temp1.getName()}")
        !temp.exists()
        !temp1.exists()

    }

    def "test delete bucket"(){
        given:

        def bucket = "rundeck"
        def options = Mock(S3DeleteBucketOptions){
            getBucket()>>bucket
            getForce()>>force
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)

        when:
        S3Command command = new S3Command()
        command.debug = true

        String errorMessage = null
        try{
            command.deleteBucket(options,out)
        }catch(Exception e){
            errorMessage = e.message
        }

        then:
        calls * out.warning("${bucket} was deleted")
        if(message){
            errorMessage.startsWith(message)
        }


        where:
        force | calls | message
        false | 0     | "The bucket you tried to delete is not empty"
        true  | 1     | null

    }

    def copyFile(String bucket, String path = null){

        Path testDir = Files.createTempDirectory("copy-test")
        def temp = File.createTempFile("test",".txt", testDir.toFile())
        temp.text="This is a test"
        temp.deleteOnExit()

        def temp1 = File.createTempFile("test1",".txt", testDir.toFile())
        temp1.text="This is a test"
        temp1.deleteOnExit()

        Path testSubDir = Files.createTempDirectory(testDir,"subfolder")

        def temp3 = File.createTempFile("test2",".txt", testSubDir.toFile())
        temp3.text="This is a test"
        temp3.deleteOnExit()

        def temp4 = File.createTempFile("test3",".txt", testSubDir.toFile())
        temp4.text="This is a test"
        temp4.deleteOnExit()

        def source = "file://${testDir.toFile().getAbsolutePath()}"
        def destination = "s3://${bucket}"
        if(path!=null){
            destination = "s3://${bucket}/${path}"
        }

        def options = Mock(S3ListOptions){
            getBucket()>>bucket
            getFormat()>>"key"
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getRecursive()>>true
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }
        def out = Mock(CommandOutput)
        S3Command command = new S3Command()
        command.createBucket(options, out)
        command.copyObjects(copyOptions,out )

        return [temp, temp1, temp3,temp4]
    }

    def createBucket(String bucket){
        def options = Mock(S3ListOptions){
            getBucket()>>bucket
            getFormat()>>"key"
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }

        def out = Mock(CommandOutput)
        S3Command command = new S3Command()
        command.createBucket(options, out)
    }

    def deleteBucket(String bucket){
        def options = Mock(S3DeleteBucketOptions){
            getBucket()>>bucket
            getForce()>>"true"
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }

        def out = Mock(CommandOutput)
        S3Command command = new S3Command()
        command.deleteBucket(options, out)
    }

    def loadFileToExistingBucket(String bucket, String key){
        Path testDir = Files.createTempDirectory("copy-test")
        def temp = File.createTempFile("test",".txt", testDir.toFile())
        temp.text="This is a test"
        temp.deleteOnExit()

        def source = "file://${temp.getAbsolutePath()}"
        def destination = "s3://${bucket}/${key}"

        def options = Mock(S3ListOptions){
            getBucket()>>bucket
            getFormat()>>"key"
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }

        def copyOptions = Mock(S3CopyOptions){
            getSource()>>source
            getDestination()>>destination
            getRecursive()>>true
            getAccessKey()>>accessKey
            getSecretKey()>>secretKey
            getEndpoint()>>minio.endpoint
        }

        def out = Mock(CommandOutput)
        S3Command command = new S3Command()
        command.copyObjects(copyOptions,out )
    }


    def filesFromFolder(Path dir){
        def list = []
        dir.eachFileRecurse (FileType.FILES) { file ->
            list << file
        }

        return list
    }
    
}
