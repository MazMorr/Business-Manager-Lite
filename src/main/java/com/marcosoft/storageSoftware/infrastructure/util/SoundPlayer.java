package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

@Lazy
@Component
public class SoundPlayer {

    private MediaPlayer mediaPlayerMusic;
    private MediaPlayer mediaPlayerSound;

    public void playMusic(String absolutePath) {
        try {
            stopMusic();
            if (mediaPlayerMusic != null) {
                mediaPlayerMusic.dispose();
            }
            Media media = new Media(new File(absolutePath).toURI().toString());
            mediaPlayerMusic = new MediaPlayer(media);
            mediaPlayerMusic.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayerMusic.play();
        } catch (Exception e) {
            System.err.println("Error al reproducir música: " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (mediaPlayerMusic != null) {
            mediaPlayerMusic.stop();
            mediaPlayerMusic.dispose();
            mediaPlayerMusic = null;
        }
    }

    public void pauseMusic() {
        if (mediaPlayerMusic != null) {
            mediaPlayerMusic.pause();
        }
    }

    public void keepPlayingMusic() {
        if (mediaPlayerMusic != null) {
            mediaPlayerMusic.play();
        }
    }

    public void playSound(String soundPath) {
        try {
            stopSound();
            if (mediaPlayerSound != null) {
                mediaPlayerSound.dispose();
            }
            Media media;
            // Permite rutas absolutas y relativas
            if (new File(soundPath).exists()) {
                media = new Media(new File(soundPath).toURI().toString());
            } else {
                media = new Media(Objects.requireNonNull(getClass().getResource(soundPath)).toExternalForm());
            }
            mediaPlayerSound = new MediaPlayer(media);
            mediaPlayerSound.setOnEndOfMedia(() -> {
                mediaPlayerSound.stop();
                mediaPlayerSound.dispose();
                mediaPlayerSound = null;
            });
            mediaPlayerSound.play();
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido: " + e.getMessage());
        }
    }

    public void stopSound() {
        if (mediaPlayerSound != null) {
            mediaPlayerSound.stop();
            mediaPlayerSound.dispose();
            mediaPlayerSound = null;
        }
    }

    public void pauseSound() {
        if (mediaPlayerSound != null) {
            mediaPlayerSound.pause();
        }
    }

    public void keepPlayingSound() {
        if (mediaPlayerSound != null) {
            mediaPlayerSound.play();
        }
    }
}

