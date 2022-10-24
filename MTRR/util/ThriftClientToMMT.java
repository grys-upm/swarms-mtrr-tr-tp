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
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.swarms.thrift.Action;
import com.swarms.thrift.Mission;
import com.swarms.thrift.MmtService;
import com.swarms.thrift.MmtService.Client;
import com.swarms.thrift.MtrrService;

import swarms.MTRR.MessagesCONSTANTS;
import swarms.ThriftProxy.ThriftProxyCONSTANTS;

public class ThriftClientToMMT {	
	private final static long TRYOUT_DELAY = 1000;
	
	private static String mmtServerAddress = MessagesCONSTANTS.DEFAULT_MMT_SERVER_ADDRESS;
	private static int mmtServerPort = MessagesCONSTANTS.DEFAULT_MMT_SERVER_PORT;	
    private FileHandler fh;
    private Logger logger = Logger.getLogger("MyThriftClientLog");
    private boolean inuse = false;
    private boolean successful = false;
    private String methodUsed = "";
	
    public static ThriftClientToMMT instance = null;
        
	private ThriftClientToMMT() {
		String propertyValue;
		
        try {
			fh = new FileHandler(MessagesCONSTANTS.THRIFT_CLIENT_LOGFILE, true);
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			
            Properties thriftProxyProperties = new Properties();
            FileInputStream in = new FileInputStream(MessagesCONSTANTS.THRIFT_CLIENT_CONFIGURATION_FILENAME);
            thriftProxyProperties.load(in);
            in.close();

            logger.info("--- Loaded properties file for MMT Service");

            if (thriftProxyProperties.containsKey(MessagesCONSTANTS.MMT_ADDRESS_PROPERTY_NAME)) {
            	propertyValue = thriftProxyProperties.getProperty(MessagesCONSTANTS.MMT_ADDRESS_PROPERTY_NAME).trim();
            	mmtServerAddress = propertyValue;
                logger.log(Level.INFO, "--- Read MMT address property as '{0}', set to {1}", new Object[] {propertyValue, mmtServerAddress});
            } else {
                logger.log(Level.INFO, "--- Using default port {0}", mmtServerAddress);
            }

            if (thriftProxyProperties.containsKey(MessagesCONSTANTS.MMT_PORT_PROPERTY_NAME)) {
            	propertyValue = thriftProxyProperties.getProperty(MessagesCONSTANTS.MMT_PORT_PROPERTY_NAME).trim();
            	mmtServerPort = Integer.parseInt(propertyValue);
                logger.log(Level.INFO, "--- Read MMT thrift port property as '{0}', set to {1}", new Object[] {propertyValue, mmtServerPort});
            } else {
                logger.log(Level.INFO, "--- Using default port {0}", mmtServerPort);
            }
			
		} catch (Exception e) {
			logger.warning("+++ Error reading the configuration file. Using default values");
		}

	}

    public static ThriftClientToMMT getInstance() {
            if(instance == null)
                instance = new ThriftClientToMMT();
            
            return instance;	
    }
	
	public synchronized void setInUse(boolean state) {
		this.inuse = state;
	}
	
	public synchronized boolean isInUse() {
		return this.inuse;
	}
    
	public synchronized void setMethodUsed(String name) {
		this.methodUsed = name;
	}
	
	public synchronized String getMethodUsed() {
		return this.methodUsed;
	}
	
	public synchronized void setSuccessful(boolean state) {
		this.successful = state;
	}
	
	public synchronized boolean isSuccessful() {
		return this.successful;
	}
    
	/**
	 * Gets the client current configuration for the MMT Server Address.
	 * 
	 * @return The client current configuration for the MMT Server Address.
	 */
	public String getMmtServerAddress() {
		return mmtServerAddress;
	}

	/**
	 * Sets the client configuration for the MMT Server Address.
	 * 
	 * @param mmtServerAddress The MMT Server Address.
	 */
	public void setMmtServerAddress(String mmtServerAddress) {
		ThriftClientToMMT.mmtServerAddress = mmtServerAddress;
	}

	/**
	 * Gets the current client configuration for the MMT Server Port.
	 * 
	 * @return The current client configuration for the MMT Server Port.
	 */
	public int getMmtServerPort() {
		return mmtServerPort;
	}

	/**
	 * Sets the client configuration for the MMT Server Port.
	 * 
	 * @param mmtServerPort The MMT Server Port.
	 */
	public void setMmtServerPort(int mmtServerPort) {
		ThriftClientToMMT.mmtServerPort = mmtServerPort;
	}

	public void sendStatusReport(Action action) {
		if (isInUse()) {
			logger.log(Level.INFO, "--- Client to MMT is being used by method: {0}. Awaiting for it to finish", getMethodUsed());
			while(isInUse());
		}
		
		setInUse(true);
		setSuccessful(false);
		while (!isSuccessful()) {
			try {
				logger.log(Level.INFO, "--- Connecting to {0} ({1})", new Object[] {mmtServerAddress, mmtServerPort});
				logger.log(Level.INFO, "--- Sending status report to MMT for action {0}...", new Object[] {action.getActionId()});

				TTransport transport;

				transport = new TSocket(mmtServerAddress, mmtServerPort);

				transport.open();

				TProtocol protocol = new TBinaryProtocol(transport);
				MmtService.Client client = new MmtService.Client(protocol);

				performSendStatusReport(client, action);

				logger.log(Level.INFO, "--- Status report to MMT for action {0} sent", new Object[] {action.getActionId()});

				transport.close();
				setSuccessful(true);
			}
			catch (TException e) {
				logger.log(Level.INFO, "--- MMT connection is busy now. Will try to send status report {0} for action {1} again in {2} seconds", new Object[] {action.getStatus(), action.getActionId(), TRYOUT_DELAY/1000});
				try {
					Thread.sleep(TRYOUT_DELAY);
				} catch (InterruptedException e1) { }
			}
		}
		setInUse(false);
	}
	
	public void sendError(int errorId, String errorMessage) {
		if (isInUse()) {
			logger.log(Level.INFO, "--- Client to MMT is being used by method: {0}. Awaiting for it to finish", getMethodUsed());
			while(isInUse());
		}
		
		setInUse(true);
		setSuccessful(false);
		while(!isSuccessful()) {
			try {
				logger.log(Level.INFO, "--- Connecting to {0} ({1})", new Object[] {mmtServerAddress, mmtServerPort});			
				logger.log(Level.INFO, "--- Sending error report to MMT ({0},\"{1}\")...", new Object[] {errorId, errorMessage});

				TTransport transport;

				transport = new TSocket(mmtServerAddress, mmtServerPort);

				transport.open();

				TProtocol protocol = new TBinaryProtocol(transport);
				MmtService.Client client = new MmtService.Client(protocol);

				performSendError(client, errorId, errorMessage);

				logger.log(Level.INFO, "--- Error report {0} to MMT sent", new Object[] {errorId});

				transport.close();
				setSuccessful(true);
			}
			catch (TException e) {
				logger.log(Level.INFO, "--- MMT connection is busy now. Will try to send error report {0} again in {1} seconds", new Object[] {errorId, TRYOUT_DELAY/1000});
				try {
					Thread.sleep(TRYOUT_DELAY);
				} catch (InterruptedException e1) { }
			}		
		}
		setInUse(false);
	}
	
	public void sendUpdatedStatusNotification() {
		if (isInUse()) {
			logger.log(Level.INFO, "--- Client to MMT is being used by method: {0}. Awaiting for it to finish", getMethodUsed());
			while(isInUse());
		}
		
		setInUse(true);
		setSuccessful(false);
		while(!isSuccessful()) {
			try {
				logger.log(Level.INFO, "--- Connecting to {0} ({1})", new Object[] {mmtServerAddress, mmtServerPort});			
				logger.log(Level.INFO, "--- Sending updated status notification...");

				TTransport transport;

				transport = new TSocket(mmtServerAddress, mmtServerPort);

				transport.open();

				TProtocol protocol = new TBinaryProtocol(transport);
				MmtService.Client client = new MmtService.Client(protocol);

				performSendUpdatedStatusNotification(client);

				logger.log(Level.INFO, "--- Updated status notification to MMT sent");

				transport.close();
				setSuccessful(true);
			}
			catch (TException e) {
				logger.log(Level.INFO, "--- MMT connection is busy now. Will try to send status notification again in {0} seconds", TRYOUT_DELAY/1000);
				try {
					Thread.sleep(TRYOUT_DELAY);
				} catch (InterruptedException e1) { }
			}		
		}
		setInUse(false);
	}

	/**
	 * Internal method that performs the actual sending of the selected plan.
	 * 
	 * @param client			Instance of the Thrift client
	 * @param current			Current action to be updated
	 * @throws TException		If there is a communication problem
	 */
	private void performSendStatusReport(MmtService.Client client, Action current) throws TException {
		client.sendStatusReport(current);
	}
	
	/**
	 * 
	 * @param client
	 * @param errorId
	 * @param errorMessage
	 * @throws TException
	 */
	private void performSendError(MmtService.Client client, int errorId, String errorMessage) throws TException {
		client.sendError(errorId, errorMessage);
	}
	
	private void performSendUpdatedStatusNotification(Client client) throws TException {
		client.sendUpdatedStatusNotification();
	}
}
