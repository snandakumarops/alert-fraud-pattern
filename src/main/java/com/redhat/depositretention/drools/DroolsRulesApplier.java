package com.redhat.depositretention.drools;

import com.google.gson.Gson;
import com.myspace.deposit_retention.Customer_Profile;
import com.myspace.deposit_retention.EventModel;
import com.myspace.deposit_retention.OFFER;

import com.redhat.depositretention.EventStreamModel;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;


public class DroolsRulesApplier {


    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsRulesApplier.class);


    /**
     * Applies the loaded Drools rules to a given String.
     *
     * @param
     * @return the String after the rule has been applied
     */


    public String processTransaction(String key, String value) {

        KieSession kieSession = DroolsSessionFactory.createDroolsSession("");

        EventStreamModel eventModel = new Gson().fromJson(value,EventStreamModel.class);
        //Graphql call to fetch customer context information
        Customer_Profile customer = new Customer_Profile();
        customer.setCcid(key);
        customer.setThirdPartyTransfer(true);
        customer.setInFlow(false);
        customer.setOutFlow(false);

        EventModel eventModel1 = new EventModel();
        eventModel1.setEVENT(eventModel.getEventId());
        eventModel1.setEVENT_DATE(eventModel.getEventDate());

        kieSession.insert(customer);
        kieSession.insert(eventModel1);
        kieSession.fireAllRules();

        Collection<?> offers = kieSession.getObjects(new ClassObjectFilter(OFFER.class));
        String offerId = "";
        OFFER offer = null;
        for(Object offerObj:offers) {
            offer = (OFFER) offerObj;

       }
        System.out.print(new Gson().toJson(offer));
       return new Gson().toJson(offer);



    }

    public void processTransactionDMN(String info) {

        DMNRuntime dmnRuntime = DroolsSessionFactory.createDMNRuntime("");
        String namespace = "https://github.com/kiegroup/drools/kie-dmn/_73328450-AA85-4769-8D87-AAA96AE9F8E7";
        String modelName = "Event";
        DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);


        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("Event", "CUSTOMER_LOGIN");
        dmnContext.set("Cash Inflow",true);
        dmnContext.set("Cash Outflow",false);
        dmnContext.set("Channel","DIGITAL");

        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        System.out.println("Results::"+dmnResult.getDecisionResults());






    }


}
