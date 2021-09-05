package com.wan.view

import RvAdapter
import RvDataObservableList
import XRecyclerView
import com.wan.model.Item
import javafx.scene.control.TextField
import kfoenix.jfxbutton
import tornadofx.*

/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2020/12/31 16:00
 *
 */
class OnlineView : View("My View") {
    private var m3u8UrlInput by singleAssign<TextField>()//m3u8文件地址输入框
    private var dirInput by singleAssign<TextField>()//下载目录输入框
    private var threadCountInput by singleAssign<TextField>()//多线程数目输入框
    private var outputFileInput by singleAssign<TextField>()//输出文件输入框

    private val dataList = RvDataObservableList<Item,ItemView>()
    override val root = vbox {
        prefWidth = 800.0
        prefHeight = 500.0

        form {
            fieldset {
                field {
                    text("m3u8文件地址：")
                    m3u8UrlInput = textfield("") {
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

        val adapter = object :RvAdapter<Item,ItemView>(dataList){
            override fun onBindData(itemView: ItemView, bean: Item, position: Int) {
                itemView.bindData(dataList,position)
            }

            override fun onClick(itemView: ItemView, position: Int) {

            }

            override fun onCreateView(): ItemView {
                return ItemView()
            }

            override fun onRightClick(itemView: ItemView, position: Int) {

            }

        }

        val rv = XRecyclerView<Item,ItemView>().setRvAdapter(adapter)
        rv.setWidth(800.0)
        rv.setHeight(200.0)
        this+=rv
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

        dataList.add(item)

    }
}
