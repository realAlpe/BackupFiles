import java.io.File
import java.io.IOException
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import kotlin.system.exitProcess

class BackupManager(
    private val sourceDirectory: String,
    private val destinationDirectory: String
) {
    private var numberOfCopies = 0
    private val logger = initializeLogger()

    /**
     * Copy the file from [sourceFile] to [destinationFile] if it exists.
     * In the case of a successful copy or an exception, a log is performed.
     */
    private fun copyFile(sourceFile: File, destinationFile: File): Boolean {
        return try {
            sourceFile.copyTo(destinationFile)
            logger.info("""Successfully copied "$sourceFile" to "$destinationFile".""")
            true
        } catch (e: NoSuchFileException) {
            logger.severe("""The source file "$sourceFile" does not exist. $e.""")
            false
        } catch (e: FileAlreadyExistsException) {
            logger.severe("""The destination file "$destinationFile" already exists. $e.""")
            false
        } catch (e: IOException) {
            logger.severe("""Something went wrong during copying. Exiting program.""")
            throw e
        }
    }

    /**
     * Backup the files from [sourceDirectory] to [destinationDirectory].
     */
    private fun backup() {
        val source = File(sourceDirectory)
        val destination = File(destinationDirectory)

        val sourceFiles = source
            .walkBottomUp()
            .filter { it.isFile }
        val destinationFiles = destination
            .walkBottomUp()
            .filter { it.isFile }

        for (file in sourceFiles) {
            if (!destinationFiles.any { it.name == file.name }) {
                val sourceFile = file.absoluteFile
                val destinationFile = File(file.absolutePath.replace(sourceDirectory, destinationDirectory))
                val success = copyFile(sourceFile, destinationFile)
                if (success) numberOfCopies++
            }
        }
    }

    /**
     * Initialize the logger for this class with the name [loggerName] and a logger file at [logFileDirectory].
     */
    private fun initializeLogger(
        logFileDirectory: String = """D:\Projects\Data""",
        loggerName: String = "BackupFilesLogger"
    ): Logger {
        val logger = Logger.getLogger(loggerName)
        val fileHandler: FileHandler

        try {
            fileHandler = FileHandler("""$logFileDirectory\$loggerName.log""", true)
            logger.addHandler(fileHandler)
            fileHandler.formatter = SimpleFormatter()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return logger
    }

    /**
     * Perform the backup from [sourceDirectory] to [destinationDirectory].
     */
    fun performBackup() {
        logger.info("""Perform a backup from "$sourceDirectory" to "$destinationDirectory".""")
        backup()
        logger.info("""Successfully performed a backup from "$sourceDirectory" to "$destinationDirectory" 
            |with $numberOfCopies copies.""".trimMargin())
    }
}

/**
 * Return true iff [path] directory exists, is an existing directory and a canonical path.
 */
fun isValidDirectoryPath(path: String): Boolean {
    return try {
        val f = File(path)
        File(path).canonicalPath
        f.isDirectory && f.exists()
    } catch (e: IOException) {
        false
    }
}

fun main() {
    var sourceDirectory: String
    var destinationDirectory: String

    println("Enter a valid and existing source directory for the backup without quotation marks.")
    while (true) {
        sourceDirectory = readln()
        if (isValidDirectoryPath(sourceDirectory)) break
        println("The given source directory is not a valid path. Enter again.")
    }

    println("Enter a valid and existing destination directory for the backup without quotation marks.")
    while (true) {
        destinationDirectory = readln()
        if (isValidDirectoryPath(destinationDirectory)) break
        println("The given destination directory is not a valid path. Enter again.")
    }

    println("""Are you absolutely sure, you want to backup your files from 
        |"$sourceDirectory" to "$destinationDirectory"? This execution will be irreversible.
        |If you still want to continue, enter "CONTINUE" without the quotation marks.
    """.trimMargin())
    val input = readln()
    if (input != "CONTINUE") {
        println("Exiting program without performing a backup.")
        exitProcess(0)
    }

    BackupManager(sourceDirectory, destinationDirectory).performBackup()
}