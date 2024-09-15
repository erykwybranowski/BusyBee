package lib;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    // Map to store multiple sounds
    private Map<String, Clip> soundClips = new HashMap<>();

    // Play a sound by specifying the file path and whether it should loop
    public void playSound(String soundFileName, boolean loop, float volume) {
        try {
            // Load the sound file from the resource folder
            InputStream audioSrc = getClass().getResourceAsStream(soundFileName);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Set the volume using FloatControl
            setVolume(clip, volume);

            // Looping or not based on the parameter
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop continuously
            } else {
                clip.start(); // Play once
            }

            // Store the clip for potential future management
            soundClips.put(soundFileName, clip);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Stop a specific sound
    public void stopSound(String soundFileName) {
        Clip clip = soundClips.get(soundFileName);
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close(); // Free up the resources when stopped
        }
    }

    // Stop all currently playing sounds
    public void stopAllSounds() {
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
                clip.close();
            }
        }
        soundClips.clear();
    }

    // Check if a sound is currently playing (useful for bee movement sound)
    public boolean isPlaying(String soundFileName) {
        Clip clip = soundClips.get(soundFileName);
        return clip != null && clip.isRunning();
    }

    // Set the volume of a Clip
    private void setVolume(Clip clip, float volume) {
        if (volume < 0f || volume > 1f) {
            throw new IllegalArgumentException("Volume not valid: " + volume);
        }
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (range * volume) + gainControl.getMinimum();
        gainControl.setValue(gain);
    }
}
