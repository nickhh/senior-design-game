package ach7nbh2game.client;

import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;

/**
 * This enum encapsulates all the sound effects of a game, so as to separate the sound playing
 * codes from the game codes.
 * 1. Define all your sound effect names and the associated wave file.
 * 2. To play a specific sound, simply invoke SoundEffect.SOUND_NAME.play().
 * 3. You might optionally invoke the static method SoundEffect.init() to pre-load all the
 *    sound files, so that the play is not paused while loading the file for the first time.
 * 4. You can use the static variable SoundEffect.volume to mute the sound.
 */
public enum SoundEffect {
    GUN_FIRE("gun_fire.wav"),
    GUN_WHIFF("gun_whiff.wav"),
    PICK_WALL("pick_wall.wav"),
    PICK_WHIFF("pick_whiff.wav"),
    PICK_PLAYER("pick_player.wav"),
    PICKUP("pickup.wav"),
    PLAYER_DEATH("player_death.wav"),
    GAME_END("game_end.wav"),
    MENU_BGM("AMemoryAway.wav"),
    GAME_START("game_start.wav"),
    PLAYER_SPAWN("player_spawn.wav"),
    BULLET_WALL("bullet_wall.wav"),
    BULLET_PLAYER("bullet_player.wav"),
    BULLET_BULLET("bullet_bullet.wav"),
    ROCKET_LAUNCH("rocket_launch.wav"),
    ROCKET_EXPLODE("rocket_explosion.wav"),
    TR_APPEAR("tr_appear.wav");

    // Nested class for specifying volume
    public static enum Volume {
        MUTE, LOW, MEDIUM, HIGH
    }

    public static Volume volume = Volume.HIGH;

    // Each sound effect has its own clip, loaded with its own sound file.
    private Clip clip;

    // Constructor to construct each element of the enum with its own sound file.
    SoundEffect(String soundFileName) {
        try {
            // Use URL (instead of File) to read from disk and JAR.
            URL url = this.getClass().getClassLoader().getResource("sounds/"+soundFileName);
            // Set up an audio input stream piped from the sound file.
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            // Get a clip resource.
            clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioInputStream);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Play or Re-play the sound effect from the beginning, by rewinding.
    public void play() {
        if (volume != Volume.MUTE) {
            if (clip.isRunning())
                clip.stop();   // Stop the player if it is still running
            clip.setFramePosition(0); // rewind to the beginning
            clip.start();     // Start playing
        }
    }

    public void loop() {
        if (volume != Volume.MUTE) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (clip.isRunning()) {
            clip.stop();
        }
    }

    // Optional static method to pre-load all the sound files.
    static void init() {
        values(); // calls the constructor for all the elements
    }
}
