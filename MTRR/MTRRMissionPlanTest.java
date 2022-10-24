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

import java.util.ArrayList;

import com.swarms.thrift.Action;
import com.swarms.thrift.Equipment;
import com.swarms.thrift.EquipmentType;
import com.swarms.thrift.Mission;
import com.swarms.thrift.Orientation;
import com.swarms.thrift.Position;
import com.swarms.thrift.Region;
import com.swarms.thrift.Task;
import com.swarms.thrift.TaskRegionType;
import com.swarms.thrift.TaskStatus;
import com.swarms.thrift.Vehicle;
import com.swarms.thrift.VehicleType;

public class MTRRMissionPlanTest {
	public static Mission getMissionPlan() {
		////////////////////////////////////////////
		// AREAS
		
		// Initial Position for all vehicles and start of mission
		Position startPosition = new Position(28.598597, 43.797763, 0, 0,0);
		
		// Mission area delimiters
		Position delimiter1 = new Position(28.607794, 43.793060, 0, 0,0);
		Position delimiter2 = new Position(28.599668, 43.787539, 0, 0,0);
		Position delimiter3 = new Position(28.592523, 43.791297, 0, 0,0);

		// Navigation area
		Region navigationArea = new Region();
		navigationArea.addToArea(startPosition);
		navigationArea.addToArea(delimiter1);
		navigationArea.addToArea(delimiter2);
		navigationArea.addToArea(delimiter3);
		
		// Area GWP.1
		Region areaGWP1 = new Region();
		areaGWP1.addToArea(delimiter1);
		
		// Area GWP.2
		Region areaGWP2 = new Region();
		areaGWP2.addToArea(delimiter2);
	
		// Area GWP.3
		Region areaGWP3 = new Region();
		areaGWP3.addToArea(delimiter3);
		
		// Area HOVER.1
		Region areaHOVER1 = new Region();
		areaHOVER1.addToArea(delimiter1);
		
		// Area GWP.2
		Region areaHOVER2 = new Region();
		areaHOVER2.addToArea(delimiter2);

		// Area GWP.3
		Region areaHOVER3 = new Region();
		areaHOVER3.addToArea(delimiter3);

		////////////////////////////////////////////
		// VEHICLES AND VEHICLE ASSOCIATED DATA
		int IXNID = 1;
		int A9ID = 2;
		int SAGAID = 3;
		
		Position IXNLocation = startPosition;
		Position A9Location = startPosition;
		Position SAGALocation = startPosition;

		Orientation IXNOrientation = new Orientation (0, 0 ,0,0);
		Orientation A9Orientation = new Orientation (0, 0 ,0,0);
		Orientation SAGAOrientation = new Orientation (0, 0 ,0,0);
		
		ArrayList<Equipment> IXNEquipment = new ArrayList<>();
		ArrayList<Equipment> A9Equipment = new ArrayList<>();
		ArrayList<Equipment> SAGAEquipment = new ArrayList<>();

		IXNEquipment.add(new Equipment(EquipmentType.ACOUSTIC, 0, "Acoustic modem"));
		IXNEquipment.add(new Equipment(EquipmentType.H2S, 0, "H2S Probe"));
		A9Equipment.add(new Equipment(EquipmentType.ACOUSTIC, 0, "Acoustic modem"));
		A9Equipment.add(new Equipment(EquipmentType.H2S, 0, "H2S Probe"));
		SAGAEquipment.add(new Equipment(EquipmentType.CAMERA, 0, "Camera"));

		Vehicle IXN = new Vehicle(IXNID,				// Vehicle ID
				"IXN",			// Vehicle Name
				IXNLocation,		// Vehicle Location
				IXNOrientation,	// Vehicle Orientation
				2.0,				// Speed
				0.0,				// Current Speed
				VehicleType.AUV,	// Vehicle Type
				5000,				// Max. Battery				                  
				3000,				// Battery Status
				2000,				// Consumption
				IXNEquipment,     // Equipment
				false,
				0,
                                30);        //Safety distance

		Vehicle A9 = new Vehicle(A9ID,				// Vehicle ID
				"A9",			// Vehicle Name
				A9Location,		// Vehicle Location
				A9Orientation,	// Vehicle Orientation
				3.0,				// Speed
				0.0,				// Current Speed
				VehicleType.AUV,	// Vehicle Type
				7000,				// Max. Battery				                  
				4000,				// Battery Status
				3000,				// Consumption
				A9Equipment,	// Equipment
				false,
				0,
                                30);        //Safety distance
		
		Vehicle SAGA = new Vehicle(SAGAID,				// Vehicle ID
				"SAGA",			// Vehicle Name
				SAGALocation,		// Vehicle Location
				SAGAOrientation,	// Vehicle Orientation
				1.0,				// Speed
				0.0,				// Current Speed
				VehicleType.ROV,	// Vehicle Type
				10000,				// Max. Battery				                  
				5000,				// Battery Status
				5000,				// Consumption
				SAGAEquipment,	// Equipment
				false,
				0,
                                30);        //Safety distance
		
		ArrayList<Vehicle> vehicles = new ArrayList<>();
		vehicles.add(IXN);
		vehicles.add(A9);
		vehicles.add(SAGA);

		////////////////////////////////////////////
		// TASKS
		
		ArrayList<EquipmentType> taskH2SEquipment = new ArrayList<>();
		ArrayList<EquipmentType> taskCameraEquipment = new ArrayList<>();
		
		taskH2SEquipment.add(EquipmentType.H2S);
		taskCameraEquipment.add(EquipmentType.CAMERA);

		Task task1 = new Task(MessagesCONSTANTS.GOTO_WAYPOINT,
						   "GOTO_WAYPOINT",
						   TaskRegionType.Area,
						   null,
						   5.0);
		
		Task task2 = new Task(MessagesCONSTANTS.HOVER,
				   "HOVER",
				   TaskRegionType.Area,
				   null,
				   5.0);
		
		////////////////////////////////////////////
		// ACTIONS
		
		// The order for the Actions parameters is as follows:
		//  1: relatedTask
		//  2: actionId
		//  3: area
		//  4: speed
		//  5: altitude
		//  6: range
		//  7: timeLapse
		//  8: bearing
		//  9: startTime
		// 10: endTime
		// 11: status
		// 12: assignedVehicleId
		// 13: parentActionId
		
		Action action1 = new Action(task1,
				1,                                     
				areaGWP1,
				1.0,
				0.0,
				0.0,
				0,
				new Orientation(0, 3, 7,0),
				0,
				10,
				TaskStatus.NotStarted,
				IXNID,
				0);

		Action action2 = new Action(task2,
				1,
				areaHOVER1,
				2.0,
				0.0,
				0.0,
				0,
				new Orientation(0, 0, 0,0),
				10,
				20,
				TaskStatus.NotStarted,
				IXNID,
				0);
		
		Action action3 = new Action(task1,
				1,
				areaGWP2,
				2.0,
				0.0,
				0.0,
				0,
				new Orientation(4, 0, 0,0),
				0,
				10,
				TaskStatus.NotStarted,
				A9ID,
				0);
		
		Action action4 = new Action(task2,
				1,
				areaHOVER2,
				2.0,
				0.0,
				0.0,
				0,
				new Orientation(0, 0, 0,0),
				10,
				20,
				TaskStatus.NotStarted,
				A9ID,
				0);

		Action action5 = new Action(task1,
				1,
				areaGWP3,
				2.0,
				0.0,
				0.0,
				0,
				new Orientation(0, 2, 0,0),
				0,
				10,
				TaskStatus.NotStarted,
				SAGAID,
				0);

		Action action6 = new Action(task2,
				1,
				areaHOVER3,
				2.0,
				0.0,
				0.0,
				0,
				new Orientation(0, 0, 0,0),
				10,
				20,
				TaskStatus.NotStarted,
				SAGAID,
				0);
		
		ArrayList<Action> actionList = new ArrayList<>();
		actionList.add(action4);
		actionList.add(action1);
		actionList.add(action3);
		actionList.add(action2);
		actionList.add(action6);
		actionList.add(action5);		
		
		////////////////////////////////////////////
		// MISSION

		Mission mission = new Mission();
		mission.setMissionId(1);
		mission.setNavigationArea(navigationArea);
		mission.setActions(actionList);
		mission.setVehicles(vehicles);
		return mission;
	}
}