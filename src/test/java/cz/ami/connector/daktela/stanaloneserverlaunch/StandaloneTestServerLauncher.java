package cz.ami.connector.daktela.stanaloneserverlaunch;

public class StandaloneTestServerLauncher {
    static public void main(String[] arg) throws Exception {
        ServerForTesting.createServerForTesting();
        while(true){
            Thread.sleep(100);
        }
    }
}
