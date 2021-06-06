package music.service;

import music.model.Device;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class DeviceServiceIT extends IntegrationTestBase {

    @Autowired
    private DeviceService deviceService;

    @Test
    public void testInsertingDevice(){
        String deviceName = "devname";
        Device device = deviceService.getOrInsert(deviceName);
        assertNotNull(device);
        assertEquals(deviceName, device.getName());
        assertTrue(device.getId() >= 0);
    }
}
