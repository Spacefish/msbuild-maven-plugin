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

package uk.org.raje.maven.plugin.msbuild.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;



/**
 * @author dmasato
 *
 */
public class VCProjectParser extends BaseParser 
{
    private static final String PATH_SEPARATOR = "/";
    private static final String PATH_ROOT = "ROOT";

    private static final String PATH_PROPERTY_GROUP = PATH_ROOT + PATH_SEPARATOR + "Project" + PATH_SEPARATOR
            + "PropertyGroup";
    private static final String PATH_OUTDIR = PATH_PROPERTY_GROUP + PATH_SEPARATOR + "OutDir";

    private static final String PATH_ITEMDEFINITION_GROUP = PATH_ROOT + PATH_SEPARATOR + "Project" + PATH_SEPARATOR
            + "ItemDefinitionGroup";
    private static final String PATH_CLCOMPILE = PATH_ITEMDEFINITION_GROUP + PATH_SEPARATOR + "ClCompile";
    private static final String PATH_ADDITIONAL_INCDIRS = PATH_CLCOMPILE + PATH_SEPARATOR 
            + "AdditionalIncludeDirectories";
    
    private static final String PATH_PREPROCESSOR_DEFS = PATH_CLCOMPILE + PATH_SEPARATOR + "PreprocessorDefinitions";
    
    public VCProjectParser( File projectFile, String platform, String configuration ) 
            throws FileNotFoundException, SAXException, ParserConfigurationException 
    {
        
        super( projectFile, platform, configuration );
        SAXParserFactory factory = SAXParserFactory.newInstance();

        parser = factory.newSAXParser();
    }

    public void updateVCProject( VCProject project )
    {
        if ( project == null ) 
        {
            throw new InvalidParameterException();
        }

        project.setOutDir( outDir );
        project.setPreprocessorDefs( preprocessorDefs );
        project.setIncludeDirectories( includeDirs );
    }

    public String getOutDir()
    {
        return outDir;
    }

    public List<String> getIncludeDirs() 
    {
        return includeDirs;
    }

    public List<String> getPreprocessorDefs() 
    {
        return preprocessorDefs;
    }
    
    @Override
    public void parse() throws IOException, ParseException 
    {
        try 
        {
            parser.parse( getInputFile(), new VCProjectHandler() );
        }
        catch ( SAXParseException sape ) 
        {
            throw new ParseException( sape.getMessage(), sape.getLineNumber() );
        }
        catch ( SAXException sae ) 
        {
            throw new ParseException( sae.getMessage(), 0 );
        }
    }
    
    private enum ElementParserState 
    {
        PARSE_IGNORE,
        PARSE_PROPERTY_GROUP,
        PARSE_CONFIGPLATFORM_GROUP,
    }

    private enum CharParserState 
    {
        PARSE_IGNORE,
        PARSE_OUTDIR,
        PARSE_INCLUDE_DIRS,
        PARSE_PREPROCESSOR_DEFS
    }
    
    private class VCProjectHandler extends DefaultHandler
    {
        @Override
        public void startElement( String uri, String localName, String qName, Attributes attributes ) 
                throws SAXException 
            {
            
            String path = paths.peek() + PATH_SEPARATOR + qName;
            paths.push( path );
            
            switch ( elementParserState ) 
            {
            case PARSE_PROPERTY_GROUP:
                if ( path.compareTo( PATH_OUTDIR ) == 0 ) 
                {
                    charParserState = CharParserState.PARSE_OUTDIR;
                }
                break;

            case PARSE_CONFIGPLATFORM_GROUP: 
                if ( path.compareTo( PATH_ADDITIONAL_INCDIRS ) == 0 ) 
                {
                    charParserState = CharParserState.PARSE_INCLUDE_DIRS;
                }
                
                if ( path.compareTo( PATH_PREPROCESSOR_DEFS ) == 0 ) 
                {
                    charParserState = CharParserState.PARSE_PREPROCESSOR_DEFS;
                }
                
                break;
            
            default: 
                if ( path.compareTo( PATH_PROPERTY_GROUP ) == 0 )
                {
                    String condition = attributes.getValue( "Condition" );

                    if ( condition != null && condition.contains( getRequiredConfigurationPlatform() ) ) 
                    {
                        elementParserState = ElementParserState.PARSE_PROPERTY_GROUP;
                    }
                }
                else if ( path.compareTo( PATH_ITEMDEFINITION_GROUP ) == 0 )
                {
                    String condition = attributes.getValue( "Condition" );

                    if ( condition != null && condition.contains( getRequiredConfigurationPlatform() ) ) 
                    {
                        elementParserState = ElementParserState.PARSE_CONFIGPLATFORM_GROUP;
                    }
                }
            }
        }

        @Override
        public void endElement( String uri, String localName, String qName ) 
                throws SAXException 
        {
            String path = paths.pop();

            if ( path.compareTo( PATH_PROPERTY_GROUP ) == 0 ) 
            {
                elementParserState = ElementParserState.PARSE_IGNORE;
            }
            if ( path.compareTo( PATH_ITEMDEFINITION_GROUP ) == 0 ) 
            {
                elementParserState = ElementParserState.PARSE_IGNORE;
            }

            charParserState = CharParserState.PARSE_IGNORE;
        }
        
        @Override
        public void characters( char[] chars, int start, int length ) 
                throws SAXException 
                {

            String entries = new String( chars, start, length );
            
            switch ( charParserState ) 
            {
            case PARSE_OUTDIR:
                entries = entries.replace( "$(Configuration)", getRequiredConfiguration() );
                entries = entries.replace( "$(Platform)", getRequiredPlatform() );
                outDir = new String( entries );
                break;

            case PARSE_INCLUDE_DIRS:
                entries = entries.replace( "$(Configuration)", getRequiredConfiguration() );
                entries = entries.replace( "$(Platform)", getRequiredPlatform() );
                includeDirs = splitEntries( entries );
                break;
                
            case PARSE_PREPROCESSOR_DEFS:
                preprocessorDefs = splitEntries( entries );
                break;
                
            default:
            }
        }
        
        private List<String> splitEntries( String entries ) 
        {
            List<String> entryList = new ArrayList<String>();
            
            for ( String entry : entries.split( ";" ) ) 
            {
                if ( !entry.startsWith( "%" ) && !entry.startsWith( "$" ) && !entry.trim().isEmpty() ) 
                {                        
                    entryList.add( entry );
                }
            }
            
            return entryList;
        }
    };    
    
    private SAXParser parser = null; 
    private LinkedList<String> paths = new LinkedList<String>( Arrays.asList( PATH_ROOT ) ); 
    private ElementParserState elementParserState = ElementParserState.PARSE_IGNORE;
    private CharParserState charParserState = CharParserState.PARSE_IGNORE;
    private String outDir;
    private List<String> includeDirs = new ArrayList<String>();
    private List<String> preprocessorDefs = new ArrayList<String>();
}
