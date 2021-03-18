package mir.routing.module;

import mir.routing.constants.Constants;

public class PlatformModule {
    public static void main(String[] args) {
        Module acquirerModule = new Module(Constants.Ports.PLATFORM_MODULE);
        acquirerModule.start();
    }
}
