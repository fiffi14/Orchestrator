/* Copyright 2026 UnitedCloud. All rights reserved. */
package uc.orchestrator.constants;

public final class StringLiterals {

  private StringLiterals() {}

  public static final String TIMESTAMP = "timestamp";
  public static final String STATUS = "status";
  public static final String MESSAGE = "message";
  public static final String ERROR = "error";
  public static final String DEFAULT_FORMAT = "mpegts";
  public static final String JOB_NOT_FOUND = "Job not found: ";
  public static final String USER_HOME = "user.home";
  public static final String TMP_PATH =
      System.getProperty(USER_HOME) + "/Orchestrator/Downloads/VODs/";

  public static final String EXT_INF_STREAM_TAG = "#EXT-X-STREAM-INF";
  public static final String BANDWIDTH_TAG = "BANDWIDTH";
  public static final String FRAMERATE_TAG = "FRAME-RATE";
  public static final String RESOLUTION_TAG = "RESOLUTION";
  public static final String CODECS_TAG = "CODECS";

  public static final String EXTM3U_TAG = "#EXTM3U";

  public static final String EXTINF_TAG = "#EXTINF";
  public static final String EXT_VERSION_TAG = "#EXT-X-VERSION";
  public static final String EXT_PLAYLIST_TAG = "#EXT-X-PLAYLIST-TYPE";
  public static final String EXT_TARGET_DURATION_TAG = "#EXT-X-TARGETDURATION";
  public static final String EXT_MEDIA_SEQ_TAG = "#EXT-X-MEDIA-SEQUENCE";

  public static final String EXT_INDEPENTENT_FRAGS_TAG = "#EXT-X-INDEPENDENT-SEGMENTS";
  public static final String EXT_ENDLIST_TAG = "#EXT-X-ENDLIST";
}
