/**
 * Copyright 2016-2018 Universidad Politécnica de Madrid (UPM).
 *
 * Authors:
 *    José-Fernan Martínez Ortega
 *    Néstor Lucas Martínez
 *    Jesús Rodríguez Molina
 * 
 * This software is distributed under a dual-license scheme:
 *
 * - For academic uses: Licensed under GNU Affero General Public License as
 *                      published by the Free Software Foundation, either
 *                      version 3 of the License, or (at your option) any
 *                      later version.
 * 
 * - For any other use: Licensed under the Apache License, Version 2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * You can get a copy of the license terms in licences/LICENSE.
 * 
 */
package swarms.ThriftProxy;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.thrift.TException;

import com.swarms.thrift.Mission;
import com.swarms.thrift.MtrrService;
import java.util.logging.Level;

import swarms.MTRR.MTRR;

public class MtrrServiceHandler implements MtrrService.Iface {

    private Logger logger;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void requestUpdateStatus() throws TException {
        try {
            logger.info("[MtrrService] >>> Received new requestUpdatedStatus request");
            // Just a try for checking the release of the Thrift connection
            (new Thread() {
            	public void run() {
                    MTRR.getInstance().requestUpdatedStatus();            		
            	}
            }).start();
//            MTRR.getInstance().requestUpdatedStatus();
        } catch (Exception e) {
            logger.log(Level.WARNING, "### Exception requesting updated status to the MTRR {0}", e.getMessage());
            e.printStackTrace();
            throw new TException(e);
        }
    }    
    
    @Override
    public void sendPlan(Mission plan) throws TException {
        try {
            logger.info("[MtrrService] >>> Received new mission plan");
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
            FileOutputStream fos = new FileOutputStream("StoredMission-" + sdf.format(date));
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(plan);
            oos.close();
             
            MTRR.getInstance().startMission(plan);
        } catch (Exception e) {
            logger.log(Level.WARNING, "### Exception passing the plan to the MTRR {0}", e.getMessage());
            e.printStackTrace();
            throw new TException(e);
        }
    }

    @Override
    public String abortVehiclePlan(int vehicleId) {
        logger.log(Level.INFO, "[MtrrService] >>> Received ABORT VEHICLE PLAN for vehicle ID {0}", vehicleId);        
    	return MTRR.getInstance().abortVehiclePlan(vehicleId);
    }
    
    @Override
    public String abortMissionPlan(int missionId) {
        logger.log(Level.INFO, "[MtrrService] >>> Received ABORT MISSION PLAN for mission ID {0}", missionId);        
    	return MTRR.getInstance().abortMissionPlan(missionId);
    }
    
    @Override
    public String abortVehiclePlanHard(int vehicleId) {
        logger.log(Level.INFO, "[MtrrService] >>> Received ABORT VEHICLE PLAN (HARD) for vehicle ID {0}", vehicleId);        
    	return MTRR.getInstance().abortVehiclePlan(vehicleId);
    }
    
    @Override
    public String abortMissionPlanHard(int missionId) {
        logger.log(Level.INFO, "[MtrrService] >>> Received ABORT MISSION PLAN (HARD) for mission ID {0}", missionId);        
    	return MTRR.getInstance().abortMissionPlan(missionId);
    }

    
    @Override
    public String ping() throws TException {
        return "Response from MtrrServiceHandler";
    }

}
