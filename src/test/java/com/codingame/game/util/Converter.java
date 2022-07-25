package com.codingame.game.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import com.codingame.game.util.converterUtil.AddMainAction;
import com.codingame.game.util.converterUtil.ConvertAction;
import com.codingame.game.util.converterUtil.Options;
import com.codingame.game.util.converterUtil.RemoveAction;
import com.codingame.game.util.converterUtil.ReplaceAction;
import com.codingame.game.util.converterUtil.TrimAction;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

public class Converter {
	
	private ArrayList<ConvertAction> tokens = new ArrayList<ConvertAction>();
	private ArrayList<ConvertAction> tokensGlobal = new ArrayList<ConvertAction>();
	
	private String classname;
	private HashMap<String, File> localClasses;
	private List<String> neededClasses = null;
	private List<String> javaImports = null;
	private boolean doAutoImport = true;
	
	private static boolean convertToClipboard;
	private static boolean convertToConsole;
	private static boolean trim;
	private static String localClassesRootDir;
	private boolean skipLines;
	
	public static void main(String[] args) {
		//load the properties
		Properties converterProperties = new Properties();
		File converterPropertiesFile = new File("src/test/java/com/codingame/game/util/converter.properties");
		InputStream inStream;
		try {
			inStream = new FileInputStream(converterPropertiesFile);
			converterProperties.load(inStream);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}
		
		String filename = converterProperties.getProperty("converterFile");
		convertToClipboard = Boolean.parseBoolean(converterProperties.getProperty("convertToClipboard", "true"));
		convertToConsole = Boolean.parseBoolean(converterProperties.getProperty("convertToConsole", "true"));
		trim = Boolean.parseBoolean(converterProperties.getProperty("trim", "false"));
		localClassesRootDir = converterProperties.getProperty("localClassesRootDir", ".");
		
		//create the converter
		Converter c = new Converter();
		
		//convert to codingame class
		if (convertToClipboard) {
			c.convertToClipboard(filename);
		}
		if (convertToConsole) {
			c.convertToConsole(filename);
		}
	}
	
	public Converter() {
		localClasses = getLocalClasses();
		neededClasses = new ArrayList<String>();
		javaImports = new ArrayList<String>();
	}
	
	public void convertToClipboard(String filename) {
		String converted = getConvertedFile(filename);
		System.out.println("Convert finished.");
		StringSelection selection = new StringSelection(converted);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
		System.out.println("Converted file is transfered into the clipboard.");
		System.out.println("Code size: "+converted.length());
	}
	
	public void convertToConsole(String filename) {
		String converted = getConvertedFile(filename);
		System.out.println(converted);
	}
	
	public String getConvertedFile(String filename) {
		File rootFile = new File(filename);
		try {
			rootFile = rootFile.getCanonicalFile();
		}
		catch (IOException e) {
			rootFile = rootFile.getAbsoluteFile();
		}
		neededClasses.clear();
		StringBuffer classes = new StringBuffer();
		
		// Convert Class
		StringBuffer rootClass = new StringBuffer(getConvertedFile(filename, neededClasses));
		
		// Collect and Convert Imports
		for (int i = 0; i < neededClasses.size(); i++) {
			String importName = neededClasses.get(i);
			File classFile = localClasses.get(importName);
			
			if (!classFile.equals(rootFile)) {
				classes.insert(0,
						getConvertedFile(classFile.getPath(), neededClasses).replace(" class ", " static class ").replace("static static", "static"));
			}
		}
		
		// Insert Imports and Classes
		StringBuffer preImports = new StringBuffer();
		for (String imp : javaImports) {
			preImports.append("import ");
			preImports.append(imp);
			preImports.append(";");
		}
		rootClass.insert(0, preImports);
		rootClass.insert(rootClass.lastIndexOf("}") - 1, classes.toString());
		
		// Format
		String formatted = formatJavaCode(rootClass.toString());
		// String formatted = rootClass.toString();
		neededClasses.clear();
		
		if (trim) {
			StringBuffer triming = new StringBuffer();
			for (String line : formatted.split("\n")) {
				triming.append(line.trim());
				triming.append("\n");
			}
			formatted = triming.toString();
		}
		
		if (formatted == null) {
			System.out.println("Google java formatter failed.");
			return rootClass.toString();
		}
		else {
			return formatted;
		}
	}
	
	private String getConvertedFile(String filename, List<String> neededClasses) {
		StringBuffer convertBuffer = new StringBuffer();
		String convert = null;
		skipLines = false;
		//boolean incomment = false;
		
		classname = new File(filename).getName().replace(".java", "");
		
		try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
			String line = br.readLine();
			
			while (line != null) {
				if (line.trim().startsWith("/* CONVERT:")) { // Convert mismatch
					line = "/*" + line.trim().substring(3);
				}
				
				if (line.trim().startsWith("/*CONVERT:")) { // Token action
					line = line.trim();
					String convertCommand = "";
					line = line.substring("/*CONVERT:".length());
					
					while (!line.trim().endsWith("*/")) {
						convertCommand += line;
						line = br.readLine();
					}
					convertCommand += line.substring(0, line.length() - "*/".length());
					executeCommand(convertCommand.toString().trim());
				}
				else if (line.trim().startsWith("import ")) { // import
					String importName = line.trim().split(" ")[1].replace(";", "");
					if (importName.startsWith("java.")) {
						if (!javaImports.contains(importName)) {
							javaImports.add(importName);
						}
					}
					else if (localClasses.containsKey(importName) && !neededClasses.contains(importName)) {
						neededClasses.add(importName);
					}
				}
				else if (line.trim().startsWith("package ")) { // package
					// ignore package line
				}
				else if (!skipLines/* || true*/) { // Normal line
					for (int i = 0; i < tokens.size(); i++) {
						line = tokens.get(i).processAction(line);
						tokens.remove(i);
						i--;
					}
					convertBuffer.append(line);
					convertBuffer.append("\n");
				}
				
				line = br.readLine();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		convert = convertBuffer.toString();
		
		for (int i = 0; i < tokensGlobal.size(); i++) {
			convert = tokensGlobal.get(i).processAction(convert);
		}
		
		if (doAutoImport) {
			autoImport(convert);
		}
		
		return convert;
	}
	
	private void executeCommand(String fullCommand) {
		fullCommand = fullCommand.replace("<classname>", classname);
		String parts[] = fullCommand.split(" ", 2);
		String command = parts[0].trim();
		String commandArguments[] = null;
		if (parts.length == 1) {
			commandArguments = new String[0];
		}
		else {
			commandArguments = split(parts[1]);
		}
		
		switch (command) {
			case "remove":
				remove(commandArguments);
				break;
			case "replace":
				replace(commandArguments);
				break;
			case "addMain":
				addMain(commandArguments);
				break;
			case "import":
				addImport(commandArguments);
				break;
			case "autoimport":
				if (commandArguments.length < 1) {
					// do nothing
				}
				else if (commandArguments[0].trim().equals("false")) {
					doAutoImport = false;
				}
				else if (!commandArguments[0].trim().equals("true")) {
					System.out.println("Unknown autoImport option: " + commandArguments[1]);
				}
				break;
			case "toggleSkip":
				skipLines = !skipLines;
				break;
		}
	}
	
	private void addImport(String[] args) {
		Options opt = new Options(args);
		
		if (opt.commandArgs.length != 1) {
			throw new RuntimeException("Invalid number of arguments in import convert command!");
		}
		
		String imp = opt.commandArgs[0];
		String fullimp = null;
		
		if (localClasses.containsKey(imp)) {
			fullimp = imp;
		}
		else {
			for (String localClass : localClasses.keySet()) {
				if (localClass.contains(imp)) {
					if (fullimp == null) {
						fullimp = localClass; // only a guess!
					}
					else {
						System.out.println("Import of " + imp + " is ambigous! Import " + fullimp + " is now used.");
						System.out.println("Fix this, by enter the full import path within the import command.");
						break;
					}
				}
			}
		}
		
		if (fullimp == null) {
			throw new RuntimeException("Convert Import: import for " + imp + " was not found in the local classes.");
		}
		else {
			if (!neededClasses.contains(fullimp)) {
				neededClasses.add(fullimp);
			}
		}
	}
	
	private static String[] split(String str) {
		ArrayList<String> parts = new ArrayList<String>();
		StringBuffer nextPart = new StringBuffer();
		String[] partsArray;
		boolean inQuotationMarks = false;
		
		for (char c : str.toCharArray()) {
			if (c == '"') {
				inQuotationMarks = !inQuotationMarks;
			}
			else if (!inQuotationMarks && c == ' ') {
				parts.add(nextPart.toString());
				nextPart.delete(0, nextPart.length());
			}
			else {
				nextPart.append(c);
			}
		}
		
		if (nextPart.length() > 0) {
			parts.add(nextPart.toString());
		}
		
		partsArray = new String[parts.size()];
		parts.toArray(partsArray);
		
		return partsArray;
	}
	
	private void addMain(String[] args) {
		tokens.add(new AddMainAction(args));
	}
	
	private void remove(String[] args) {
		Options opt = new Options(args);
		
		if (opt.global) {
			tokensGlobal.add(new RemoveAction(opt.commandArgs));
		}
		else {
			tokens.add(new RemoveAction(opt.commandArgs));
		}
		
		if (opt.trim) {
			if (opt.global) {
				tokensGlobal.add(new TrimAction());
			}
			else {
				tokens.add(new TrimAction());
			}
		}
	}
	
	private void replace(String[] args) {
		Options opt = new Options(args);
		
		if (opt.commandArgs.length != 2) {
			throw new RuntimeException("Invalid number of arguments in replace convert command!");
		}
		
		if (opt.global) {
			tokensGlobal.add(new ReplaceAction(opt.commandArgs[0], opt.commandArgs[1]));
		}
		else {
			tokens.add(new ReplaceAction(opt.commandArgs[0], opt.commandArgs[1]));
		}
		
		if (opt.trim) {
			if (opt.global) {
				tokensGlobal.add(new TrimAction());
			}
			else {
				tokens.add(new TrimAction());
			}
		}
	}
	
	private void autoImport(String str) {
		String[] lines = str.split("\n");
		forClass: for (String localClass : localClasses.keySet()) {
			if (!neededClasses.contains(localClass) && !classNameUsed(localClasses.get(localClass).getName().replace(".java", ""))) {
				String className = localClass.substring(localClass.lastIndexOf(".") + 1);
				int i = 0;
				int line = 0;
				int linecharcnt = 0;
				
				if (className.equals("Converter")) {
					continue;
				}
				
				i = str.indexOf(className, i);
				while (i != -1) {
					while (line != lines.length - 1 && i > linecharcnt + lines[line].length()) {
						linecharcnt += lines[line].length();
						line++;
					}
					int commentStart = lines[line].indexOf("//");
					int indexInLine = lines[line].indexOf(className);
					if (!(lines[line].trim().startsWith("*") // not in the comments
							|| lines[line].trim().startsWith("/*") || (commentStart != -1 && commentStart < indexInLine))) {
						char roi[] = new char[className.length() + 2];
						str.getChars(i - 1, i + className.length() + 1, roi, 0);
						
						if (!Character.isAlphabetic(roi[0]) && !Character.isAlphabetic(roi[roi.length - 1])) {
							neededClasses.add(localClass);
							continue forClass;
						}
					}
					
					i = str.indexOf(className, i + 1);
				}
			}
		}
	}
	
	private static HashMap<String, File> getLocalClasses() {
		File root = new File(localClassesRootDir);
		HashMap<String, File> localClasses = new HashMap<>();
		Stack<File> searchin = new Stack<File>();
		searchin.push(root);
		
		while (searchin.size() > 0) {
			File next = searchin.pop();
			
			if (next.isDirectory()) {
				for (File child : next.listFiles()) {
					searchin.push(child);
				}
			}
			else if (next.getName().endsWith(".java")) {
				File absolute = null;
				try {
					absolute = next.getCanonicalFile();
				}
				catch (IOException e) {
					absolute = next.getAbsoluteFile();
				}
				
				String importName = next.getPath().replace(File.separator, "/").replace("./src/", "").replace(".java", "").replace("/", ".");
				localClasses.put(importName, absolute);
			}
		}
		
		return localClasses;
	}
	
	public boolean classNameUsed(String classname) {
		for (String importName : neededClasses) {
			File localFile = localClasses.get(importName);
			String name = localFile.getName().replace(".java", "");
			
			if (name.equals(classname)) {
				return true;
			}
		}
		return false;
	}
	
	public String formatJavaCode(String javafile) {
		try {
			String formattedSource = new Formatter().formatSource(javafile);
			return formattedSource;
		}
		catch (FormatterException e) {
			e.printStackTrace();
		}
		return null;
	}
}