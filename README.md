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
2. **JavaFX** library: Oracle's JDK comes with JavaFX embedded. For OpenJDK, one has to manually
   download and install JavaFX on Ubuntu 14.04. On Ubuntu 16.04 one can install the openjfx package
   using the command:

     ```bash
     apt-get install openjfx
     ```

3. **autoconf**, **libtool**, **bison**, **python-dev** and **swig** packages which are required for
   the [CMUSphinx][1] installation.

4. **[Gradle][3]**: Gradle is used to build the application from its sources

### Steps
After you have installed all the [Prerequisites](#prerequisites) you are ready to install the
application using the following commands:

1. Clone the repository:

    ```bash
    git clone https://github.com/gouzouni625/personalized_automatic_speech_recognition.git
    ```

2. Run the setup.sh script that will install [CMUSphinx][1] (Note that the installation will be
   done inside the directory personalized_automatic_speech_recognition, no files will be created or
   changed anywhere else on your file system):

   ```bash
   cd personalized_automatic_speech_recognition
   ./setup.sh
   ```

   The setup script will look for Java at the location `/usr/lib/jvm/default-java`. If this is not
   the valid location of your Java installation, you should provide the correct path as an argument
   to the setup script like this:

   ```bash
   ./setup.sh /your/java/installation/path
   ```

   After the installation script is done, you can check the setup.log file to make sure everything
   was installed correctly.

3. Build the application using [Gradle][3]:

   ```bash
   gradle build
   ```

4. After building is finished, you can run the application. A helper script has been created for
   this purpose. Simply run:

   ```bash
   ./start.sh
   ```

## Further Reading
To get information about how to use the application, you can read the [user guide][4].

[1]: http://cmusphinx.sourceforge.net/wiki/about
[2]: http://www.oracle.com/technetwork/java/javase/overview/javafx-overview-2158620.html
[3]: https://gradle.org/
[4]: docs/user_guide.pdf
