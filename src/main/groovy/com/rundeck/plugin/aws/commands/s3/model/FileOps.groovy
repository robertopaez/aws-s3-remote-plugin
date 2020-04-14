package com.rundeck.plugin.aws.commands.s3.model

interface FileOps {

    URI getPath();

    List<FileData> listFiles(boolean recursive, String include, String exclude, boolean createFolder);

    boolean delete();

    boolean exists(FileData file);

}
