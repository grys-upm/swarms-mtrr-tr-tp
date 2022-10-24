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
package swarms.ThriftProxy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import com.swarms.thrift.MtrrService;
import com.swarms.thrift.SemanticQueryService;
import java.util.logging.Level;

public class ThriftProxyServer {

    private int thriftPort = ThriftProxyCONSTANTS.DEFAULT_THRIFT_PORT;

    private FileHandler fh;
    private Logger logger = Logger.getLogger("MyThriftLog");

    public ThriftProxyServer() {
    	String propertyValue;
    	
        try {
            fh = new FileHandler("ThriftProxy.log", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            logger.info("STARTING THIFT PROXY --------------------------------");

            Properties thriftProxyProperties = new Properties();
            FileInputStream in = new FileInputStream(ThriftProxyCONSTANTS.THRIFT_CONFIGURATION_FILENAME);
            thriftProxyProperties.load(in);
            in.close();

            logger.info("--- Loaded properties file for MTRR Thrift Service");

            if (thriftProxyProperties.containsKey(ThriftProxyCONSTANTS.THRIFT_PORT_PROPERTY_NAME)) {
            	propertyValue = thriftProxyProperties.getProperty(ThriftProxyCONSTANTS.THRIFT_PORT_PROPERTY_NAME).trim();
            	thriftPort = Integer.parseInt(propertyValue);
                logger.log(Level.INFO, "--- Read thrift server port property as {0}, set to {1}", new Object[] {propertyValue, thriftPort});
            } else {
                logger.log(Level.INFO, "--- Using default port {0}", thriftPort);
            }

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "+++ Invalid port number. Using default port {0}", thriftPort);
        } catch (SecurityException e) {
            logger.warning("+++ Security exception creating Thrift Proxy Server");
        } catch (FileNotFoundException e) {
            logger.warning("+++ Configuration file not found. Using default values");
        } catch (IOException e) {
            logger.warning("+++ Error reading the configuration file. Using default values");
        }
    }

    /**
     * Starts the Thrift service as a new thread
     */
    public void startThriftProxy() {
        TMultiplexedProcessor processor = new TMultiplexedProcessor();

        Runnable multiplex = new Runnable() {
            public void run() {
                multiplex(processor);
            }
        };

        new Thread(multiplex).start();
    }

    private void multiplex(TMultiplexedProcessor processor) {
        MtrrServiceHandler mtrrServiceHandler = new MtrrServiceHandler();
        mtrrServiceHandler.setLogger(logger);

        try {
            processor.registerProcessor(
                    ThriftProxyCONSTANTS.MTRR_SERVICE_NAME,
                    new MtrrService.Processor(mtrrServiceHandler)
            );

            processor.registerProcessor(
                    ThriftProxyCONSTANTS.SEMANTIC_QUERY_SERVICE_NAME,
                    new SemanticQueryService.Processor(new SemanticQueryServiceHandler())
            );

            TServerTransport serverTransport = new TServerSocket(thriftPort);

            TTransportFactory factory = new TFramedTransport.Factory();

            TServer.Args args = new TServer.Args(serverTransport);
            args.processor(processor);
            args.transportFactory(factory);
            TSimpleServer server = new TSimpleServer(args);

            logger.info("--- Starting Thrift Proxy V2");
            server.serve();
        } catch (TTransportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
