package com.wan.view

import M3u8Info
import M3u8Util
import com.starsone.controls.common.TornadoFxUtil
import com.starsone.controls.common.showDialog
import com.starsone.controls.common.showLoadingDialog
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.FileChooser
import kfoenix.jfxbutton
import kfoenix.jfxcheckbox
import tornadofx.*
import java.io.File

/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2020/12/31 16:03
 *
 */
class LocalView : View("My View") {
    val localViewModel by inject<LocalViewModel>()

    override val root = vbox {

        form {
            fieldset {
                field("m3u8文件路径") {
                    textfield(localViewModel.m3u8FilePath) { }
                    jfxbutton("选择m3u8文件") {
                        action {
                            val files = chooseFile("选择m3u8文件", arrayOf(FileChooser.ExtensionFilter("m3u8文件","*.m3u8")))
                            if (files.isNotEmpty()) {
                                localViewModel.m3u8FilePath.value = files[0].path
                            }
                        }
                    }
                }
                field("输出文件名") {
                    textfield(localViewModel.outputFilePath) {
                        promptText="输出名默认为output.mp4"
                    }
                    jfxcheckbox(localViewModel.isDeleteAfter, "合并后是否删除所有ts文件?") { }
                }
            }

        }
        jfxbutton("开始解密并合并") {
            style {
                //设置背景颜色
                backgroundColor += c("#66bef1")
                //设置按钮文字颜色
                textFill = c("white")
            }
            action {
                val filePath = localViewModel.m3u8FilePath.value
                var outputFileName = localViewModel.outputFilePath.value
                var isDeleteAfter = localViewModel.isDeleteAfter.value

                if (filePath.isBlank()) {
                    showDialog(currentStage,"提示","请先输入m3u8文件的路径")
                    return@action
                }
                if (!filePath.toUpperCase().endsWith(".M3U8")) {
                    showDialog(currentStage,"提示","m3u8文件路径不合法,请重新输入")
                    return@action
                }

                //默认输出文件名为output.mp4
                if (outputFileName.isBlank()) {
                    outputFileName = "output.mp4"
                } else if (outputFileName.toLowerCase().endsWith(".mp4")){
                    outputFileName = "${outputFileName}.mp4"
                }

                val m3u8File = File(filePath)
                val outputFile = File(m3u8File.parent, outputFileName)

                val m3u8Info = M3u8Info(file = m3u8File, outputFile = outputFile)

                showLoadingDialog(currentStage, "提示", "解密并合并中", "", negativeBtnOnclickListener = null) { alert ->
                    runAsync {
                        M3u8Util.parseInfoForLocal(m3u8Info)
                        M3u8Util.decrypt(m3u8Info)
                        M3u8Util.merge(m3u8Info,isDeleteAfter)
                    }ui {
                        alert.hideWithAnimation()
                        showDialog(currentStage,"提示","合并成功,文件路径为",outputFile.path,false)
                    }
                }
            }
        }

        hyperlink(("本地合并功能使用请查看文档说明,点击可跳转")) {
            action {
                TornadoFxUtil.openUrl("https://github.com/Stars-One/M3u8Downloader")
            }
        }
    }
}

class LocalViewModel : ViewModel() {

    //m3u8文件
    val m3u8FilePath = SimpleStringProperty("")

    //合并完成是否删除ts文件
    val isDeleteAfter = SimpleBooleanProperty(true)
    val outputFilePath = SimpleStringProperty("")
}
