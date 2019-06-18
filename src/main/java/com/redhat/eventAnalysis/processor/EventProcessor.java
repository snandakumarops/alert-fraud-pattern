package com.redhat.eventAnalysis.processor;

import com.google.gson.Gson;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;


public class EventProcessor {


    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);


    /**
     * Applies the loaded Drools rules to a given String.
     *
     * @param
     * @return the String after the rule has been applied
     */


    public String processEvent(String key, String value) {

        String eventAnalysis = null;

        if(value.contains("\"eventCategory\": \"ATM_WITHDRAWAL\"")) {
            eventAnalysis = "ATMWTH";
        }

        if(null != eventAnalysis) {
            AbstractMap.SimpleEntry<String,String > keyValue = new AbstractMap.SimpleEntry(key, eventAnalysis);
            System.out.println("Gson map"+new Gson().toJson(keyValue));
            return new Gson().toJson(keyValue);

        }
        return null;
    }

    public String parse(Windowed<String> key, Long value) {
        AbstractMap.SimpleEntry<String,String > keyValue = new AbstractMap.SimpleEntry(key.key(), String.valueOf(value));
        return new Gson().toJson(keyValue);

    }



}
