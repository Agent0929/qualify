/*Copyright (c) 2010-2012, Mathieu Bordas
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1- Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
2- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
3- Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package qualify.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import qualify.ErrorsAndWarnings;

public class CommandLineTool {

	static Logger logger = Logger.getLogger(CommandLineTool.class);

	public static final String OPTION_FILE = "option_file", OPTION_VALUE_SEPARATOR = ",";
	public static final String OPTION_SYSTEM_PROPERTIES_PREFIX = "Q";

	private HashMap<String, String> options = null;

	public CommandLineTool(String[] commandLine) {
		if(options == null) {
			options = new HashMap<String, String>();
		}

		loadOptionsFromCommandLine(commandLine, true);

		if(isOptionInCommandLine(OPTION_FILE)) {
			String fileNames = getOptionValue(OPTION_FILE);
			if(fileNames.contains(",")) {
				for(String fileName : fileNames.split(",")) {
					File optionFile = new File(fileName);
					if(optionFile.exists()) {
						loadOptionsFromXMLFile(optionFile, false);
					} else {
						ErrorsAndWarnings.addError("Option file '" + optionFile.getAbsolutePath() + "' does not exist");
					}
				}
			} else {
				File optionFile = new File(fileNames);
				if(optionFile.exists()) {
					loadOptionsFromXMLFile(optionFile, false);
				} else {
					ErrorsAndWarnings.addError("Option file '" + optionFile.getAbsolutePath() + "' does not exist");
				}
			}

		}

		for(String option : options.keySet()) {
			logger.info("OPTION [" + option + "] : " + options.get(option));
		}
	}

	private void loadOptionsFromXMLFile(File xmlFile, boolean override) {
		SAXBuilder parser = new SAXBuilder();
		Document optionDocument = null;
		try {
			optionDocument = parser.build(xmlFile);

			for(Object optionTagObject : optionDocument.getRootElement().getChildren("option")) {
				Element optionTag = (Element) optionTagObject;
				setOption(optionTag.getAttribute("name").getValue(), optionTag.getText(), override);
			}
		} catch(JDOMException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void setOption(String optionName, String optionValue, boolean override) {
		if(!options.containsKey(optionName) || (override == true)) {
			options.put(optionName, optionValue);
		} else {
			String warnMessage = "Option '" + optionName + "' already set to: " + getOptionValue(optionName);

			ErrorsAndWarnings.addWarning(warnMessage);
		}
	}

	public boolean isOptionInCommandLine(String optionName) {
		boolean result = false;
		result = options.containsKey(optionName);
		if(!result) {
			result = System.getProperties().containsKey(OPTION_SYSTEM_PROPERTIES_PREFIX + optionName);
		}
		return result;
	}

	public static boolean isOptionInCommandLine(String[] commandLine, String optionName) {
		CommandLineTool cmd = new CommandLineTool(commandLine);
		return cmd.isOptionInCommandLine(optionName);
	}

	private void loadOptionsFromCommandLine(String[] args, boolean override) {
		for(String optionString : args) {
			if(optionString.contains("=")) {
				setOption(optionString.split("=")[0], optionString.split("=")[1], override);
			}
		}
	}

	public String getOptionValue(String optionName) {
		String result = System.getProperty(OPTION_SYSTEM_PROPERTIES_PREFIX + optionName);
		if(result == null) {
			result = options.get(optionName);
		}
		return result;
	}

	public String[] getOptionValues(String optionName) {
		String value = getOptionValue(optionName);
		if(value.contains(OPTION_VALUE_SEPARATOR)) {
			return value.split(OPTION_VALUE_SEPARATOR);
		} else {
			return new String[] { value };
		}
	}

	public HashMap<String, String> getOptions() {
		return options;
	}

}
