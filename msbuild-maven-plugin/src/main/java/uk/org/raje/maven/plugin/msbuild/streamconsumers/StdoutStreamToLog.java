/*
 * Copyright 2013 Andrew Everitt, Andrew Heckford, Daniele Masato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.raje.maven.plugin.msbuild.streamconsumers;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * StreamConsumer that writes lines from a stream to the supplied Log. The default is to log at 'info' level, we also 
 * try to identify errors and warning messages and log these at the appropriate level.
 */
public class StdoutStreamToLog implements StreamConsumer
{
    /**
     * Construct an instance to output to specified Log
     * @param logger the Log to write to
     */
    public StdoutStreamToLog( Log logger ) 
    {
        this.logger = logger;
    }
    
    @Override
    public void consumeLine( String line )
    {
        // Regexs devised using http://www.regexplanet.com/advanced/java/index.html

        // Look for errors with Regex: .*((?:(?i)error\:)|(?:\: (?:fatal )?error (?:[A-Z]+[0-9]+)?\:)).*
        if ( line.matches( ".*((?:(?i)error\\:)|(?:\\: (?:fatal )?error (?:[A-Z]+[0-9]+)?\\:)).*" ) )
        {
            logger.error( line );
        }
        // Look for warnings with Regex: .*((?:(?i)warning\:)|(?:\: warning (?:[A-Z]+[0-9]+)?\:)).*
        else if ( line.matches( ".*((?:(?i)warning\\:)|(?:\\: warning (?:[A-Z]+[0-9]+)?\\:)).*" ) ) 
        {
            logger.warn( line );
        }
        else
        {
            logger.info( line );
        }
    }
    
    private Log logger;
}
