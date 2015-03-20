/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.shenkers.model;

import com.google.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.EventSource;

/**
 *
 * @author sol
 */
public class CoordinateChangeEventInterceptor implements MethodInterceptor {

    private static final Logger logger = LogManager.getLogger ();
    
    private EventSource coordinateChangeEvents;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        logger.info("intercepting '{}'",invocation.getMethod().getName());
        Object result = invocation.proceed();
        coordinateChangeEvents.push("!");
        return result;
    }

    @Inject 
    public void setCoordinateChangeEvents(@CoordinateChange EventSource coordinateChangeEvents) {
        logger.info("injecting event source");
        this.coordinateChangeEvents = coordinateChangeEvents;
    }
   
}
