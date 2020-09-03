package com.wan.view

import com.wan.model.Item
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import kfoenix.jfxbutton
import tornadofx.*

class MainView : View("m3u8视频下载合并器1.1 by stars-one") {
    private var m3u8UrlInput by singleAssign<TextField>()//m3u8文件地址输入框
    private var dirInput by singleAssign<TextField>()//下载目录输入框
    private var threadCountInput by singleAssign<TextField>()//多线程数目输入框
    private var outputFileInput by singleAssign<TextField>()//输出文件输入框
    private var contentVbox by singleAssign<VBox>() //任务列表
    override val root = vbox {
        prefWidth = 800.0
        prefHeight = 500.0
        menubar {

            menu("帮助") {
                item("关于") {
                    action {
                        find(AboutView::class).openModal()
                    }
                }
            }
        }
        form {
            fieldset {
                field {
                    text("m3u8文件地址：")
                    m3u8UrlInput = textfield {
                        promptText = "输入m3u8文件的在线地址"
                    }
                }
                field {
                    text("下载目录：")
                    dirInput = textfield {
                        promptText = "输入下载的目录"
                    }
                    jfxbutton("选择文件夹") {
                        action {
                            val dirFile = chooseDirectory("选择下载目录")
                            if (dirFile != null) {
                                dirInput.text = dirFile.path
                            }
                        }
                    }
                }
                field {
                    text("下载线程数：")
                    threadCountInput = textfield("5") {
                        tooltip = tooltip("线程选择范围为5-128，不要太大")
                    }
                }
                field {
                    text("合并输出文件名：")
                    outputFileInput = textfield {
                        promptText = "不需要扩展名，不填则默认为out.mp4"
                    }
                }
                field {
                    jfxbutton("开始下载") {
                        style {
                            //设置背景颜色
                            backgroundColor += c("#66bef1")
                            //设置按钮文字颜色
                            textFill = c("white")
                        }
                        action {
                            addNewTask()
                        }
                    }
                }
            }
        }
        scrollpane {
            prefWidth = 800.0
            prefHeight = 200.0
            contentVbox = vbox()
        }
    }

    private fun addNewTask() {
        val m3u8Url = m3u8UrlInput.text
        val threadCount = threadCountInput.text.toInt()
        val dirPath = dirInput.text
        var outputFileName = outputFileInput.text

        if (outputFileName.isBlank()) {
            outputFileName = "out.mp4"
        } else {
            //不是mp4结尾的，自动添加
            if (!outputFileName.endsWith(".mp4")) {
                outputFileName += ".mp4"
            }
        }
        val item = Item(m3u8Url, outputFileName, threadCount, dirPath)

        val itemView = ItemView(item)
        contentVbox.add(itemView)
        itemView.startDownload()
    }
}