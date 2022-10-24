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

import swarms.MTRR.MessagesCONSTANTS;

public final class TaskHelper {
	public static String getTaskName(int taskID) {
		switch(taskID) {
		case MessagesCONSTANTS.GOTO_WAYPOINT:
			return "GOTO_WAYPOINT";
		case MessagesCONSTANTS.HOVER:
			return "HOVER";
		case MessagesCONSTANTS.CONFIGURE:
			return "CONFIGURE";
		case MessagesCONSTANTS.FOLLOW_TARGET:
			return "FOLLOW_TARGET";
		case MessagesCONSTANTS.FOLLOW_STRUCTURE:
			return "FOLLOW_STRUCTURE";
		case MessagesCONSTANTS.FOLLOW_ROW:
			return "FOLLOW ROW";
		case MessagesCONSTANTS.SPIRAL:
			return "SPIRAL";
		case MessagesCONSTANTS.WAIT:
			return "WAIT";
		case MessagesCONSTANTS.TRANSIT:
			return "TRANSIT";
		case MessagesCONSTANTS.SURVEY:
			return "SURVEY";
		case MessagesCONSTANTS.INSPECT:
			return "INSPECT";
		case MessagesCONSTANTS.PICKUP:
			return "PICKUP";
		case MessagesCONSTANTS.GRASP_OBJECT:
			return "GRASP_OBJECT";
		case MessagesCONSTANTS.SONAR_ACQUISITION:
			return "SONAR_ACQUISITION";
		case MessagesCONSTANTS.CAMERA_ACQUISITION:
			return "CAMERA_ACQUISITION";
		default:
			return "UNKNOWN TASK TYPE";
		}
	}

	public static String getStatusName(int status) {		
		switch(status) {
		case MessagesCONSTANTS.TASK_REPORT_CODE_PENDING:
			return "PENDING / NOT STARTED";
		case MessagesCONSTANTS.TASK_REPORT_CODE_RUNNING:
			return "RUNNING";
		case MessagesCONSTANTS.TASK_REPORT_CODE_COMPLETED:
			return "COMPLETED";
		case MessagesCONSTANTS.TASK_REPORT_CODE_WAITING:
			return "WAITING";
		case MessagesCONSTANTS.TASK_REPORT_CODE_SUSPENDED:
			return "SUSPENDED";
		case MessagesCONSTANTS.TASK_REPORT_CODE_PLANNED:
			return "PLANNED";
		case MessagesCONSTANTS.TASK_REPORT_CODE_EXEC_FAILED:
			return "EXEC_FAILED";
		case MessagesCONSTANTS.TASK_REPORT_CODE_PLAN_FAILED:
			return "PLAN_FAILED";
		case MessagesCONSTANTS.TASK_REPORT_CODE_ABORTED:
			return "ABORTED";
		case MessagesCONSTANTS.TASK_REPORT_CODE_CANCELLED:
			return "CANCELLED";
		case MessagesCONSTANTS.TASK_REPORT_CODE_REJECTED:
			return "REJECTED";
		default:
			return "UNKNOWN STATUS TYPE";
		}		
	}
}
