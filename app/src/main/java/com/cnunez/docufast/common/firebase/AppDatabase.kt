package com.cnunez.docufast.common.firebase

abstract class AppDatabase {
    abstract fun photoDao(): PhotoDaoFirebase
    abstract fun textFileDao(): TextFileDaoFirebase
}