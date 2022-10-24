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

import com.swarms.thrift.Mission;

/**
 * Interface to feed the MTRR
 * 
 * @since 20170312
 * @author N&eacute;stor Lucas Mart&iacute;nez &lt;nestor.lucas@upm.es&gt;
 * @version 20170312
 *
 */
public interface MTFeeder {
	public void startMission(Mission globalMissionPlan) throws InterruptedException;
	public void endMission(int missionID, byte reason);
	public void enablePeriodicEnvironmentalReport();
	public void disablePeriodicEnvironmentalReport();
}