package com.feature.dayjson

import com.feature.dayjson.model.DayNewModel
import com.feature.dayjson.model.HttpResult
import com.feature.dayjson.network.DayRepository
import com.feature.dayjson.utils.JsonFileIOUtils
import com.google.gson.Gson
import java.io.File

object DayJsonMain {

    private const val USER_DIR = "user.dir"
    private const val JSON = "json"
    private const val API = "api"
    private const val DAY = "day"
    private const val FILE_TYPE = ".json"
    private const val BACKUP_FILE_NAME = "1.json"
    private const val CODE_SUCCESS = 200

    private val mainGSon = Gson()
    private var dayListFile = File("")

    private val backupList = ArrayList<DayNewModel>()

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== main init ===")
        initCreateJsonFile()
        handleBackupDayNewList()
        handleDayNewList()
        println("=== main end ===")
    }

    private fun handleAllDayNewList(list: List<DayNewModel>): ArrayList<DayNewModel> {
        val newList = ArrayList<DayNewModel>(list)
        for (oldItem in backupList) {
            var isExist = false
            for (item in list) {
                if (oldItem.guid == item.guid) {
                    isExist = true
                    break
                }
            }
            if (!isExist) {
                newList.add(oldItem)
            }
        }
        return newList
    }

    private fun handleBackupDayNewList() {
        println("=== handleBackupDayNewList call... ===")
        val disposable = DayRepository.get()
                .getBackupDayNewList(BACKUP_FILE_NAME)
                .map { result -> result.data }
                .doOnError { e ->
                    println("=== Backup doOnError called... ===$e")
                }
                .doOnSuccess { list ->
                    println("=== Backup doOnSuccess called... ===" + "list:" + list.size)
                    backupList.clear()
                    backupList.addAll(list)
                }
                .subscribe({ }, { })
    }

    private fun handleDayNewList() {
        println("=== handleDayNewList call... ===")
        val disposable = DayRepository.get()
                .getDayNewList(1, "android", "36")
                .doOnError { e ->
                    println("=== doOnError called... ===$e")
                }
                .doOnSuccess { list ->
                    println("=== doOnSuccess called... ===" + "list:" + list.size)
                    val newList = handleAllDayNewList(list)
                    dayNewModelListJsonFile(newList, dayListFile.absolutePath, mainGSon)
                }
                .subscribe({ }, { })
    }

    private fun initCreateJsonFile() {
        println("initCreateJsonFile call...")
        val dirFile = File(System.getProperty(USER_DIR))
        val userDirFile = if (dirFile.absolutePath.contains("dayjson"))
            dirFile.parentFile else dirFile
        println("userDirFile:${userDirFile.absolutePath}")

        val createJsonFile = File(File(userDirFile, JSON), API)
        println("createJsonFile:${createJsonFile.absolutePath}")

        dayListFile = File(createJsonFile, DAY)
        println("movieListFile:${dayListFile.absolutePath}")
        println("initCreateJsonFile end ... ")
    }

    private fun dayNewModelListJsonFile(list: List<DayNewModel>,
                                    rootPath: String, mainGSon: Gson) {
        val dayNewModelList = handleNewModelList(list)
        val filePath = File(File(rootPath),
                "1".plus(FILE_TYPE)).absolutePath
        val httpResult = HttpResult(CODE_SUCCESS,
                "", dayNewModelList)
        val fileJson = mainGSon.toJson(httpResult)
        val isKeyPageSuccess = JsonFileIOUtils.writeFileFromString(filePath, fileJson)
        println("isKeyPageSuccess:$isKeyPageSuccess, filePath:$filePath")
        println("filePath: " + File(filePath).exists())
        println("=== new list size... ===" + "list:" + list.size)
    }

    private fun handleNewModelList(list: List<DayNewModel>): ArrayList<DayNewModel> {
        val newList = ArrayList<DayNewModel>()
        for (item in list) {
            if (!newList.contains(item)) {
                newList.add(item)
            }
        }
        return newList
    }
}