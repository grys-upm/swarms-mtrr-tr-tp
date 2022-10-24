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

public class StoredEvent {
	public int missionId;
	public byte vehicleId;
	public byte subtype;
	public byte seqopration;
	
	public StoredEvent() {
	
	}
	
	public StoredEvent(int missionId, byte vehicleId, byte subtype, byte seqopration) {
		super();
		this.missionId = missionId;
		this.vehicleId = vehicleId;
		this.subtype = subtype;
		this.seqopration = seqopration;
	}

	public int getMissionId() {
		return missionId;
	}

	public void setMissionId(int missionId) {
		this.missionId = missionId;
	}

	public byte getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(byte vehicleId) {
		this.vehicleId = vehicleId;
	}

	public byte getSubtype() {
		return subtype;
	}

	public void setSubtype(byte subtype) {
		this.subtype = subtype;
	}

	public byte getSeqopration() {
		return seqopration;
	}

	public void setSeqopration(byte seqopration) {
		this.seqopration = seqopration;
	}
}
