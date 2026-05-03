package space.xiaoxiao.databasemanager.features

import androidx.compose.runtime.*
import space.xiaoxiao.databasemanager.core.*

/**
 * Handles all DDL ("Data Definition Language") operations:
 * creating, dropping, renaming, and truncating tables;
 * adding, modifying, and dropping columns;
 * creating and dropping indexes;
 * creating and dropping databases.
 *
 * Receives references to [ConnectionViewModel] (for the client) and
 * [BrowserViewModel] (to refresh metadata after structural changes).
 */
class SchemaEditorViewModel(
    private val conn: ConnectionViewModel,
    private val browser: BrowserViewModel
) {

    var isManagingTable by mutableStateOf(false)
        internal set

    // ── Table management ──

    suspend fun createTable(definition: TableDefinition): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持表管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.createTable(definition)
            if (result.isSuccess) {
                browser.loadTables()
            } else {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun dropTable(tableName: String): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持表管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.dropTable(tableName)
            if (result.isSuccess) {
                browser.loadTables()
                if (browser.tableSchema?.tableName == tableName) {
                    // force-clear the stale schema reference
                    browser.clearState()
                    browser.loadTables()
                }
            } else {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun renameTable(oldName: String, newName: String): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持表管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.renameTable(oldName, newName)
            if (result.isSuccess) {
                browser.loadTables()
            } else {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun truncateTable(tableName: String): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持表管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.truncateTable(tableName)
            if (!result.isSuccess) {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    // ── Column management ──

    suspend fun addColumn(tableName: String, column: ColumnDefinition): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持字段管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.addColumn(tableName, column)
            if (result.isSuccess) {
                browser.loadTableSchema(tableName)
            } else {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun modifyColumn(tableName: String, modification: ColumnModification): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持字段管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.modifyColumn(tableName, modification)
            if (result.isSuccess) {
                browser.loadTableSchema(tableName)
            } else {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun dropColumn(tableName: String, columnName: String): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持字段管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.dropColumn(tableName, columnName)
            if (result.isSuccess) {
                browser.loadTableSchema(tableName)
            } else {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    // ── Index management ──

    suspend fun createIndex(
        tableName: String,
        indexName: String,
        columns: List<String>,
        isUnique: Boolean
    ): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持索引管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.createIndex(tableName, indexName, columns, isUnique)
            if (result.isSuccess) {
                browser.loadIndexes(tableName)
            } else {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun dropIndex(tableName: String, indexName: String): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持索引管理"
            return false
        }
        isManagingTable = true
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.dropIndex(tableName, indexName)
            if (result.isSuccess) {
                browser.loadIndexes(tableName)
            } else {
                conn.lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    // ── Database management ──

    suspend fun createDatabase(name: String, charset: String? = null): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持创建数据库"
            return false
        }
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.createDatabase(name, charset)
            result.onSuccess {
                browser.loadServerDatabases()
            }.onFailure {
                conn.lastErrorMessage = it.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        }
    }

    suspend fun dropDatabase(name: String): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持删除数据库"
            return false
        }
        conn.lastErrorMessage = null
        return try {
            val result = relational.metadata.dropDatabase(name)
            result.onSuccess {
                browser.loadServerDatabases()
                if (browser.currentDatabaseName == name) {
                    // current-database name gets cleared by loadServerDatabases refresh
                }
            }.onFailure {
                conn.lastErrorMessage = it.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        }
    }

    // ── Internal ──

    /** Resets the managing flag (called on disconnect). */
    internal fun reset() {
        isManagingTable = false
    }
}
