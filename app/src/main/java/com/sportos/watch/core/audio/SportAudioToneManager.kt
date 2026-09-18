package com.sportos.watch.core.audio

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Pre-built Android Audio Tone Generator for real-time sports feedback.
 * Provides authentic referee whistles, stadium quarter buzzers, and countdown beeps
 * using Android's lightweight hardware ToneGenerator.
 */
class SportAudioToneManager {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
        } catch (_: Exception) {
            // Audio stream might not be immediately available on some emulators
        }
    }

    /**
     * Referee Whistle blast (used in Football referee mode and period start/end).
     */
    fun playRefereeWhistle() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
        } catch (_: Exception) {}
    }

    /**
     * Stadium court buzzer (used in Basketball quarter end and shot clock).
     */
    fun playBasketballBuzzer() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 500)
        } catch (_: Exception) {}
    }

    /**
     * Crisp lap split / milestone notification chime.
     */
    fun playMilestoneChime() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
        } catch (_: Exception) {}
    }

    /**
     * 3-2-1 countdown tick.
     */
    fun playCountdownTick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        } catch (_: Exception) {}
    }

    /**
     * Start/Go alert.
     */
    fun playStartAlert() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 250)
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
