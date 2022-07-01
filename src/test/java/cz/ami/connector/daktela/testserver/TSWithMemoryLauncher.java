package cz.ami.connector.daktela.testserver;

public class TSWithMemoryLauncher {
    static public void main(String[] arg) throws Exception {
        TSWithMemory.createServerForTesting();
        while(true){
            Thread.sleep(100);
        }
    }
}
