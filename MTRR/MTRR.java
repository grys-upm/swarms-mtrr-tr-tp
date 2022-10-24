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
package swarms.MTRR;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.thrift.TException;

import com.swarms.thrift.Action;
import com.swarms.thrift.Mission;
import com.swarms.thrift.Position;
import com.swarms.thrift.Region;
import com.swarms.thrift.TaskStatus;
import com.swarms.thrift.Vehicle;
import com.swarms.thrift.VehicleType;

import swarms.DAM.SemanticQuery;
import swarms.MTRR.util.MessageFormatter;
import swarms.MTRR.util.MissionParser;
import swarms.MTRR.util.StoredEvent;
import swarms.MTRR.util.StoredTask;
import swarms.MTRR.util.TaskHelper;
import swarms.MTRR.util.ThriftClientToMMT;
import swarms.PSManager.PSManagerCONSTANTS;
import swarms.PSManager.PSManagerGlobalVariables;
import swarms.PSManager.PSManagerInterfaces;
import swarms.ThriftProxy.ThriftProxyServer;
import swarms.environment_reporter.ProcessedEnvironmentalData;
import swarmsDDLs.SWARMsmsg;
import swarmsPDUs.basic.SWARMsDDSFrame;

/**
 * Mission and Tasks Register and Reporter (MTRR) for SWARMs
 *
 */
public class MTRR implements MTFeeder, MTReporter {
    public static final int NOT_STARTED = 0;
    public static final int RUNNING = 1;
    public static final int FINISHED = 2;
    public static final int STOPPED = 3;
    
    // Maps 
    private HashMap<Integer, Vehicle> vehicleMap = new HashMap<Integer, Vehicle>();
    private HashMap<Integer, LinkedList<Action>> vehiclePlansMap = new HashMap<Integer, LinkedList<Action>>();
    private HashMap<Integer, SWARMsDDSFrame> lastMessagePerVehicle = new HashMap<Integer, SWARMsDDSFrame>();
    private HashMap<Integer, Action> currentActionMap = new HashMap<Integer, Action>();
    private HashMap<Integer, Boolean> vehicleAvailability = new HashMap<Integer, Boolean>();
	private HashMap<Integer, Byte> stateVectorVehicleRequestIdMap = new HashMap<Integer, Byte>();
	private HashMap<Integer, Action> awaitingActions = new HashMap<Integer, Action>();
    
    // Lists
	private ArrayList<StoredEvent> receivedEvents = new ArrayList<StoredEvent>();
	private ArrayList<StoredTask> receivedTasks = new ArrayList<StoredTask>();
    
    
    // Booleans    
    private boolean missionActive = false;
    private boolean requestUpdate = false;
	private boolean notifyStatusToMMT = true;
    private boolean isCDTDiscoveryRequired = false;
	private boolean internalRequestUpdatedStatus = false;
    private boolean requestedUpdatedStatus = false;
    private boolean CDTavailable = false;
    private boolean doGetNeighbours = false;
    
    // Values
    private int currentMissionID;
    private double refCoordsLongitude = MessagesCONSTANTS.DEFAULT_REFERENCE_COORDINATE_LONGITUDE;
    private double refCoordsLatitude = MessagesCONSTANTS.DEFAULT_REFERENCE_COORDINATE_LATITUDE;

    private PSManagerInterfaces psManager;
   
    private byte requestID = 0;
    private byte assignmentMode = MessagesCONSTANTS.ASSIGNMENT_WAIT_TO_COMPLETE;

    private FileHandler fh;
    private FileHandler fhsci;
    private Logger logsci = Logger.getLogger("SCILog");
    private Logger logger = Logger.getLogger("MyMTRRLog");
    private ThriftProxyServer thriftProxy;
    
    public static MTRR instance = null;
    
    // new variables
    private long stateVectorTimeout = MessagesCONSTANTS.DEFAULT_STATE_VECTOR_TIMEOUT;
    private int stateVectorRefreshTimeIP = MessagesCONSTANTS.DEFAULT_ENVIRONMENT_STATE_VECTOR_REFRESH_TIME_IP;
    private int stateVectorRefreshTimeAcoustic = MessagesCONSTANTS.DEFAULT_ENVIRONMENT_STATE_VECTOR_REFRESH_TIME_ACOUSTIC;
    private long getNeighboursTimeout = MessagesCONSTANTS.DEFAULT_GET_NEIGHBOURS_TIMEOUT;
    private long setNeighboursTimeout = MessagesCONSTANTS.DEFAULT_SET_NEIGHBOURS_TIMEOUT;
    private int getNeighboursMaxTryouts = MessagesCONSTANTS.DEFAULT_GET_NEIGHBOURS_TOTAL_TRYOUTS;
    private DecimalFormat df = new DecimalFormat("#0.000000");
    private String neighbourDiscoveryStyle = MessagesCONSTANTS.DEFAULT_NEIGHBOUR_DISCOVERY_STYLE;
    
    /**
     * Constructor setting the PSManager available to this instance
     */
    private MTRR() {
        try {            
            SimpleFormatter formatter = new SimpleFormatter();

            fh = new FileHandler("MTRR.log", true);
            logger.addHandler(fh);
            fh.setFormatter(formatter);
            
            fhsci = new FileHandler("SCI.log", true);
            logsci.addHandler(fhsci);
            logsci.setUseParentHandlers(false);
            fhsci.setFormatter(new SimpleFormatter() {
                private static final String format = "%1$tF,%1$tT,%2$-7s,%3$s %n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                            new Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            });
            
            logger.log(Level.INFO, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            logger.log(Level.INFO, "STARTING MTRR ---------------------------------------");
            
            MessageFormatter.getInstance().setLogger(logger);
            
            // Load configuration
            loadConfiguration();

            // WARNING! The ThriftProxyServer should be started by swarms.Main!!!
            thriftProxy = new ThriftProxyServer();
            thriftProxy.startThriftProxy();

        } catch (SecurityException e) {
        	logger.log(Level.SEVERE, "!!! Security exception while creating the MTRR instance.");
        	logger.log(Level.SEVERE, e.getMessage());
        } catch (IOException e) {
        	logger.log(Level.SEVERE, "!!! I/O exception while creating the MTRR instance.");
        	logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public static MTRR getInstance() {
            if(instance == null)
                instance = new MTRR();
            
            return instance;	
    }

    public void setPSManager(PSManagerInterfaces psmanager) {
        this.psManager = psmanager;
        logger.log(Level.INFO, "--- Got the reference for the P/S Manager");
    }

    /**
     * Requests the status update for all available, and updates their availability upon the result.
     * 
     */
    public synchronized void requestUpdatedStatus() {
    	String methodName = "requestUpdatedStatus";
    	logsci.log(Level.INFO, methodName + ",entry," + System.currentTimeMillis());
    	requestedUpdatedStatus = true;
    	requestUpdate = true;
    	
    	// Reload configuration
    	loadConfiguration();
    	
    	// Get the list of vehicles that can be used in the mission planning
    	logsci.log(Level.INFO, methodName + ",call to SQ.getAllVehicles: before," + System.currentTimeMillis());
    	List<Vehicle> availableVehicles = SemanticQuery.getInstance().getAllVehicles();
    	logsci.log(Level.INFO, methodName + ",call to SQ.getAllVehicles: after," + System.currentTimeMillis());
    	stateVectorVehicleRequestIdMap.clear();

    	// Create availability map for the MTRR
    	vehicleAvailability.clear();
    	for (Vehicle vehicle : availableVehicles) {
    		vehicleAvailability.put(vehicle.getId(), false);
    	}
    	
    	// Request the STATE_VECTOR using the IP channel
    	for (Vehicle vehicle : availableVehicles) {    		
    		requestStateVectorIP(vehicle, requestID);    		
    		stateVectorVehicleRequestIdMap.put(vehicle.getId(), (byte) requestID);
    		requestID++;
    	}
    	
    	if (CDTavailable) {
    		if (neighbourDiscoveryStyle.toLowerCase().equals(MessagesCONSTANTS.NEIGHBOUR_DISCOVERY_STYLE_SLEEP)) {
    			logger.log(Level.INFO, "--- Performing neighbour discovery using sleeps");
    			sendSetNeighbours(availableVehicles);
    	    	
    	    	try {
    	    		Thread.sleep(getNeighboursTimeout);
    	    	} catch (InterruptedException e) {
    	    		logger.log(Level.WARNING, "+++ SET_NEIGHBOURS WAIT INTERRUPTED IN REQUEST UPDATED STATUS");
    	    	}    	
    	    	
    	    	sendGetNeighbours();
    	    	
    	    	try {
    	    		Thread.sleep(getNeighboursTimeout);
    	    	} catch (InterruptedException e) {
    	    		logger.log(Level.WARNING, "+++ GET_NEIGHBOURS WAIT INTERRUPTED IN REQUEST UPDATED STATUS");
    	    	}    	
    	    	
    	    	// Request the STATE_VECTOR using the ACOUSTIC channel
    	    	for (Vehicle vehicle : availableVehicles) {
    	    		if (vehicle.getType().equals(VehicleType.AUV)) {
    	   				requestStateVectorAcoustic(vehicle, stateVectorVehicleRequestIdMap.get(vehicle.getId()));
    	    		}
    	    	}
    	    	
    	    	// Wait for STATE_VECTOR response
    	    	try {
    				Thread.sleep(stateVectorTimeout);
    			} catch (InterruptedException e) {
    				 logger.log(Level.WARNING, "+++ STATE VECTOR WAIT INTERRUPTED IN REQUEST UPDATED STATUS");
    			}    	
    		}
    		else {
    			logger.log(Level.INFO, "--- Performing neighbour discovery awaiting for CDT responses");
    			// SET_NEIGHBOURS
    			// Uncomment these two lines to allow the requestUpdatedStatus method to ALLWAYS send the SET_NEIGHBOURS request 
//    			MTRRContext.getInstance().setSetNeighboursResponseSuccessful(false);

		    	logsci.log(Level.INFO, methodName + ",set_neighbours procedure: begin," + System.currentTimeMillis());
    			if (!MTRRContext.getInstance().isSetNeighboursResponseSuccessful()) {
    				sendSetNeighbours(availableVehicles);
    				while (!MTRRContext.getInstance().isSetNeighboursResponseSuccessful());
    			}
		    	logsci.log(Level.INFO, methodName + ",set_neighbours procedure: end," + System.currentTimeMillis());

    			if (doGetNeighbours) {
    				// GET_NEIGHBOURS
    				// Uncomment these two lines to allow the requestUpdatedStatus method to ALLWAYS send the GET_NEIGHBOURS request   	
//    				MTRRContext.getInstance().setGetNeighboursResponseReceived(false);
//    				MTRRContext.getInstance().setGetNeighboursResponseSuccessful(false);

    		    	logsci.log(Level.INFO, methodName + ",get_neighbours procedure: begin," + System.currentTimeMillis());
    				int getNeighboursTry = 0;
    				while ((!MTRRContext.getInstance().isGetNeighboursResponseSuccessful()) && (getNeighboursTry < getNeighboursMaxTryouts)) {
    					getNeighboursTry++;
    					sendGetNeighbours();
    					while (!MTRRContext.getInstance().isGetNeighboursResponseReceived());

    					if (MTRRContext.getInstance().isGetNeighboursResponseSuccessful()) {
    						logger.log(Level.INFO, "--- RequestUpdatedStatus -> Received successful GET_NEIGHBOURS response (result == 0)");
    					}
    					else {
    						logger.log(Level.INFO, "--- RequestUpdatedStatus -> Received unsuccessful GET_NEIGHBOURS response (result != 0)");    			
    					}
    				}

    				// START_DISCOVERY
    				if (!MTRRContext.getInstance().isGetNeighboursResponseSuccessful()) {    		
    					while (!MTRRContext.getInstance().isStart_discoveryResponseSuccessful()) {
    						sendStartDiscovery();
    						while (!MTRRContext.getInstance().isStart_discoveryResponseReceived());

    						if (MTRRContext.getInstance().isStart_discoveryResponseSuccessful()) {
    							logger.log(Level.INFO, "--- RequestUpdatedStatus -> Received successful START_DISCOVERY response (result == 0)");
    						}
    						else {
    							logger.log(Level.INFO, "--- RequestUpdatedStatus -> Received unsuccessful START_DISCOVERY response (result != 0)");    			
    						}        		    			
    					}
    				}
    		    	logsci.log(Level.INFO, methodName + ",get_neighbours procedure: end," + System.currentTimeMillis());
    			}
    			
    	    	// Request the STATE_VECTOR using the ACOUSTIC channel
    	    	for (Vehicle vehicle : availableVehicles) {
    	    		if (vehicle.getType().equals(VehicleType.AUV)) {
    	   				requestStateVectorAcoustic(vehicle, stateVectorVehicleRequestIdMap.get(vehicle.getId()));
    	    		}
    	    	}    	    	
    		}
    	}
    	    	    	
    	if (!internalRequestUpdatedStatus) {
    		ThriftClientToMMT.getInstance().sendUpdatedStatusNotification();
    	}
    	
    	requestUpdate = false;
    	logsci.log(Level.INFO, methodName + ",exit," + System.currentTimeMillis());
    }
    
	/**
     * This method will be called from the MMT to start a mission defined by the
     * globalMissionPlan
     *
     * @param globalMissionPlan
     * @return The missionID assigned to the mission
     * @throws InterruptedException
     */
    public void startMission(Mission globalMissionPlan) throws InterruptedException {
    	String methodName = "startMission";
    	logsci.log(Level.INFO, methodName + ",entry," + System.currentTimeMillis());
    	// Extra verification
    	if (globalMissionPlan == null) {
    		logger.log(Level.WARNING, "+++ Global mission plan is NULL... Ignoring start mission request");
        	logsci.log(Level.INFO, methodName + ",exit: mission is empty," + System.currentTimeMillis());
    		return;
    	}
    	
        logger.log(Level.INFO, "*** START MISSION: new mission plan received with ID {0}", globalMissionPlan.getMissionId());
        logger.log(Level.INFO, "--- The mission {0} has {1} actions", new Object[] {globalMissionPlan.getMissionId(), globalMissionPlan.getActions().size()});

        if (missionActive) {
        	ThriftClientToMMT.getInstance().sendError(1062, "Requested new mission " + globalMissionPlan.getMissionId() + " while mission " + currentMissionID + " is still running");

        	logger.log(Level.WARNING, "+++ Mission {0} requested while mission {1} still active", new Object[] {globalMissionPlan.getMissionId(), currentMissionID});
        	logger.log(Level.WARNING, "+++ Ignoring new mission request for mission {0}", globalMissionPlan.getMissionId());
        	       
        	// Exits the method without further processing of the new mission request
        	logsci.log(Level.INFO, methodName + ",exit: there is an active mission," + System.currentTimeMillis());
        	return;
        }

        currentMissionID = globalMissionPlan.getMissionId();
        
        if ((!globalMissionPlan.isSetActions()) || globalMissionPlan.getActions().isEmpty() ) {
            logger.log(Level.WARNING, "+++ Mission {0} plan is empty!", currentMissionID);
            
            // As the plan is empty, the method returns without further processing of the mission plan
        	logsci.log(Level.INFO, methodName + ",exit: mission plan is empty," + System.currentTimeMillis());            
            return;
        }     
        
        if ((!globalMissionPlan.isSetVehicles()) || globalMissionPlan.getVehicles().isEmpty() || globalMissionPlan.getVehiclesSize() == 0) {
            logger.log(Level.WARNING, "+++ Mission {0} vehicle list is empty!", currentMissionID);
            
            // As the plan is empty, the method returns without further processing of the mission plan
        	logsci.log(Level.INFO, methodName + ",exit: mission has no vehicles," + System.currentTimeMillis());
            return;        	
        }

        missionActive = true; 
        loadConfiguration();
    	logger.log(Level.INFO, "### LOGGING ACTIONS IN THE PLAN PROVIDED BY THE MMT");
    	logsci.log(Level.INFO, methodName + ",logging actions in the plan: begin," + System.currentTimeMillis());
        for (Action action : globalMissionPlan.getActions()) {
        	// Area
        	Region area = action.getArea();
        	int positionIndex = 0;
        	for (Position position : area.getArea()) {
            	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Area (Position {2}): Longitude = {3} | Latitude = {4} | Altitude = {5} | Depth = {6}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), positionIndex, df.format(position.getLongitude()), df.format(position.getLatitude()), position.getAltitude(), position.getDepth()});
            	positionIndex++;
        		
        	}
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Speed = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getSpeed()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Altitude = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getAltitude()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Range = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getRange()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Timelapse = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getTimeLapse()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Bearing (Pitch) = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getBearing().getPitch()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Bearing (Roll) = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getBearing().getRoll()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Bearing (Yaw) = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getBearing().getYaw()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Start Time = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getStartTime()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): End Time = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getEndTime()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Status = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getStatus()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Assigned Vehicle ID = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getAssignedVehicleId()});
        	logger.log(Level.INFO, "### --- ACTION {0} ({1}): Parent Action ID = {2}", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), action.getParentActionId()});
        }
    	logsci.log(Level.INFO, methodName + ",logging actions in the plan: end," + System.currentTimeMillis());
        MissionParser missionParser = new MissionParser(); 
        missionParser.setLogger(logger);
        MTRRContext.getInstance().currentPlanHasAUVs = missionParser.hasAUVs(globalMissionPlan);
        
        PSManagerGlobalVariables.missionID = currentMissionID;
        logger.log(Level.INFO, "--- Mission ID: {0}", PSManagerGlobalVariables.missionID);
                
        MessageFormatter.getInstance().setMission_origin_latitude(refCoordsLatitude);
        MessageFormatter.getInstance().setMission_origin_longitude(refCoordsLongitude);
        
        /////////////////////////////////////////////////////////////
        // STORE MISSION IN DATABASE
        /////////////////////////////////////////////////////////////
        try {
        	logsci.log(Level.INFO, methodName + ",calling SQ.storeRefCoords: before," + System.currentTimeMillis());
            SemanticQuery.getInstance().storeRefCoords(
                    PSManagerGlobalVariables.missionID , MessageFormatter.getInstance().getMission_origin_latitude(), MessageFormatter.getInstance().getMission_origin_longitude());
        	logsci.log(Level.INFO, methodName + ",calling SQ.storeRefCoords: after," + System.currentTimeMillis());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "!!! Exception while storing reference coordinates into the SQ");
            logger.log(Level.SEVERE, e.getMessage());
        }        
        
        /////////////////////////////////////////////////////////////
        // ACTIONS REQUIRED FOR STARTING A MISSION
        /////////////////////////////////////////////////////////////       
        // Store mission in the database
        try {
        	logsci.log(Level.INFO, methodName + ",calling SQ.storeMission: before," + System.currentTimeMillis());
            SemanticQuery.getInstance().storeMission(globalMissionPlan);
        	logsci.log(Level.INFO, methodName + ",calling SQ.storeMission: after," + System.currentTimeMillis());
        } 
        catch (Exception e) {
            logger.log(Level.SEVERE, "!!! Exception while storing the mission into the SQ");
            logger.log(Level.SEVERE, e.getMessage());
        }  
        
        //Get vehicles involved in mission   
    	logsci.log(Level.INFO, methodName + ",calling MP.parseVehiclesInMission: before," + System.currentTimeMillis());
        List<Vehicle> vehicleList = missionParser.parseVehiclesInMission(globalMissionPlan); 
    	logsci.log(Level.INFO, methodName + ",calling MP.parseVehiclesInMission: after," + System.currentTimeMillis());
        
        // Store assigned vehicles to the mission in the database
        try {
        	logsci.log(Level.INFO, methodName + ",calling SQ.storeAssignedVehicles: before," + System.currentTimeMillis());
            SemanticQuery.getInstance().storeAssignedVehicles(globalMissionPlan.missionId, vehicleList);
        	logsci.log(Level.INFO, methodName + ",calling SQ.storeAssignedVehicles: after," + System.currentTimeMillis());
        }
        catch (Exception e) {
        	logger.log(Level.SEVERE, "!!! Exception while storing the assigned vehicles into the SQ");
        	logger.log(Level.SEVERE, e.getMessage());
        } 
        
        vehicleMap.clear();
        vehiclePlansMap.clear();
        lastMessagePerVehicle.clear();
        awaitingActions.clear();
        receivedEvents.clear();
        receivedTasks.clear();
        
        // Check if a requestUpdatedStatus has been requested
        if (!requestedUpdatedStatus) {
        	logger.log(Level.INFO, "--- Requesting status update from startMission");
        	internalRequestUpdatedStatus = true;
        	requestUpdatedStatus();
        	internalRequestUpdatedStatus = false;
        }
                
        // SUBSCRIPTION TO EVENTS
    	logsci.log(Level.INFO, methodName + ",subscription to events procedure: begin," + System.currentTimeMillis());
        for (Vehicle vehicle : vehicleList) {
        	byte subscriptionRequestID = (byte) requestID++;
        	
        	requestEventsSubscription((byte) vehicle.getId(), (byte) subscriptionRequestID, PSManagerCONSTANTS.REQUEST_EVENTS_IP);
        	        	
        	if (vehicle.getType().equals(VehicleType.AUV)) {
        		if (!isCDTDiscoveryRequired || MTRRContext.getInstance().CDTready) {
                	requestEventsSubscription((byte) vehicle.getId(), (byte) subscriptionRequestID, PSManagerCONSTANTS.REQUEST_EVENTS_ACOUSTIC);
        		}
        	}
        }
    	logsci.log(Level.INFO, methodName + ",subscription to events procedure: end," + System.currentTimeMillis());
       

        // FIRST TASK ASSIGNMENT
        logger.log(Level.INFO, "--- Assignment of first task for each vehicle");
       
        // Task assignment per vehicle        
        if (assignmentMode == MessagesCONSTANTS.ASSIGNMENT_WAIT_TO_COMPLETE) {
        	// For each vehicle taking part in the mission
        	for (Vehicle vehicle : vehicleList) {        	
        		int vehicleID = vehicle.getId();         		

        		// Update the vehicles map (will be used by the reportTask method)
        		vehicleMap.put(vehicleID, vehicle);
        		logger.log(Level.INFO, "--- Adding vehicle {0} ({1}) to the internal MTRR vehicle map", new Object[] {vehicle.getName(), vehicleID});

        		// Get the filtered and sorted plan for each vehicle
        		vehiclePlansMap.put(vehicleID, missionParser.parseVehiclePlan(vehicle, globalMissionPlan));
        		
        		// EXTRA LOG FOR TESTS 20180605
        		logger.log(Level.INFO, "... Parsed plan for vehicle {0} ({1}) is as follows:", new Object[] {vehicle.getName(), vehicleID});
        		for (Action action : vehiclePlansMap.get(vehicleID)) {
        			logger.log(Level.INFO, "... --- Action {0} ({1}) starting at {2}", new Object[]{action.getRelatedTask().getDescription(), action.getActionId(), action.getStartTime()});
        		}

        		// Get the first task of the plan and remove it from the list
        		Action action = vehiclePlansMap.get(vehicleID).poll();
        		currentActionMap.put(vehicleID, action);

        		// And assign the first task
        		logger.log(Level.INFO, "--- Assigning task {0} ({1}) to vehicle {2} ({3})", new Object[]{action.getRelatedTask().getDescription(), action.getActionId(), vehicle.getName(), vehicle.getId()});
        		assignTask(action, vehicle);

        		// NOTE: Next tasks are sent by the reportTask method upon receiving a COMPLETED status
        	}          
        }
        else if (assignmentMode == MessagesCONSTANTS.ASSIGNMENT_FULL_SEQUENCE) {        	
        // Tasks assignment by start time order
        	logsci.log(Level.INFO, methodName + ",calling MP.parsePlan: before," + System.currentTimeMillis());
        	Mission filteredMission = missionParser.parsePlan(globalMissionPlan);
        	logsci.log(Level.INFO, methodName + ",calling MP.parsePlan: after," + System.currentTimeMillis());
        	logsci.log(Level.INFO, methodName + ",calling MP.getVehiclesMap: before," + System.currentTimeMillis());
        	HashMap<Integer, Vehicle> vehiclesMap = missionParser.getVehiclesMap(globalMissionPlan);
        	logsci.log(Level.INFO, methodName + ",calling MP.getVehiclesMap: after," + System.currentTimeMillis());

        	logsci.log(Level.INFO, methodName + ",first task assignment procedure for all vehicles: before," + System.currentTimeMillis());
        	for (Action action : filteredMission.getActions()) {
        		Vehicle assignedVehicle = vehiclesMap.get(action.getAssignedVehicleId());
        		logger.log(Level.INFO, "--- Assigning task {0} ({1}) to vehicle {2} ({3})", new Object[]{action.getRelatedTask().getDescription(), action.getActionId(), assignedVehicle.getName(), assignedVehicle.getId()});
        		assignTask(action, assignedVehicle);
        	}
        	logsci.log(Level.INFO, methodName + ",first task assignment procedure for all vehicles: after," + System.currentTimeMillis());
        }   
    	logsci.log(Level.INFO, methodName + ",exit," + currentMissionID + "," + System.currentTimeMillis());
    }

    /**
     * This method will be called from the MMT to end a mission identified by
     * missionID
     *
     * @param missionID The mission to be ended
     */
    @Override
    public void endMission(int missionID, byte reason) {
    	String methodName = "endMission";
    	logsci.log(Level.INFO, methodName + ",entry," + missionID + "," + reason + "," + System.currentTimeMillis());
    	missionActive = false;
    	switch(reason) {
    	case MessagesCONSTANTS.END_REASON_FINISHED:
    		logger.log(Level.INFO, "*** LAST TASK IN MISSION HAS BEEN ASSIGNED ***");
    		break;
    	case MessagesCONSTANTS.END_REASON_ABORTED:
    		logger.log(Level.INFO, "*** MISSION ABORTED ***");
    		break;
    	default:
    		logger.log(Level.INFO, "*** MISSION END ***");
    	
    	}
    	logsci.log(Level.INFO, methodName + ",exit," + missionID + "," + "reason" + "," + System.currentTimeMillis());
    }

    /**
     * This method allows the MMT to enable the periodical report of
     * environmental data
     */
    @Override
    public void enablePeriodicEnvironmentalReport() {
        SWARMsmsg msg;
        SWARMsDDSFrame ddsFrame = new SWARMsDDSFrame();
        msg = ddsFrame;
        psManager.publish(msg, PSManagerCONSTANTS.REQUEST_ENVIRONMENT_ACOUSTIC);
    }

    /**
     * This method allows the MMT to disable the periodical report of
     * environmental data
     */
    @Override
    public void disablePeriodicEnvironmentalReport() {
        psManager.unpublish(PSManagerCONSTANTS.REQUEST_ENVIRONMENT_ACOUSTIC);
    }

    @Override
	public void reportEnvironment(ProcessedEnvironmentalData data, int missionId) {
    	String methodName = "reportEnvironment";
    	logsci.log(Level.INFO, methodName + ",entry," + missionId + "," + data.seOperation + "," + System.currentTimeMillis());
		if (requestUpdate) {    		
			vehicleAvailability.put((int) data.getVid(), true);
		}
		
		logger.info(" @@@@@@@@@@@	   STORE REPORT in DDBB and ONTOLOGY ");
	    try {
	    	logsci.log(Level.INFO, methodName + ",calling SQ.storeStateVectorInDB: before," + System.currentTimeMillis());
	        SemanticQuery.getInstance().storeStateVectorInDB(missionId, data.getVid(), data.getLatitude(), data.getLongitude(), data.getAltitude(), data.getDepth(), 
	        		data.getPitch(), data.getRoll(), data.getYaw(), data.getSpeed(), data.getRemaining_battery(), data.getTimems(), data.getResult());
	    	logsci.log(Level.INFO, methodName + ",calling SQ.storeStateVectorInDB: after," + System.currentTimeMillis());

	    	logsci.log(Level.INFO, methodName + ",calling SQ.storeStateVector: before," + System.currentTimeMillis());
	        SemanticQuery.getInstance().storeStateVector(data.getVid(), data.getLatitude(), data.getLongitude(), data.getAltitude(), data.getDepth(), 
			data.getPitch(), data.getRoll(), data.getYaw(), data.getSpeed(), data.getRemaining_battery(), data.getTimems());
	    	logsci.log(Level.INFO, methodName + ",calling SQ.storeStateVector: after," + System.currentTimeMillis());

	        // If the concentration measurement is not zero
	        if (data.getConcentration() != 0) {
	        	logger.log(Level.INFO, ">>> REPORT ENVIRONMENT: Received environment report with salinity concentration value of {0}", data.getConcentration());
	        	logsci.log(Level.INFO, methodName + ",calling SQ.storeSalinity: before," + System.currentTimeMillis());
	        	SemanticQuery.getInstance().storeSalinity(missionId,
	        			data.getVid(),
	        			data.getLatitude(),
	        			data.getLongitude(),
	        			data.getDepth(),
	        			data.getAltitude(),
	        			data.getConcentration(),
	        			data.getTimems());
	        	logsci.log(Level.INFO, methodName + ",calling SQ.storeSalinty: after," + System.currentTimeMillis());
	        }
	
	    } catch (Exception e) {
	        logger.log(Level.SEVERE, "MTRR: Report Environment Exception: {0}", e.getMessage());            
	    }
    	logsci.log(Level.INFO, methodName + ",exit," + missionId + "," + data.seOperation + "," + System.currentTimeMillis());
	}

	@Override
    public void reportEvent(SWARMsDDSFrame data, int missionId) {
		String methodName = "reportEvent";
    	logsci.log(Level.INFO, methodName + ",entry," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    	logger.log(Level.INFO, "@@@ EVENT REPORT > MTRR");
    	
    	StoredEvent event = new StoredEvent(missionId, data.vid, data.subtype, data.seqoperation);
    	    	    	
    	if (receivedEvents.contains(event)) { 
    		logger.log(Level.INFO, "--- Duplicated event report {0} ({1}: {2}) for vehicle {3} ({4})", new Object[] {data.event_description, data.subtype, data.eventID, vehicleMap.get(data.vid).getName(), data.vid});
    	}
    	else {
    		receivedEvents.add(event);
    		try {
    	    	logsci.log(Level.INFO, methodName + ",calling SQ.storeEvent: before," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    			SemanticQuery.getInstance().storeEvent(data.vid,
    					missionId,
    					data.subtype,
    					data.seqoperation,
    					data.data_epoch_time,
    					data.id_error,
    					data.event_description,
    					data.eventID);
    	    	logsci.log(Level.INFO, methodName + ",calling SQ.storeEvent: after," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    		} catch (TException e) {
    			logger.log(Level.WARNING, "There was an exception while trying to store an event report from {0}: {1}", new Object[] {data.vid, data.event_description});
    			logger.log(Level.WARNING, "Exception received: {0}", e.getMessage());
    		}

//    		if (notifyStatusToMMT) {
//    	    	logsci.log(Level.INFO, methodName + ",calling CTM.sendError: before," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
//    			ThriftClientToMMT.getInstance().sendError(data.id_error, data.event_description);
//    	    	logsci.log(Level.INFO, methodName + ",calling CTM.sendError: after," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
//    		}
    	}
    	logsci.log(Level.INFO, methodName + ",exit," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());		
    }
    
    @Override
    public synchronized void reportTask(SWARMsDDSFrame data, int missionId) {
    	String methodName = "reportTask";
    	logsci.log(Level.INFO, methodName + ",entry," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    	//Information provided:vehicle id, type, subtype, sequence operation, data 
    	logger.log(Level.INFO, "@@@ RECEIVED TASK REPORT");
    	
    	if (!missionActive) {
    		logger.log(Level.INFO, "--- Received a task status report with no active mission running");
        	logsci.log(Level.INFO, methodName + ",exit: no active mission running," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    		return;
    	}
    	
    	if (currentMissionID != missionId) {
    		logger.log(Level.INFO, "--- Received a task status report for mission ID {0} while running mission ID {1}", new Object[] {missionId, currentMissionID});
        	logsci.log(Level.INFO, methodName + ",exit: report for different mission ID," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    		return;
    	}
    		
    	byte vehicleID = data.vid;
		byte taskID = data.subtype;
		int status = data.id_error;
		String vehicleName;
		vehicleName = SemanticQuery.getInstance().getVehicle(vehicleID).getName();
		String taskName = TaskHelper.getTaskName(taskID);
		String statusName = TaskHelper.getStatusName(status);
    	logger.log(Level.INFO, "--- TASK REPORT > Vehicle: {0} ({1}) | Task: ({2}) {3} | Status: ({4}) {5} | SeqOp: {6}", new Object[] {vehicleName, vehicleID, taskID, taskName, status, statusName, data.sequenceOperation});
    	logsci.log(Level.INFO, methodName + ",entry_point," + System.currentTimeMillis() + "," + vehicleName + "," + vehicleID + "," + taskID + "," + taskName + "," + status + "," + statusName + "," + data.sequenceOperation);
    	
    	StoredTask task = new StoredTask(missionId, data.vid, data.subtype, data.sequenceOperation, data.id_error);
    	
    	if (receivedTasks.contains(task)) {
    		logger.log(Level.INFO, "--- Duplicated task report {0} ({1}: {2}) for vehicle {3} ({4}) [seqOp: {5}]", new Object[] {data.task_description, data.subtype, data.id_error, vehicleName, data.vid, data.sequenceOperation});
        	logsci.log(Level.INFO, methodName + ",exit: duplicated task report," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());
        	return;
    	}

    	receivedTasks.add(task);
    	try 
    	{    			
    		// STEP 0.0: Store the report into the ontology
    		logsci.log(Level.INFO, methodName + ",calling SQ.storeTaskReport: begin," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    		SemanticQuery.getInstance().storeTaskReport(data.vid, missionId, data.subtype, data.sequenceOperation, data.id_error, data.data_epoch_time);
    		logsci.log(Level.INFO, methodName + ",calling SQ.storeTaskReport: end," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	

    		// STEP 0.1: Send the report to the MMT
    		boolean currentActionMapError = false;
    		boolean supportedMMTStatus = true;
    		boolean statusError = false;
    		int errorId = data.id_error;
    		String errorMessage = "";
    		Action action = new Action();

    		if (currentActionMap.isEmpty()) {
    			logger.log(Level.SEVERE, "### Unexpected task report: there are no active/pending tasks");
    			currentActionMapError = true;
    		}
    		else if (!currentActionMap.containsKey((int) data.vid)) {
    			logger.log(Level.SEVERE, "### Unexpected task report for vehicle {0} ({1}): vehicle has no active/pending tasks", new Object[] {vehicleName, vehicleID});
    			currentActionMapError = true;
    		}
    		else {
    			action = currentActionMap.get((int) data.vid);

    			switch(data.id_error) {
    			case MessagesCONSTANTS.TASK_REPORT_CODE_PENDING:
    				action.setStatus(TaskStatus.NotStarted);
    				break;
    			case MessagesCONSTANTS.TASK_REPORT_CODE_RUNNING:
    				action.setStatus(TaskStatus.Running);
    				break;
    			case MessagesCONSTANTS.TASK_REPORT_CODE_COMPLETED:
    				action.setStatus(TaskStatus.Finished);
    				break;
    			case MessagesCONSTANTS.TASK_REPORT_CODE_EXEC_FAILED:
    				statusError = true;
    				errorMessage = "Execution failed";
    				break;
    			case MessagesCONSTANTS.TASK_REPORT_CODE_PLAN_FAILED:
    				statusError = true;
    				errorMessage = "Plan Failed";
    				break;
    			case MessagesCONSTANTS.TASK_REPORT_CODE_ABORTED:
    				statusError = true;
    				errorMessage = "Aborted";
    				awaitingActions.remove((int)action.getActionId());
    				//    					vehiclePlansMap.get((int)data.vid).clear();
    				break;
    			case MessagesCONSTANTS.TASK_REPORT_CODE_CANCELLED:
    				statusError = true;
    				errorMessage = "Cancelled";
    				break;
    			case MessagesCONSTANTS.TASK_REPORT_CODE_REJECTED:
    				statusError = true;
    				errorMessage = "Rejected";
    				break;    			
    			default:
    				supportedMMTStatus = false;
    				break;
    			}
    		}

    		// STEP 1: EXTRA TEST
    		if (lastMessagePerVehicle.isEmpty() || !lastMessagePerVehicle.containsKey((int) data.vid)) {
    			logger.log(Level.WARNING, "There is no previous message sent stored for vehicle {0}", (int) data.vid);
    		}
    		else {
    			SWARMsDDSFrame lastMessageSentToVehicle = lastMessagePerVehicle.get((int) data.vid);

    			if (lastMessageSentToVehicle.subtype != data.subtype) {
    				//    					statusError = true;
    				//    					errorId = 400;
    				//    					errorMessage = "Received status report from old task, old mission";
    				supportedMMTStatus = false;
    				logger.log(Level.INFO, "--- Subtype mismatch {0} {1})", new Object[]{lastMessageSentToVehicle.subtype, data.subtype});
    				return;
    			}
    		}

    		if (notifyStatusToMMT) {
    			if (currentActionMapError) {
    				logger.log(Level.WARNING, "### Unexpected task report from non existing task in active mission: errorId={0}", errorId);
//    				logsci.log(Level.INFO, methodName + ",calling CTM.sendError: before," + missionId + "," + data.sequenceOperation + "," + errorId + ","+ System.currentTimeMillis());	
//    				ThriftClientToMMT.getInstance().sendError(errorId, "Unexpected task report for non existing task");   
//    				logsci.log(Level.INFO, methodName + ",calling CTM.sendError: after," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    			}
    			else {
    				if (!statusError) {        				
    					if (supportedMMTStatus) {
    						logger.log(Level.INFO, "--- Sending task status report to MMT for action {0} ({1}): {2} ({3})", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), TaskHelper.getStatusName(errorId), errorId});
    						logsci.log(Level.INFO, methodName + ",calling CTM.sendStatusReport: before," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    						ThriftClientToMMT.getInstance().sendStatusReport(action);
    						logsci.log(Level.INFO, methodName + ",calling CTM.sendStatusReport: after," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());
    					} 
    				}
    				else {
    					logger.log(Level.INFO, "### Sending error from task status report: {0}: {1}", new Object[] {errorId, errorMessage});
//    					logsci.log(Level.INFO, methodName + ",calling CTM.sendError: before," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
//    					ThriftClientToMMT.getInstance().sendError(errorId, errorMessage);
//    					logsci.log(Level.INFO, methodName + ",calling CTM.sendError: after," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    				}
    			}
    		}

    		// STEP 2: If the assignment mode is WAIT_TO_COMPLETE 
    		//         AND the status is COMPLETED
    		//         AND the vehicle plan has pending tasks, send the next one to the vehicle
    		if (assignmentMode == MessagesCONSTANTS.ASSIGNMENT_WAIT_TO_COMPLETE) {
    			if (data.id_error == MessagesCONSTANTS.TASK_REPORT_CODE_COMPLETED) {
    				logsci.log(Level.INFO, methodName + ",next task assignment procedure: begin," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    				logger.log(Level.INFO, "--- Removing action {1} ({0}) from awaiting list", new Object[] {action.getRelatedTask().getDescription(), action.getActionId()});
    				awaitingActions.remove((int) action.getActionId());        			

    				if (!vehiclePlansMap.get((int) data.vid).isEmpty()) {
    					action = vehiclePlansMap.get((int) data.vid).poll();
    					Vehicle vehicle = vehicleMap.get((int) data.vid);
    					currentActionMap.put((int) data.vid, action);

    					logger.log(Level.INFO, "--- Assigning task {0} ({1}) to vehicle {2} ({3})", new Object[]{action.getRelatedTask().getDescription(), action.getActionId(), vehicle.getName(), vehicle.getId()});
    					assignTask(action, vehicle);
    				}
    				else {
    					if (awaitingActions.isEmpty()) {
    						logger.log(Level.INFO, "--- There are no more pending tasks in the mission.");
    						logger.log(Level.INFO, "--- Ending mission.");         					
    						endMission(currentMissionID, MessagesCONSTANTS.END_REASON_FINISHED);
    					}
    					else {
    						for (int id : awaitingActions.keySet()) {
    							logger.log(Level.INFO, "--- Task {1} ({0}) still in waiting list (awaiting for COMPLETED status)", new Object[] {awaitingActions.get(id).getRelatedTask().getDescription(), id});
    						}
    					}
    				}
    				logsci.log(Level.INFO, methodName + ",next task assignment procedure: end," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    			}
    		}

    	} catch (Exception e) {
    		logger.log(Level.SEVERE, "!!! Exception while processing a task report");
    		logger.log(Level.SEVERE, e.getMessage());
    	}
    	logsci.log(Level.INFO, methodName + ",exit," + missionId + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    }
    
    @Override
    public void reportCDT(SWARMsDDSFrame data, int missionId) {
    	String methodName = "reportCDT";
    	logsci.log(Level.INFO, methodName + ",entry," + missionId + "," + data.subtype + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    	logger.log(Level.INFO, "*** Received CDT report of subtype {0} ({1})", new Object[] {MessageFormatter.getInstance().getSubtypeName(data.type, data.subtype), data.subtype});
    	
    	switch(data.subtype) {
    	case MessagesCONSTANTS.SUBTYPE_START_DISCOVERY:
    		MTRRContext.getInstance().setStart_discoveryResponseReceived(true);
    		if (data.result == 0) {
    			MTRRContext.getInstance().CDTready = true;
    			MTRRContext.getInstance().setStart_discoveryResponseSuccessful(true);
    			logger.log(Level.INFO, "--- START_DISCOVERY response OK -> CDT is ready");
    		}
    		else {
    			MTRRContext.getInstance().setStart_discoveryResponseSuccessful(false);
    			logger.log(Level.INFO, "--- START_DISCOVERY response NOT OK: result != 0");
    		}
    		break;
    	case MessagesCONSTANTS.SUBTYPE_SET_NEIGHBOURS:
			MTRRContext.getInstance().setSetNeighboursResponseReceived(true);
    		if (data.result == 0) {
    			MTRRContext.getInstance().setSetNeighboursResponseSuccessful(true);
    			logger.log(Level.INFO, "--- SET_NEIGHBOURS response OK");
    		}
    		else {
    			MTRRContext.getInstance().setSetNeighboursResponseSuccessful(false);
    			logger.log(Level.INFO, "--- SET_NEIGHBOURS response NOT OK: result != 0");
    			sendSetNeighbours(SemanticQuery.getInstance().getAllVehicles());
    		}
    		break;
    	case MessagesCONSTANTS.SUBTYPE_GET_NEIGHBOURS:
    		MTRRContext.getInstance().setGetNeighboursResponseReceived(true);
    		if (data.result == 0) {
    			MTRRContext.getInstance().setGetNeighboursResponseSuccessful(true);
        		logger.log(Level.INFO, "--- GET_NEIGHBOURS response OK from VID {0}", data.vid);
    			
    		}
    		else {
    			MTRRContext.getInstance().setGetNeighboursResponseSuccessful(false);
    			logger.log(Level.INFO, "--- GET NEIGHBOURS response NOT OK from VID {0}: result != 0", data.vid);
    		}
    		break;
    	case MessagesCONSTANTS.SUBTYPE_STOP_POLLING:
    		logger.log(Level.INFO, "--- CDT reports STOP POLLING");
			sendSetNeighbours(SemanticQuery.getInstance().getAllVehicles());
    		break;
    	}
    	logsci.log(Level.INFO, methodName + ",exit," + missionId + "," + data.subtype + "," + data.sequenceOperation + "," + System.currentTimeMillis());	
    }
    
    public String abortVehiclePlan(int vehicleId) {
    	String methodName = "abortVehiclePlan";
    	logsci.log(Level.INFO, methodName + ",entry," + vehicleId + "," + System.currentTimeMillis());
    	
    	if (!vehicleMap.containsKey(vehicleId)) {
        	logsci.log(Level.INFO, methodName + ",exit: vehicle is not active in the mission," + vehicleId + System.currentTimeMillis());
    		return "NOK: Vehicle " + vehicleId + " is not active in the mission " + PSManagerGlobalVariables.missionID;
    	}
    	
		vehiclePlansMap.get(vehicleId).clear();
    	
    	// DDS Frame preparation
    	SWARMsmsg msg;
        SWARMsDDSFrame frame = new SWARMsDDSFrame();
        int[] data = new int[MessagesCONSTANTS.DATA_ARRAY_SIZE];
        Arrays.fill(data, 0);

    	frame.setType(MessagesCONSTANTS.TYPE_NOTIFICATION_MESSAGE);
    	frame.setVid((byte) vehicleId);
    	frame.setSubtype(MessagesCONSTANTS.NOTIFY_ABORT_PLAN);
    	frame.setSeqoperation((byte) requestID++);

    	frame.setDataInt(data);
    	msg = frame;

        logger.log(Level.INFO, "--- Notifying vehicle plan abort for vehicle {0} via IP link", vehicleId);
        
        this.psManager.publish(msg, PSManagerCONSTANTS.NOTIFY_IP + "_" + vehicleId);

        // And if the vehicle is not a ROV, also send it through the ACOUSTIC channel
        if (!vehicleMap.get(vehicleId).type.equals(VehicleType.ROV)) {
            logger.log(Level.INFO, "--- Notifying vehicle plan abort for vehicle {0} via acoustic link", vehicleId);
            this.psManager.publish(msg, PSManagerCONSTANTS.NOTIFY_ACOUSTIC + "_" + vehicleId);
        }
        
    	logsci.log(Level.INFO, methodName + ",exit," + vehicleId + System.currentTimeMillis());
    	return "OK";
    }
    
    public String abortMissionPlan(int missionId) {
    	String methodName = "abortMissionPlan";
    	logsci.log(Level.INFO, methodName + ",entry," + missionId + System.currentTimeMillis());
    	
    	if (missionId != PSManagerGlobalVariables.missionID) {
        	logsci.log(Level.INFO, methodName + ",exit: mission ID mismatch," + missionId + System.currentTimeMillis());
    		return "NOK: Specified mission ID " + missionId + " does not match current active mission ID " + PSManagerGlobalVariables.missionID;
    	}

    	if (vehicleMap.isEmpty()) {
    		logger.log(Level.WARNING, "+++ Aborting mission plan: There are no vehicles in the map");
    	}
    	
    	for (int vehicleId : vehicleMap.keySet()) {
    		logger.log(Level.INFO, "--- Aborting mission plan -> aborting vehicle plan for vehicle {0}", vehicleId);
    		abortVehiclePlan(vehicleId);

    		// Sleep added to prevent communication issues due to fast notification
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "+++ Sleep interrupted while aborting mission plan {0}", missionId);
			}     	            
    	}
    	
    	endMission(missionId, MessagesCONSTANTS.END_REASON_ABORTED);
    	
    	logsci.log(Level.INFO, methodName + ",exit," + missionId + System.currentTimeMillis());
        return "OK";
    }

    public String abortVehiclePlanHard(int vehicleId) {
    	String methodName = "abortVehiclePlanHard";
    	logsci.log(Level.INFO, methodName + ",entry," + vehicleId + System.currentTimeMillis());
    	
    	if (!vehicleMap.containsKey(vehicleId)) {
        	logsci.log(Level.INFO, methodName + ",exit: vehicle is not active in the mission," + vehicleId + System.currentTimeMillis());
    		return "NOK: Vehicle " + vehicleId + " is not active in the mission " + PSManagerGlobalVariables.missionID;
    	}
    	
		vehiclePlansMap.get(vehicleId).clear();
    	
    	// DDS Frame preparation
    	SWARMsmsg msg;
        SWARMsDDSFrame frame = new SWARMsDDSFrame();
        int[] data = new int[MessagesCONSTANTS.DATA_ARRAY_SIZE];
        Arrays.fill(data, 0);

    	frame.setType(MessagesCONSTANTS.TYPE_NOTIFICATION_MESSAGE);
    	frame.setVid((byte) vehicleId);
    	frame.setSubtype(MessagesCONSTANTS.NOTIFY_SAFETY_ACTION);
    	frame.setSeqoperation((byte) requestID++);

    	frame.setDataInt(data);
    	msg = frame;

        logger.log(Level.INFO, "--- Notifying vehicle plan abort for vehicle {0} via IP link", vehicleId);
        
        this.psManager.publish(msg, PSManagerCONSTANTS.NOTIFY_IP + "_" + vehicleId);

        // And if the vehicle is not a ROV, also send it through the ACOUSTIC channel
        if (!vehicleMap.get(vehicleId).type.equals(VehicleType.ROV)) {
            logger.log(Level.INFO, "--- Notifying vehicle plan abort for vehicle {0} via acoustic link", vehicleId);
            this.psManager.publish(msg, PSManagerCONSTANTS.NOTIFY_ACOUSTIC + "_" + vehicleId);
        }
        
    	logsci.log(Level.INFO, methodName + ",exit," + vehicleId + System.currentTimeMillis());
    	return "OK";
    }
    
    public String abortMissionPlanHard(int missionId) {
    	String methodName = "abortMissionPlanHard";
    	logsci.log(Level.INFO, methodName + ",entry," + missionId + System.currentTimeMillis());

    	if (missionId != PSManagerGlobalVariables.missionID) {
        	logsci.log(Level.INFO, methodName + ",exit: mission mismatch," + missionId + System.currentTimeMillis());
    		return "NOK: Specified mission ID " + missionId + " does not match current active mission ID " + PSManagerGlobalVariables.missionID;
    	}

    	if (vehicleMap.isEmpty()) {
    		logger.log(Level.WARNING,"+++ Aborting mission plan: There are no vehicles in the map");
    	}
    	
    	for (int vehicleId : vehicleMap.keySet()) {
    		logger.log(Level.INFO, "--- Aborting mission plan -> aborting vehicle plan for vehicle {0}", vehicleId);
    		abortVehiclePlanHard(vehicleId);

    		// Sleep added to prevent communication issues due to fast notification
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "--- Sleep interrupted while aborting mission plan {0}", missionId);
			}     	            
    	}
    	
    	endMission(missionId, MessagesCONSTANTS.END_REASON_ABORTED);
    	
    	logsci.log(Level.INFO, methodName + ",exit," + missionId + System.currentTimeMillis());    	
        return "OK";
    }
    
    public int getOngoingMissionID() {
    	if (missionActive) {
    		return currentMissionID;
    	}
    	else {
    		return -1;
    	}
    }
    
    private void sendSetNeighbours(List<Vehicle> availableVehicles) {
    	String methodName = "sendSetNeighbours";
    	logsci.log(Level.INFO, methodName + ",entry," + System.currentTimeMillis());

		MTRRContext.getInstance().setSetNeighboursResponseReceived(false);
		MTRRContext.getInstance().setSetNeighboursResponseSuccessful(false);
    	ArrayList<Integer> auvIDs = new ArrayList<Integer>();
    	
		for (Vehicle vehicle : availableVehicles) {
    		if (vehicle.getType().equals(VehicleType.AUV)) {
    			auvIDs.add(vehicle.getId());
    		}
		}
		
		SWARMsmsg msg = MessageFormatter.getInstance().getSetNeighboursMessage(auvIDs, requestID);
		
		logger.log(Level.INFO, "--- Sending SET_NEIGHBOURS with request id {0} and vehicle IDs: ", requestID);
		for (int vehicleID : auvIDs) {
			logger.log(Level.INFO, "------ VID: {0}", vehicleID);
		}
		psManager.publish(msg, MessagesCONSTANTS.TOPIC_SET_NEIGHBOURS_REQUEST);
		
		requestID++;
    	logsci.log(Level.INFO, methodName + ",exit," + System.currentTimeMillis());
	}
    
    private void sendGetNeighbours() {
    	String methodName = "sendGetNeighbours";
    	logsci.log(Level.INFO, methodName + ",entry," + System.currentTimeMillis());

    	MTRRContext.getInstance().setGetNeighboursResponseReceived(false);
    	MTRRContext.getInstance().setGetNeighboursResponseSuccessful(false);
		SWARMsmsg msg = MessageFormatter.getInstance().getGetNeighboursMessage(requestID);

		logger.log(Level.INFO, "--- Sending GET_NEIGHBOURS request id {0}", requestID);
    	psManager.publish(msg, MessagesCONSTANTS.TOPIC_GET_NEIGHBOURS_REQUEST);
    	
    	requestID++;
    	logsci.log(Level.INFO, methodName + ",exit," + System.currentTimeMillis());
    }

	private void sendStartDiscovery() {
		String methodName = "sendSartDiscovery";
    	logsci.log(Level.INFO, methodName + ",entry," + System.currentTimeMillis());

		MTRRContext.getInstance().setStart_discoveryResponseReceived(false);
		MTRRContext.getInstance().setStart_discoveryResponseSuccessful(false);
		SWARMsmsg msg = MessageFormatter.getInstance().getStartDiscoveryMessage(requestID);
		
		logger.log(Level.INFO, "--- Sending a START_DISCOVERY request id {0}", requestID);
		psManager.publish(msg, PSManagerCONSTANTS.REQUEST_CDT);

    	requestID++;
    	logsci.log(Level.INFO, methodName + ",exit," + System.currentTimeMillis());
	}
       
    /**
     * This method is internal to the class, and is used to assign each task in
     * the plan for each vehicle
     *
     * @param action
     */
    private void assignTask(Action action, Vehicle vehicle) { 
    	String methodName = "assignTask";
    	logsci.log(Level.INFO, methodName + ",entry," + vehicle.getId() + "," + action.actionId + "," + System.currentTimeMillis());

    	SWARMsmsg msg = MessageFormatter.getInstance().getSWARMsTaskMessage(action, requestID++);
    	lastMessagePerVehicle.put(vehicle.getId(), (SWARMsDDSFrame) msg);
    	awaitingActions.put(action.getActionId(), action);
    	
    	int actionID = action.getActionId();
    	int vehicleID = action.getAssignedVehicleId();
    	String actionName = action.getRelatedTask().getDescription();
    	String vehicleName = vehicleMap.get(vehicleID).getName();

    	logger.log(Level.INFO, "--- Sending task assignment {0} ({1}) to {2} ({3}) over IP channel", new Object[] {actionName, actionID, vehicleName, vehicleID});                
    	
    	this.psManager.publish(msg, PSManagerCONSTANTS.REQUEST_TASK_IP + "_" + vehicleID);

    	if (vehicle.type.equals(VehicleType.AUV)) {
    		if (!isCDTDiscoveryRequired || MTRRContext.getInstance().CDTready) {
    			logger.log(Level.INFO, "--- Sending task assignment {0} ({1}) to {2} ({3}) over Acoustic channel", new Object[] {actionName, actionID, vehicleName, vehicleID});                
    	    	logsci.log(Level.INFO, "::: assingTask {0} ({1}) to {2} ({3}) over acoustic channel - timestamp = {4}", new Object[] {actionName, actionID, vehicleName, vehicleID, System.currentTimeMillis()});
    			this.psManager.publish(msg, PSManagerCONSTANTS.REQUEST_TASK_ACOUSTIC + "_" + vehicleID);
    		}
    	}
    	logsci.log(Level.INFO, methodName + ",exit," + vehicle.getId() + "," + action.actionId + "," + System.currentTimeMillis());
    }

	private void requestStateVectorIP(Vehicle vehicle, int requestID) {
		String methodName = "requestStateVectorIP";
    	logsci.log(Level.INFO, methodName + ",entry," + vehicle.getId() + "," + requestID + "," + System.currentTimeMillis());
		SWARMsmsg msg = MessageFormatter.getInstance().getSWARMsStateVectorRequest((byte) vehicle.getId(), (byte) requestID, stateVectorRefreshTimeIP);

    	// Publish the message using the IP topic
    	logger.log(Level.INFO, "--- Sending REQUEST_ENVIRONMENT for vehicle {0} with request ID {1} over IP channel", new Object[]{vehicle.getId(), requestID});
    	psManager.publish(msg, PSManagerCONSTANTS.REQUEST_ENVIRONMENT_IP); 
    	logsci.log(Level.INFO, methodName + ",exit," + vehicle.getId() + "," + requestID + "," + System.currentTimeMillis());
    }
	
	private void requestStateVectorAcoustic(Vehicle vehicle, int requestID) {
		String methodName = "requestStateVectorAcoustic";
    	logsci.log(Level.INFO, methodName + ",entry," + vehicle.getId() + "," + requestID + "," + System.currentTimeMillis());

        if (vehicle.getType().equals(VehicleType.AUV)) {
        	SWARMsmsg msg = MessageFormatter.getInstance().getSWARMsStateVectorRequest((byte) vehicle.getId(), (byte) requestID, stateVectorRefreshTimeAcoustic);

        	// Publish the message using the Acoustic topic
        	logger.log(Level.INFO, "--- Sending REQUEST_ENVIRONMENT for vehicle {0} with request ID {1} over ACOUSTIC channel", new Object[]{vehicle.getId(), requestID});
        	psManager.publish(msg, PSManagerCONSTANTS.REQUEST_ENVIRONMENT_ACOUSTIC);
    	}
    	logsci.log(Level.INFO, methodName + ",exit," + vehicle.getId() + "," + requestID + "," + System.currentTimeMillis());
	}

	private void requestEventsSubscription(byte vehicleID, byte subscriptionRequestID, String topic) {
		String methodName = "requestEventsSubscription";
    	logsci.log(Level.INFO, methodName + ",entry," + vehicleID + "," + subscriptionRequestID + "," + topic + "," + System.currentTimeMillis());
		SWARMsmsg msg = MessageFormatter.getInstance().getSWARMsEventsSubscription(vehicleID, subscriptionRequestID);

		logger.log(Level.INFO, "--- Sending SUBSCRIPTION_TO_VEHICLE_EVENTS for vehicle {0} using topic {1}", new Object[] {vehicleID, topic});
		psManager.publish(msg, topic);
    	logsci.log(Level.INFO, methodName + ",exit," + vehicleID + "," + subscriptionRequestID + "," + topic + "," + System.currentTimeMillis());
	}

	private void loadConfiguration() {
		String methodName = "loadConfiguration";
    	logsci.log(Level.INFO, methodName + ",entry," + System.currentTimeMillis());
		String propertyValue;
        Properties configurationProperties = new Properties();
        FileInputStream in;
		try {
			in = new FileInputStream(MessagesCONSTANTS.CONFIGURATION_FILENAME);
			configurationProperties.load(in);
			in.close();

			logger.log(Level.INFO, "--- Loaded SWARSMs properties file");

			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_DO_GET_NEIGHBOURS)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_DO_GET_NEIGHBOURS).trim();
				doGetNeighbours = Boolean.parseBoolean(propertyValue);				
				logger.log(Level.INFO, "--- Read DO GET NEIGHBOURS property as {0}, set to {1}", new Object[] {propertyValue, doGetNeighbours});
			} else {
				logger.log(Level.INFO, "--- Using default DO GET NEIGHBOURS as {0}", doGetNeighbours);
			}

			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_IS_CDT_AVAILABLE)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_IS_CDT_AVAILABLE).trim();
				CDTavailable = Boolean.parseBoolean(propertyValue);				
				logger.log(Level.INFO, "--- Read CDT available property as {0}, set to {1}", new Object[] {propertyValue, CDTavailable});
			} else {
				logger.log(Level.INFO, "--- Using default CDT available as {0}", CDTavailable);
			}
			
			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_NEIGHBOUR_DISCOVERY_STYLE)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_NEIGHBOUR_DISCOVERY_STYLE).trim();
				neighbourDiscoveryStyle = propertyValue;				
				logger.log(Level.INFO, "--- Read neighbour discovery style property as {0}, set to {1}", new Object[] {propertyValue, neighbourDiscoveryStyle});
			} else {
				logger.log(Level.INFO, "--- Using default neighbour discovery style as {0}", neighbourDiscoveryStyle);
			}
			
			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_GET_NEIGHBOURS_TRYOUTS)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_GET_NEIGHBOURS_TRYOUTS).trim();
				getNeighboursMaxTryouts = Integer.parseInt(propertyValue);				
				logger.log(Level.INFO, "--- Read GET_NEIGHBOURS tryouts property as {0}, set to {1}", new Object[] {propertyValue, getNeighboursMaxTryouts});
			} else {
				logger.log(Level.INFO, "--- Using default GET_NEIGHBOURS tryouts as {0}", getNeighboursMaxTryouts);
			}
			
			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_GET_NEIGHBOURS_TIMEOUT)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_GET_NEIGHBOURS_TIMEOUT).trim();
				getNeighboursTimeout = Long.parseLong(propertyValue);				
				logger.log(Level.INFO, "--- Read GET_NEIGHBOURS timeout property as {0}, set to {1}", new Object[] {propertyValue, getNeighboursTimeout});
			} else {
				logger.log(Level.INFO, "--- Using default GET_NEIGHBOURS timeout as {0}", getNeighboursTimeout);
			}
			
			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_SET_NEIGHBOURS_TIMEOUT)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_SET_NEIGHBOURS_TIMEOUT).trim();
				setNeighboursTimeout = Long.parseLong(propertyValue);				
				logger.log(Level.INFO, "--- Read SET_NEIGHBOURS timeout property as {0}, set to {1}", new Object[] {propertyValue, setNeighboursTimeout});
			} else {
				logger.log(Level.INFO, "--- Using default SET_NEIGHBOURS timeout as {0}", setNeighboursTimeout);
			}
			
			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_STATE_VECTOR_TIMEOUT)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_STATE_VECTOR_TIMEOUT).trim();
				stateVectorTimeout = Long.parseLong(propertyValue);				
				logger.log(Level.INFO, "--- Read STATE_VECTOR timeout property as {0}, set to {1}", new Object[] {propertyValue, stateVectorTimeout});
			} else {
				logger.log(Level.INFO, "--- Using default STATE_VECTOR timeout as {0}", stateVectorTimeout);
			}

			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_STATE_VECTOR_REFRESH_TIME_IP)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_STATE_VECTOR_REFRESH_TIME_IP).trim();
				stateVectorRefreshTimeIP = Integer.parseInt(propertyValue);
				logger.log(Level.INFO, "--- Read STATE_VECTOR IP refresh time property as {0}, set to {1}", new Object[] {propertyValue, stateVectorRefreshTimeIP});
			} else {
				logger.log(Level.INFO, "--- Using default STATE_VECTOR IP refresh time as {0}", stateVectorRefreshTimeIP);
			}

			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_STATE_VECTOR_REFRESH_TIME_ACOUSTIC)) {
				propertyValue =	configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_STATE_VECTOR_REFRESH_TIME_ACOUSTIC).trim();
				stateVectorRefreshTimeAcoustic = Integer.parseInt(propertyValue);
				logger.log(Level.INFO, "--- Read STATE_VECTOR acoustic refresh time property as {0}, set to {1}", new Object[] {propertyValue, stateVectorRefreshTimeAcoustic});
			} else {
				logger.log(Level.INFO, "--- Using default STATE_VECTOR acoustic refresh time as {0}", stateVectorRefreshTimeAcoustic);
			}
			
			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_IS_CDT_DISCOVERY_REQUIRED)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_IS_CDT_DISCOVERY_REQUIRED).trim();
				isCDTDiscoveryRequired = Boolean.parseBoolean(propertyValue);
				logger.log(Level.INFO, "--- Read CDT DISCOVERY required property as {0}, set to {1}", new Object[] {propertyValue, isCDTDiscoveryRequired});
			} else {
				logger.log(Level.INFO, "--- Using default CDT required as {0}", isCDTDiscoveryRequired);
			}
	        
			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_REF_COORDS_LONGITUDE)) {
				propertyValue = configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_REF_COORDS_LONGITUDE).trim();
				refCoordsLongitude = Double.parseDouble(propertyValue);
				logger.log(Level.INFO, "--- Read LONGITUDE reference coordinate property as {0}, set to {1}", new Object[] {propertyValue, refCoordsLongitude});
			} else {
				logger.log(Level.INFO, "--- Using default LONGITUDE reference coordinate as {0}", refCoordsLongitude);
			}
	        
			if (configurationProperties.containsKey(MessagesCONSTANTS.PROPERTY_REF_COORDS_LATITUDE)) {
				propertyValue =	configurationProperties.getProperty(MessagesCONSTANTS.PROPERTY_REF_COORDS_LATITUDE).trim();
				refCoordsLatitude = Double.parseDouble(propertyValue);
				logger.log(Level.INFO, "--- Read LATITUDE reference coordinate property as {0}, set to {1}", new Object[] {propertyValue, String.valueOf(refCoordsLatitude)});
			} else {
				logger.log(Level.INFO, "--- Using default LATITUDE reference coordinate as {0}", refCoordsLatitude);
			}
			
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "+++ Properties file {0} not found. Using default values", MessagesCONSTANTS.CONFIGURATION_FILENAME);
		} catch (IOException e) {
			logger.log(Level.WARNING, "+++ Error reading configuration file {0}", MessagesCONSTANTS.CONFIGURATION_FILENAME);
		}
    	logsci.log(Level.INFO, methodName + ",exit," + System.currentTimeMillis());	
	}
}
