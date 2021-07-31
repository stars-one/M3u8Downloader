package com.wan.view

import ItemViewBase
import M3u8Info
import M3u8Util
import com.jfoenix.controls.JFXProgressBar
import com.wan.model.Item
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import kfoenix.jfxbutton
import kfoenix.jfxprogressbar
import site.starsone.download.KxDownloader
import tornadofx.*
import java.awt.Desktop
import java.io.File
import kotlin.concurrent.thread

/**
 *
 * @author StarsOne
 * @date Create in  2020/1/14 0014 22:07
 * @description
 *
 */
class ItemView : ItemViewBase<Item, ItemView>(null, null) {
    private var videoName by singleAssign<Label>()
    private var flagText by singleAssign<Text>()
    private var progressbar by singleAssign<JFXProgressBar>()

    var itemViewModel = ItemViewModel()

    private lateinit var item: Item

    override val root = hbox {
        alignment = Pos.CENTER_LEFT
        spacing = 20.0
        text("视频名：") {
            style {
                //粗体
                fontWeight = FontWeight.BOLD
                //字体大小，第二个参数是单位，一个枚举类型
                fontSize = Dimension(18.0, Dimension.LinearUnits.px)
            }
        }
        videoName = label("") {
            prefWidth = 200.0
            style {
                //粗体
                fontWeight = FontWeight.BOLD
                //字体大小，第二个参数是单位，一个枚举类型
                fontSize = Dimension(18.0, Dimension.LinearUnits.px)
            }
        }
        flagText = text(itemViewModel.flagText) {
            style {
                fill = c("green")
            }
        }
        progressbar = jfxprogressbar {
            itemViewModel.progress = progressProperty()
        }

        jfxbutton("打开文件夹") {
            action {
                Desktop.getDesktop().open(File(item.dirPath))
            }
        }
        jfxbutton("取消下载") { }

        setDataChange()
    }

    fun startDownload() {
        val (m3u8Url, outputFileName, threadCount, dirPath) = item

        val m3u8File = File(dirPath, "index.m3u8")
        val outputFile = File(dirPath, outputFileName)
        val m3u8Info = M3u8Info(file = m3u8File, outputFile = outputFile)
        m3u8Info.url = m3u8Url

        runLater {
            itemViewModel.flagText.value = "下载m3u8文件及初始化"
        }

        M3u8Util.parseInfo(m3u8Info) {
            onProgress {
                println(it.percent)
            }

            onFinish {
                runLater {
                    itemViewModel.flagText.value = "m3u8文件解析中"
                }

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

                runLater {
                    itemViewModel.flagText.value = "下载视频文件中"
                }

                KxDownloader.downloadFileListByMultiThread(urlList, fileList, m3u8Info.httpParam, null) {
                    onItemFinish {
                        println(it.file.name + "已下载完成")
                    }
                    onItemError { downloadMessage, e ->
                        println(downloadMessage.file.name + "失败,原因" + e.message)
                    }
                    onProgress {
                        runLater {
                            itemViewModel.progress.value = it.progress
                        }
                    }
                    onFinish {
                        runLater {
                            itemViewModel.flagText.value = "解密中"
                        }
                        println("全部下载完毕")
                        M3u8Util.decrypt(m3u8Info)
                        runLater {
                            itemViewModel.flagText.value = "合并视频中"
                        }
                        println("解密完成")
                        M3u8Util.merge(m3u8Info)
                        runLater {
                            itemViewModel.flagText.value = "已完成"
                            itemViewModel.progress.value = 1.0
                        }
                    }

                }
            }
        }
    }

    override fun bindData(beanT: Item) {
        item = beanT
        videoName.text = item.fileName

        //开启线程执行任务,避免UI线程堵塞
        thread {
            startDownload()
        }
    }

    override fun inputChange() {

    }
}

class ItemViewModel : ViewModel() {
    lateinit var progress: DoubleProperty
    val flagText = SimpleStringProperty("")
}
