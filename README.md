
# DFORG
A Windows app made for Rita. She is tired of order her media, so a i have an excuse for play with javafx, jpackage and java.nio.

## What does?
It will organize al media files (video, image and audio) in a folders like yyyy-MM

## How to use?
 1. Select source folders: this are the folders from where the files will be processed
 2. Select folder: the results of the process will be dumped in this folder (also the logs)
 3. Select copy or not: if not checked no file will be copied and only the logs will be generated
 4. Process: to init the process
 
## Understanding logs
 1. copied-media.log: it means that the file was copied without any problem
 2. copied-media-unknown-date.log: it means that the file is media but can not decode the date of creation, so copied to "unknownDate" folder
 3. copied-media-name-conflict.log: it means that the file was copied but a diff file already use that name, so the file is copied with "(n)" at the end.
 4. not-copied-repeated.log: it means that a file with the same name and content already exist, so not copied
 5. not-copied-not-media.log: it means that the process does not understand the file as a media file, so not copied
 6. not-copied-fail.log: the process fail in copy this file.

## Devs

### Eclipse VM Arguments

```bash

--module-path JAVAFX_SDK_JAR_PATH --add-modules javafx.base,javafx.controls,javafx.graphics

```

### JavaFX SDK

https://gluonhq.com/products/javafx/

### Build

```bash

mvn clean package

```

Must have WiX Toolset installed: https://wixtoolset.org/