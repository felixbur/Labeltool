package emo.recorder.gui;

import java.util.Properties;

import emo.recorder.Util;

public class LabelToolProperties {
    protected static Properties props;

    public LabelToolProperties() {
        props = new Properties();
        props.putAll(Util.getValuesFromFile("recorderProperties.txt"));
    }

}
