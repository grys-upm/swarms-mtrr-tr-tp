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

public class StoredTask {
	public int missionId;
	public byte vehicleId;
	public byte subtype;
	public int seqoperation;
	public int id_error;
	
	public StoredTask(int missionId, byte vehicleId, byte subtype, int seqoperation, int id_error) {
		super();
		this.missionId = missionId;
		this.vehicleId = vehicleId;
		this.subtype = subtype;
		this.seqoperation = seqoperation;
		this.id_error = id_error;
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

	public int getSeqoperation() {
		return seqoperation;
	}

	public void setSeqoperation(int seqoperation) {
		this.seqoperation = seqoperation;
	}

	public int getId_error() {
		return id_error;
	}

	public void setId_error(int id_error) {
		this.id_error = id_error;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id_error;
		result = prime * result + missionId;
		result = prime * result + seqoperation;
		result = prime * result + subtype;
		result = prime * result + vehicleId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoredTask other = (StoredTask) obj;
		if (id_error != other.id_error)
			return false;
		if (missionId != other.missionId)
			return false;
		if (seqoperation != other.seqoperation)
			return false;
		if (subtype != other.subtype)
			return false;
		if (vehicleId != other.vehicleId)
			return false;
		return true;
	}		
}
