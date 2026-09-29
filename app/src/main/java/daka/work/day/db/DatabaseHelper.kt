package daka.work.day.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import daka.work.day.model.WorkRecord

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "work_time.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "work_records"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_CLOCK_IN = "clock_in_time"
        private const val COLUMN_CLOCK_OUT = "clock_out_time"
        private const val COLUMN_NOTE = "note"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_CLOCK_IN INTEGER,
                $COLUMN_CLOCK_OUT INTEGER,
                $COLUMN_NOTE TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun clockIn(dateStr: String, timestamp: Long): Boolean {
        val db = writableDatabase
        val existing = getRecordsByDate(dateStr).firstOrNull { it.clockOutTime == null }

        return if (existing != null) {
            val values = ContentValues().apply {
                put(COLUMN_CLOCK_IN, timestamp)
                if (existing.clockOutTime != null) {
                    putNull(COLUMN_CLOCK_OUT)
                }
            }
            db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(existing.id.toString())) > 0
        } else {
            val values = ContentValues().apply {
                put(COLUMN_DATE, dateStr)
                put(COLUMN_CLOCK_IN, timestamp)
            }
            db.insert(TABLE_NAME, null, values) != -1L
        }
    }

    fun clockOut(dateStr: String, timestamp: Long): Boolean {
        val db = writableDatabase
        val existing = getRecordsByDate(dateStr).firstOrNull { it.clockOutTime == null }

        return if (existing != null) {
            val values = ContentValues().apply {
                if (existing.clockInTime == null) {
                    put(COLUMN_CLOCK_IN, timestamp)
                }
                put(COLUMN_CLOCK_OUT, timestamp)
            }
            db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(existing.id.toString())) > 0
        } else {
            val values = ContentValues().apply {
                put(COLUMN_DATE, dateStr)
                put(COLUMN_CLOCK_IN, timestamp)
                put(COLUMN_CLOCK_OUT, timestamp)
            }
            db.insert(TABLE_NAME, null, values) != -1L
        }
    }

    fun getRecordByDate(dateStr: String): WorkRecord? {
        return getRecordsByDate(dateStr).firstOrNull()
    }

    fun getRecordsByDate(dateStr: String): List<WorkRecord> {
        val list = mutableListOf<WorkRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(dateStr),
            null, null,
            "$COLUMN_ID DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(parseRecordFromCursor(it))
            }
        }
        return list
    }

    fun getRecordsByMonth(yearMonthStr: String): List<WorkRecord> {
        val list = mutableListOf<WorkRecord>()
        val db = readableDatabase
        // Match dates like "2025-03%"
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_DATE LIKE ?",
            arrayOf("$yearMonthStr%"),
            null, null,
            "$COLUMN_DATE DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(parseRecordFromCursor(it))
            }
        }
        return list
    }

    fun getAllRecords(): List<WorkRecord> {
        val list = mutableListOf<WorkRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null, null, null, null, null,
            "$COLUMN_DATE DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(parseRecordFromCursor(it))
            }
        }
        return list
    }

    fun saveOrUpdateRecord(record: WorkRecord): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, record.date)
            if (record.clockInTime != null) {
                put(COLUMN_CLOCK_IN, record.clockInTime)
            } else {
                putNull(COLUMN_CLOCK_IN)
            }
            if (record.clockOutTime != null) {
                put(COLUMN_CLOCK_OUT, record.clockOutTime)
            } else {
                putNull(COLUMN_CLOCK_OUT)
            }
            put(COLUMN_NOTE, record.note)
        }

        val existing = getRecordByDate(record.date)
        return if (existing != null) {
            db.update(TABLE_NAME, values, "$COLUMN_DATE = ?", arrayOf(record.date)) > 0
        } else {
            db.insert(TABLE_NAME, null, values) != -1L
        }
    }

    fun deleteRecord(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
    }

    fun clearAllRecords(): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_NAME, null, null) >= 0
    }

    private fun parseRecordFromCursor(cursor: Cursor): WorkRecord {
        val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
        val dateIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE)
        val clockInIndex = cursor.getColumnIndexOrThrow(COLUMN_CLOCK_IN)
        val clockOutIndex = cursor.getColumnIndexOrThrow(COLUMN_CLOCK_OUT)
        val noteIndex = cursor.getColumnIndexOrThrow(COLUMN_NOTE)

        val id = cursor.getLong(idIndex)
        val date = cursor.getString(dateIndex)
        val clockInTime = if (cursor.isNull(clockInIndex)) null else cursor.getLong(clockInIndex)
        val clockOutTime = if (cursor.isNull(clockOutIndex)) null else cursor.getLong(clockOutIndex)
        val note = if (cursor.isNull(noteIndex)) "" else cursor.getString(noteIndex)

        return WorkRecord(
            id = id,
            date = date,
            clockInTime = clockInTime,
            clockOutTime = clockOutTime,
            note = note
        )
    }
}
