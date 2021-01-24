package com.wan.view

import com.wan.util.VideoUtils
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.FileChooser
import kfoenix.jfxbutton
import kfoenix.jfxcheckbox
import tornadofx.*

/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2020/12/31 16:03
 *
 */
class LocalView : View("My View") {
    val model = LocalViewModel()

    override val root = vbox {

        form {
            fieldset {
                field("m3u8文件路径") {
                    textfield(model.m3u8FilePath) { }
                    jfxbutton("选择m3u8文件") {
                        action {
                            val files = chooseFile("选择m3u8文件", arrayOf(FileChooser.ExtensionFilter("*.m3u8")))
                            if (files.isNotEmpty()) {
                                model.m3u8FilePath.value = files[0].path
                            }
                        }
                    }
                }
                field("key文件") {
                    textfield(model.keyFilePath) { }
                    jfxbutton("选择文件夹") {
                        action {
                            val dirFile = chooseDirectory("选择下载目录")
                            if (dirFile != null) {
                                model.keyFilePath.value = dirFile.path
                            }
                        }
                    }
                }
                field("输出文件名") {
                    textfield(model.keyFilePath) { }
                    jfxcheckbox(model.isDeleteAfter, "合并后是否删除所有ts文件?") { }
                }
            }

        }
        jfxbutton("开始解密并合并") {
            action{
                val videoUtils = VideoUtils(model.m3u8FilePath.value)

            }
        }
    }
}

class LocalViewModel : ViewModel() {
    //key文件
    val keyFilePath = SimpleStringProperty("")

    //m3u8文件
    val m3u8FilePath = SimpleStringProperty("")

    //合并完成是否删除ts文件
    val isDeleteAfter = SimpleBooleanProperty(false)
    val outputFilePath = SimpleStringProperty("")
}
