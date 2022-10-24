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

import java.util.HashMap;

import swarms.datatypes.TaskStatus;
import swarms.environment_reporter.ProcessedEnvironmentalData;
import swarmsPDUs.basic.SWARMsDDSFrame;

/**
 * 
 * @author N&eacute;stor Lucas Mart&iacute;nez &lt;nestor.lucas@upm.es&gt;
 *
 */
public interface MTReporter 
{
	public void reportEvent(SWARMsDDSFrame data, int missionId);
	public void reportTask(SWARMsDDSFrame data, int missionId);
	public void reportCDT(SWARMsDDSFrame data, int missionId);
	public void reportEnvironment(ProcessedEnvironmentalData data, int missionId);
}
