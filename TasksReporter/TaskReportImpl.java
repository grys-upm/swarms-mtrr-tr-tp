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
package swarms.TasksReporter;

import swarms.EventReporter.EventReporterImpl;
import swarms.MTRR.MTRR;
import swarmsPDUs.basic.SWARMsDDSFrame;

public class TaskReportImpl implements TaskReport {
	//variable related to MTRR
//	PSManagerInterfaces psManagerInterfaces;
	//MTRR mtrr;
	public static TaskReportImpl instance = null;
	
//	public TaskReportImpl(PSManagerInterfaces psmi, MTRR mtrr)
//	{
//		psManagerInterfaces = psmi;
//		this.mtrr = mtrr;
//		//mtrr = new MTRR(psmi);
//	}
	
	public TaskReportImpl() {}
	
	public static TaskReportImpl getInstance() {
        if(instance == null)
            instance = new TaskReportImpl();
        
        return instance;
	}
	
	/*public void setMTRR(MTRR mtrr) {
		this.mtrr = mtrr;
	}*/
	
	public void sendTaskReport(SWARMsDDSFrame ddsFrame, int missionId)  
	{
		MTRR.getInstance().reportTask(ddsFrame, missionId);
	}
	public void sendCDTReport(SWARMsDDSFrame ddsFrame, int missionId)  
	{	
		MTRR.getInstance().reportCDT(ddsFrame, missionId);
	}
	
}