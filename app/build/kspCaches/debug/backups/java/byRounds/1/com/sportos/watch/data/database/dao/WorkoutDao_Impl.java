package com.sportos.watch.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.sportos.watch.data.database.entity.PersonalRecordEntity;
import com.sportos.watch.data.database.entity.WorkoutSessionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WorkoutDao_Impl implements WorkoutDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WorkoutSessionEntity> __insertionAdapterOfWorkoutSessionEntity;

  private final EntityInsertionAdapter<PersonalRecordEntity> __insertionAdapterOfPersonalRecordEntity;

  private final EntityDeletionOrUpdateAdapter<WorkoutSessionEntity> __deletionAdapterOfWorkoutSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAllSessions;

  public WorkoutDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWorkoutSessionEntity = new EntityInsertionAdapter<WorkoutSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `workout_sessions` (`id`,`sportType`,`subMode`,`startTimeMs`,`endTimeMs`,`durationMs`,`caloriesKcal`,`avgHeartRate`,`maxHeartRate`,`distanceMeters`,`avgPaceMinPerKm`,`sportSpecificSummary`,`serializedRoutePoints`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkoutSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSportType());
        statement.bindString(3, entity.getSubMode());
        statement.bindLong(4, entity.getStartTimeMs());
        statement.bindLong(5, entity.getEndTimeMs());
        statement.bindLong(6, entity.getDurationMs());
        statement.bindDouble(7, entity.getCaloriesKcal());
        statement.bindDouble(8, entity.getAvgHeartRate());
        statement.bindDouble(9, entity.getMaxHeartRate());
        statement.bindDouble(10, entity.getDistanceMeters());
        statement.bindDouble(11, entity.getAvgPaceMinPerKm());
        statement.bindString(12, entity.getSportSpecificSummary());
        statement.bindString(13, entity.getSerializedRoutePoints());
      }
    };
    this.__insertionAdapterOfPersonalRecordEntity = new EntityInsertionAdapter<PersonalRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `personal_records` (`recordKey`,`sportType`,`title`,`value`,`displayValue`,`achievedAtMs`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PersonalRecordEntity entity) {
        statement.bindString(1, entity.getRecordKey());
        statement.bindString(2, entity.getSportType());
        statement.bindString(3, entity.getTitle());
        statement.bindDouble(4, entity.getValue());
        statement.bindString(5, entity.getDisplayValue());
        statement.bindLong(6, entity.getAchievedAtMs());
      }
    };
    this.__deletionAdapterOfWorkoutSessionEntity = new EntityDeletionOrUpdateAdapter<WorkoutSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `workout_sessions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkoutSessionEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfClearAllSessions = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM workout_sessions";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final WorkoutSessionEntity session,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfWorkoutSessionEntity.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertOrUpdatePR(final PersonalRecordEntity pr,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPersonalRecordEntity.insert(pr);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSession(final WorkoutSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfWorkoutSessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllSessions(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllSessions.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllSessions.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WorkoutSessionEntity>> getAllSessions() {
    final String _sql = "SELECT * FROM workout_sessions ORDER BY startTimeMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"workout_sessions"}, new Callable<List<WorkoutSessionEntity>>() {
      @Override
      @NonNull
      public List<WorkoutSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSportType = CursorUtil.getColumnIndexOrThrow(_cursor, "sportType");
          final int _cursorIndexOfSubMode = CursorUtil.getColumnIndexOrThrow(_cursor, "subMode");
          final int _cursorIndexOfStartTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMs");
          final int _cursorIndexOfEndTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMs");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfCaloriesKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesKcal");
          final int _cursorIndexOfAvgHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "avgHeartRate");
          final int _cursorIndexOfMaxHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHeartRate");
          final int _cursorIndexOfDistanceMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceMeters");
          final int _cursorIndexOfAvgPaceMinPerKm = CursorUtil.getColumnIndexOrThrow(_cursor, "avgPaceMinPerKm");
          final int _cursorIndexOfSportSpecificSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "sportSpecificSummary");
          final int _cursorIndexOfSerializedRoutePoints = CursorUtil.getColumnIndexOrThrow(_cursor, "serializedRoutePoints");
          final List<WorkoutSessionEntity> _result = new ArrayList<WorkoutSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WorkoutSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSportType;
            _tmpSportType = _cursor.getString(_cursorIndexOfSportType);
            final String _tmpSubMode;
            _tmpSubMode = _cursor.getString(_cursorIndexOfSubMode);
            final long _tmpStartTimeMs;
            _tmpStartTimeMs = _cursor.getLong(_cursorIndexOfStartTimeMs);
            final long _tmpEndTimeMs;
            _tmpEndTimeMs = _cursor.getLong(_cursorIndexOfEndTimeMs);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final double _tmpCaloriesKcal;
            _tmpCaloriesKcal = _cursor.getDouble(_cursorIndexOfCaloriesKcal);
            final double _tmpAvgHeartRate;
            _tmpAvgHeartRate = _cursor.getDouble(_cursorIndexOfAvgHeartRate);
            final double _tmpMaxHeartRate;
            _tmpMaxHeartRate = _cursor.getDouble(_cursorIndexOfMaxHeartRate);
            final double _tmpDistanceMeters;
            _tmpDistanceMeters = _cursor.getDouble(_cursorIndexOfDistanceMeters);
            final double _tmpAvgPaceMinPerKm;
            _tmpAvgPaceMinPerKm = _cursor.getDouble(_cursorIndexOfAvgPaceMinPerKm);
            final String _tmpSportSpecificSummary;
            _tmpSportSpecificSummary = _cursor.getString(_cursorIndexOfSportSpecificSummary);
            final String _tmpSerializedRoutePoints;
            _tmpSerializedRoutePoints = _cursor.getString(_cursorIndexOfSerializedRoutePoints);
            _item = new WorkoutSessionEntity(_tmpId,_tmpSportType,_tmpSubMode,_tmpStartTimeMs,_tmpEndTimeMs,_tmpDurationMs,_tmpCaloriesKcal,_tmpAvgHeartRate,_tmpMaxHeartRate,_tmpDistanceMeters,_tmpAvgPaceMinPerKm,_tmpSportSpecificSummary,_tmpSerializedRoutePoints);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<WorkoutSessionEntity>> getRecentSessions(final int limit) {
    final String _sql = "SELECT * FROM workout_sessions ORDER BY startTimeMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"workout_sessions"}, new Callable<List<WorkoutSessionEntity>>() {
      @Override
      @NonNull
      public List<WorkoutSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSportType = CursorUtil.getColumnIndexOrThrow(_cursor, "sportType");
          final int _cursorIndexOfSubMode = CursorUtil.getColumnIndexOrThrow(_cursor, "subMode");
          final int _cursorIndexOfStartTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMs");
          final int _cursorIndexOfEndTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMs");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfCaloriesKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesKcal");
          final int _cursorIndexOfAvgHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "avgHeartRate");
          final int _cursorIndexOfMaxHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHeartRate");
          final int _cursorIndexOfDistanceMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceMeters");
          final int _cursorIndexOfAvgPaceMinPerKm = CursorUtil.getColumnIndexOrThrow(_cursor, "avgPaceMinPerKm");
          final int _cursorIndexOfSportSpecificSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "sportSpecificSummary");
          final int _cursorIndexOfSerializedRoutePoints = CursorUtil.getColumnIndexOrThrow(_cursor, "serializedRoutePoints");
          final List<WorkoutSessionEntity> _result = new ArrayList<WorkoutSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WorkoutSessionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSportType;
            _tmpSportType = _cursor.getString(_cursorIndexOfSportType);
            final String _tmpSubMode;
            _tmpSubMode = _cursor.getString(_cursorIndexOfSubMode);
            final long _tmpStartTimeMs;
            _tmpStartTimeMs = _cursor.getLong(_cursorIndexOfStartTimeMs);
            final long _tmpEndTimeMs;
            _tmpEndTimeMs = _cursor.getLong(_cursorIndexOfEndTimeMs);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final double _tmpCaloriesKcal;
            _tmpCaloriesKcal = _cursor.getDouble(_cursorIndexOfCaloriesKcal);
            final double _tmpAvgHeartRate;
            _tmpAvgHeartRate = _cursor.getDouble(_cursorIndexOfAvgHeartRate);
            final double _tmpMaxHeartRate;
            _tmpMaxHeartRate = _cursor.getDouble(_cursorIndexOfMaxHeartRate);
            final double _tmpDistanceMeters;
            _tmpDistanceMeters = _cursor.getDouble(_cursorIndexOfDistanceMeters);
            final double _tmpAvgPaceMinPerKm;
            _tmpAvgPaceMinPerKm = _cursor.getDouble(_cursorIndexOfAvgPaceMinPerKm);
            final String _tmpSportSpecificSummary;
            _tmpSportSpecificSummary = _cursor.getString(_cursorIndexOfSportSpecificSummary);
            final String _tmpSerializedRoutePoints;
            _tmpSerializedRoutePoints = _cursor.getString(_cursorIndexOfSerializedRoutePoints);
            _item = new WorkoutSessionEntity(_tmpId,_tmpSportType,_tmpSubMode,_tmpStartTimeMs,_tmpEndTimeMs,_tmpDurationMs,_tmpCaloriesKcal,_tmpAvgHeartRate,_tmpMaxHeartRate,_tmpDistanceMeters,_tmpAvgPaceMinPerKm,_tmpSportSpecificSummary,_tmpSerializedRoutePoints);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSessionById(final long id,
      final Continuation<? super WorkoutSessionEntity> $completion) {
    final String _sql = "SELECT * FROM workout_sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WorkoutSessionEntity>() {
      @Override
      @Nullable
      public WorkoutSessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSportType = CursorUtil.getColumnIndexOrThrow(_cursor, "sportType");
          final int _cursorIndexOfSubMode = CursorUtil.getColumnIndexOrThrow(_cursor, "subMode");
          final int _cursorIndexOfStartTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMs");
          final int _cursorIndexOfEndTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMs");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfCaloriesKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesKcal");
          final int _cursorIndexOfAvgHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "avgHeartRate");
          final int _cursorIndexOfMaxHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "maxHeartRate");
          final int _cursorIndexOfDistanceMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceMeters");
          final int _cursorIndexOfAvgPaceMinPerKm = CursorUtil.getColumnIndexOrThrow(_cursor, "avgPaceMinPerKm");
          final int _cursorIndexOfSportSpecificSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "sportSpecificSummary");
          final int _cursorIndexOfSerializedRoutePoints = CursorUtil.getColumnIndexOrThrow(_cursor, "serializedRoutePoints");
          final WorkoutSessionEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSportType;
            _tmpSportType = _cursor.getString(_cursorIndexOfSportType);
            final String _tmpSubMode;
            _tmpSubMode = _cursor.getString(_cursorIndexOfSubMode);
            final long _tmpStartTimeMs;
            _tmpStartTimeMs = _cursor.getLong(_cursorIndexOfStartTimeMs);
            final long _tmpEndTimeMs;
            _tmpEndTimeMs = _cursor.getLong(_cursorIndexOfEndTimeMs);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final double _tmpCaloriesKcal;
            _tmpCaloriesKcal = _cursor.getDouble(_cursorIndexOfCaloriesKcal);
            final double _tmpAvgHeartRate;
            _tmpAvgHeartRate = _cursor.getDouble(_cursorIndexOfAvgHeartRate);
            final double _tmpMaxHeartRate;
            _tmpMaxHeartRate = _cursor.getDouble(_cursorIndexOfMaxHeartRate);
            final double _tmpDistanceMeters;
            _tmpDistanceMeters = _cursor.getDouble(_cursorIndexOfDistanceMeters);
            final double _tmpAvgPaceMinPerKm;
            _tmpAvgPaceMinPerKm = _cursor.getDouble(_cursorIndexOfAvgPaceMinPerKm);
            final String _tmpSportSpecificSummary;
            _tmpSportSpecificSummary = _cursor.getString(_cursorIndexOfSportSpecificSummary);
            final String _tmpSerializedRoutePoints;
            _tmpSerializedRoutePoints = _cursor.getString(_cursorIndexOfSerializedRoutePoints);
            _result = new WorkoutSessionEntity(_tmpId,_tmpSportType,_tmpSubMode,_tmpStartTimeMs,_tmpEndTimeMs,_tmpDurationMs,_tmpCaloriesKcal,_tmpAvgHeartRate,_tmpMaxHeartRate,_tmpDistanceMeters,_tmpAvgPaceMinPerKm,_tmpSportSpecificSummary,_tmpSerializedRoutePoints);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PersonalRecordEntity>> getAllPRs() {
    final String _sql = "SELECT * FROM personal_records ORDER BY achievedAtMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"personal_records"}, new Callable<List<PersonalRecordEntity>>() {
      @Override
      @NonNull
      public List<PersonalRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRecordKey = CursorUtil.getColumnIndexOrThrow(_cursor, "recordKey");
          final int _cursorIndexOfSportType = CursorUtil.getColumnIndexOrThrow(_cursor, "sportType");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfDisplayValue = CursorUtil.getColumnIndexOrThrow(_cursor, "displayValue");
          final int _cursorIndexOfAchievedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "achievedAtMs");
          final List<PersonalRecordEntity> _result = new ArrayList<PersonalRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonalRecordEntity _item;
            final String _tmpRecordKey;
            _tmpRecordKey = _cursor.getString(_cursorIndexOfRecordKey);
            final String _tmpSportType;
            _tmpSportType = _cursor.getString(_cursorIndexOfSportType);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final double _tmpValue;
            _tmpValue = _cursor.getDouble(_cursorIndexOfValue);
            final String _tmpDisplayValue;
            _tmpDisplayValue = _cursor.getString(_cursorIndexOfDisplayValue);
            final long _tmpAchievedAtMs;
            _tmpAchievedAtMs = _cursor.getLong(_cursorIndexOfAchievedAtMs);
            _item = new PersonalRecordEntity(_tmpRecordKey,_tmpSportType,_tmpTitle,_tmpValue,_tmpDisplayValue,_tmpAchievedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPRByKey(final String key,
      final Continuation<? super PersonalRecordEntity> $completion) {
    final String _sql = "SELECT * FROM personal_records WHERE recordKey = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, key);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PersonalRecordEntity>() {
      @Override
      @Nullable
      public PersonalRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfRecordKey = CursorUtil.getColumnIndexOrThrow(_cursor, "recordKey");
          final int _cursorIndexOfSportType = CursorUtil.getColumnIndexOrThrow(_cursor, "sportType");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfDisplayValue = CursorUtil.getColumnIndexOrThrow(_cursor, "displayValue");
          final int _cursorIndexOfAchievedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "achievedAtMs");
          final PersonalRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpRecordKey;
            _tmpRecordKey = _cursor.getString(_cursorIndexOfRecordKey);
            final String _tmpSportType;
            _tmpSportType = _cursor.getString(_cursorIndexOfSportType);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final double _tmpValue;
            _tmpValue = _cursor.getDouble(_cursorIndexOfValue);
            final String _tmpDisplayValue;
            _tmpDisplayValue = _cursor.getString(_cursorIndexOfDisplayValue);
            final long _tmpAchievedAtMs;
            _tmpAchievedAtMs = _cursor.getLong(_cursorIndexOfAchievedAtMs);
            _result = new PersonalRecordEntity(_tmpRecordKey,_tmpSportType,_tmpTitle,_tmpValue,_tmpDisplayValue,_tmpAchievedAtMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
