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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.swarms.thrift.Mission;

import swarms.MTRR.MessagesCONSTANTS;

public class StoredPlanReader {
    private FileHandler fh;
    private Logger logger = Logger.getLogger("MyThriftClientLog");	
	
    public static StoredPlanReader instance = null;
	
    private StoredPlanReader() {
		try {
			fh = new FileHandler("StoredPlanReader.log", true);
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException | IOException e) {
			System.err.println("Unable to start the logger for StoredPlanReader");
		}
    }
    
    public static StoredPlanReader getInstance() {
        if(instance == null)
            instance = new StoredPlanReader();
        
        return instance;	
    }	

	public Mission readStoredPlan(String filename) {
		Mission plan = null;
		
		try {		
			plan = (Mission) readFile(filename).readObject();			
		} catch (IOException e) {
			logger.log(Level.WARNING, "Exception while reading stored plan " + filename + ": \n" + e.getMessage());
		} catch (ClassNotFoundException e) {
			logger.log(Level.WARNING, "File " + filename + " does not contain an object of the type Mission");
		}		
		
		return plan;
	}

	private ObjectInputStream readFile(String filename) throws IOException, FileNotFoundException {
		return new ObjectInputStream(new FileInputStream(filename));
	}
}
