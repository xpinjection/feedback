# HR feedback processing tool
This is a simple tool to generate beautiful summary reports based on CSV feedback data in HTML format. Supports customization with Freemarker templates and flexible configuration.

## Provided functionality
Following functionality was implemented:

- Aggregate feedback by employee name and generate HTML reports for all employees by Freemarker template.
- Calculate average marks by competence, if evaluation options have specific format like *'2 (met expectations)'*.
- Gather comment for each competence evaluation if it follows evaluation in the next column and header name there contains *'comment'* word.
- Automaticall detect anonymous feedback and replace feedback author with *'Anonymous'*.

## System requirements
To build and run this tool you need Java 8+ and Maven 3.5+ on you machine.

## Running instructions
The easiest option to run this tool is to use ***spring-boot:run*** command with Maven from command line, passing target format as run argument:

`mvn spring-boot:run -Dspring-boot.run.arguments=FORMAT`

Alternative option is to build executable JAR with Maven and then execute following command from command line:

`java -jar target/feedback-0.1-SNAPSHOT-spring-boot.jar FORMAT`

## Configuration options
There are several configuration options for this tool:

- feedback data is expected in ***feedback.csv*** file in base directory;
- tool is prepackaged with following formats support: ***360***, ***smart-city***, ***healthcare***, ***data-intelligence***;
- for any custom format Freemarker template must be placed in base directory with name ***feedback-FORMAT.html*** and headers mapping must be added to classpath with name ***FORMAT-config.properties*** (for example, take a look at *feedback-360.html* and *src/main/resources/360-config.properties*).

Following CSV headers are supported in format configuration file:

- **AUTHOR** - full name of employee, who left feedback;
- **FEEDBACK_FOR** - full name of employee, who got feedback;
- **FEEDBACK_PERIOD** - name of feedback period;
- **ANONYMOUS_CHECK** - confirmation that feedback is non-anonymous and open (expected *'Yes'* answer);
- **COMPETENCIES** - list of evaluated competencies, splitted by *' | '* separator;
- **WHAT_IS_GOOD** - what was good from the last review;
- **WHAT_TO_IMPROVE** - what could be improved to the next review.
