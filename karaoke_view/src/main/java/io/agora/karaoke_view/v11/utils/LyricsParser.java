package io.agora.karaoke_view.v11.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.agora.karaoke_view.v11.constants.Constants;
import io.agora.karaoke_view.v11.internal.PitchesModel;
import io.agora.karaoke_view.v11.model.KrcPitchData;
import io.agora.karaoke_view.v11.model.KrcPitchModel;
import io.agora.karaoke_view.v11.model.LyricsLineModel;
import io.agora.karaoke_view.v11.model.LyricsModel;
import io.agora.logging.LogManager;

/**
 * 加载歌词
 *
 * @author chenhengfei(Aslanchen)
 * @date 2021/7/6
 */
public class LyricsParser {
    private static final String TAG = Constants.TAG + "-LyricsParser";

    private static void checkParameters(File file) {
        if (file == null || !file.isFile() || !file.exists() || !file.canRead() || file.length() == 0) {
            StringBuilder builder = new StringBuilder("Not a valid file for parser: " + file);
            if (file != null) {
                builder.append("\n");
                builder.append("{isFile: " + file.isFile() + ", ");
                builder.append("exists: " + file.exists() + ", ");
                builder.append("canRead: " + file.canRead() + ", ");
                builder.append("length: " + file.length() + "}");
            }
            throw new IllegalArgumentException(builder.toString());
        }
    }

    @Nullable
    public static LyricsModel parse(@NonNull File lyrics) {
        checkParameters(lyrics);
        return doParse(null, lyrics, null);
    }

    //This interface is unstable and is not recommended for use
    @Nullable
    public static LyricsModel parse(@NonNull File lyrics, @Nullable File pitches) {
        checkParameters(lyrics);
        return doParse(null, lyrics, pitches);
    }

    @Nullable
    public static LyricsModel parse(@NonNull byte[] lyricsData) {
        return doParse(null, lyricsData, null);
    }

    @Nullable
    public static LyricsModel parse(@NonNull byte[] lyricsData, @Nullable byte[] pitchesData) {
        return doParse(null, lyricsData, pitchesData);
    }

    @Nullable
    public static LyricsModel parse(@NonNull LyricsModel.Type type, @NonNull File file) {
        checkParameters(file);
        return doParse(type, file, null);
    }

    @Nullable
    private static LyricsModel doParse(LyricsModel.Type type, File lyrics, File pitches) {
        type = probeLyricsFileType(type, lyrics);

        PitchesModel pitchesModel = null;
        if (pitches != null) {
            pitchesModel = PitchParser.doParse(pitches);
        }

        LyricsModel model;

        if (type == LyricsModel.Type.General) {
            model = LyricsParserGeneral.parseLrc(lyrics);
            if (model == null) {
                return null;
            }

            // Replace tones and set the pitch value
            // Each tone lasts for 100ms
            for (int i = 0; i < model.lines.size() - 1; i++) {
                LyricsLineModel line = model.lines.get(i);
                long start = line.tones.get(0).begin;
                long end = line.tones.get(0).end;
                String words = line.tones.get(0).word;
                LyricsLineModel.Lang lang = line.tones.get(0).lang;

                // Change the end time of first tone
                line.tones.get(0).end = start + (100 - 1);
                line.tones.get(0).pitch = (int) PitchParser.fetchPitchWithRange(pitchesModel, model.startOfVerse, line.tones.get(0).begin, line.tones.get(0).end);

                int numberOfTones = (int) (end - start) / 100;
                // Figure out how many tones need to be added and do it
                for (int j = 1; j < numberOfTones; j++) {
                    LyricsLineModel.Tone tone = new LyricsLineModel.Tone();
                    tone.begin = start + 100 * j;
                    tone.end = tone.begin + (100 - 1);
                    tone.pitch = (int) PitchParser.fetchPitchWithRange(pitchesModel, model.startOfVerse, tone.begin, tone.end);
                    line.tones.add(tone);
                }
            }
        } else if (type == LyricsModel.Type.Xml) {
            model = LyricsParserXml.parseLrc(lyrics);
            if (model == null) {
                return null;
            }

            // Replace tones and set the pitch value
            // Each tone lasts for the specified time
            for (int i = 0; i < model.lines.size() - 1; i++) {
                LyricsLineModel cur = model.lines.get(i);

            }
        } else {
            LogManager.instance().error(TAG, "Do not support the lyrics file type " + type);
            return null;
        }
        return model;
    }

    @Nullable
    private static LyricsModel doParse(LyricsModel.Type type, byte[] lyrics, byte[] pitches) {
        type = probeLyricsFileType(type, lyrics);

        PitchesModel pitchesModel = null;
        if (pitches != null) {
            pitchesModel = PitchParser.doParse(pitches);
        }

        LyricsModel model;

        if (type == LyricsModel.Type.General) {
            model = LyricsParserGeneral.parseLrc(lyrics);
            if (model == null) {
                return null;
            }

            // Replace tones and set the pitch value
            // Each tone lasts for 100ms
            for (int i = 0; i < model.lines.size() - 1; i++) {
                LyricsLineModel line = model.lines.get(i);
                long start = line.tones.get(0).begin;
                long end = line.tones.get(0).end;
                String words = line.tones.get(0).word;
                LyricsLineModel.Lang lang = line.tones.get(0).lang;

                // Change the end time of first tone
                line.tones.get(0).end = start + (100 - 1);
                line.tones.get(0).pitch = (int) PitchParser.fetchPitchWithRange(pitchesModel, model.startOfVerse, line.tones.get(0).begin, line.tones.get(0).end);

                int numberOfTones = (int) (end - start) / 100;
                // Figure out how many tones need to be added and do it
                for (int j = 1; j < numberOfTones; j++) {
                    LyricsLineModel.Tone tone = new LyricsLineModel.Tone();
                    tone.begin = start + 100 * j;
                    tone.end = tone.begin + (100 - 1);
                    tone.pitch = (int) PitchParser.fetchPitchWithRange(pitchesModel, model.startOfVerse, tone.begin, tone.end);
                    line.tones.add(tone);
                }
            }
        } else if (type == LyricsModel.Type.Xml) {
            model = LyricsParserXml.parseLrc(lyrics);
            if (model == null) {
                return null;
            }

            // Replace tones and set the pitch value
            // Each tone lasts for the specified time
            for (int i = 0; i < model.lines.size() - 1; i++) {
                LyricsLineModel cur = model.lines.get(i);

            }
        } else {
            LogManager.instance().error(TAG, "Do not support the lyrics file type " + type);
            return null;
        }
        return model;
    }

    private static LyricsModel.Type probeLyricsFileType(LyricsModel.Type type, File file) {
        if (type == null) {
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new FileInputStream(file);
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                if (line != null && (line.contains("xml") || line.contains("<song>"))) {
                    type = LyricsModel.Type.Xml;
                } else {
                    type = LyricsModel.Type.General;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return type;
    }

    private static LyricsModel.Type probeLyricsFileType(LyricsModel.Type type, byte[] fileData) {
        if (type == null) {
            try {
                String fileContent = new String(fileData);
                String[] lines = fileContent.split("\n");
                if (lines.length > 0) {
                    String firstLine = lines[0];
                    if (!TextUtils.isEmpty(firstLine)) {
                        if (firstLine.contains("xml") || firstLine.contains("<song>")) {
                            type = LyricsModel.Type.Xml;
                        } else {
                            type = LyricsModel.Type.General;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return type;
    }

    public static LyricsModel parseLyricFile(File krcFile, File pitchFile) {
        return parseLyricFile(krcFile, pitchFile, true);
    }

    public static LyricsModel parseLyricFile(File krcFile, File pitchFile, Boolean includeCopyrightSentence) {
        LyricsModel lyricsModel = KRCParser.doParse(krcFile);
        KrcPitchModel pitchModel = PitchParser.doPitchParse(pitchFile);
        lyricsModel.pitchDatas = pitchModel.pitchDatas;
        lyricsModel.hasPitch = pitchModel.pitchDatas != null && !pitchModel.pitchDatas.isEmpty();
        if (lyricsModel.hasPitch) {
            lyricsModel.startOfVerse = lyricsModel.pitchDatas.get(0).startTime;
        }
        // 移除版权信息类型的句子
        if (!includeCopyrightSentence && lyricsModel.lines != null && !lyricsModel.lines.isEmpty()) {
            List<LyricsLineModel> tempLines = lyricsModel.lines;
            List<LyricsLineModel> newLines = new ArrayList<>();
            for (int i = 0; i < tempLines.size(); i++) {
                LyricsLineModel lineModel = tempLines.get(i);
                if (lineModel.getStartTime() > lyricsModel.startOfVerse) {
                    newLines.add(lineModel);
                }
            }
            lyricsModel.lines = newLines;
        }
        return lyricsModel;
    }

    public static LyricsModel parseLyricData(byte[] krcData, byte[] pitchData) {
        return parseLyricData(krcData, pitchData, true);
    }

    public static LyricsModel parseLyricData(byte[] krcData, byte[] pitchData, Boolean includeCopyrightSentence) {
        LyricsModel lyricsModel = KRCParser.doParse(krcData);
        KrcPitchModel pitchModel = PitchParser.doPitchParse(pitchData);
        lyricsModel.pitchDatas = pitchModel.pitchDatas;
        lyricsModel.hasPitch = pitchModel.pitchDatas != null && !pitchModel.pitchDatas.isEmpty();
        if (lyricsModel.pitchDatas != null && !lyricsModel.pitchDatas.isEmpty()) {
            lyricsModel.startOfVerse = lyricsModel.pitchDatas.get(0).startTime;
        }
        return lyricsModel;
    }
}
