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
package swarms.MTRR.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.swarms.thrift.Action;
import com.swarms.thrift.Mission;
import com.swarms.thrift.Vehicle;
import com.swarms.thrift.VehicleType;

public class MissionParser {
	
    private Logger logger;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
	/**
	 * Parses a full mission plan and returns the same plan filtered and ordered.
	 * 
	 * The plan is filtered so vehicles with on board planner will have assigned only
	 * high level actions and simple actions with no childs; and vehicles without on board
	 * planner will have assigned only low level actions and simple actions with no childs.
	 * 
	 * The plan is ordered according to the StarTime parameter for each Action, so it is
	 * "startime ordered".
	 * 
	 * @param plan		The plan to be parsed
	 * @return			The parsed plan
	 */
	public Mission parsePlan(Mission plan) {
		List<Action> filteredPlan = new ArrayList<Action>();
		
		HashMap<Integer, List<Action>> vehiclesPlan = new HashMap<Integer, List<Action>>();
		HashMap<Integer, Vehicle> vehiclesMap = new HashMap<Integer, Vehicle>();
		HashMap<Integer, Boolean> actionsMap = new HashMap<Integer, Boolean>();
		
		for (Vehicle vehicle : plan.getVehicles()) {
			vehiclesMap.put(vehicle.getId(), vehicle);
			vehiclesPlan.put(vehicle.getId(), new ArrayList<Action>());
		}
		
		for (Action action : plan.getActions()) {
			actionsMap.put(action.getActionId(), false);
		}
		
		for (Action action : plan.getActions()) {
			if (action.isSetParentActionId()) {
				if (action.getActionId() != action.getParentActionId()) {
					actionsMap.put(action.getParentActionId(), true);
				}
			}
		}
		
		for (Action action : plan.getActions()) {
			int assignedVehicleID = action.getAssignedVehicleId();
			boolean actionHasParent = ((action.isSetParentActionId()) && (action.getParentActionId() != action.getActionId()));
			boolean actionHasChilds = actionsMap.get(action.getActionId());
			
			// The vehicle has an on board planner
			if (vehiclesMap.get(assignedVehicleID).isOnboardPlanner()) {
				
				// Only actions that are complex or have no parent
				if (!actionHasParent) {
					vehiclesPlan.get(assignedVehicleID).add(action);
				}
			}
			
			// The vehicle does not have an on board planner
			else {
				
				// Only actions that are simple or have no childs
				if (actionHasParent || !actionHasChilds) {
					vehiclesPlan.get(assignedVehicleID).add(action);			
				}
			}			
		}
		
		for (int vehicleID : vehiclesMap.keySet()) {
			for (Action action : vehiclesPlan.get(vehicleID)) {
				filteredPlan.add(action);
			}
		}
		
		Collections.sort(filteredPlan, new ActionComparatorGlobal());
		
		return plan.setActions(filteredPlan);		
	}
	
	public HashMap<Integer, Vehicle> getVehiclesMap(Mission plan) {
		HashMap<Integer, Vehicle> vehiclesMap = new HashMap<Integer, Vehicle>();
		
		for (Vehicle vehicle : plan.getVehicles()) {
			vehiclesMap.put(vehicle.getId(), vehicle);
		}
		
		return vehiclesMap;
	}
	
	/************************/
	/*** OLD PARSER BELOW ***/
	/************************/
	
    /**
     * Returns the vehicles that perform some action in the mission
     *
     * @param plan		The Mission plan
     * @return			The list of vehicles in the Mission plan
     */
    public List<Vehicle> parseVehiclesInMission(Mission plan) {
        List<Vehicle> vehicles = plan.getVehicles();
        ArrayList<Integer> vehicelIDList = new ArrayList<>();
        List<Vehicle> involvedVehicles = new ArrayList<>();

        for (Action action : plan.getActions()) {
            int vehicleID = action.getAssignedVehicleId();
            if (!vehicelIDList.contains(vehicleID)) {
                vehicelIDList.add(vehicleID);
                logger.info("Adding vehicle with id " + vehicleID);
                involvedVehicles.add(findVehicle(vehicles, vehicleID));
            }
        }

        return involvedVehicles;
    }

    private Vehicle findVehicle(List<Vehicle> vehicles, int vid) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId() == vid) {
                return vehicle;
            }
        }
        return null;
    }

    public LinkedList<Action> parseVehiclePlan(Vehicle vehicle, Mission plan) {
    	List<Action> vehiclePlan = new ArrayList<Action>();
    	LinkedList<Action> filteredVehiclePlan = new LinkedList<Action>();
		HashMap<Integer, Boolean> hasChilds = new HashMap<Integer, Boolean>();
		
		// STEP 0: Get only the actions for the requested vehicle
		// (unsorted and not filtered)
		for (Action action : plan.getActions()) {
			if (action.getAssignedVehicleId() == vehicle.getId()) {
				vehiclePlan.add(action);
				logger.log(Level.INFO, ">>> [MissionParser.parseVehiclePlan] >>> Pre-added action {0} ({1}) to vehicle plan for vehicle {2} ({3})", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), vehicle.getName(), vehicle.getId()});
			}
		}

		// STEP 1: Create a map of actions to check for child actions		
		for (Action action : vehiclePlan) {
			hasChilds.put(action.getActionId(), false);
		}

		// STEP 2: Update the map of actions for if an action has childs	
		for (Action action : vehiclePlan) {
			if (action.isSetParentActionId()) {
				if (action.getParentActionId() != 0) {
					hasChilds.put(action.getParentActionId(), true);
				}
			}
		}
										
		// STEP 3: Filter the actions for vehicles with/without on board planner
		for (Action action : vehiclePlan) {
			if (vehicle.isOnboardPlanner()) {
				logger.log(Level.INFO, ">>> [MissionParser.parseVehiclePlan] >>> Vehicle {0} ({1}) has an onboard planner -> Only high level and single actions", new Object[] {vehicle.getName(), vehicle.getId()});
				
				if (action.getParentActionId() == 0) {
					logger.log(Level.INFO, ">>> [MissionParser.parseVehiclePlan] >>> Added action {0} ({1}) to filtered vehicle plan for vehicle {2} ({3})", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), vehicle.getName(), vehicle.getId()});
					filteredVehiclePlan.add(action);
				}
			}
			else {
				logger.log(Level.INFO, ">>> [MissionParser.parseVehiclePlan] >>> Vehicle {0} ({1}) does not have an onboard planner -> Only low level and single actions", new Object[] {vehicle.getName(), vehicle.getId()});
				if ((action.getParentActionId() != 0) || (!hasChilds.get(action.getActionId()))) {
					logger.log(Level.INFO, ">>> [MissionParser.parseVehiclePlan] >>> Added action {0} ({1}) to filtered vehicle plan for vehicle {2} ({3})", new Object[] {action.getRelatedTask().getDescription(), action.getActionId(), vehicle.getName(), vehicle.getId()});
					filteredVehiclePlan.add(action);
				}
			}
		}
		
		// STEP 4: Sort the filtered actions according to the StartTime parameter
		Collections.sort(filteredVehiclePlan, new ActionComparator());
		logger.log(Level.INFO, ">>> [MissionParser.parseVehiclePlan] >>> Sorting vehicle plan for vehicle {0} ({1}) according to starTime", new Object[] {vehicle.getName(), vehicle.getId()});
		
		return filteredVehiclePlan;		
		
//		// STEP 1: Create a map of actions to check for child actions		
//		for (Action action : vehiclePlan) {
//			hasChilds.put(action.getActionId(), false);
//		}
//		
//		// STEP 2: Update the map of actions for if an action has childs	
//		for (Action action : vehiclePlan) {
//			if (action.isSetParentActionId()) {
//				if (action.getActionId() != action.getParentActionId()) {
//					hasChilds.put(action.getParentActionId(), true);
//				}
//			}
//		}
//		
//		// STEP 3: Filter the actions for vehicles with/without on board planner
//		for (Action action : vehiclePlan) {
//			boolean actionHasParent = ((action.isSetParentActionId()) && (action.getParentActionId() != action.getActionId()));
//			boolean actionHasChilds = hasChilds.get(action.getActionId());
//			
//			// The vehicle has an on board planner
//			if (vehicle.isOnboardPlanner()) {
//				
//				// Only actions that are complex or have no parent
//				if (!actionHasParent) {
//					filteredVehiclePlan.add(action);
//				}
//			}
//			
//			// The vehicle does not have an on board planner
//			else {
//				
//				// Only actions that are simple or have no childs
//				if (actionHasParent || !actionHasChilds) {
//					filteredVehiclePlan.add(action);			
//				}
//			}			
//		}
//			
//		// STEP 4: Sort the filtered actions according to the StartTime parameter
//		Collections.sort(filteredVehiclePlan, new ActionComparator());
//		
//		return filteredVehiclePlan;		

    }
    
    public boolean hasAUVs(Mission plan) {
    	for (Vehicle vehicle : plan.getVehicles()) {
    		if (vehicle.getType().equals(VehicleType.AUV)) {
    			return true;
    		}
    	}
    	return false;
    }
}
