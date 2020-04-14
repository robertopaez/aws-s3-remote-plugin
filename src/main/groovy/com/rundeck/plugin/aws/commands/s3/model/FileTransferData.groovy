package com.rundeck.plugin.aws.commands.s3.model

import com.rundeck.plugin.aws.util.AwsPluginUtil

class FileTransferData {
    FileData source
    FileData destination

    void setDestinationValue(URI destination){

        if(destination.scheme ==  "file"){
            if(AwsPluginUtil.isDirectory(destination)){
                File dir = new File(AwsPluginUtil.getFile(destination))
                if(!dir.exists()){
                    dir.parentFile.mkdirs()
                }

                String path = dir.absolutePath + File.separator + source.key
                if(source.basePath){
                    path =   dir.absolutePath + File.separator +  source.key.replaceFirst(source.basePath, "")
                }
                File file = new File(path)
                if(!file.parentFile.exists()){
                    file.parentFile.mkdirs()
                }
                this.destination = new FileData(file, dir.absolutePath)
            }else{
                File file = new File(AwsPluginUtil.getFile(destination))

                if(!file.parentFile.exists()){
                    throw new Exception("Parent folder ${file.parentFile.absolutePath} doesn't exists")
                }

                this.destination = new FileData(file, file.absolutePath)
            }
        }

        if(destination.scheme ==  "s3"){

            def getKey = {object ->
                String key = null

                if(object.key){
                    if(object.key.endsWith("/")){
                        key = object.key.substring(0, object.key.size()-1)
                    }else{
                        key = object.key
                    }
                }else{
                    key = ""
                }

                if(this.source.type ==  "s3") {
                    if(object.path == null){
                        return this.source.key
                    }else{
                        return key + this.source.key
                    }
                }else{
                    File sourceFile = new File(this.source.path)
                    if(this.source.basePath){
                        String path = sourceFile.absolutePath.replaceAll(this.source.basePath,"")

                        return key + path
                    }else{
                        return key + sourceFile.name
                    }
                }
            }

            if(destination.path == null || destination.path.endsWith("/") || !destination.path.contains(".")){
                //just he bucket was set
                FileData destinationObject = new FileData(destination)
                String key = getKey(destinationObject)
                if(key.startsWith("/")){
                    key = key.replaceFirst("/","")
                }
                destinationObject.key = key
                this.destination = destinationObject
            }

            if(destination.path.contains(".")){
                this.destination = new FileData(destination)
            }

        }
    }


}
