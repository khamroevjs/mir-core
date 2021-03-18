package mir.routing.module;

import mir.routing.constants.Constants;

public class IssuerModule {
    public static void main(String[] args) {
        Module acquirerModule = new Module(Constants.Ports.ISSUER_MODULE);
        acquirerModule.start();
    }
}
