
/*
  This code can be used to execute system commands from your console or IDE.
  Please be advised that commands requiring additional input e.g. 'sudo su', etc. will not work!
*/

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
  
class CONSTANTS {
  
    static final List<String> LIST_OF_COMMANDS = []; //-> add your command(s) to this list
    static final Boolean LOG_SYSTEM_DATA = false; //-> if true, certain system stats (i.e. number of cores, memory, and ram) will be logged
    static final Boolean LOG_EXIT_CODE = false; //-> if true, exit code will be logged
}
  
Logger logger = Logger.getLogger("de.louis.scriptrunner.console.runShellCommands");
logger.setLevel(Level.INFO);
  
Runtime runtime = Runtime.getRuntime();
LinkedHashMap<String, String> systemProperties = System.getProperties() as LinkedHashMap<String, String>;
 
if (CONSTANTS.LOG_SYSTEM_DATA) {
  
    logger.info("Runtime information:");
    logger.info("Operating system name: " + systemProperties.get("java.vm.vendor"));
    logger.info("Operating system base: " + systemProperties.get("os.name"));
    logger.info("Runtime version: " + systemProperties.get("java.runtime.version"));
    logger.info("System archtecture: ${systemProperties.get("sun.arch.data.model")} bit");
    try{
 
        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Base URL: https://${systemProperties.get("catalina.connector.proxyName")}/");
        logger.info("Host name: " + InetAddress.getLocalHost().getHostName());
        logger.info("IP address: " + InetAddress.getLocalHost().getHostAddress());
        logger.info("----------------------------------------------------------------------------------------------------");
    }catch(UnknownHostException uhe) {
 
        logger.error("Caught unknown host exception ${uhe}.");
    }
    logger.info("Number of cores: " + runtime.availableProcessors());
    logger.info("Total memory: ${(new File("/").getTotalSpace()/1073741824L).round(2)} GB");
    logger.info("Free memory: ${(new File("/").getFreeSpace()/1073741824L).round(2)} GB");
    logger.info("Total RAM: ${(runtime.totalMemory()/1073741824L).round(2)} GB");
    logger.info("Free RAM: ${(runtime.freeMemory()/1073741824L).round(2)} GB");
    logger.info("(approx.) RAM in use: ${Math.subtractExact(((runtime.totalMemory()/1073741824L).round(0) as Long), ((runtime.freeMemory()/1073741824L).round(0) as Long))} GB");
    logger.info("----------------------------------------------------------------------------------------------------");
}
  
for (String command in CONSTANTS.LIST_OF_COMMANDS){
  
    if (!command){
  
        logger.warn("No valid command found! - Skipping this one.");
        continue;
    } else {
  
        try{
 
            Process process = runtime.exec(command);
            Integer exitCode = process.waitFor();
            if (exitCode == 0) {
  
                if (CONSTANTS.LOG_EXIT_CODE) {
 
                    logger.info("Exit code: ${exitCode} - success!");
                    logger.info("Current command: " + command);
                    logger.info("---------------------------------");
                }
                InputStream inputStream = process.getInputStream();
                BufferedReader inputReadder = new BufferedReader(new InputStreamReader(inputStream));
                List<String> inputResults = inputReadder.readLines().each{ String inputResult -> logger.info(inputResult)};
            } else {
  
                if (CONSTANTS.LOG_EXIT_CODE) {
 
                    logger.info("Exit code: ${exitCode} - success!");
                    logger.info("Current command: " + command);
                    logger.info("---------------------------------");
                }
                InputStream errorStream = process.getErrorStream();
                BufferedReader errorReadder = new BufferedReader(new InputStreamReader(errorStream));
                List<String> errorResults = errorReadder.readLines().each{ String errorResult -> logger.error(errorResult)};
            }
            logger.info("----------------------------------------------------------------------------------------------------");
  
        } catch (IOException ioe) {
  
            logger.error("Caught I/O exception ${ioe}.");
        }
    }
}
  
return;