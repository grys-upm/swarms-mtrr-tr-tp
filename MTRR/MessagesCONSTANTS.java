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

public class MessagesCONSTANTS {
	// ...
	public static final int DATA_ARRAY_SIZE = 15;
	public static final int START_MISSION_PSMANAGER_DELAY = 60000;
	
	public static final byte CDT_REQUEST_VID = 0x00;
	public static final byte ENVIRONMENT_REQUEST_VID = 0x00;
	public static final byte EVENTS_REQUEST_VID = 0x00;
	
	public static final byte CDT_TOTAL_TRYOUTS = 1;
	public static final byte DEFAULT_GET_NEIGHBOURS_TOTAL_TRYOUTS = 2; 
	
	// TIME RELATED CONSTANTS
	public static final int CDT_START_DISCOVERY_DISCOVERY_TIME = 10;  //Total discovery time
	public static final int CDT_START_DISCOVERY_PERIOD = 10;   //time to discover
	public static final long CDT_START_DISCOVERY_TIMEOUT = 13000;			// Timeout of 35 seconds, bigger that discovery_time
	public static final int DEFAULT_ENVIRONMENT_STATE_VECTOR_REFRESH_TIME_IP = 5;
	public static final int DEFAULT_ENVIRONMENT_STATE_VECTOR_REFRESH_TIME_ACOUSTIC = 60;
	public static final long DEFAULT_STATE_VECTOR_TIMEOUT = 5000;
	public static final long DEFAULT_GET_NEIGHBOURS_TIMEOUT = 5000;
	public static final long DEFAULT_SET_NEIGHBOURS_TIMEOUT = 2000;	
	
	// TASK ASIGNMENT MODES
	public static final byte ASSIGNMENT_FULL_SEQUENCE = 0x01;
	public static final byte ASSIGNMENT_WAIT_TO_COMPLETE = 0x02;
	
	// MESSAGES
	// Message Types
	public static final byte TYPE_CDT_MESSAGE = 0x04;
	public static final byte TYPE_ENVIRONMENT_MESSAGE = 0x01;
	public static final byte TYPE_TASK_MESSAGE = 0x01;
	public static final byte TYPE_NOTIFICATION_MESSAGE = 0x02;
	public static final byte TYPE_EVENTS_MESSAGE = 0x03;
	public static final byte TYPE_SET_NEIGHBOURS = 0x04;
	public static final byte TYPE_GET_NEIGHBOURS = 0x04;	
	
	// CDT Message Sub-types
	public static final byte START_DISCOVERY = 0x00;
	public static final byte STOP_DISCOVERY = 0x02;
	public static final byte START_POLLING = 0x03;
	public static final byte STOP_POLLING = 0x04;
	public static final byte NOTIFY_SAFETY_ACTION = 0x00;
	public static final byte NOTIFY_ABORT_PLAN = 0x01;
	public static final byte SUBSCRIPTION_TO_VEHICLE_EVENTS = 0x00;
	public static final byte SUBTYPE_START_DISCOVERY = 0x00;	
	public static final byte SUBTYPE_STOP_DISCOVERY = 0x02;	
	public static final byte SUBTYPE_START_POLLING = 0x03;	
	public static final byte SUBTYPE_STOP_POLLING = 0x04;	
	public static final byte SUBTYPE_GET_NEIGHBOURS = 0x05;
	public static final byte SUBTYPE_SET_NEIGHBOURS = 0x06;
	
	// Environment Message Sub-types
	public static final byte STATE_VECTOR = 0x00;
	public static final byte STATE_MISSION = 0x01;
	
	// Task Message Sub-types
	public static final byte GOTO_WAYPOINT = 0x04;
	public static final byte HOVER = 0x05;
	public static final byte CONFIGURE = 0x06;
	public static final byte FOLLOW_TARGET = 0x07;
	public static final byte FOLLOW_STRUCTURE = 0x08;
	public static final byte FOLLOW_ROW = 0x09;
	public static final byte SPIRAL = 0x0A;
	public static final byte WAIT = 0x0B;
	public static final byte TRANSIT = 0x0C;
	public static final byte SURVEY = 0x0D;
	public static final byte INSPECT = 0x0E;
	public static final byte PICKUP = 0x0F;
	public static final byte GRASP_OBJECT = 0x10;
	public static final byte SONAR_ACQUISITION  = 0x11;
	public static final byte CAMERA_ACQUISITION = 0x12;
	
	// Task Report Error Codes
	public static final byte TASK_REPORT_CODE_PENDING = 0x00;
	public static final byte TASK_REPORT_CODE_NOT_STARTED = 0x00;
	public static final byte TASK_REPORT_CODE_RUNNING = 0x01;
	public static final byte TASK_REPORT_CODE_COMPLETED = 0x02;
	public static final byte TASK_REPORT_CODE_WAITING = 0x03;
	public static final byte TASK_REPORT_CODE_SUSPENDED = 0x04;
	public static final byte TASK_REPORT_CODE_PLANNED = 0x05;
	public static final byte TASK_REPORT_CODE_EXEC_FAILED = -1;
	public static final byte TASK_REPORT_CODE_PLAN_FAILED = -2;
	public static final byte TASK_REPORT_CODE_ABORTED = -3;
	public static final byte TASK_REPORT_CODE_CANCELLED = -4;
	public static final byte TASK_REPORT_CODE_REJECTED = -10;
	
	// Thrift Client to MMT Defaults
	public static final String DEFAULT_MMT_SERVER_ADDRESS = "192.168.0.3";
	public static final int DEFAULT_MMT_SERVER_PORT = 9096;
	
	// Thrift Client to MMT Properties names
	public static String MMT_ADDRESS_PROPERTY_NAME = "MMTAddress";
	public static String MMT_PORT_PROPERTY_NAME = "MMTPort";
	
	// Filenames
	public static final String THRIFT_CLIENT_LOGFILE = "ClientToMMT.log";
//	public static final String THRIFT_CLIENT_CONFIGURATION_FILENAME = "ThriftClient.properties";
	public static final String THRIFT_CLIENT_CONFIGURATION_FILENAME = "swarms.properties";
	public static final String CONFIGURATION_FILENAME = "swarms.properties";
	
	// Other default reference values
	public static final double DEFAULT_REFERENCE_COORDINATE_LONGITUDE = 9.545972;
	public static final double DEFAULT_REFERENCE_COORDINATE_LATITUDE = 63.593722;
	public static final String PROPERTY_STATE_VECTOR_TIMEOUT = "state_vector.timeout";	
	public static final String PROPERTY_GET_NEIGHBOURS_TIMEOUT = "get_neighbours.timeout";	
	public static final String PROPERTY_GET_NEIGHBOURS_TRYOUTS = "get_neighbours.tryouts";	
	public static final String PROPERTY_SET_NEIGHBOURS_TIMEOUT = "set_neighbours.timeout";	
	public static final String PROPERTY_STATE_VECTOR_REFRESH_TIME_IP = "state_vector.refresh_time.ip";
	public static final String PROPERTY_STATE_VECTOR_REFRESH_TIME_ACOUSTIC = "state_vector.refresh_time.acoustic";
	public static final String PROPERTY_IS_CDT_DISCOVERY_REQUIRED = "cdt.legacy_discovery_required";
	public static final String PROPERTY_IS_CDT_AVAILABLE = "cdt.available";
	public static final String PROPERTY_REF_COORDS_LONGITUDE = "coords.reference.longitude";
	public static final String PROPERTY_REF_COORDS_LATITUDE = "coords.reference.latitude";
	public static final String PROPERTY_NEIGHBOUR_DISCOVERY_STYLE = "neighbour_discovery.style";
	public static final String TOPIC_SET_NEIGHBOURS_REQUEST = "request_CDT";
	public static final String TOPIC_GET_NEIGHBOURS_REQUEST = "request_CDT";
	public static final String NEIGHBOUR_DISCOVERY_STYLE_SLEEP = "sleep";	
	public static final String NEIGHBOUR_DISCOVERY_STYLE_WAIT = "wait";	
	public static final String DEFAULT_NEIGHBOUR_DISCOVERY_STYLE = NEIGHBOUR_DISCOVERY_STYLE_SLEEP;
	public static final String PROPERTY_DO_GET_NEIGHBOURS = "do.get_neighbours";
	public static final byte END_REASON_FINISHED = 0x01;
	public static final byte END_REASON_ABORTED = 0x02;
}
