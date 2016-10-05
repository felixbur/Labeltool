package emo.recorder.gui;

import java.util.Properties;

import emo.recorder.Util;

public class RecorderProperties {
    protected static Properties props;

    public RecorderProperties() {
        props = new Properties();
        props.putAll(Util.getValuesFromFile("recorderProperties.txt"));
    }

}
