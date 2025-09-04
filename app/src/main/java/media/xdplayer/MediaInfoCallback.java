package media.xdplayer;

public interface MediaInfoCallback {

    boolean isPlaying();

    int getDuration();

    int getCurrentPosition();
}
