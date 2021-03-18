package mir.routing.module;

import mir.routing.constants.Constants;

public class AcquirerModule {
    public static void main(String[] args) {
        Module acquirerModule = new Module(Constants.Ports.ACQUIRER_MODULE);
        acquirerModule.start();
    }
}
