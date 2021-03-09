package com.wan.view

import ItemViewBase
import com.jfoenix.controls.JFXProgressBar
import com.wan.model.Item
import com.wan.util.VideoUtil
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
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
        val videoUtil = VideoUtil(m3u8Url, dirPath)


        //测试用的下载地址 https://video.buycar5.cn/20200813/uNqvsBhl/2000kb/hls/index.m3u8
        KxDownloader.downloadFileListByMultiThread(videoUtil.tsUrls, videoUtil.tsFiles) {
            onBefore {
                runLater {
                    itemViewModel.flagText.value = "下载m3u8文件及初始化"
                }
                videoUtil.parseM3u8File()

                runLater {
                    videoName.text = item.fileName
                    videoName.tooltip = Tooltip(item.fileName)
                    itemViewModel.flagText.value = "下载中"
                }

            }

            onProgress {
                runLater {
                    itemViewModel.progress.value = it.progress
                }
                println(it.progress)
            }

            onItemError { downloadMessage, e ->
                println("${downloadMessage.file.name}:${e.message}")
            }

            onFinish {

                println("已下载完毕")
                runLater {
                    itemViewModel.flagText.value = "解密中"
                }
                videoUtil.decryptTs()
                runLater {
                    itemViewModel.flagText.value = "合并中"
                    itemViewModel.progress.value = -1.0
                }
                //合并输出mp4文件
                if (outputFileName.isBlank()) {
                    videoUtil.mergeTsFile()
                } else {
                    videoUtil.mergeTsFile(outputFileName)
                }
                runLater {
                    itemViewModel.flagText.value = "已完成"
                    itemViewModel.progress.value = 1.0
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
