package com.rundeck.plugin.aws.commands.s3.model

import com.rundeck.plugin.aws.util.AwsPluginUtil
import software.amazon.awssdk.services.s3.model.S3Object

class FileData  implements Comparable  {

    String type
    String bucket
    String key
    String hash
    String path
    Date lastModified
    String owner
    long size
    String storageClass
    String basePath

    FileData(File file, String basePath) {
        this.type = "file"
        this.path = file.absolutePath
        this.lastModified = new Date(file.lastModified())
        this.size = file.size()
        this.storageClass = "FileSystem"
        this.basePath = basePath

        if(file.exists()){
            this.hash = AwsPluginUtil.getFileHash(file)
        }
    }

    FileData(S3Object object, String bucket, String basePath = null) {

        Date lastModified = Date.from(object.lastModified)

        this.bucket = bucket
        this.type = "s3"
        this.key = object.key
        this.hash = object.eTag()?.replaceAll("\"","")
        this.lastModified = lastModified
        this.owner = object.owner
        this.size = object.size
        this.storageClass = object.storageClass
        this.basePath = basePath
    }

    FileData(URI uri) {
        this.type = uri.scheme
        if(this.type == "s3"){
            this.bucket = uri.host
            this.key = AwsPluginUtil.getS3Key(uri)
        }else{
            File file = AwsPluginUtil.getFile(uri)
            this.path = file.absolutePath
            this.lastModified = new Date(file.lastModified())
            this.size = file.size()
            this.storageClass = "FileSystem"
        }


    }

    URI getUri(){
        if(this.type == "s3"){
            return new URI("${this.type}://${this.bucket}/${this.key.replaceAll(" ", "%20")}")
        }else{
            return new File(this.path).toURI()
        }
    }

    String getName(){
        if(this.type == "s3"){
            return "Bucket: ${this.bucket}, key:${this.key}"
        }else{
            return "${this.path}"
        }
    }


    String comparablePath(){
        if(this.type == "s3"){
            if(basePath){
                return this.key.replaceFirst(basePath, "")
            }else{
                return this.key
            }

        }else{
            File file = new File(this.path)
            String  comparablePath = file.absolutePath.replaceFirst(basePath, "")
            if(comparablePath.startsWith("/")){
                comparablePath = comparablePath.replaceFirst("/","")
            }
            return comparablePath
        }
    }

    @Override
    public String toString() {
        return "FileData{" +
                ", type='" + type + '\'' +
                ", key='" + key + '\'' +
                ", hash='" + hash + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    @Override
    int compareTo(Object o) {
        if (hash != o.hash)
            return -1
        if (hash == o.hash){
            String path1 = comparablePath()
            String path2 = o.comparablePath()
            if(path1!=path2){
                return -1
            }else{
                return 0
            }
        }
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true
        if (obj == null)
            return false
        if (getClass() != obj.getClass())
            return false
        FileData other = (FileData) obj
        if (hash != other.hash)
            return false
        if (hash == other.hash){
            String path1 = comparablePath()
            String path2 = other.comparablePath()
            if(path1!=path2){
                return false
            }else{
                return true
            }
        }


        return false
        return true;
    }
}
