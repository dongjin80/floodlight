/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.core;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import net.floodlightcontroller.core.coap.CoapManager;
import net.floodlightcontroller.core.coap.dataparsers.BeaconStatsParser;
import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.statsmanager.MainStatsManager;
import net.floodlightcontroller.core.coap.statsmanager.NeighborhoodMapManager;
import net.floodlightcontroller.core.coap.util.CoapConstants;
import net.floodlightcontroller.core.coap.util.ThreadMonitor;
import net.floodlightcontroller.core.internal.CmdLineSettings;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.FloodlightModuleLoader;
import net.floodlightcontroller.core.module.IFloodlightModuleContext;
import net.floodlightcontroller.restserver.IRestApiService;

/**
 * Host for the Floodlight main method
 * @author alexreimers
 */
public class Main {

    /**
     * Main method to load configuration and modules
     * @param args
     * @throws FloodlightModuleException 
     */
    public static void main(String[] args) throws FloodlightModuleException {
        // Setup logger
        System.setProperty("org.restlet.engine.loggerFacadeClass", 
                "org.restlet.ext.slf4j.Slf4jLoggerFacade");
        
        CmdLineSettings settings = new CmdLineSettings();
        CmdLineParser parser = new CmdLineParser(settings);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.printUsage(System.out);
            System.exit(1);
        }
        
        // Load modules
        FloodlightModuleLoader fml = new FloodlightModuleLoader();
        IFloodlightModuleContext moduleContext = fml.loadModulesFromConfig(settings.getModuleFile());
        // Run REST server
        IRestApiService restApi = moduleContext.getServiceImpl(IRestApiService.class);
        restApi.run();
        // Run the main floodlight module
        IFloodlightProviderService controller =
                moduleContext.getServiceImpl(IFloodlightProviderService.class);
        
        // Ashish: Begin mods for COAP
        // Add the COAP server code here.
        ThreadMonitor monitor = new ThreadMonitor();

        /*
         *  Start the stats related threads (StatsManager module): All the stats related thread are started here. 
         *  Each class performs a specialized task.
         */
        // Main thread for collecting wireless statistics.
        MainStatsManager mainStatsManager = new MainStatsManager(controller);
        Thread mainStatsThread = new Thread(mainStatsManager, "COAP main stats thread");
        mainStatsThread.start();
        monitor.addThreadToMonitor(mainStatsThread, mainStatsThread.getName());
        
        // Poll for observable beacons to find neighboring APs.
        /*
        NeighborhoodMapManager neighborManager = new NeighborhoodMapManager(controller);
        Thread neighborStatsThread = new Thread(neighborManager, "Neighbor stats thread");
        neighborStatsThread.start();
        monitor.addThreadToMonitor(neighborStatsThread, neighborStatsThread.getName());
        */
        
        /*
         *  Run policy engine. Executes a set of predefined policies for wireless configuration of APs.
         */
        if (CoapConstants.USE_POLICY_ENGINE) {
          CoapManager policyEngine = new CoapManager(controller);
          Thread policyEngineThread = new Thread(policyEngine, "Policy engine");
          policyEngineThread.start();
          monitor.addThreadToMonitor(policyEngineThread, policyEngineThread.getName());
        }
        
        /*
         * Start the database committer thread that periodically pushes the COAP
         * data into a persistent backend.
         */
        DatabaseCommitter committer = new DatabaseCommitter();
        Thread committer_thread = new Thread(committer, "DB committer");
        committer_thread.start();
        monitor.addThreadToMonitor(committer_thread, committer_thread.getName());
        // Ashish: End mods for COAP
        
        // This call blocks, it has to be the last line in the main
        controller.run();
    }
}
