package com.redhat.depositretention.drools;

import com.CustomerHistory;
import com.Event;
import com.eventAnalysis;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.beans.decoder.StringElementHandler;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class DroolsRulesApplier {


    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsRulesApplier.class);


    /**
     * Applies the loaded Drools rules to a given String.
     *
     * @param
     * @return the String after the rule has been applied
     */


    public String processEvent(String key, String value) {

        KieSession kieSession = DroolsSessionFactory.createDroolsSession();

        //look up customer history
        CustomerHistory customerHistory = getCustomerHistory(new Gson().fromJson(key,String.class));

        kieSession.insert(customerHistory);

        Event event = new Gson().fromJson(value,Event.class);
        kieSession.insert(event);

        kieSession.fireAllRules();

        Collection<?> events = kieSession.getObjects(new ClassObjectFilter(com.eventAnalysis.class));

        eventAnalysis eventAnalysis = null;
        for(Object evnt:events) {
            eventAnalysis = (eventAnalysis) evnt;
        }


        if(null != eventAnalysis) {
            Gson jsonObj = new Gson();
            AbstractMap.SimpleEntry<String,String > keyValue = new AbstractMap.SimpleEntry(key, eventAnalysis);
            return new Gson().toJson(keyValue);
        }
        return null;
    }

    private CustomerHistory getCustomerHistory(String key) {


        CustomerHistory customerHistory = new CustomerHistory();
        switch (key) {
            case "John": customerHistory.setCustomerAttribute("OFFER_UPGRADE");
                          break;
            case "James": customerHistory.setCustomerAttribute("HIGH_BALANCE_DEBT");
                          break;
        }
        return customerHistory;

    }

}
