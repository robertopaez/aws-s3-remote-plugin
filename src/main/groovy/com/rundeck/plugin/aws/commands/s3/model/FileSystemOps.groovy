package com.rundeck.plugin.aws.commands.s3.model

import com.rundeck.plugin.aws.util.AwsPluginUtil
import groovy.io.FileType

import java.util.regex.Pattern

class FileSystemOps implements FileOps {

    URI path
    File file

    FileSystemOps(URI path) {
        this.path = path
    }

    FileSystemOps(File file) {
        this.file = file
    }

    @Override
    URI getPath() {
        return path
    }

    @Override
    List<FileData> listFiles(boolean recursive, String include, String exclude) {

        if(include=="false"){
            include = null
        }

        if(exclude=="false"){
            exclude = null
        }

        List<FileData> list = []
        //check if is a folder o file
        if(AwsPluginUtil.isDirectory(path)){
            File dir = new File(path)
            if(!dir.exists()){
                throw new Exception("${dir.absolutePath} doesn't exists")
            }

            Pattern excludePattern = exclude!=null?~/${exclude}/:null
            Pattern includePattern = include!=null?~/${include}/:null

            //TODO: filter by include and exclude
            if(recursive){
                dir.eachFileRecurse (FileType.FILES) { file ->
                    if(checkAddFile(file,includePattern, excludePattern )){
                        list << new FileData(file, dir.absolutePath)
                    }
                }
            }else{
                dir.eachFile (FileType.FILES) { file ->
                    if(checkAddFile(file,includePattern, excludePattern )){
                        list << new FileData(file, dir.absolutePath)
                    }
                }
            }
        }else{
            //if it is a file, check if exists
            File file =  new File(AwsPluginUtil.getFile(path))
            list << new FileData(file, file.parentFile.absolutePath)
            if(!file.exists()){
                throw new Exception("${file.absolutePath} doesn't exists")
            }
        }
        return list
    }

    @Override
    boolean delete() {
        if(this.file){
            return this.file.delete()
        }else{
            if(this.path){
                this.file = new File(this.path)
                if(this.file.exists()){
                    this.file.delete()
                }
            }else{
                return false
            }
        }
    }

    @Override
    boolean exists(FileData file) {
        File localFile = new File(file.path)
        return localFile.exists()
    }

    def checkAddFile = { File file, Pattern includePattern, Pattern excludePattern   ->
        Boolean excludeFile = null
        Boolean includeFile = null

        if(excludePattern){
            if(checkFilenamePattern(file,excludePattern)) {
                excludeFile = true
            }else{
                excludeFile = false
            }
        }

        if(includePattern){
            if(checkFilenamePattern(file,includePattern)) {
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

    def checkFilenamePattern = { File file, Pattern filePattern ->
        if (filePattern.matcher(file.name).find()) {
            return true
        }
        return false
    }
}
