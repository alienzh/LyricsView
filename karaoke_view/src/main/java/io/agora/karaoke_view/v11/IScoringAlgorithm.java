package io.agora.karaoke_view.v11;

import java.util.LinkedHashMap;

import io.agora.karaoke_view.v11.model.LyricsLineModel;

public interface IScoringAlgorithm {
    /**
     * normalized score(0, 1) for the pitch
     *
     * @param currentPitch
     * @param currentRefPitch
     * @return
     */
    float getPitchScore(float currentPitch, float currentRefPitch);

    /**
     * score for the line just finished
     *
     * @param pitchesForLine
     * @param indexOfLineJustFinished
     * @param lineJustFinished
     * @return
     */
    int getLineScore(LinkedHashMap<Long, Float> pitchesForLine, final int indexOfLineJustFinished, final LyricsLineModel lineJustFinished);

    int getMaximumScoreForLine();

    void setScoringLevel(int level);

    void setScoringCompensationOffset(int offset);

    int getScoringLevel();

    int getScoringCompensationOffset();
}
