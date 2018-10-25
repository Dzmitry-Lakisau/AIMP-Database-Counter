package by.dzmitry_lakisau.aimp_database_counter.database

import android.content.Context
import android.database.Cursor
import by.dzmitry_lakisau.aimp_database_counter.model.Track
import io.reactivex.Observable

class DatabaseWorker(context: Context) {
    private val mDatabaseHelper: DatabaseHelper

    init {
        mDatabaseHelper = DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
    }

    companion object {
        //database must lay in assets/database
        private const val DATABASE_NAME = "AIMP2.db"
        private const val DATABASE_VERSION = 1
        private const val COLUMN_ARTIST = "sArtist"
        private const val COLUMN_TITLE = "sTitle"
        private const val COLUMN_PLAYCOUNT = "PlayCount"
        private const val COLUMN_LAST_PLAY = "LastPlay"
        private const val COLUMN_ADDED = "Added"
}

    fun count(dateRanges: HashMap<String, Float>): Observable<String> {
        return Observable.create { emitter ->
            try {
                for (i in 0..dateRanges.size-2){
                    val sum = countTracksListenedInRange(ArrayList(dateRanges.values)[i], ArrayList(dateRanges.values)[i+1]) +
                            countTracksAddedBeforeAndListenedOnceInRange(ArrayList(dateRanges.values)[i], ArrayList(dateRanges.values)[i+1])

                    val listOfDuplicates = countTracksListenedMoreThanTwoTimes(ArrayList(dateRanges.values)[i], ArrayList(dateRanges.values)[i+1])
                    var stringFromListOfDuplicates = String()
                    for (track in listOfDuplicates){
                        stringFromListOfDuplicates += "\n${track.artist} - ${track.title}"
                    }

                    emitter.onNext("Listened once between ${dateRanges.keys.toList()[i]} and" +
                    " ${dateRanges.keys.toList()[i+1]}: $sum.\nListened probably more than once - ${listOfDuplicates.size}:$stringFromListOfDuplicates")
                }
                emitter.onComplete()
            } catch (e: Exception) {
                if (!emitter.isDisposed) {
                    emitter.onError(e)
                }
            }
        }
    }

    private fun countTracksListenedInRange(start: Float, end: Float): Int {
        val database = mDatabaseHelper.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = database.rawQuery("SELECT SUM($COLUMN_PLAYCOUNT) FROM MAIN " +
                    "WHERE $COLUMN_LAST_PLAY BETWEEN $start AND $end AND $COLUMN_ADDED>$start "+
                    "ORDER BY $COLUMN_LAST_PLAY ASC", null)

            return if (cursor != null && cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        } finally {
            cursor?.close()
        }
    }

    private fun countTracksAddedBeforeAndListenedOnceInRange(start: Float, end: Float): Int {
        val database = mDatabaseHelper.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = database.rawQuery("SELECT COUNT($COLUMN_PLAYCOUNT) FROM MAIN " +
                    "WHERE ($COLUMN_LAST_PLAY BETWEEN $start AND $end AND $COLUMN_ADDED<$start AND $COLUMN_PLAYCOUNT = 2) " +
                    "OR ($COLUMN_ADDED BETWEEN $start AND $end AND $COLUMN_LAST_PLAY>$end AND $COLUMN_PLAYCOUNT = 2) " +
                    "ORDER BY $COLUMN_LAST_PLAY ASC", null)

            return if (cursor != null && cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        } finally {
            cursor?.close()
        }
    }

    private fun countTracksListenedMoreThanTwoTimes(start: Float, end: Float): ArrayList<Track> {
        val database = mDatabaseHelper.readableDatabase
        var cursor: Cursor? = null
        val result = ArrayList<Track>()

        try {
            cursor = database.rawQuery("SELECT $COLUMN_ARTIST, $COLUMN_TITLE FROM MAIN " +
                    "WHERE ($COLUMN_LAST_PLAY BETWEEN $start AND $end AND $COLUMN_PLAYCOUNT > 2) " +
                    "OR ($COLUMN_ADDED BETWEEN $start AND $end AND $COLUMN_PLAYCOUNT > 2) " +
                    "ORDER BY $COLUMN_LAST_PLAY ASC", null)

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val trackTitleColumn = cursor.getColumnIndexOrThrow(COLUMN_TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(COLUMN_ARTIST)
                    result.add(Track(cursor.getString(artistColumn), cursor.getString(trackTitleColumn)))
                }
                while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        return result
    }
}
