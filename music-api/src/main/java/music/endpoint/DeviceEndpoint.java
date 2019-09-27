package music.endpoint;

import music.mapper.DeviceMapper;
import music.model.Device;
import music.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device")
public class DeviceEndpoint {

    private final DeviceService deviceService;

    @Autowired
    public DeviceEndpoint(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/name/{name}")
    public Device getDeviceByName(@PathVariable String name) {
        return deviceService.getOrInsert(name);
    }
}
