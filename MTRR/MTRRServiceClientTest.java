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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import com.swarms.thrift.MtrrService;

import swarms.ThriftProxy.ThriftProxyCONSTANTS;

public class MTRRServiceClientTest {
	public static void main(String[] args) {
		int thriftPort = ThriftProxyCONSTANTS.DEFAULT_THRIFT_PORT;
		String thriftAddress = ThriftProxyCONSTANTS.DEFAULT_THRIFT_ADDRESS;

		FileHandler fh;
		Logger logger = Logger.getLogger("MyMtrrServiceClientTestLog");
		
		try {
			fh = new FileHandler("MtrrServiceClientTest.log", true);  
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);

			logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			logger.info("STARTING MTRR SERVICE CLIENT TESTER -----------------");

			Properties thriftProxyProperties = new Properties();
			FileInputStream in = new FileInputStream("ThriftProxy.properties");
			thriftProxyProperties.load(in);
			in.close();

			logger.info("--- Loaded properties file for MTRR Thrift Service");

			if (thriftProxyProperties.containsKey(ThriftProxyCONSTANTS.THRIFT_PORT_PROPERTY_NAME)) {
				thriftPort = Integer.parseInt(thriftProxyProperties.getProperty(ThriftProxyCONSTANTS.THRIFT_PORT_PROPERTY_NAME));
				logger.info(">>> Port configured in properties as " + thriftPort);
			}
			else {
				logger.info("--- Using default port " + thriftPort);
			}

			TFramedTransport transport;
			transport = new TFramedTransport(new TSocket(thriftAddress, thriftPort));
			transport.open();
			
			TProtocol protocol = new TBinaryProtocol(transport);
			
			TMultiplexedProtocol mp1 = new TMultiplexedProtocol(protocol, ThriftProxyCONSTANTS.MTRR_SERVICE_NAME);
			MtrrService.Client mtrrService = new MtrrService.Client(mp1);
			
			logger.info("--- Sending plan to the server");
			mtrrService.send_sendPlan(MTRRMissionPlanTest.getMissionPlan());
			
			transport.close();
			
		} catch (TException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			logger.warning("+++ Invalid port number. Using default port " + thriftPort);
		} catch (SecurityException e) {
			logger.warning("+++ Security exception creating Thrift Proxy Server");
		} catch (FileNotFoundException e) {
			logger.warning("+++ Configuration file not found. Using default values");			
		} catch (IOException e) {
			logger.warning("+++ Error reading the configuration file. Using default values");
		}
	}	
}