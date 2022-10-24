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

public class ThriftProxyCONSTANTS {
//	public static String THRIFT_CONFIGURATION_FILENAME = "ThriftProxy.properties";
	public static String THRIFT_CONFIGURATION_FILENAME = "swarms.properties";
	
	public static String THRIFT_PORT_PROPERTY_NAME = "ThriftPort";

	public static int DEFAULT_THRIFT_PORT = 9090;
	public static String DEFAULT_THRIFT_ADDRESS = "localhost";
	
	public static String MTRR_SERVICE_NAME = "MtrrService";
	public static String SEMANTIC_QUERY_SERVICE_NAME = "SemanticQueryService";
	
}
