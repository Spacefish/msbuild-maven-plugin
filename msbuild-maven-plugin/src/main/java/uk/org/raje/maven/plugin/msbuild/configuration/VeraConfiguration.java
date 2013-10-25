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

package uk.org.raje.maven.plugin.msbuild.configuration;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;


/**
 * Configuration holder for Vera++ configuration values.
 */
public class VeraConfiguration
{
    /**
     * The name to output on debug/information messages
     */    
    public static final String TOOL_NAME = "Vera++";

    /**
     * The name of the environment variable that points to the Vera++ installation directory
     */
    public static final String HOME_ENVVAR = "VERA_HOME";
    
    /**
     * The name of the property that that points to the Vera++ installation directory
     */
    public static final String HOME_PROPERTY = "vera.home";

    /**
     * The message to use when skipping Sonar execution
     */
    public static final String SKIP_MESSAGE = "Skipping coding style analysis";
    
    /**
     * Get the configured value for skip.
     * @return the configured value or false if not configured
     */
    public final boolean skip()
    {
        return skip;
    }

    /**
     * Get the configured installation directory for Vera++.
     * @return the installation directory for Vera++
     */
    public final File getVeraHome()
    {
        return veraHome;
    }

    /**
     * Set the path to the installation directory for Vera++.
     * @param newVeraHome the new path to the installation directory for Vera++
     */
    public final void setVeraHome( File newVeraHome )
    {
        veraHome = newVeraHome;
    }

    /**
     * Get the prefix to be added to the report files generated by Vera++.
     * @return the prefix fo Vera++ reports
     */
    public final String getReportName()
    {
        return reportName;
    }

    /**
     * Get the Vera++ check profile (that is, the rules used to analyse the coding style).
     * @return the Vera++ rule checking profile
     */
    public final String getProfile()
    {
        return profile;
    }

    /**
     * Get the regular expression used to determine which Visual C++ projects to exclude from the Vera++ analysis.
     * @return the configured regular expression, or null otherwise
     */
    public final String getExcludeProjectRegex()
    {
        return excludeProjectRegex;
    }

    /**
     * Set to true to skip Vera++ code analysis
     */
    @Parameter( 
            defaultValue = "false", 
            readonly = false )
    private boolean skip = false; 

    /**
     * <p>The path to the installation directory for Vera++.</p>
     * <p><strong>Note:</strong> The property name specified here is only for documentation, this doesn't work and needs
     * to be manually fixed in {@link AbstractMSBuildPluginMojo}</p>
     */
    @Parameter( 
            property = HOME_PROPERTY,
            readonly = false, 
            required = false )
    private File veraHome;

    /**
     * The prefix to be added to the report files generated by Vera++
     */
    @Parameter( 
            defaultValue = "vera-report", 
            readonly = false, 
            required = false )
    private String reportName = "vera-report";
    
    /**
     * The Vera++ check profile (that is, the rules used to analyse the coding style)
     */
    @Parameter( 
            defaultValue = "full",
            readonly = false, 
            required = false )
    private String profile = "full";
    
    /**
     * The regular expression used to determine which Visual C++ projects to exclude from the Vera++ analysis
     */
    @Parameter( 
            readonly = false, 
            required = false )
    private String excludeProjectRegex;
}
