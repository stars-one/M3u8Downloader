package com.wan.view

import kfoenix.jfxtabpane
import tornadofx.*

class MainView : View("m3u8视频下载合并器1.1 by stars-one") {

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

        jfxtabpane {
            tab("在线m3u8下载及解密合并") {
                this += OnlineView()
            }
            tab("本地m3u8文件解密合并") {
                this += LocalView()
            }
        }


    }


}