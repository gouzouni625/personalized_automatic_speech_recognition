## Personalized Automatic Speech Recognition (PASR)
Implementation of a desktop application for adapting to the voice of a single user (personalized) or
environment and applying automatic speech recognition on a set of e-mails on english.

## Description
This project is developed as part of my thesis on ASR during my undergraduate studies at the school
of Electrical and Computer Engineering of Aristotle University of Thessaloniki, Greece.

It is a desktop application that can be used for automatic speech recognition. Concretely, using
this application, one can:

1. Provide sample recordings of one's voice that will be used to adapt the speech recognition engine
   to one's voice.
2. Provide a set of e-mails that will be used as a search corpus during the recognition.
3. Dictate any sequence of words from within the provided corpus, and get the written transcript as
   a result.

## Implementation
The speech recognition engine consist of two parts. The ASR part and the Correction part.

### ASR
For the ASR part, [CMUSphinx][1] is used. The provided sample recordings are used to adapt the
default acoustic model of CMUSphinx to the user's voice. The provided e-mails are used to create a
language model and a dictionary for CMUSphinx.

### Correction
The Correction part is an algorithm designed to correct any errors in the output of the ASR part
based on the corpus, the language model and the dictionary mentioned above.

## Installation
The application is written in the Java programming language and the [JavaFX][2] library is used. The
development and testing is done on Ubuntu 14.04.

### Prerequisites
1. **Java 8**
2. **JavaFX**
  * Ubuntu 14.04: The easies way to install Java 8 and JavaFX is to install Oracle JDK. To do that
                  see [this][3] post.
  * Ubuntu 16.04: You can install Oracle JDK the same way you would install it on Ubuntu 14.04 but
                  you can also install OpenJDK and OpenJFX from aptitude:
                  
    ```bash
    apt-get install openjdk-8-jdk
    apt-get install openjfx
    ```

3. **Python** version 2.7 or greater. You can install Python with the following command:

   ```bash
   apt-get install python
   ```

3. **autoconf**, **libtool**, **bison**, **python-dev**, **swig** and **wget** packages. You can
   install these packages with the following command:

   ```bash
      apt-get install autoconf libtool bison python-dev swig wget
   ```

5. **[Gradle][4]** version 2.4 or greater. Gradle is used to build the application from its sources.
                   To automatically install Gradle, see the installation [Steps](#steps).

### Steps
After you have installed all the [Prerequisites](#prerequisites) you are ready to install the
application using the following commands:

1. Clone the repository:

    ```bash
    git clone https://github.com/gouzouni625/personalized_automatic_speech_recognition.git
    ```

2. Run the setup.py script that will install [CMUSphinx][1] (Note that the installation will be
   done inside the directory personalized_automatic_speech_recognition, no files will be created or
   changed anywhere else on your file system):

   ```bash
   cd personalized_automatic_speech_recognition
   ./setup.py
   ```

   The setup script will look for Java at the location `/usr/lib/jvm/default-java`. If this is not
   the valid location of your Java installation, you should provide the correct path as an argument
   to the setup script like this:

   ```bash
   ./setup.py  --java-path /your/java/installation/path
   ```

   If you installed Oracle JDK using a PPA, the java path will probably be:

   ```bash
   ./setup.py  --java-path /usr/lib/jvm/java-8-oracle
   ```

   If you don't have [Gradle][4] installed, the setup.py script can install it for you (the
   installation will be done inside the directory of the cloned repository) by passing the flag:
   
   ```bash
   ./setup.py  --java-path /your/java/installation/path --gradle-install
   ```

   After the installation script is done, you can check the setup.log file to make sure everything
   was installed correctly.

4. After that, you can run the application. A helper script has been created for this purpose.
   Simply run:

   ```bash
   ./start.sh
   ```

## Further Reading
To get information about how to use the application, you can read the [user guide][5].

[1]: http://cmusphinx.sourceforge.net/wiki/about
[2]: http://www.oracle.com/technetwork/java/javase/overview/javafx-overview-2158620.html
[3]: http://askubuntu.com/questions/521145/how-to-install-oracle-java-on-ubuntu-14-04
[4]: https://gradle.org/
[5]: docs/user_guide.pdf
