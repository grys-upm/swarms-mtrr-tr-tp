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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.swarms.thrift.Action;
import com.swarms.thrift.EquipmentType;

import swarms.MTRR.MessagesCONSTANTS;
import swarmsDDLs.SWARMsmsg;
import swarmsPDUs.basic.SWARMsDDSFrame;

public class MessageFormatter {
    private Logger logger;
    private double mission_origin_latitude;
    private double mission_origin_longitude;  
    private DecimalFormat df = new DecimalFormat("#0.000000");
  
    private int mask_deltas = 0b00000000000000111111111111111111;
    private int mask_roll   = 0b00000000000000000000000011111111;
    private int mask_pitch  = 0b00000000000000000000000011111111;
    
    public static MessageFormatter instance = null;
    
    private MessageFormatter() {}

    public static MessageFormatter getInstance() {
            if(instance == null)
                instance = new MessageFormatter();
            
            return instance;	
    }
      
    public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public double getMission_origin_latitude() {
		return mission_origin_latitude;
	}

	public void setMission_origin_latitude(double mission_origin_latitude) {
		this.mission_origin_latitude = mission_origin_latitude;
	}

	public double getMission_origin_longitude() {
		return mission_origin_longitude;
	}

	public void setMission_origin_longitude(double mission_origin_longitude) {
		this.mission_origin_longitude = mission_origin_longitude;
	}
	
	public SWARMsmsg getStartDiscoveryMessage(byte requestID) {
		SWARMsmsg msg;
        SWARMsDDSFrame frame = new SWARMsDDSFrame();
        int[] data = new int[MessagesCONSTANTS.DATA_ARRAY_SIZE];
        Arrays.fill(data, 0);
    	        
		int discoveryTime = MessagesCONSTANTS.CDT_START_DISCOVERY_DISCOVERY_TIME;
		int period = MessagesCONSTANTS.CDT_START_DISCOVERY_PERIOD;

		// DDS Frame Header
		frame.setType(MessagesCONSTANTS.TYPE_CDT_MESSAGE);
		frame.setVid(MessagesCONSTANTS.CDT_REQUEST_VID);
		frame.setSubtype(MessagesCONSTANTS.START_DISCOVERY);
		frame.setSeqoperation((byte) requestID++);
		data[0] = (int) (discoveryTime << 6 | period);

		frame.setDataInt(data);
		msg = frame;
		
		return msg;
	}
	
	public SWARMsmsg getGetNeighboursMessage(byte requestID) {
		SWARMsmsg msg;
        SWARMsDDSFrame frame = new SWARMsDDSFrame();
        int[] data = new int[MessagesCONSTANTS.DATA_ARRAY_SIZE];
        Arrays.fill(data, 0);
    	        
    	frame.setType(MessagesCONSTANTS.TYPE_GET_NEIGHBOURS);
    	frame.setVid((byte) 0x00);
    	frame.setSubtype(MessagesCONSTANTS.SUBTYPE_GET_NEIGHBOURS);
    	frame.setSeqoperation((byte) requestID);
    	frame.setDataInt(data);
    	msg = frame;
		
		return msg;
	}
	
	public SWARMsmsg getSetNeighboursMessage(ArrayList<Integer> auvIDs, byte requestID) {
		SWARMsmsg msg;
        SWARMsDDSFrame frame = new SWARMsDDSFrame();
        int[] data = new int[MessagesCONSTANTS.DATA_ARRAY_SIZE];
        Arrays.fill(data, 0);
    	        
    	frame.setType(MessagesCONSTANTS.TYPE_SET_NEIGHBOURS);
    	frame.setVid((byte) 0x00);
    	frame.setSubtype(MessagesCONSTANTS.SUBTYPE_SET_NEIGHBOURS);
    	frame.setSeqoperation((byte) requestID);
    	int i = 0;
    	for (int vehicleID : auvIDs) {
    		data[i++] = vehicleID;
    	}
    	frame.setDataInt(data);
    	msg = frame;
		
		return msg;
	}

	public SWARMsmsg getSWARMsStateVectorRequest(byte vehicleID, byte requestID, int refreshTime) {		
		SWARMsmsg msg;
        SWARMsDDSFrame frame = new SWARMsDDSFrame();
        int[] data = new int[MessagesCONSTANTS.DATA_ARRAY_SIZE];
        Arrays.fill(data, 0);
    	        
    	frame.setType(MessagesCONSTANTS.TYPE_ENVIRONMENT_MESSAGE);
    	frame.setVid(vehicleID);
    	frame.setSubtype(MessagesCONSTANTS.STATE_VECTOR);
    	frame.setSeqoperation((byte) requestID);
    	data[0] = refreshTime;
    	frame.setDataInt(data);
    	msg = frame;
		
		return msg;
	}
	
	public SWARMsmsg getSWARMsEventsSubscription(byte vehicleID, byte subscriptionRequestID) {
		SWARMsmsg msg;
        SWARMsDDSFrame frame = new SWARMsDDSFrame();
        int[] data = new int[MessagesCONSTANTS.DATA_ARRAY_SIZE];
        Arrays.fill(data, 0);
		
		Arrays.fill(data, 0);        
		frame.setType(MessagesCONSTANTS.TYPE_EVENTS_MESSAGE);
		frame.setVid((byte) vehicleID);
		frame.setSubtype(MessagesCONSTANTS.SUBSCRIPTION_TO_VEHICLE_EVENTS);
		frame.setSeqoperation(subscriptionRequestID);
		frame.setDataInt(data);
		msg = frame;
		
		return msg;
	}

	/**
     * 
     */
    public SWARMsmsg getSWARMsTaskMessage(Action action, byte requestID) {
        SWARMsDDSFrame frame = new SWARMsDDSFrame();
        long depth;
        long delta_latitude;
        long delta_longitude;
        int length;
        int azimuth;
        long altitude;
        int radius;
        int clockwise;
        int sensor;
        
    	// Variables for SONAR ADQUISITION. They must be obtained elsewhere
        int range;
    	
    	// Variables for CAMERA ADQUISITION. They must be obtained elsewhere
    	int delay;

        // DDS Frame Header is task independent
        frame.setType(MessagesCONSTANTS.TYPE_TASK_MESSAGE);
        frame.setVid((byte) action.getAssignedVehicleId());
        frame.setSubtype((byte) action.getRelatedTask().taskTypeId);
        frame.setSeqoperation((byte) requestID);    	

        int yaw = ((Double) (action.getBearing().getYaw() * 10)).intValue();
        int roll = ((Double) (action.getBearing().getRoll() * 10)).intValue();
        int pitch = ((Double) (action.getBearing().getPitch() * 10)).intValue();
        int speed = ((Double) (action.getSpeed() * 10)).intValue();

        int[] data = new int[15];
        Arrays.fill(data, 0);

        // DDS Frame specifics for each task type
        logger.log(Level.INFO, ">> PROCESSING ACTION: Task subtype {0}", action.getRelatedTask().getDescription());
        switch (((byte) action.getRelatedTask().getTaskTypeId())) {

            case MessagesCONSTANTS.GOTO_WAYPOINT:
                depth = ((Double) (action.getArea().getArea().get(1).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(1).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(1).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(1).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                data[0] = (int) (yaw << 25 | (roll & mask_roll) << 17 | (pitch & mask_pitch) << 9 | speed);
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) (delta_latitude & mask_deltas);
                data[3] = (int) (delta_longitude & mask_deltas);
                
                logger.log(Level.INFO, ">> yaw: {0}, roll: {1}, pitch: {2}, speed: {3}", new Object[] {yaw, roll, pitch, speed});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[] {altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> delta_latitude: {0}", delta_latitude);
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> delta_longitude: {0}", delta_longitude);
                logger.log(Level.INFO, ">> data[3] {0}: {1}", new Object[] {data[3], String.format("%32s", Integer.toBinaryString(data[3])).replace(' ', '0')});
        
                break;
            case MessagesCONSTANTS.HOVER:
                depth = ((Double) (action.getArea().getArea().get(0).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(0).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                // TODO: Get clockwise and radius data from the Action
                clockwise = 0;
                radius = 0;

                data[0] = (int) (yaw << 25 | (roll & mask_roll) << 17 | (pitch & mask_pitch) << 9 | speed);
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) (radius << 18 | (delta_latitude & mask_deltas));
                data[3] = (int) (clockwise << 18 | (delta_longitude & mask_deltas));
                
                logger.log(Level.INFO, ">> yaw: {0}, roll: {1}, pitch: {2}, speed: {3}", new Object[] {yaw, roll, pitch, speed});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[] {altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> radius: {0}, delta_latitude: {1}", new Object[] {radius, delta_latitude});
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> clockwise: {0}, delta_longitude: {1}", new Object[] {clockwise, delta_longitude});
                logger.log(Level.INFO, ">> data[3] {0}: {1}", new Object[] {data[3], String.format("%32s", Integer.toBinaryString(data[3])).replace(' ', '0')});

                break;
            case MessagesCONSTANTS.CONFIGURE:
                // According to Bilbao agreement
            	int on = 0;
            	sensor = 0;
            	if (action.getRelatedTask().getDescription().startsWith("ON")) {
            		on = 1;
            	}

            	if (action.getRelatedTask().getRequiredTypes().get(0) == EquipmentType.CAMERA) {
            		sensor = 0;
            	}
            	else if (action.getRelatedTask().getRequiredTypes().get(0) == EquipmentType.H2S) {
            		sensor = 1;
            	}
            	else if (action.getRelatedTask().getRequiredTypes().get(0) == EquipmentType.SONAR) {
            		sensor = 2;
            	}
            	else if (action.getRelatedTask().getRequiredTypes().get(0) == EquipmentType.LIGHT) {
            		sensor = 3;
            	}

                data[0] = (int) (on << 2 | sensor);
                
                logger.log(Level.INFO, ">> on: {0}, sensor: {1}", new Object[] {on, sensor});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                break;
            case MessagesCONSTANTS.FOLLOW_TARGET:
                // TODO: Get the target ID from the Action
                int targetID = 0;

                data[0] = (int) (targetID);

                logger.log(Level.INFO, ">> target ID: {0}", new Object[] {targetID});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                break;
            case MessagesCONSTANTS.FOLLOW_STRUCTURE:
                depth = ((Double) (action.getArea().getArea().get(0).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(0).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)).intValue();
                
                // TODO: Get the azimuth value from the Action
                azimuth = 0;

                data[0] = (int) (yaw << 25 | (roll & mask_roll) << 17 | (pitch & mask_pitch) << 9 | speed);
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) (azimuth << 18 | (delta_latitude & mask_deltas));
                data[3] = (int) ((delta_longitude & mask_deltas));
                
                logger.log(Level.INFO, ">> yaw: {0}, roll: {1}, pitch: {2}, speed: {3}", new Object[] {yaw, roll, pitch, speed});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[] {altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> azimuth: {0}, delta_latitude: {1}", new Object[] {azimuth ,delta_latitude});
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> delta_longitude: {0}", delta_longitude);
                logger.log(Level.INFO, ">> data[3] {0}: {1}", new Object[] {data[3], String.format("%32s", Integer.toBinaryString(data[3])).replace(' ', '0')});
                
                break;
            case MessagesCONSTANTS.FOLLOW_ROW:
                depth = ((Double) (action.getArea().getArea().get(0).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(0).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                // TODO: Get the length and azimuth from the Action
                length = 0;
                azimuth = 0;

                data[0] = (int) (speed);
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) (azimuth << 18 | (delta_latitude & mask_deltas));
                data[3] = (int) (length << 18 | (delta_longitude & mask_deltas));

                logger.log(Level.INFO, ">> speed: {0}", speed);
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[] {altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> azimuth: {0}, delta_latitude:: {1}", new Object[] {azimuth, delta_latitude});
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> length: {0}, delta_longitude: {1}", new Object[] {length, delta_longitude});
                logger.log(Level.INFO, ">> data[3] {0}: {1}", new Object[] {data[3], String.format("%32s", Integer.toBinaryString(data[3])).replace(' ', '0')});
                
                break;
            case MessagesCONSTANTS.WAIT:
                depth = ((Double) (action.getArea().getArea().get(0).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(0).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                // TODO: Get clockwise and radius data from the Action
                clockwise = 0;
                radius = 0;

                data[0] = (int) (yaw << 25 | (roll & mask_roll) << 17 | (pitch & mask_pitch) << 9 | speed);
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) (radius << 18 | (delta_latitude & mask_deltas));
                data[3] = (int) (clockwise << 18 | (delta_longitude & mask_deltas));
                                
                logger.log(Level.INFO, ">> yaw: {0}, roll: {1}, pitch: {2}, speed: {3}", new Object[] {yaw, roll, pitch, speed});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[] {altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> radius: {0}, delta_latitude: {1}", new Object[] {radius, delta_latitude});
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> clockwise: {0}, delta_longitude: {1}", new Object[] {clockwise, delta_longitude});
                logger.log(Level.INFO, ">> data[3] {0}: {1}", new Object[] {data[3], String.format("%32s", Integer.toBinaryString(data[3])).replace(' ', '0')});

                break;
            case MessagesCONSTANTS.TRANSIT:
            	//TRANSIT IN THE MMT has 2 locations: 1st one is where the vehicle is, and 2nd is where vehicle has to go
            	depth = ((Double) (action.getArea().getArea().get(1).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(1).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(1).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(1).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                data[0] = (int) (yaw << 25 | (roll & mask_roll) << 17 | (pitch & mask_pitch) << 9 | speed);
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) (delta_latitude & mask_deltas);
                data[3] = (int) (delta_longitude & mask_deltas);
                
                logger.log(Level.INFO, ">> yaw: {0}, roll: {1}, pitch: {2}, speed: {3}", new Object[]{yaw, roll, pitch, speed});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[] {altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> delta_latitude: {0}", delta_latitude);
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> delta_longitude: {0}", delta_longitude);
                logger.log(Level.INFO, ">> data[3] {0}: {1}", new Object[] {data[3], String.format("%32s", Integer.toBinaryString(data[3])).replace(' ', '0')});
                           
                break;
            case MessagesCONSTANTS.SURVEY:
                depth = ((Double) (action.getArea().getArea().get(0).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(0).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                azimuth = 0; // TODO: Get azimuth from ACTION
                sensor = 0; // TODO: Get sensor from ACTION
                length = 0; // TODO: Get length from ACTION

                int delta_latitude12 = ((Double) ((action.getArea().getArea().get(1).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                int delta_longitude12 = ((Double) ((action.getArea().getArea().get(1).getLongitude() - mission_origin_longitude) * 1e6)).intValue();
                int delta_latitude21 = ((Double) ((action.getArea().getArea().get(2).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                int delta_longitude21 = ((Double) ((action.getArea().getArea().get(2).getLongitude() - mission_origin_longitude) * 1e6)).intValue();
                int delta_latitude22 = ((Double) ((action.getArea().getArea().get(3).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                int delta_longitude22 = ((Double) ((action.getArea().getArea().get(3).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                data[0] = (int) (altitude << 16 | depth);
                data[1] = (int) (sensor << 24 | azimuth << 18 | (delta_latitude & mask_deltas));
                data[2] = (int) (length << 18 | (delta_longitude & mask_deltas));
                data[3] = (int) (speed << 18 | (delta_latitude12 & mask_deltas));
                data[4] = (int) (delta_longitude12 & mask_deltas);
                data[5] = (int) (delta_latitude21 & mask_deltas);
                data[6] = (int) (delta_longitude21 & mask_deltas);
                data[7] = (int) (delta_latitude22 & mask_deltas);
                data[8] = (int) (delta_longitude22 & mask_deltas);

                logger.log(Level.INFO, ">> Reference coordinates for SURVEY: Longitude {0} - Latitude {1}", new Object[] {df.format(mission_origin_longitude), df.format(mission_origin_latitude)});
                logger.log(Level.INFO, ">> First point coordinates: Longitude {0} - Latitude {1}", new Object[] {df.format(action.getArea().getArea().get(0).getLongitude()), df.format(action.getArea().getArea().get(0).getLatitude())});
                logger.log(Level.INFO, ">> Second point coordinates: Longitude {0} - Latitude {1}", new Object[] {df.format(action.getArea().getArea().get(1).getLongitude()), df.format(action.getArea().getArea().get(1).getLatitude())});
                logger.log(Level.INFO, ">> Third point coordinates: Longitude {0} - Latitude {1}", new Object[] {df.format(action.getArea().getArea().get(2).getLongitude()), df.format(action.getArea().getArea().get(2).getLatitude())});
                logger.log(Level.INFO, ">> Fourth point coordinates: Longitude {0} - Latitude {1}", new Object[] {df.format(action.getArea().getArea().get(3).getLongitude()), df.format(action.getArea().getArea().get(3).getLatitude())});
                
                logger.log(Level.INFO, ">>-- Delta longitude (1st point) without scaling (decimal value): {0} - {1} = {2}", new Object[] {df.format(action.getArea().getArea().get(0).getLongitude()), df.format(mission_origin_longitude), df.format((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude))});
                logger.log(Level.INFO, ">>-- Delta longitude (1st point) scaled (decimal value): ({0} - {1}) * 1e6 = {2}", new Object[] {df.format(action.getArea().getArea().get(0).getLongitude()), df.format(mission_origin_longitude), df.format((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)});
                logger.log(Level.INFO, ">>-- Delta longitude (1st point) scaled (integer value): [({0} - {1}) * 1e6] = {2}", new Object[] {df.format(action.getArea().getArea().get(0).getLatitude()), df.format(mission_origin_latitude), ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue()});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[] {altitude, depth});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> sensor: {0}, azimuth: {1}, delta_latitude: {2}", new Object[] {sensor, azimuth, delta_latitude});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> length: {0}, delta_longitude: {1}", new Object[] {length, delta_longitude});
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> speed: {0}, delta_latitude12: {1}", new Object[] {speed, delta_latitude12});
                logger.log(Level.INFO, ">> data[3] {0}: {1}", new Object[] {data[3], String.format("%32s", Integer.toBinaryString(data[3])).replace(' ', '0')});

                logger.log(Level.INFO, ">> delta_longitude12: {0}", delta_longitude12);
                logger.log(Level.INFO, ">> data[4] {0}: {1}", new Object[] {data[4], String.format("%32s", Integer.toBinaryString(data[4])).replace(' ', '0')});

                logger.log(Level.INFO, ">> delta_latitude21: {0}", delta_latitude21);
                logger.log(Level.INFO, ">> data[5] {0}: {1}", new Object[] {data[5], String.format("%32s", Integer.toBinaryString(data[5])).replace(' ', '0')});

                logger.log(Level.INFO, ">> delta_longitude21: {0}", delta_longitude21);
                logger.log(Level.INFO, ">> data[6] {0}: {1}", new Object[] {data[6], String.format("%32s", Integer.toBinaryString(data[6])).replace(' ', '0')});

                logger.log(Level.INFO, ">> delta_latitude22: {0}", delta_latitude22);
                logger.log(Level.INFO, ">> data[7] {0}: {1}", new Object[] {data[7], String.format("%32s", Integer.toBinaryString(data[7])).replace(' ', '0')});

                logger.log(Level.INFO, ">> delta_longitude22: {0}", delta_longitude22);
                logger.log(Level.INFO, ">> data[8] {0}: {1}", new Object[] {data[8], String.format("%32s", Integer.toBinaryString(data[8])).replace(' ', '0')});

                break;
            case MessagesCONSTANTS.INSPECT:
                depth = ((Double) (action.getArea().getArea().get(0).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(0).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                radius = ((Double) action.getRange()).intValue();
                clockwise = 1; // TODO: Get radius from ACTION
                pitch = action.getTimeLapse();

                data[0] = (int) (yaw << 25 | (roll & mask_roll) << 17 | (pitch & mask_pitch) << 9 | speed);
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) (radius << 18 | (delta_latitude & mask_deltas));
                data[3] = (int) (clockwise << 18 | (delta_longitude & mask_deltas));
                
                logger.log(Level.INFO, ">> yaw: {0}, roll: {1}, pitch: {2}, speed: {3}", new Object[]{yaw, roll, pitch, speed});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[] {altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> radius: {0}, delta_latitude {1}", new Object[]{radius, delta_latitude});
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> clockwise: {0}, delta_longitude {1}", new Object[]{clockwise, delta_longitude});
                logger.log(Level.INFO, ">> data[3] {0}: {1}", new Object[] {data[3], String.format("%32s", Integer.toBinaryString(data[3])).replace(' ', '0')});
                
                break;
                
            // NEW TASK TYPES FROM ISSUE 10.0
            case MessagesCONSTANTS.PICKUP:
                depth = ((Double) (action.getArea().getArea().get(0).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(0).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                data[0] = (int) (yaw << 26 | (roll & mask_roll) << 18 | (delta_latitude & mask_deltas));
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) ((pitch & mask_pitch) << 18 | (delta_longitude & mask_deltas));
                
                logger.log(Level.INFO, ">> yaw: {0}, roll: {1}, delta_latitude: {2}", new Object[]{yaw, roll, delta_latitude});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[]{altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> pitch: {0}, delta_longitude: {1}", new Object[]{pitch, delta_longitude});
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                break;                
            case MessagesCONSTANTS.GRASP_OBJECT:
                depth = ((Double) (action.getArea().getArea().get(0).getDepth() * 10)).intValue();
                altitude = ((Double) (action.getArea().getArea().get(0).getAltitude() * 10)).intValue();
                delta_latitude = ((Double) ((action.getArea().getArea().get(0).getLatitude() - mission_origin_latitude) * 1e6)).intValue();
                delta_longitude = ((Double) ((action.getArea().getArea().get(0).getLongitude() - mission_origin_longitude) * 1e6)).intValue();

                data[0] = (int) (yaw << 26 | (roll & mask_roll) << 18 | (delta_latitude & mask_deltas));
                data[1] = (int) (altitude << 16 | depth);
                data[2] = (int) ((pitch & mask_pitch) << 18 | (delta_longitude & mask_deltas));
                
                logger.log(Level.INFO, ">> yaw: {0}, roll: {1}, delta_latitude: {2}", new Object[]{yaw, roll, delta_latitude});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> altitude: {0}, depth: {1}", new Object[]{altitude, depth});
                logger.log(Level.INFO, ">> data[1] {0}: {1}", new Object[] {data[1], String.format("%32s", Integer.toBinaryString(data[1])).replace(' ', '0')});
                
                logger.log(Level.INFO, ">> pitch: {0}, delta_longitude: {1}", new Object[]{pitch, delta_longitude});
                logger.log(Level.INFO, ">> data[2] {0}: {1}", new Object[] {data[2], String.format("%32s", Integer.toBinaryString(data[2])).replace(' ', '0')});
                
                break;
            case MessagesCONSTANTS.SONAR_ACQUISITION:
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});

                break;
            case MessagesCONSTANTS.CAMERA_ACQUISITION:
            	// TODO: CAMERA ACQUISITION PARAMETERS MUST BE OBTAINED ELSEWHERE
            	delay = 0;
            	range = 0;
            	
                data[0] = (int) (delay << 11 | range);
                
                logger.log(Level.INFO, ">> delay: {0}, range: {1}", new Object[]{delay, range});
                logger.log(Level.INFO, ">> data[0] {0}: {1}", new Object[] {data[0], String.format("%32s", Integer.toBinaryString(data[0])).replace(' ', '0')});
                
                break;
                               
            default:
                logger.info("## ERROR PROCESSING ACTION: Task type " + action.getRelatedTask().getTaskTypeId() + " not recognized");
                break;

        }

        frame.setDataInt(data);
        return frame;
    }

    public String getSubtypeName(byte type, byte subtype) {
    	String subtypeName = "Please update getSubtypeName method with subtype " + subtype;
    	
    	switch(type) {
    	case MessagesCONSTANTS.TYPE_CDT_MESSAGE:
    		switch(subtype) {
    		case MessagesCONSTANTS.SUBTYPE_START_DISCOVERY:
    			subtypeName = "START_DISCOVERY";
    			break;
    		case MessagesCONSTANTS.SUBTYPE_STOP_DISCOVERY:
    			subtypeName = "STOP_DISCOVERY";
    			break;
    		case MessagesCONSTANTS.SUBTYPE_START_POLLING:
    			subtypeName = "START_POLLING";
    			break;
    		case MessagesCONSTANTS.SUBTYPE_STOP_POLLING:
    			subtypeName = "STOP_POLLING";
    			break;
    		case MessagesCONSTANTS.SUBTYPE_SET_NEIGHBOURS:
    			subtypeName = "SET_NEIGHBOURS";
    			break;
    		case MessagesCONSTANTS.SUBTYPE_GET_NEIGHBOURS:
    			subtypeName = "GET_NEIGHBOURS";
    			break;
    		}
    	}
    	
    	return subtypeName;
    }
}
