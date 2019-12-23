# AWS S3 plugin 

This plugin provides a series of workflow node steps that perform AWS S3 operations like move files from/to S3 buckets. The plugin is a java base plugin which runs on the remote node.

## Requirements

* Java must be installed on the remote node

## Install

* run `gradle build`
* copy `script-pluginn/libs/aws-s3-plugin-1.0.0-SNAPSHOT.zip` to rundeck `libext` dolder

## Workflow Steps

### List buckets (aws/ remote / s3 / ls)
List S3 objects and common prefixes under a prefix or all S3 buckets

Parameters:
* Bucket: (require) S3 Bucket name
* Prefix: (optional) path Prefix
* Recursive: (optional) Command is performed on all files or objects under the specified directory or prefix.

* Region: (optional) AWS region name to use
* Endpoint: (optional) a custom S3 compatible endpoint to use, such as https://my-host.com/s3
* Access Key: (require) AWS Access Key
* Secret Key: (require) AWS Secret Key (selected from key storage)

### Create Bucket (aws / remote / s3 / mb)
Creates a S3 bucket.

Parameters:
* Bucket: (require) S3 Bucket name

* Region: (optional) AWS region name to use
* Endpoint: (optional) a custom S3 compatible endpoint to use, such as https://my-host.com/s3
* Access Key: (require) AWS Access Key
* Secret Key: (require) AWS Secret Key (selected from key storage)

### Remove Bucket (aws / remote / s3 / mb)
Remove a S3 bucket.

Parameters:
* Bucket: (require) S3 Bucket name
* Force: (require) Force to delete objects if the bucket is not empty

* Region: (optional) AWS region name to use
* Endpoint: (optional) a custom S3 compatible endpoint to use, such as https://my-host.com/s3
* Access Key: (require) AWS Access Key
* Secret Key: (require) AWS Secret Key (selected from key storage)

### Copy files from/to buckets (aws / remote / s3 / cp)
Copies a local file or S3 object to another location locally or in S3. <LocalPath> <S3Uri> or <S3Uri> <LocalPath> or <S3Uri> <S3Uri>

Parameters:
* Source: (require) Source <LocalPath> or <S3Uri>
* Destination: (require) Destination <LocalPath> or <S3Uri>
* Recursive: (optional) Command is performed on all files or objects under the specified directory or prefix.
* Exclude: (optional) Exclude all files or objects from the command that matches the specified pattern.
* Include: (optional) Don't exclude files or objects in the command that match the specified pattern.

* Region: (optional) AWS region name to use
* Endpoint: (optional) a custom S3 compatible endpoint to use, such as https://my-host.com/s3
* Access Key: (require) AWS Access Key
* Secret Key: (require) AWS Secret Key (selected from key storage)

### Remove files from buckets (aws/ remote / s3 / rm)
Remove S3 objects and common prefixes under a prefix or all S3 buckets

Parameters:
* Bucket: (require) S3 Bucket name
* Prefix: (optional) path Prefix
* Recursive: (optional) Command is performed on all files or objects under the specified directory or prefix.

* Region: (optional) AWS region name to use
* Endpoint: (optional) a custom S3 compatible endpoint to use, such as https://my-host.com/s3
* Access Key: (require) AWS Access Key
* Secret Key: (require) AWS Secret Key (selected from key storage)


### Move files from/to buckets (aws / remote / s3 / mv)
Move a local file or S3 object to another location locally or in S3. <LocalPath> <S3Uri> or <S3Uri> <LocalPath> or <S3Uri> <S3Uri>

Parameters:
* Source: (require) Source <LocalPath> or <S3Uri>
* Destination: (require) Destination <LocalPath> or <S3Uri>
* Recursive: (optional) Command is performed on all files or objects under the specified directory or prefix.
* Exclude: (optional) Exclude all files or objects from the command that matches the specified pattern.
* Include: (optional) Don't exclude files or objects in the command that match the specified pattern.

* Region: (optional) AWS region name to use
* Endpoint: (optional) a custom S3 compatible endpoint to use, such as https://my-host.com/s3
* Access Key: (require) AWS Access Key
* Secret Key: (require) AWS Secret Key (selected from key storage)


### Syncs directories and S3 buckets. (aws / remote / s3 / sync)
Syncs directories and S3 prefixes. <LocalPath> <S3Uri> or <S3Uri> <LocalPath> or <S3Uri> <S3Uri>

Parameters:
* Source: (require) Source <LocalPath> or <S3Uri>
* Destination: (require) Destination <LocalPath> or <S3Uri>
* Delete: (optional) Files that exist in the destination but not in the source are deleted during sync.
* Exclude: (optional) Exclude all files or objects from the command that matches the specified pattern.
* Include: (optional) Don't exclude files or objects in the command that match the specified pattern.

* Region: (optional) AWS region name to use
* Endpoint: (optional) a custom S3 compatible endpoint to use, such as https://my-host.com/s3
* Access Key: (require) AWS Access Key
* Secret Key: (require) AWS Secret Key (selected from key storage)



