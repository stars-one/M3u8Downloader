package com.wan.app

import com.wan.util.VideoUtil
import java.io.File
import java.io.RandomAccessFile
import java.net.URL
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import javax.xml.bind.JAXBElement
import kotlin.coroutines.coroutineContext

/**
 *
 * @author StarsOne
 * @date Create in  2020/8/5 0005 15:13
 * @description
 *
 */
fun main() {

    val v= VideoUtil("https://omts.tc.qq.com/AIZuzwuJY-L02ju7BaD_UPmUJqT5xepaO8PFKvSe5Unc/uwMROfz2r5xgoaQXGdGnC2df64gVTKzl5C_X6A3JOVT0QIb-/9yoPA9sjj9wjORQvJTbdMsuizV8U_Oe6XhNLjfTOnrnDp2rF_PgVkXNjj3-lKry9Ai_2qFaWfMjDgJQcPwhCuYvegRsG4RI2J2OTSsDMVBf75o9EPYojNOpUWrIjbR8ff5YzrXHQrZvvD1qoFZKrOrF8aSRqeYz_jm1w9oOlIuY/t0034y8h4s1.321004.ts.m3u8?ver=4","Q:\\下载\\新建文件夹 (2)")
    v.downloadAndMerageTsList()
    /*val list = mutableListOf("")
    val one=download(list,"https://img2020.cnblogs.com/blog/1210268/202004/1210268-20200413161422035-1188549898.gif","test.gif")
    val two=download(list,"https://img2020.cnblogs.com/blog/1210268/202004/1210268-20200413161422035-1188549898.gif","test2.gif")
    runBlocking {
        one.await()
        two.await()
        println("zuiho:"+list.size)
    }*/

    /* val contentLength = URL("https://img2020.cnblogs.com/blog/1210268/202004/1210268-20200413161422035-1188549898.gif").openConnection().contentLength
     val conn = URL("https://img2020.cnblogs.com/blog/1210268/202004/1210268-20200413161422035-1188549898.gif").openConnection()
     val file = File("Q:\\服务器连接\\text.gif")
     val start = if (file.exists()) {
         file.readBytes().size
     } else {
         0
     }
     conn.setRequestProperty("RANGE", "bytes=$start-$contentLength");
     val inputStream = conn.inputStream
     val bufferArray = ByteArray(10 * 1024)
     val oSavedFile = RandomAccessFile(file, "rw");
     //val fos = file.outputStream()
     var length = 0
     val readBytes = inputStream.readBytes()
     oSavedFile.seek(start.toLong())
     oSavedFile.write(readBytes)
     *//*while (true) {
        length = inputStream.read(bufferArray)
        if (length == -1) {
            break
        }
        oSavedFile.write(bufferArray, 0, length)
        break
    }*//*

    oSavedFile.close()*/

}


fun download(list: MutableList<String>,url: String,file:String)  {
    val bytes = URL(url).openConnection().getInputStream().readBytes()
    File("Q:\\服务器连接\\$file").writeBytes(bytes)
    list.add("")
    println(list.size)
}

