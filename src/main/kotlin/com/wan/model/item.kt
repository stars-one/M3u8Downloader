package com.wan.model

import java.io.File
import javax.crypto.Cipher

/**
 *
 * @author StarsOne
 * @date Create in  2020/1/14 0014 22:24
 * @description
 *
 */
data class Item(var m3u8Url: String, var fileName: String, var threadCount: Int, var dirPath: String)


class KeyItem() {
    //解密对象
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    var keyBytes = byteArrayOf()
    var ivBytes = byteArrayOf()

    //是否加密
    var isEncrypt =false
    //ts文件列表
    val tsFileItemList = arrayListOf<TsFileItem>()
}

/**
 * @property index ts索引
 * @property url ts文件的下载地址
 * @property tsFile ts文件
 * @property outputFile 解密出的ts文件
 */
data class TsFileItem(var index: Int, var url: String, var tsFile: File, var outputFile: File)

