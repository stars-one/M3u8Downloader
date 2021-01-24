package com.wan.util

import java.io.*
import java.net.URL
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 此工具类仅做备份，已经重构为VideoUtil
 * @author StarsOne
 * @date Create in  2019-8-26 0026 10:09:17
 * @description
 *
 * <pre>
 *     //    val videoUtils = VideoUtils(File("Q:\\m3u8破解\\新视频\\key"))
//    println(videoUtils.decryptTs("Q:\\m3u8破解\\新视频\\新下载", "Q:\\m3u8破解\\新视频\\new.mp4"))
//    videoUtils.decryptTs("Q:\\m3u8破解\\新视频\\新下载","Q:\\m3u8破解\\新视频\\out.mp4")
 * </pre>
 */
class VideoUtils() {
    private val algorithm = "AES"
    private val transformation = "AES/CBC/PKCS5Padding"
    private var keyBytes = ByteArray(16)
    private var ivBytes: ByteArray? = null
    private var m3u8File: File? = null
    private var playTsLists = ArrayList<String>()
    private val cipher = Cipher.getInstance(transformation)

    /**
     * 使用此构造方法，需要修改m3u8中的key的uri路径
     */
    constructor(m3u8FilePath: String) : this() {

        m3u8File = File(m3u8FilePath)

        val readLines = m3u8File?.readLines() as List<String>

        for (readLine in readLines) {
            if (readLine.contains("URI")) {
                val start = readLine.indexOf("\"")
                val last = readLine.lastIndexOf("\"")
                val keyPath = readLine.substring(start + 1, last)
                //没有IV，为""
                val ivString = if (readLine.contains("IV")) readLine.substringAfter("IV=0x") else ""

                //如果是网址，则直接获得key字节数组
                val p = "[a-zA-z]+://[^\\s]*"
                if (Pattern.matches(p, keyPath)) {
                    val url = URL(keyPath)
                    val connection = url.openConnection()//打开链接
                    connection.getInputStream().read(keyBytes)
                } else {
                    val keyFile = File(keyPath)
                    keyBytes = keyFile.readBytes()
                }

                ivBytes = if (ivString.isBlank()) byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x00) else decodeHex(ivString)
                val skey = SecretKeySpec(keyBytes, algorithm)
                val iv = IvParameterSpec(ivBytes)

                cipher.init(Cipher.DECRYPT_MODE, skey, iv)// 初始化

            } else {
                if (readLine.endsWith(".ts")) {
                    playTsLists.add(readLine)
                }
            }
        }
    }

    /**
     * 只有key的情况下
     */
    constructor(keyFile: File) : this() {
        keyBytes = FileInputStream(keyFile).readBytes()
        val skey = SecretKeySpec(keyBytes, algorithm)
        ivBytes = byteArrayOf(0.toByte(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        val iv = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, skey, iv)// 初始化
    }

    constructor(key: String, ivString: String) : this() {
        keyBytes = decodeHex(key)
        ivBytes = decodeHex(ivString)
    }

    constructor(keyFile: File, ivString: String) : this() {
        keyBytes = FileInputStream(keyFile).readBytes()
        ivBytes = decodeHex(ivString)
    }

    /**
     * 解密并合并视频（视频在当前文件夹，格式为mp4)
     * @param dirPath 存放ts文件的文件夹目录
     * @return 返回mp4文件路径
     */
    public fun decryptTs(dirPath: String): String {
        val dir = File(dirPath)
        if (dir.isDirectory) {
            val tsFiles = dir.listFiles { dir, name -> name.endsWith(".ts") }
            //获得解密后的所有ts文件
            val outTsList = decryptTs(tsFiles.toList())
            //输出文件
            val outFile = File(dirPath, "new.mp4")

            //合并所有ts文件
            for (file in outTsList) {
                //文件存在就合并
                if (file.exists()) {
                    outFile.appendBytes(file.readBytes())
                    //合并之后删除文件
                    file.delete()
                }
            }
            return outFile.path
        }
        return ""
    }

    /**
     * @param dirPath 存放ts文件的文件夹目录
     * @param outFilePath 输出路径文件名（类似Q:\test\my.mp4)
     * @return 返回输出mp4文件的路径
     */
    fun decryptTs(dirPath: String, outFilePath: String): String {
        val dir = File(dirPath)
        val outFile = File(outFilePath)
        if (dir.isDirectory) {
            val tsFiles = dir.listFiles { file -> file.name.endsWith(".ts") }
            val outTsList = decryptTs(tsFiles.toList())
            for (file in outTsList) {
                if (file.exists()) {
                    outFile.appendBytes(file.readBytes())
                    file.delete()
                }
            }
            return outFile.path
        }
        return ""
    }

    private fun decryptTs(tsList: List<File>): List<File> {
        val files = ArrayList<File>()
        //没有m3u8文件，单独对某文件夹里的ts文件进行解密
        if (playTsLists.size == 0) {
            for (file in tsList) {
                //输出的ts文件路径 (Q:\test\b_440.ts...)
                val outFile = File("${file.parent}${File.separator}b_${file.name}")
                //添加到list中，之后合并
                files.add(outFile)
                //得到解密后的ts文件
                decryptTs(file, outFile)
            }
            return files
        }
        //有m3u8文件
        val iterator = playTsLists.iterator()
        while (iterator.hasNext()) {
            val name = iterator.next()

            for (file in tsList) {
                val srcname = file.name
                //保证顺序与m3u8文件中的顺序相同
                if (srcname == name) {
                    //输出的ts文件路径 (Q:\test\b_440.ts...)
                    val outFile = File("${file.parent}${File.separator}b_$name")
                    //添加到list中，之后合并
                    files.add(outFile)
                    //得到解密后的ts文件
                    decryptTs(file, outFile)
                    break
                }
            }
        }
        return files
    }

    /**
     * AES（256）解密并输出ts文件
     * @param srcFile  输入文件
     * @param outFile  输出文件
     * @throws Exception
     */
    public fun decryptTs(srcFile: File, outFile: File) {
        try {

            //返回解密之后的文件bytes[]
            val readBytes = srcFile.readBytes()
            val result = cipher.doFinal(readBytes)
            bytesWriteToFile(result, outFile)
        } catch (e: Exception) {
            println("${srcFile.name}解密出错，错误为${e.message}")
        } finally {
            return
        }
    }


    /**
     * 输出解密后的ts文件（将Byte数组转换成文件）
     */
    private fun bytesWriteToFile(bytes: ByteArray?, outFile: File) {
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null

        try {
            fos = FileOutputStream(outFile)
            bos = BufferedOutputStream(fos)
            bos.write(bytes!!)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (bos != null) {
                try {
                    bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun decodeHex(input: String): ByteArray {
        val data = input.toCharArray()
        val len = data.size
        if (len and 0x01 != 0) {
            try {
                throw Exception("Odd number of characters.")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        val out = ByteArray(len shr 1)

        try {
            var i = 0
            var j = 0
            while (j < len) {
                var f = toDigit(data[j], j) shl 4
                j++
                f = f or toDigit(data[j], j)
                j++
                out[i] = (f and 0xFF).toByte()
                i++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return out
    }

    @Throws(Exception::class)
    private fun toDigit(ch: Char, index: Int): Int {
        val digit = Character.digit(ch, 16)
        if (digit == -1) {
            throw Exception("Illegal hexadecimal character $ch at index $index")
        }
        return digit
    }

    private fun cipher(cipher: Cipher) = cipher

}
