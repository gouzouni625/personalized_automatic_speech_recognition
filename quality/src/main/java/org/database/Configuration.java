package org.database;


public class Configuration {
    public Configuration(){
        directory = "";
        filesIds = "";
        transcriptions = "";
    }

    public Configuration(String directory, String filesIds, String transcriptions){
        this.directory = directory;
        this.filesIds = filesIds;
        this.transcriptions = transcriptions;
    }

    public String getDirectory () {

        return directory;
    }

    public String getFilesIds () {
        return filesIds;
    }

    public String getFileIdsPath(){
        return directory + filesIds;
    }

    public String getTranscriptions () {
        return transcriptions;
    }

    public String getTranscriptionsPath() {
        return directory + transcriptions;
    }

    public void setDirectory (String directory) {
        this.directory = directory;
    }

    public void setFilesIds (String filesIds) {
        this.filesIds = filesIds;
    }

    public void setTranscriptions (String transcriptions) {
        this.transcriptions = transcriptions;
    }

    private String directory;
    private String filesIds;
    private String transcriptions;

}
