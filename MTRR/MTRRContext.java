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

public class MTRRContext {
	public boolean CDTready = false;
	public boolean currentPlanHasAUVs = false;
    public boolean setNeighboursResponseReceived = false;
    public boolean setNeighboursResponseSuccessful = false;
    public boolean getNeighboursResponseReceived = false;
    public boolean getNeighboursResponseSuccessful = false;
    public boolean start_discoveryResponseReceived = false;
    public boolean start_discoveryResponseSuccessful = false;
	
	private static MTRRContext instance = null;
	
	private MTRRContext() {
		this.CDTready = false;
		this.currentPlanHasAUVs = false;
		this.setNeighboursResponseReceived = false;
		this.getNeighboursResponseReceived = false;
	}
	
	public static MTRRContext getInstance() {
		if (instance == null)
			instance = new MTRRContext();
        
        return instance;			
	}

	public synchronized boolean isCDTready() {
		return CDTready;
	}

	public synchronized void setCDTready(boolean CDTready) {
		this.CDTready = CDTready;
	}

	public synchronized boolean isSetNeighboursResponseReceived() {
		return setNeighboursResponseReceived;
	}

	public synchronized void setSetNeighboursResponseReceived(boolean setNeighboursResponseReceived) {
		this.setNeighboursResponseReceived = setNeighboursResponseReceived;
	}

	public synchronized boolean isGetNeighboursResponseReceived() {
		return getNeighboursResponseReceived;
	}

	public synchronized void setGetNeighboursResponseReceived(boolean getNeighboursResponseReceived) {
		this.getNeighboursResponseReceived = getNeighboursResponseReceived;
	}

	public boolean isCurrentPlanHasAUVs() {
		return currentPlanHasAUVs;
	}

	public void setCurrentPlanHasAUVs(boolean currentPlanHasAUVs) {
		this.currentPlanHasAUVs = currentPlanHasAUVs;
	}

	public synchronized boolean isSetNeighboursResponseSuccessful() {
		return setNeighboursResponseSuccessful;
	}

	public synchronized void setSetNeighboursResponseSuccessful(boolean setNeighboursResponseError) {
		this.setNeighboursResponseSuccessful = setNeighboursResponseError;
	}

	public synchronized boolean isGetNeighboursResponseSuccessful() {
		return getNeighboursResponseSuccessful;
	}

	public synchronized void setGetNeighboursResponseSuccessful(boolean getNeighboursResponseError) {
		this.getNeighboursResponseSuccessful = getNeighboursResponseError;
	}

	public synchronized boolean isStart_discoveryResponseReceived() {
		return start_discoveryResponseReceived;
	}

	public synchronized void setStart_discoveryResponseReceived(boolean start_discoveryResponseReceived) {
		this.start_discoveryResponseReceived = start_discoveryResponseReceived;
	}

	public synchronized boolean isStart_discoveryResponseSuccessful() {
		return start_discoveryResponseSuccessful;
	}

	public synchronized void setStart_discoveryResponseSuccessful(boolean start_dsicveryResponseSuccessful) {
		this.start_discoveryResponseSuccessful = start_dsicveryResponseSuccessful;
	}	
}