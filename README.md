# Oxymoron
A pseudocode compiler is an Oxymoron.


### Purpose
Oxymoron is a primitive compiler and interpreter for the textbook titled ***Starting out with >>> Programming Logic And Design, edition 5, by Tony Gaddis***.

### Target users
Specific users: Students who are taking ITP100 under this developer at NVCC Manassas.

General users: Anyone who is using the above mentioned textbook and wants to run those pseudocode programs.

### Disclaimer and Disclosures

While the developer has made every effort to conform to the pseudocode syntax presented by the book, there will necessarily be minor discrepancies between the pseudocode algorithms and the testable programs. Where such discrepancies occur, Oxymoron is not the authority to the correctness of your pseudocode algorithm.

The developer of Oxymoron is not in any way affiliated to the author(s) or publisher of the above mentioned textbook. 

Oxymoron is an self-funded hobbie project primarily for the benefits of the students under this developer.

### Features
Oxymoron has been tested with all the pseudocode programs from Chapter 2 to Chapter 14 of the textbook. Some examples from the book required minor modifications in order to be consistent with the overall syntax. All the tested examples may be found in [chapters2-13](src/examples/standalone/) and [chapter14](src/examples/objectoriented).

Oxymoron may be run in the following modes:
* The GUI mode provides the user with a simple interactive environment to run and test pseudocode programs. 
* The command-line mode provides a script-friendly interface for the user to batch run a group of programs. 
* The batch test mode is primarily for homework grading. To use the batch test mode, create three directories: *source_directory*, *stdin_directory*, and *stdout_directory*. Put all program scripts in the *source_directory*. In the *stdin_directory*, provide one file for each test run. Data in the file will be fed to each program script as if it were from stdin. Any stdout output from the program scripts will be logged in the *stdout_directory*.

### System requirements:
Java 8


### How to create a pseudocode program
You may write your pseudocode program using a code editor like Notepad++, vim or Xcode. Avoid editors like TextEdit, Word, and Notepad which are not suitable environments for code writing.

Once you have written a program, save it as a simple text file with the ".txt" extension. When saving the file, make sure the file name does not contain any spaces or special characters. 


### How to run
To run the pre-packaged jar file, download [oxymoron.jar](oxymoron.jar). Then run the jar file in any of the following modes with examples show below:

GUI mode:
* Double-click the icon or Type *java -jar oxymoron.jar*

Command-line compilation of stand-alone programs:
* java -jar oxymoron.jar [program2-13.txt](src/examples/standalone/ch2/program2-13.txt)

Command-line compilation of orient-oriented programs:
* java -jar oxymoron.jar [program14-1.txt](src/examples/objectoriented/ch14/program14-1.txt) [classlisting14-3.txt](src/examples/objectoriented/ch14/classlisting14-3.txt)

Batch test mode:
* java -jar oxymoron.jar [ch2-echo](testcases/testbed-hw1/ch2-echo) [ch2-echo-data](testcases/testbed-hw1/ch2-echo-data) [ch2-echo-out](testcases/testbed-hw1/ch2-echo-out)



### Technical Support
This application is provided AS IS. Should you discover any bugs or missing features, you are very welcome to fork off the project and provide your own fixes. Due to this developer's limited online availability, it is unlikely that the developer will follow up on any issues.

