package com.wan.view

import com.jfoenix.controls.JFXProgressBar
import com.wan.model.Item
import com.wan.util.VideoUtil
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import kfoenix.jfxbutton
import kfoenix.jfxprogressbar
import tornadofx.*
import java.awt.Desktop
import java.io.File

/**
 *
 * @author StarsOne
 * @date Create in  2020/1/14 0014 22:07
 * @description
 *
 */
class ItemView : Fragment {
    private var videoName by singleAssign<Label>()
    private var flagText by singleAssign<Text>()
    private var progressbar by singleAssign<JFXProgressBar>()

    private lateinit var item: Item

    constructor(item: Item){
        this.item = item
    }

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
        flagText = text("下载中") {
            style {
                fill = c("green")
            }
        }
        progressbar = jfxprogressbar { }
        jfxbutton("打开文件夹") {
            action {
                Desktop.getDesktop().open(File(item.dirPath))
            }
        }
        jfxbutton("取消下载") { }
    }

    fun startDownload() {
        runAsync {
            val (m3u8Url, outputFileName, threadCount, dirPath) = item
            val videoUtil = VideoUtil(m3u8Url, dirPath)
            runLater {
                videoName.text = item.fileName
                videoName.tooltip = Tooltip(item.fileName)
            }
            videoUtil.downloadTsFile(threadCount)//下载所有ts文件
            while (videoUtil.progess!=videoUtil.tsUrls.size) {
                println(videoUtil.progess)
                Thread.sleep(100)
                //更新进度条
                runLater {
                    progressbar.progress = videoUtil.progess / videoUtil.tsUrls.size.toDouble()
                }
            }
            runLater {
                flagText.text = "解密中"
                progressbar.progress = -1.0
            }
            videoUtil.decryptTs()//解密ts文件
            runLater {
                flagText.text = "合并中"
            }
            //合并输出mp4文件
            if (outputFileName.isBlank()) {
                videoUtil.mergeTsFile()
            } else {
                videoUtil.mergeTsFile(outputFileName)
            }
            runLater {
                flagText.text = "已完成"
                progressbar.progress = 1.0
            }
        }
    }
}
