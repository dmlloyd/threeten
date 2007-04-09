/*
 * Copyright (c) 2007, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jsr310.codegen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * Code generates the repetitive parts of the JSR code.
 * 
 * @author Stephen Colebourne
 */
public class CodeGen {

    private static final File TEMPLATE_DIR = new File("src/codegen/java/jsr310/codegen");
    private static final File GENERATED_DIR = new File("src/main/java/javax/time");

    public static void main(String[] args) {
        try {
            Velocity.init();
            
            CodeGen cg = new CodeGen();
            cg.processPeriod();
            cg.processField();
            System.out.println("Done");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //-----------------------------------------------------------------------
    void processPeriod() throws Exception {
        Template template = Velocity.getTemplate("src/codegen/java/jsr310/codegen/Period.template");
        
        processPeriod(template, "Years", true);
        processPeriod(template, "Months", true);
        processPeriod(template, "Weeks", true);
        processPeriod(template, "Days", true);
        processPeriod(template, "Hours", false);
        processPeriod(template, "Minutes", false);
        processPeriod(template, "Seconds", false);
    }

    void processPeriod(Template template, String classname, boolean date) throws Exception {
        File file = new File(GENERATED_DIR, classname + ".java");
        
        // find the part of the file that has been customised
        List<String> methodLines = Collections.emptyList();
        if (file.exists()) {
            List<String> origLines = readFile(file);
            int startToStringPos = indexOfLineContaining(origLines, "public String toString() {", 0);
            int endToStringPos = indexOfEmptyLine(origLines, startToStringPos);
            methodLines = origLines.subList(endToStringPos + 1, origLines.size() - 1);
        }
        
        // setup data to insert
        VelocityContext vc = new VelocityContext();
        vc.put("Type", classname);
        vc.put("type", classname.toLowerCase());
        vc.put("stringPrefix", date ? "" : "T");
        vc.put("stringSuffix", classname.substring(0, 1));
        vc.put("methods", methodLines);
        
        // write output
        FileWriter out = new FileWriter(file);
        template.merge(vc, out);
        out.close();
    }

    //-----------------------------------------------------------------------
    void processField() throws Exception {
        Template template = Velocity.getTemplate("src/codegen/java/jsr310/codegen/Field.template");
        
        processField(template, "Year", "year", null);
        processField(template, "MonthOfYear", "month of year", MONTH_OF_YEARS);
        processField(template, "DayOfYear", "day of year", null);
        processField(template, "DayOfMonth", "day of month", null);
        processField(template, "DayOfWeek", "day of week", DAY_OF_WEEKS);
    }

    void processField(
            Template template,
            String classname,
            String desc,
            FieldSingleton[] singletons) throws Exception {
        File file = new File(GENERATED_DIR, classname + ".java");
        
        // find the part of the file that has been customised
        List<String> methodLines = Collections.emptyList();
        if (file.exists()) {
            List<String> origLines = readFile(file);
            int startToStringPos = indexOfLineContaining(origLines, "public int hashCode() {", 0);
            int endToStringPos = indexOfEmptyLine(origLines, startToStringPos);
            methodLines = origLines.subList(endToStringPos + 1, origLines.size() - 1);
        }
        
        // setup data to insert
        VelocityContext vc = new VelocityContext();
        vc.put("Type", classname);
        String mixedType = classname.substring(0, 1).toLowerCase() + classname.substring(1);
        vc.put("type", mixedType);
        vc.put("desc", desc);
        vc.put("singletons", singletons == null ? Collections.EMPTY_LIST : Arrays.asList(singletons));
        vc.put("methods", methodLines);
        
        // write output
        FileWriter out = new FileWriter(file);
        template.merge(vc, out);
        out.close();
    }

    //-----------------------------------------------------------------------
    List<String> readFile(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> lines = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        reader.close();
        return lines;
    }

    void writeFile(File file, List<String> lines) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
    }

    int indexOfEmptyLine(List<String> lines, int start) throws Exception {
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.length() == 0) {
                return i;
            }
        }
        return -1;
    }

    int indexOfLineContaining(List<String> lines, String part, int start) throws Exception {
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(part)) {
                return i;
            }
        }
        return -1;
    }

    //-----------------------------------------------------------------------
    private static final FieldSingleton[] MONTH_OF_YEARS = new FieldSingleton[] {
        new FieldSingleton("JANUARY", "The singleton instance for the month of January.", "1"),
        new FieldSingleton("FEBRUARY", "The singleton instance for the month of February.", "2"),
        new FieldSingleton("MARCH", "The singleton instance for the month of March.", "3"),
        new FieldSingleton("APRIL", "The singleton instance for the month of April.", "4"),
        new FieldSingleton("MAY", "The singleton instance for the month of May.", "5"),
        new FieldSingleton("JUNE", "The singleton instance for the month of June.", "6"),
        new FieldSingleton("JULY", "The singleton instance for the month of July.", "7"),
        new FieldSingleton("AUGUST", "The singleton instance for the month of August.", "18"),
        new FieldSingleton("SEPTEMBER", "The singleton instance for the month of September.", "9"),
        new FieldSingleton("OCTOBER", "The singleton instance for the month of October.", "10"),
        new FieldSingleton("NOVEMBER", "The singleton instance for the month of November.", "11"),
        new FieldSingleton("DECEMBER", "The singleton instance for the month of December.", "12"),
    };

    private static final FieldSingleton[] DAY_OF_WEEKS = new FieldSingleton[] {
        new FieldSingleton("MONDAY", "The singleton instance for the day of week of Monday.", "1"),
        new FieldSingleton("TUESDAY", "The singleton instance for the day of week of Tuesday.", "2"),
        new FieldSingleton("WEDNESDAY", "The singleton instance for the day of week of Wednesday.", "3"),
        new FieldSingleton("THURSDAY", "The singleton instance for the day of week of Thursday.", "4"),
        new FieldSingleton("FRIDAY", "The singleton instance for the day of week of Friday.", "5"),
        new FieldSingleton("SATURDAY", "The singleton instance for the day of week of Saturday.", "6"),
        new FieldSingleton("SUNDAY", "The singleton instance for the day of week of Sunday.", "7"),
    };

    public static class FieldSingleton {
        private String type;
        private String desc;
        private String value;
        public FieldSingleton(String type, String desc, String value) {
            super();
            this.type = type;
            this.desc = desc;
            this.value = value;
        }
        public String getDesc() {
            return desc;
        }
        public String getType() {
            return type;
        }
        public String getValue() {
            return value;
        }
    }

}