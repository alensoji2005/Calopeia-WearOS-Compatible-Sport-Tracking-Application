package com.sportos.watch.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.sportos.watch.MainActivity
import com.sportos.watch.R
import com.sportos.watch.core.healthservices.HealthServicesManager
import com.sportos.watch.core.imu.ImuSensorManager
import com.sportos.watch.core.simulator.TelemetrySimulator
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseType
import com.sportos.watch.sports.SportEngine
import com.sportos.watch.sports.SportEngineState
import com.sportos.watch.sports.running.RunningEngine
import com.sportos.watch.sports.running.RunningMode
import com.sportos.watch.sports.basketball.BasketballEngine
import com.sportos.watch.sports.basketball.BasketballMode
import com.sportos.watch.sports.football.FootballEngine
import com.sportos.watch.sports.cricket.CricketEngine
import com.sportos.watch.sports.cricket.CricketMode
import com.sportos.watch.sports.tennis.TennisEngine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WorkoutService : LifecycleService() {

    private val binder = LocalBinder()
    
    lateinit var healthServicesManager: HealthServicesManager
        private set
        
    lateinit var imuSensorManager: ImuSensorManager
        private set

    var activeEngine: SportEngine? = null
        private set

    private var simulator: TelemetrySimulator? = null

    private var isWorkoutActive = false
    private var dataCollectionJob: Job? = null
    var currentSportType: String = "Running"
        private set

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
        val engineState: StateFlow<SportEngineState?> 
            get() = activeEngine?.engineState ?: MutableStateFlow(null).asStateFlow()
        val currentSport: String
            get() = currentSportType
        val isWorkoutActive: Boolean
            get() = this@WorkoutService.isWorkoutActive
        val latestStateSnapshot: SportEngineState?
            get() = activeEngine?.engineState?.value

        fun pause() {
            activeEngine?.pause()
            imuSensorManager.stopListening()
            lifecycleScope.launch { healthServicesManager.pauseExercise() }
        }

        fun resume() {
            activeEngine?.resume()
            imuSensorManager.startListening(
                includeAccelerometer = true,
                includeGyroscope = requiresGyroForSport(currentSportType),
                maxReportLatencyUs = 150_000
            )
            lifecycleScope.launch { healthServicesManager.resumeExercise() }
        }

        fun lap() {
            activeEngine?.triggerLap()
        }
    }

    private fun requiresGpsForSport(sport: String): Boolean = when (sport) {
        "Running", "Football" -> true
        else -> false
    }

    private fun requiresGyroForSport(sport: String): Boolean = when (sport) {
        "Basketball", "Cricket", "Tennis" -> true
        else -> false
    }

    override fun onCreate() {
        super.onCreate()
        healthServicesManager = HealthServicesManager(this)
        imuSensorManager = ImuSensorManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val action = intent?.action
        val sportType = intent?.getStringExtra(EXTRA_SPORT_TYPE) ?: currentSportType
        val sportMode = intent?.getStringExtra(EXTRA_SPORT_MODE) ?: ""
        val targetPace = intent?.getDoubleExtra(EXTRA_TARGET_PACE, 5.0) ?: 5.0
        val isDemo = intent?.getBooleanExtra(EXTRA_DEMO_MODE, true) ?: true // Default true on dev

        when (action) {
            ACTION_START_WORKOUT -> {
                if (!isWorkoutActive) {
                    startWorkoutForeground(sportType, sportMode, targetPace, isDemo)
                }
            }
            ACTION_STOP_WORKOUT -> stopWorkoutForeground()
            ACTION_PAUSE_WORKOUT -> binder.pause()
            ACTION_RESUME_WORKOUT -> binder.resume()
            ACTION_LAP_WORKOUT -> binder.lap()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private fun startWorkoutForeground(
        sportType: String,
        sportMode: String,
        targetPace: Double,
        isDemo: Boolean
    ) {
        isWorkoutActive = true
        currentSportType = sportType
        
        activeEngine = when (sportType) {
            "Running" -> RunningEngine(
                targetPaceMinPerKm = targetPace,
                mode = RunningMode.GHOST_PACER
            )
            "Basketball" -> BasketballEngine(
                mode = if (sportMode == "PRACTICE_DRILL") BasketballMode.PRACTICE_DRILL else BasketballMode.GAME_AUTOMATIC
            )
            "Football" -> FootballEngine()
            "Cricket" -> CricketEngine(
                mode = if (sportMode == "BATTING") CricketMode.BATTING else CricketMode.BOWLING
            )
            "Tennis" -> TennisEngine()
            else -> RunningEngine()
        }
        
        activeEngine?.start()
        
        // Feed sensor data to the active engine
        dataCollectionJob = lifecycleScope.launch {
            imuSensorManager.imuFlow.onEach { data ->
                activeEngine?.processImuData(data)
            }.launchIn(this)
            
            healthServicesManager.exerciseState.onEach { update ->
                update?.let { activeEngine?.processHealthData(it) }
            }.launchIn(this)
        }
        
        // Start live health services with selective GPS gating
        val needsGps = requiresGpsForSport(sportType)
        val needsGyro = requiresGyroForSport(sportType)

        lifecycleScope.launch {
            val exType = when (sportType) {
                "Running" -> ExerciseType.RUNNING
                "Basketball" -> ExerciseType.BASKETBALL
                "Football" -> ExerciseType.SOCCER
                "Cricket" -> ExerciseType.CRICKET
                "Tennis" -> ExerciseType.TENNIS
                else -> ExerciseType.WORKOUT
            }
            healthServicesManager.prepareExercise(exType, enableGps = needsGps)

            val dataTypes = mutableSetOf<DataType<*, *>>(DataType.HEART_RATE_BPM)
            if (needsGps) {
                dataTypes.add(DataType.LOCATION)
                dataTypes.add(DataType.DISTANCE_TOTAL)
                dataTypes.add(DataType.PACE)
            } else {
                dataTypes.add(DataType.DISTANCE_TOTAL)
                dataTypes.add(DataType.STEPS_PER_MINUTE)
            }
            if (sportType == "Running") {
                dataTypes.add(DataType.STEPS_PER_MINUTE)
            }

            healthServicesManager.startExercise(exType, dataTypes)
        }
        
        imuSensorManager.startListening(
            includeAccelerometer = true,
            includeGyroscope = needsGyro,
            maxReportLatencyUs = 150_000
        )

        // If in Demo Mode (or on emulator), run TelemetrySimulator to keep all screens dynamically animated
        if (isDemo) {
            activeEngine?.let { eng ->
                simulator = TelemetrySimulator(eng, sportType).apply { start() }
            }
        }
        
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Setup OngoingActivity for Wear OS Watch Face
        val ongoingActivityStatus = Status.Builder()
            .addTemplate("Calopeia • $sportType")
            .build()
            
        val ongoingActivity = OngoingActivity.Builder(
            applicationContext, 
            NOTIFICATION_ID, 
            buildNotificationBuilder(ongoingActivityStatus)
        )
            .setAnimatedIcon(R.drawable.ic_sport_running)
            .setStaticIcon(R.drawable.ic_sport_running)
            .setTouchIntent(getMainActivityPendingIntent())
            .setStatus(ongoingActivityStatus)
            .build()
            
        ongoingActivity.apply(applicationContext)
    }

    private fun stopWorkoutForeground() {
        isWorkoutActive = false
        simulator?.stop()
        simulator = null
        dataCollectionJob?.cancel()
        dataCollectionJob = null
        activeEngine?.stop()
        imuSensorManager.stopListening()
        lifecycleScope.launch {
            healthServicesManager.endExercise()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    override fun onDestroy() {
        if (isWorkoutActive) {
            stopWorkoutForeground()
        }
        imuSensorManager.stopListening()
        super.onDestroy()
    }

    private fun buildNotificationBuilder(status: Status? = null): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Calopeia • $currentSportType")
            .setContentText("Recording performance data...")
            .setSmallIcon(R.drawable.ic_sport_running)
            .setOngoing(true)
            .setContentIntent(getMainActivityPendingIntent())
    }
    
    private fun buildNotification(): Notification {
        return buildNotificationBuilder().build()
    }

    private fun getMainActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START_WORKOUT = "com.sportos.watch.ACTION_START_WORKOUT"
        const val ACTION_STOP_WORKOUT = "com.sportos.watch.ACTION_STOP_WORKOUT"
        const val ACTION_PAUSE_WORKOUT = "com.sportos.watch.ACTION_PAUSE_WORKOUT"
        const val ACTION_RESUME_WORKOUT = "com.sportos.watch.ACTION_RESUME_WORKOUT"
        const val ACTION_LAP_WORKOUT = "com.sportos.watch.ACTION_LAP_WORKOUT"

        const val EXTRA_SPORT_TYPE = "EXTRA_SPORT_TYPE"
        const val EXTRA_SPORT_MODE = "EXTRA_SPORT_MODE"
        const val EXTRA_TARGET_PACE = "EXTRA_TARGET_PACE"
        const val EXTRA_DEMO_MODE = "EXTRA_DEMO_MODE"

        private const val CHANNEL_ID = "WorkoutServiceChannel"
        private const val NOTIFICATION_ID = 101
    }
}

