package com.wan.app

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile
import java.net.URL
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlin.coroutines.coroutineContext

/**
 *
 * @author StarsOne
 * @date Create in  2020/8/5 0005 15:13
 * @description
 *
 */
fun main() {

    val list = mutableListOf("")
    val one=download(list,"https://img2020.cnblogs.com/blog/1210268/202004/1210268-20200413161422035-1188549898.gif","test.gif")
    val two=download(list,"https://img2020.cnblogs.com/blog/1210268/202004/1210268-20200413161422035-1188549898.gif","test2.gif")
    runBlocking {
        one.await()
        two.await()
        println("zuiho:"+list.size)
    }

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

fun download(list: MutableList<String>,url: String,file:String) = GlobalScope.async {
    val bytes = URL(url).openConnection().getInputStream().readBytes()
    File("Q:\\服务器连接\\$file").writeBytes(bytes)
    list.add("")
    println(list.size)
}

