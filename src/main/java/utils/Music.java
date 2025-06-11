package utils;

import java.net.URL;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public final class Music {
    private static final float DEFAULT_VOLUME = 0.5f;
    private static float currentVolume = DEFAULT_VOLUME;
    private static float effectsVolume = DEFAULT_VOLUME;

    /** Clip de musique/son. */
    private static Clip bgMusicClip = null;
    private static Clip startBgm = null;
    private static long pausePosition;

    private Music() {
    }

    /**
     * Définit le volume des effets sonores.
     * @param volume Volume en tant que valeur de type float
     */
    public static void setEffectsVolume(final float volume) {
        if (volume < 0f || volume > 1f) {
            throw new IllegalArgumentException("Volume value has to be between 0 and 1");
        }
        effectsVolume = volume;
    }

    /**
     * Définit le volume de la musique de fond.
     * @param volume Volume en tant que valeur de type float
     */
    public static void setBgMusicVolume(final float volume) {
        if (volume < 0f || volume > 1f) {
            throw new IllegalArgumentException("Volume value has to be between 0 and 1");
        }
        currentVolume = volume;
        if (isBgmPlaying()) {
            setClipVolume(bgMusicClip, currentVolume);
        }
    }

    /**
     * Définit le volume d'un Clip.
     * @param clip Clip audio
     * @param volume Volume en tant que valeur de type float
     */
    private static void setClipVolume(final Clip clip, final float volume) {
        if (clip != null) {
            FloatControl volumeControl =
                (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            final float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            volumeControl.setValue(dB);
        }
    }

    /**
     * Joue un effet sonore une fois.
     * @param soundFileName Nom du fichier dans le répertoire "resources"
     */
    public static void playSound(final String soundFileName) {
        try {
            Clip sound = AudioSystem.getClip();
            sound.open(AudioSystem.getAudioInputStream(
                new URL(Resources.getPathOrContent(soundFileName))
            ));

            if (soundFileName.equals("start")) {
                if (isStartBgmPlaying()) {
                    startBgm.close();
                }
                startBgm = sound;
            }

            setClipVolume(sound, effectsVolume);

            LineListener listener = new LineListener() {
                @Override
                public void update(final LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        sound.removeLineListener(this);
                        sound.close();
                    }
                }
            };
            sound.addLineListener(listener);
            sound.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Joue la musique de fond en boucle.
     * @param soundFileName Nom du fichier dans le répertoire "resources"
     */
    public static void playLoopingBgMusic(final String soundFileName) {
        stopBackgroundMusic();
        if (isStartBgmPlaying()) {
            startBgm.close();
        }
        try {
            bgMusicClip = AudioSystem.getClip();
            bgMusicClip.open(AudioSystem.getAudioInputStream(
                new URL(Resources.getPathOrContent(soundFileName))
            ));

            setClipVolume(bgMusicClip, currentVolume);

            bgMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Arrête la musique de fond actuelle. */
    public static void stopBackgroundMusic() {
        if (bgMusicClip != null && bgMusicClip.isOpen()) {
            bgMusicClip.close();
        }
    }

    /**
     * Met en pause la lecture de la musique.
     */
    public static void pause() {
        if (isBgmPlaying()) {
            pausePosition = bgMusicClip.getMicrosecondPosition();
            bgMusicClip.stop();
        }
    }

    /**
     * Reprend la lecture de la musique.
     */
    public static void resume() {
        if (bgMusicClip != null && !isBgmPlaying()) {
            bgMusicClip.setMicrosecondPosition(pausePosition);
            bgMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static float getBgMusicVolume() {
        return currentVolume;
    }

    public static float getEffectsVolume() {
        return effectsVolume;
    }

    public static void setBgMusicClip(final Clip bgMusicClip) {
        Music.bgMusicClip = bgMusicClip;
    }

    public static boolean isBgmPlaying() {
        return bgMusicClip != null && bgMusicClip.isActive();
    }

    public static boolean isStartBgmPlaying() {
        return startBgm != null && startBgm.isActive();
    }

    public static Clip getStartBgm() {
        return startBgm;
    }
}
