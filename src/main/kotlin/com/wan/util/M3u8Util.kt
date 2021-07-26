import site.starsone.download.KxDownloader
import site.starsone.model.DownloadMessage
import site.starsone.model.HttpParam
import java.io.File
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

fun main() {

    val dirFile = "D:\\temp\\m3u8\\test6"

    val m3u8File = File(dirFile, "test.m3u8")
    val outputFile = File(dirFile, "output.mp4")
    val m3u8Info = M3u8Info(file = m3u8File, outputFile = outputFile)
    m3u8Info.url = "https://video.dious.cc/20200617/fYdT3OVu/1000kb/hls/index.m3u8"
    //m3u8Info.url = "https://v5.szjal.cn/20201205/HNH1sNwn/index.m3u8"


    M3u8Util.parseInfo(m3u8Info) {
        onProgress {
            println(it.percent)
        }

        onFinish {
            println("下载m3u8文件结束")
            val urlList = arrayListOf<String>()
            val fileList = arrayListOf<File>()
            m3u8Info.tsInfoList.forEach {
                it.tsFiles.forEach { file ->
                    fileList.add(file)
                }
                it.tsUrlList.forEach { url ->
                    urlList.add(url)
                }
            }

            KxDownloader.downloadFileListByMultiThread(urlList, fileList, m3u8Info.httpParam, null) {
                onItemFinish {
                    println(it.file.name + "已下载完成")
                }
                onItemError { downloadMessage, e ->
                    println(downloadMessage.file.name + "失败,原因" + e.message)
                }
                onItemProgress {
                    //println("${it.file.name} : ${it.progress}")
                }
                onFinish {
                    println("全部下载完毕")
                    M3u8Util.decrypt(m3u8Info)
                    println("解密完成")
                    M3u8Util.merge(m3u8Info)
                }

            }
        }
    }

}

class M3u8Util {

    companion object {
        private val algorithm = "AES"
        private val transformation = "AES/CBC/PKCS5Padding"
        private val cipher = Cipher.getInstance(transformation) //解密对象

        private val urlRegex = "[a-zA-z]+://[^\\s]*"//网址正则表达式


        fun parseInfo(m3u8Info: M3u8Info, uiUpdateListenerBuilder: UiUpdateListenerBuilder.() -> Unit) {
            val m3u8Url = m3u8Info.url
            val m3u8File = m3u8Info.file
            if (Pattern.matches(urlRegex, m3u8Url)) {
                val webUrl = m3u8Url.substringBeforeLast("/")
                m3u8Info.webUrl = webUrl
            } else {
                println("参数不是一个url网址")
            }

            val uiUpdateListener = UiUpdateListenerBuilder().also(uiUpdateListenerBuilder)
            KxDownloader.downloadFile(m3u8Url, m3u8File)

            KxDownloader.downloadFile(m3u8Url, m3u8File, m3u8Info.httpParam) {
                onFinish {
                    //解析文件信息
                    getM3u8Info(m3u8Info)
                    uiUpdateListener.onFinishAction?.invoke()
                }
                onProgress {
                    uiUpdateListener.onProgressAction?.invoke(it)
                }
                onError { downloadMessage, e ->
                    println(e.message)
                }
            }
        }


        /**
         * 处理本地已下载好的方法
         *
         * @param m3u8Info
         */
        fun parseInfo(m3u8Info: M3u8Info) {

        }

        /**
         * 从m3u8文件中获取ts文件的地址、key的信息以及IV
         */
        private fun getM3u8Info(m3u8Info: M3u8Info) {
            val m3u8File = m3u8Info.file
            //读取m3u8（注意是utf-8格式）
            val readLines = m3u8File.readLines(charset("utf-8"))

            //固定获得声明是否有key加密的那行的下标
            val indexList = arrayListOf<Int>()
            readLines.forEachIndexed { index, s ->
                if (s.contains("#EXT-X-KEY")) {
                    indexList.add(index)
                }
            }
            //最末尾的行数下标
            indexList.add(readLines.size)

            //将整个m3u8文件分割成几份,获得对应的key和ts的url地址
            for (i in 0 until indexList.size - 1) {
                val startIndex = indexList[i]
                val endIndex = indexList[i + 1]

                val subList = readLines.subList(startIndex, endIndex)

                //获取key的信息
                val keyLine = subList.find { it.contains("AES-128") }
                if (keyLine != null) {
                    //获得key的url
                    val start = keyLine.indexOf("\"")
                    val last = keyLine.lastIndexOf("\"")
                    val keyUrl = keyLine.substring(start + 1, last)

                    //keyUrl可能是网址
                    val keyBytes = if (Pattern.matches(urlRegex, keyUrl)) {
                        val keyFile = KxDownloader.downloadFile(keyUrl, File(m3u8File.parentFile, "key${m3u8Info.tsInfoList.size}.key"))
                        keyFile.readBytes()
                    } else {
                        //todo 考虑到本地的情况,该怎么判断
                        //不是网址，则进行拼接
                        // 拼接key文件的url文件，并下载在本地，获得key文件的字节数组
                        val keyFile = KxDownloader.downloadFile("${m3u8Info.webUrl}/$keyUrl", File(m3u8File.parentFile, "key${m3u8Info.tsInfoList.size}.key"))
                        keyFile.readBytes()
                    }
                    //获得偏移量IV字符串
                    val ivString = if (keyLine.contains("IV=0x")) keyLine.substringAfter("IV=0x") else ""
                    //m3u8未定义IV则使用默认的字节数组（0）
                    val ivBytes = if (ivString.isBlank()) ByteArray(16) else decodeHex(ivString)

                    m3u8Info.tsInfoList.add(TsInfo(keyBytes, ivBytes))
                } else {
                    m3u8Info.tsInfoList.add(TsInfo(ByteArray(0), ByteArray(0)))
                }

                subList.forEach { line ->
                    //ts的url地址获取
                    if (!line.startsWith("#")) {
                        val tsInfo = m3u8Info.tsInfoList.last()
                        val tsInfoIndex = m3u8Info.tsInfoList.lastIndex
                        val tsFiles = tsInfo.tsFiles
                        val tsUrlList = tsInfo.tsUrlList
                        val tsIndex = tsFiles.size
                        val tsName = "$tsInfoIndex-$tsIndex.ts"
                        val dirFile = m3u8File.parentFile
                        tsFiles.add(File(dirFile, tsName))
                        if (Pattern.matches(urlRegex, line)) {
                            //地址和对应的文件名都存放起来
                            tsUrlList.add(line)
                        } else {
                            tsUrlList.add("${m3u8Info.webUrl}/$line")
                        }
                    }
                }
            }

        }

        /**
         * 解密
         *
         * @param m3u8Info
         */
        fun decrypt(m3u8Info: M3u8Info) {
            m3u8Info.tsInfoList.forEach {
                if (it.keyByteArray.size > 0) {
                    //解密工具初始化
                    val skey = SecretKeySpec(it.keyByteArray, algorithm)
                    val iv = IvParameterSpec(it.ivByteArray)
                    cipher.init(Cipher.DECRYPT_MODE, skey, iv)
                    it.tsFiles.forEach { tsFile ->
                        //解密,输出文件,保存在tempList中,后面合并需要
                        if (tsFile.exists()) {
                            val readBytes = tsFile.readBytes()
                            if (readBytes.size % 16 == 0) {
                                val result = cipher.doFinal(readBytes)
                                val tempFile = File(tsFile.parent, "${tsFile.name}-temp")
                                tempFile.writeBytes(result)
                                it.tempTsFiles.add(tempFile)
                            }
                        }
                    }
                }
            }
        }

        /**
         * 合并输出文件
         *
         * @param m3u8Info
         */
        fun merge(m3u8Info: M3u8Info) {
            val outputFile = m3u8Info.outputFile
            println("合并中")
            m3u8Info.tsInfoList.forEach {
                it.tempTsFiles.forEach { file ->
                    if (file.exists()) {
                        outputFile.appendBytes(file.readBytes())
                        file.delete()
                    }
                }
            }
            //todo key文件和m3u8文件删除
            m3u8Info.tsInfoList.forEach {
                for (tsFile in it.tsFiles) {
                    tsFile.delete()
                }
            }

            println("合并完成")

        }

        /**
         * 将字符串转为16进制并返回字节数组
         */
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
    }

}

/**
 * M3u8info 通过解析m3u8文件获得
 *
 * @property url m3u8文件的在线地址
 * @property url m3u8文件域名前缀
 * @property file m3u8文件
 * @property httpParam 请求头列表
 * @property outputFileName 输出文件
 * @property keyList key的List(之前遇到会有多个key的情况)
 * @property ivList iv的List(与上述对应)
 * @property tsUrlList ts文件的地址
 * @property tsFiles ts文件的List
 * @property tempTsFiles 已解密的ts文件列表(如果m3u8文件没有加密,此列表为空)
 */
data class M3u8Info(
        var url: String = "",
        var webUrl: String = "",
        var file: File,
        val httpParam: HttpParam = HttpParam(),
        var outputFile: File,
        val tsInfoList: ArrayList<TsInfo> = arrayListOf()
)


data class TsInfo(var keyByteArray: ByteArray, var ivByteArray: ByteArray, val tsUrlList: ArrayList<String> = arrayListOf(), val tsFiles: ArrayList<File> = arrayListOf(), val tempTsFiles: ArrayList<File> = arrayListOf())

class UiUpdateListenerBuilder {

    var onProgressAction: ((downloadMessage: DownloadMessage) -> Unit)? = null

    //下载完成
    var onFinishAction: (() -> Unit)? = null


    fun onProgress(action: (progress: DownloadMessage) -> Unit) {
        onProgressAction = action
    }

    fun onFinish(action: () -> Unit) {
        onFinishAction = action
    }
}