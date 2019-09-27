package music.service;

import music.model.Device;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class DeviceServiceIT {

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
